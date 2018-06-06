package de.cedricrupb.react.model;


import de.cedricrupb.config.model.Example;
import de.cedricrupb.config.model.LearningDomainConfig;
import de.cedricrupb.config.model.NegativeExample;
import de.cedricrupb.config.model.PositiveExample;
import org.aksw.limes.core.io.config.KBInfo;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.*;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.SPARQLReasoner;
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

public class ClassLearner implements Runnable{

    static Logger logger = LoggerFactory.getLogger(ClassLearner.class.getName());

    private LearningDomainConfig config;
    private Throwable exception;
    private String restriction;
    private double accuracy;

    public ClassLearner(LearningDomainConfig config){
        this.config = config;
    }


    @Override
    public void run() {
        LearningDomainConfig cfg = config;

        if(cfg.getInfo().getRestrictions() != null &&
                !cfg.getInfo().getRestrictions().isEmpty()){
            restriction = cfg.getInfo().getRestrictions().get(0);
            return;
        }

        KnowledgeSource ks;

        try{
            ks = initKS(cfg.getInfo());
        } catch (ComponentInitException e) {
            logger.error("Failed to init knowledge source", e);
            exception = e;
            return;
        } catch (MalformedURLException e) {
            logger.error("Crazy url?", e);
            exception = e;
            return;
        }

        AbstractReasonerComponent reasoner = null;

        if(isLocal(cfg.getInfo())){
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
            return;
        }

        AbstractClassExpressionLearningProblem<? extends Score> lp = createProblem(cfg.getExamples());
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

        logger.info("Finished execution with accuracy: "+alg.getCurrentlyBestAccuracy());

        restriction = transformExpr(alg.getCurrentlyBestDescription(), cfg.getInfo().getVar());
        restriction = revertPrefix(restriction, cfg.getInfo().getPrefixes());

        accuracy = alg.getCurrentlyBestAccuracy();


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
        return accuracy;
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

    private KnowledgeSource initKS(KBInfo info) throws ComponentInitException, MalformedURLException {
        if(isLocal(info)){
            OWLFile ks = new OWLFile();
            ks.setFileName(info.getEndpoint());
            ks.init();
            return ks;
        }else{
            SparqlEndpointKS ks = new SparqlEndpointKS();
            ks.setUrl(new URL(info.getEndpoint()));

            ks.init();
            return ks;
        }
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



    private String transformExpr(OWLClassExpression expr, String rootVar){

        OWLClassExpressionToSPARQLConverter converter = new OWLClassExpressionToSPARQLConverter();

        String str = String.valueOf(converter.asQuery(rootVar, expr));

        final Matcher matcher = Pattern.compile("WHERE").matcher(str);

        String restrictions = "";

        if(matcher.find()){
            restrictions = (str.substring(matcher.end()).trim());
            restrictions = restrictions.substring(1, restrictions.length()-2);
            restrictions = restrictions.replaceAll("\\s+", " ");


        }else{
            System.out.println("Problem while extracting Where clause: "+str);
        }

        return restrictions;
    }


    private String revertPrefix(String str, Map<String, String> prefix){

        final Matcher matcher = Pattern.compile("<\\S+>").matcher(str);

        Map<String, String> replace = new HashMap<>();

        while(matcher.find()){
            String entity = matcher.group();
            String replaceStr = entity.substring(1, entity.length()-1);

            for(Map.Entry<String, String> p: prefix.entrySet()){
                if(replaceStr.contains(p.getValue())){
                    replaceStr = replaceStr.replace(p.getValue(), p.getKey()+":");
                    replace.put(entity, replaceStr);
                    break;
                }
            }
        }

        for(Map.Entry<String, String> e: replace.entrySet())
            str = str.replaceAll(Pattern.quote(e.getKey()), e.getValue());

        return str;
    }

}
