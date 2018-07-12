package de.cedricrupb.react.learner;


import de.cedricrupb.config.model.*;
import de.cedricrupb.utils.LazyQueryFactory;
import de.cedricrupb.utils.PrefixHelper;
import org.aksw.limes.core.io.config.KBInfo;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Resource;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.*;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.OWLOntologyKnowledgeSource;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.kb.sparql.simple.SparqlSimpleExtractor;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderOWL;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL2;
import org.dllearner.utilities.owl.OWLClassExpressionToSPARQLConverter;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;


import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 * Class that Learns from Classes obtained from SPARQL Endpoints, OWLOntologies etc..
 *
 * @author Cedric Richter
 */


public class ClassLearner implements Runnable{

    public static final int SPARQL_RECURSION_DEPTH = 1;


    static Logger logger = LoggerFactory.getLogger(ClassLearner.class.getName());

    private LearningDomainConfig config;
    private Set<Example> examples;

    private Throwable exception;
    private String restriction;
    private CELOE algorithm;


    public ClassLearner(LearningDomainConfig config, Set<Example> examples){
        this.config = config;
        this.examples = examples;
    }


    @Override
    public void run() {
        LearningDomainConfig cfg = config;

        Set<Example> examples = this.examples;

        if(cfg.getInfo().getRestrictions() != null &&
                !cfg.getInfo().getRestrictions().isEmpty()){
            examples = expandByRestriction(cfg.getInfo(), cfg.getInfo().getRestrictions(), examples);
        }

        examples = expandByNegative(cfg.getInfo(), examples);

        AbstractReasonerComponent reasoner = initReasoner(cfg.getInfo(), collectInstances(examples));

        if(reasoner == null)
            return;

        AbstractClassExpressionLearningProblem<? extends Score> lp = createProblem(examples);
        lp.setReasoner(reasoner);

        try {
            lp.init();
        } catch (ComponentInitException e) {
            logger.error("Cannot initialize learning problem", e);
            exception = e;
            return;
        }

        CELOE alg = new CELOE();

        alg.setLearningProblem(lp);
        alg.setReasoner(reasoner);
        alg.setMaxExecutionTimeInSecondsAfterImprovement(5);

        try {
            alg.init();
        } catch (ComponentInitException e) {
            logger.error("Cannot initialize algorithm", e);
            exception = e;
            return;
        }

        alg.start();

        algorithm = alg;

        logger.info("Finished execution with accuracy: "+alg.getCurrentlyBestAccuracy());

        restriction = transformExpr(alg.getCurrentlyBestDescription(), cfg.getInfo().getVar());
        restriction = PrefixHelper.revertPrefix(restriction, cfg.getInfo().getPrefixes());



    }

    public boolean hasException(){
        return exception != null;
    }

    public Throwable getException(){
        return exception;
    }

    public String getRestriction(){
        return restriction;
    }

    public double getAccuracy() {
        if(algorithm != null)
            return algorithm.getCurrentlyBestAccuracy();
        if(restriction != null)
            return 1.0;
        return 0.0;
    }


    public OWLClassExpression getLearnedExpression() {
        return algorithm != null? algorithm.getCurrentlyBestDescription() : null;
    }

    private boolean isLocal(KBInfo info){
        if("N3".equalsIgnoreCase(info.getType()) || "NT".equalsIgnoreCase(info.getType()) ||
                "N-TRIPLE".equalsIgnoreCase(info.getType()) ||
                "TURTLE".equalsIgnoreCase(info.getType()) ||
                "RDF/XML".equalsIgnoreCase(info.getType())){
            return true;
        }

        if(!"sparql".equalsIgnoreCase(info.getType()) &&
                Files.exists(Paths.get(info.getEndpoint()))){
            info.setType("N3");
            return true;
        }

        info.setType("sparql");
        return false;
    }

