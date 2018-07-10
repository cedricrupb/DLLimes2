package de.cedricrupb.react.learner;

import de.cedricrupb.config.model.Example;
import de.cedricrupb.config.model.NegativeReference;
import de.cedricrupb.config.model.PositiveReference;
import de.cedricrupb.config.model.Reference;
import de.cedricrupb.utils.*;
import org.aksw.limes.core.io.cache.ACache;
import org.aksw.limes.core.io.cache.Instance;
import org.aksw.limes.core.io.config.KBInfo;
import org.aksw.limes.core.measures.measure.MeasureType;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

import java.util.*;

public class PropertyLearner implements Runnable {

    static Logger logger = LoggerFactory.getLogger(ClassLearner.class.getName());

    private KBInfo srcInfo;
    private KBInfo targetInfo;
    private Set<Reference> mapping;
    private double epsilon;

    private Set<String> srcProperties;
    private Set<String> targetProperties;

    public PropertyLearner(KBInfo srcInfo, KBInfo targetInfo, Set<Reference> mapping, double epsilon) {
        this.srcInfo = srcInfo;
        this.targetInfo = targetInfo;
        this.mapping = mapping;
        this.epsilon = epsilon;
    }

    @Override
    public void run() {
        Set<PositiveReference> pos = new HashSet<>();
        Set<NegativeReference> neg = new HashSet<>();

        for(Reference r: mapping){
            if(r instanceof PositiveReference)
                pos.add((PositiveReference)r);
            if(r instanceof NegativeReference)
                neg.add((NegativeReference)r);
        }

        Set<String> currSrcPropeties = new HashSet<>(srcInfo.getProperties());
        Set<String> currTarPropeties = new HashSet<>(targetInfo.getProperties());

        logger.info("Build source test set");
        ISimilaritySet posTestSet = buildTestSet(pos, srcInfo.getProperties(), targetInfo.getProperties());

        fixSet(currSrcPropeties, posTestSet.getSource());
        fixSet(currTarPropeties, posTestSet.getTarget());

        logger.info("Build target test set");
        ISimilaritySet negTestSet = buildTestSet(neg, srcInfo.getProperties(), targetInfo.getProperties());

        fixSet(currSrcPropeties, negTestSet.getSource());
        fixSet(currTarPropeties, negTestSet.getTarget());

        List<Measure> measures = initMeasureMap();

        Set<String> srcOut = new HashSet<>();
        Set<String> targetOut = new HashSet<>();


        for(Measure measure: measures) {
            for (String src : currSrcPropeties) {
                if (srcOut.contains(src)) continue;

                for (String tar : currTarPropeties) {
                    if (targetOut.contains(tar)) continue;

                    int posScore = posTestSet.score(src, tar, measure.type, measure.name,  measure.threshold);
                    int negScore = negTestSet.score(src, tar, measure.type, measure.name,  measure.threshold);

                    double f = fmeasure(posScore, neg.size() - negScore, pos.size());

                    if (f >= epsilon) {
                        logger.info("Applicable property:\nSource: " + src + "\n Target:" + tar);
                        srcOut.add(src);
                        targetOut.add(tar);
                        break;
                    }

                }
            }
        }

        srcProperties = srcOut;
        targetProperties = targetOut;

    }

    public Set<String> getSrcProperties() {
        return srcProperties;
    }

    public Set<String> getTargetProperties() {
        return targetProperties;
    }


    private void fixSet(Set<String> properties, ACache cache){

        if(cache.size() == 0)
            return;

        Set<String> props = cache.getAllProperties();
        Set<String> comp = new HashSet<>();

        for(String p: props)
            comp.add(PrefixHelper.revertSinglePrefix(p, srcInfo.getPrefixes()));

        properties.retainAll(comp);
    }


    private double fmeasure(int posScore, int negScore, int allPos){

        if(posScore == 0)return 0.0;

        double precision = (double)posScore / (posScore + negScore);
        double recall = (double)posScore / allPos;

        return 2 * (precision * recall) / (precision + recall);
    }

    private List<Measure> initMeasureMap(){
        List<Measure> list = new ArrayList<>();

        list.add(new Measure("trigram", MeasureType.TRIGRAM, 0.6));
        list.add(new Measure("levenshtein", MeasureType.LEVENSHTEIN, 0.7));

        return list;
    }

    private ISimilaritySet buildTestSet(Set<? extends Reference> list, List<String> srcProp, List<String> targetProp){
        List<OWLIndividual> srcI = new ArrayList<>();
        List<OWLIndividual> tarI = new ArrayList<>();

        for(Reference r: list){
            srcI.add(fromSrcExample(r.getSource()));
            tarI.add(fromTargetExample(r.getTarget()));
        }

        Map<String, Instance> src = createInstances(srcInfo, srcI, srcProp);
        Map<String, Instance> tar = createInstances(targetInfo, tarI, targetProp);

        Map<String, String> prefixes = new HashMap<>(srcInfo.getPrefixes());
        prefixes.putAll(targetInfo.getPrefixes());

        return new SimpleSimilaritySet(prefixes, list, src, tar);
    }

    private Map<String, Instance> createInstances(KBInfo kb, List<OWLIndividual> ind, List<String> prop){

        Map<String, Instance> map = new HashMap<>();

        for(OWLIndividual i: ind){
            map.put(i.toStringID(), new Instance(i.toStringID()));
        }

        List<String> keys = new ArrayList<>();

        for(String e: map.keySet()){
            keys.add(PrefixHelper.revertSinglePrefix(e, kb.getPrefixes(), true));
        }

        LazyQueryFactory factory = new LazyQueryFactory();

        String query = EndPointHelper.instance().genPropertyQuery("?x", "?p", "?z",  keys, new HashSet<>(prop));
        query = EndPointHelper.instance().addPrefix(kb, query);

        try {
            for (QuerySolution sol : factory.create(kb, query)) {
                Resource inst = sol.getResource("?x");

                Instance instance = map.get(inst.getURI());

                if(instance == null)continue;

                logger.info("Loaded instance: "+instance.getUri());

                Resource property = sol.getResource("?p");
                RDFNode node = sol.get("?z");

                instance.addProperty(property.getURI(),
                                     node.toString());



            }
        }finally{
            factory.close();
        }

        return map;
    }

    private OWLIndividual fromSrcExample(Example example){
        return new OWLNamedIndividualImpl(
                IRI.create(
                    PrefixHelper.resolveSinglePrefix(example.getUri(), srcInfo.getPrefixes())
                )
        );
    }

    private OWLIndividual fromTargetExample(Example example){
        return new OWLNamedIndividualImpl(
                IRI.create(
                        PrefixHelper.resolveSinglePrefix(example.getUri(), targetInfo.getPrefixes())
                )
        );
    }

    private class Measure{

        private String name;
        private MeasureType type;

        private double threshold;

        public Measure(String name, MeasureType type, double threshold) {
            this.name = name;
            this.type = type;
            this.threshold = threshold;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Measure measure = (Measure) o;
            return Double.compare(measure.threshold, threshold) == 0 &&
                    Objects.equals(name, measure.name) &&
                    type == measure.type;
        }

        @Override
        public int hashCode() {

            return Objects.hash(name, type, threshold);
        }
    }

}
