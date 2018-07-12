package de.cedricrupb.react.learner.descriptionLearning;


import de.cedricrupb.config.model.Example;
import org.aksw.limes.core.io.config.KBInfo;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.OWLOntologyKnowledgeSource;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.kb.sparql.simple.SparqlSimpleExtractor;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.SPARQLReasoner;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class DLComponentFactory {

    static Logger logger = LoggerFactory.getLogger(DLComponentFactory.class.getName());

    public static final int SPARQL_RECURSION_DEPTH = 1;


    private KBInfo info;

    public DLComponentFactory(KBInfo info){
        this.info = info;
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
        if(!useSparqlReasoner()){

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

    public boolean isLocal(){
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

    public boolean useSparqlReasoner(){
        return !isLocal() && !info.getEndpoint().toLowerCase().contains("dbpedia.org");
    }


    public KnowledgeSource createKnowledgeSource(Set<String> instances) throws ComponentInitException, MalformedURLException {
        if(isLocal()){
            OWLFile ks = new OWLFile();
            ks.setFileName(info.getEndpoint());
            ks.init();
            return ks;
        }else{
            return initSparql(info, instances);
        }
    }

    public KnowledgeSource createExampleKnowledgeSource(Set<Example> examples) throws ComponentInitException, MalformedURLException {
        SortedSet<OWLIndividual> set = getInstances(info, examples);
        Set<String> iris = new HashSet<>();

        for(OWLIndividual i: set){
            iris.add(i.toStringID());
        }

        return createKnowledgeSource(iris);
    }

    public AbstractReasonerComponent createReasoner(KnowledgeSource ks) throws ComponentInitException {
        AbstractReasonerComponent reasoner;

        if(ks instanceof OWLOntologyKnowledgeSource){
            reasoner = new ClosedWorldReasoner();
        }else{
            reasoner = new SPARQLReasoner();
        }

        reasoner.setSources(ks);

        reasoner.init();


        return reasoner;
    }


    public AbstractReasonerComponent createReasoner(Set<String> examples) throws MalformedURLException, ComponentInitException {
        KnowledgeSource ks = this.createKnowledgeSource(examples);
       return createReasoner(ks);
    }

    public AbstractReasonerComponent createExampleReasoner(Set<Example> examples) throws MalformedURLException, ComponentInitException {
        KnowledgeSource ks = this.createExampleKnowledgeSource(examples);
        return createReasoner(ks);
    }

    public SortedSet<OWLIndividual> getInstances(KBInfo info, Set<Example> examples){

        SortedSet<OWLIndividual> instances = new TreeSet<>();

        for(Example example: examples){

            IRI iri = resolvePrefix(info, example.getUri());

            instances.add(new OWLNamedIndividualImpl(
                    iri
            ));
        }

        return instances;
    }

    public IRI resolvePrefix(KBInfo info, String s){
        String[] split = s.split(":");
        if(split.length == 2){
            String prefix = split[0];
            Map<String, String> prefixes = info.getPrefixes();

            if(prefixes.containsKey(prefix)){
                prefix = prefixes.get(prefix);
                return IRI.create(prefix + split[1]);
            }
        }
        return IRI.create(s);
    }

    private OWLIndividual fromExample(KBInfo info, Example example){
        return new OWLNamedIndividualImpl(resolvePrefix(info, example.getUri()));
    }


}