    private AbstractReasonerComponent initReasoner(KBInfo info, Set<String> examples){
        KnowledgeSource ks;

        try{
            ks = initKS(info, examples);
        } catch (ComponentInitException e) {
            logger.error("Failed to init knowledge source", e);
            exception = e;
            return null;
        } catch (MalformedURLException e) {
            logger.error("Crazy url?", e);
            exception = e;
            return null;
        }

        AbstractReasonerComponent reasoner;

        if(ks instanceof OWLOntologyKnowledgeSource){
            reasoner = new ClosedWorldReasoner();
        }else{
            reasoner = new SPARQLReasoner();
        }

        reasoner.setSources(ks);
        try {
            reasoner.init();
        } catch (ComponentInitException e) {
            logger.error("Cannot initialize reasoner", e);
            exception = e;
            return null;
        }

        return reasoner;
    }


    private Set<Example> expandByNegative(KBInfo info, Set<Example> examples){

        SortedSet<OWLIndividual> instances = new TreeSet<>();
        Set<String> strInstances = new HashSet<>();

        boolean hasNegative = false;

        for(Example example: examples){
            hasNegative |= example instanceof NegativeExample;

            IRI iri = resolvePrefix(example.getUri());

            instances.add(new OWLNamedIndividualImpl(
                                iri
                            ));
            strInstances.add(iri.toString());
        }

        if(hasNegative)return examples;

        AbstractReasonerComponent reasoner = initReasoner(info, strInstances);

        if(reasoner == null)
            return examples;

        if(reasoner instanceof SPARQLReasoner){
            AutomaticNegativeExampleFinderSPARQL2 negativeFinder = new AutomaticNegativeExampleFinderSPARQL2((SPARQLReasoner) reasoner);

            Set<Example> out = new HashSet<>(examples);

            ExampleFactory factory = new ExampleFactory(new HashMap<>());

            for(OWLIndividual individual: negativeFinder.getNegativeExamples(instances, Math.max(instances.size()*2, 1000))){
                out.add(factory.createNegative(ExampleFactory.ExampleSource.SOURCE, individual.toStringID()));
            }

            return out;
        }else{
            AutomaticNegativeExampleFinderOWL negativeFinder = new AutomaticNegativeExampleFinderOWL(
                    instances,
                    reasoner
            );

            Set<Example> out = new HashSet<>(examples);

            ExampleFactory factory = new ExampleFactory(new HashMap<>());

            for(OWLIndividual individual: negativeFinder.getNegativeExamples(Math.max(instances.size()*2, 1000), true)){
                out.add(factory.createNegative(ExampleFactory.ExampleSource.SOURCE, individual.toStringID()));
            }

            return out;

        }


    }


    private String initABoxFilter(){

        List<String> forbiddenRelation = new ArrayList<>();

        forbiddenRelation.add("http://dbpedia.org/ontology/wikiPageUsesTemplate");
        forbiddenRelation.add("http://dbpedia.org/ontology/wikiPageExternalLink");
        forbiddenRelation.add("http://dbpedia.org/ontology/wordnet_type");
        forbiddenRelation.add("http://www.w3.org/2002/07/owl#sameAs");


        String out = "";

        for(String relation : forbiddenRelation){
            out += String.format("FILTER(?p!=<%s>) .", relation);
        }

        return out;
    }

    private String initTboxFilter(){
        List<String> forbiddenPrefix = new ArrayList<>();

        forbiddenPrefix.add("http://dbpedia.org/class/yago/");
        forbiddenPrefix.add("http://dbpedia.org/resource/Category:");


        String out = "";

        for(String prefix : forbiddenPrefix){
            out += String.format("FILTER ( !regex(str(?class), \'^%s\')) .", prefix);
        }

        out += "FILTER (regex(str(?p), \'^http://xmlns.com/foaf/0.1/\')).";

        return out;
    }


    private KnowledgeSource initSparql(KBInfo info, Set<String> instances) throws MalformedURLException, ComponentInitException {
        if(info.getEndpoint().toLowerCase().contains("dbpedia.org")){

            List<String> schemaUrls = new ArrayList<>();
            schemaUrls.add("src/main/resources/dbpedia_3.6.owl");

            List<String> inst = new ArrayList<>(instances);

            SparqlSimpleExtractor extractor = new SparqlSimpleExtractor();
            extractor.setRecursionDepth(SPARQL_RECURSION_DEPTH);

            extractor.setDefaultGraphURI(info.getGraph());
            extractor.setEndpointURL(info.getEndpoint());


            extractor.setInstances(inst);
            extractor.setOntologySchemaUrls(schemaUrls);

            extractor.setAboxfilter(initABoxFilter());
            extractor.setTboxfilter(initTboxFilter());

            extractor.init();

            return extractor;
        }

        SparqlKnowledgeSource ks = new SparqlKnowledgeSource();
        ks.setUrl(new URL(info.getEndpoint()));
        ks.setInstances(instances);

        ks.init();
        return ks;
    }


    private KnowledgeSource initKS(KBInfo info, Set<String> instances) throws ComponentInitException, MalformedURLException {
        if(isLocal(info)){
            OWLFile ks = new OWLFile();
            ks.setFileName(info.getEndpoint());
            ks.init();
            return ks;
        }else{
            return initSparql(info, instances);
        }
    }

    private Set<String> collectInstances(Set<Example> examples){
        Set<String> instances = new HashSet<>();

        for(Example example: examples){
            instances.add(resolvePrefix(example.getUri()).toString());
        }

        return instances;
    }

    private IRI resolvePrefix(String s){
        String[] split = s.split(":");
        if(split.length == 2){
            String prefix = split[0];
            Map<String, String> prefixes = config.getInfo().getPrefixes();

            if(prefixes.containsKey(prefix)){
                prefix = prefixes.get(prefix);
                return IRI.create(prefix + split[1]);
            }
        }
        return IRI.create(s);
    }

    private OWLIndividual fromExample(Example example){
        return new OWLNamedIndividualImpl(resolvePrefix(example.getUri()));
    }

    private AbstractClassExpressionLearningProblem<? extends Score> createProblem(Collection<? extends Example> examples){
        Set<OWLIndividual> pos = new HashSet<>();
        Set<OWLIndividual> neg = new HashSet<>();

        for(Example example: examples) {
            if (example instanceof PositiveExample)
                pos.add(fromExample(example));
            if (example instanceof NegativeExample)
                neg.add(fromExample(example));
        }

        if(neg.isEmpty()){
            PosOnlyLP lp = new PosOnlyLP();
            lp.setPositiveExamples(pos);
            return lp;
        }else {
            PosNegLP lp = new PosNegLPStandard();
            lp.setPositiveExamples(pos);
            lp.setNegativeExamples(neg);
            return lp;
        }
    }


    private Set<Example> expandByRestriction(KBInfo info, List<String> restriction, Set<Example> examples){
        Set<Example> set = new HashSet<>(examples);
        String res = createRestriction(restriction);
        String query = "select ?x where {"+res+"}";

        LazyQueryFactory factory = new LazyQueryFactory();
        ExampleFactory exampleFactory = new ExampleFactory(new HashMap<>());

        for(QuerySolution solution: factory.create(info, query)){

            Resource r = solution.getResource("?x");
            if(r != null)
                set.add(exampleFactory.createPositive(
                        ExampleFactory.ExampleSource.SOURCE, r.getURI()
                ));

        }

        return set;
    }

    private String createRestriction(List<String> restrictions){
        String query = "", where;

        for (int i = 0; i < restrictions.size(); i++) {
            where = restrictions.get(i).trim();
            if (where.length() > 3) {
                query = query + where + " .\n";
            }
        }

        return query;
    }



    private String transformExpr(OWLClassExpression expr, String rootVar){

        OWLClassExpressionToSPARQLConverter converter = new OWLClassExpressionToSPARQLConverter();

        String s = converter.convert(rootVar, expr).trim();

        if(s.matches("(.*)\\.")){
            s = s.substring(0, s.length()-1);
        }
        return s;
    }

}
