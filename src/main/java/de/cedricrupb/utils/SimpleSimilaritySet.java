package de.cedricrupb.utils;

import de.cedricrupb.config.model.NegativeReference;
import de.cedricrupb.config.model.PositiveReference;
import de.cedricrupb.config.model.Prefix;
import de.cedricrupb.config.model.Reference;
import org.aksw.limes.core.io.cache.ACache;
import org.aksw.limes.core.io.cache.Instance;
import org.aksw.limes.core.io.cache.MemoryCache;
import org.aksw.limes.core.io.mapping.AMapping;
import org.aksw.limes.core.measures.mapper.IMapper;
import org.aksw.limes.core.measures.mapper.MapperFactory;
import org.aksw.limes.core.measures.measure.IMeasure;
import org.aksw.limes.core.measures.measure.MeasureType;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * A Simple Similarity Set Based on prefixes, references, Source & Target instances.
 * Also provides the similarity score.
 *
 * @author Cedric Richter
 */


public class SimpleSimilaritySet implements ISimilaritySet {

    private Map<String, String> prefixes;
    private Collection<? extends Reference> references;
    private ACache sourceInstances;
    private ACache targetInstances;

    public SimpleSimilaritySet(Map<String, String> prefixes, Collection<? extends Reference> references, Map<String, Instance> sourceInstances, Map<String, Instance> targetInstances) {
        this.prefixes = prefixes;
        this.references = references;
        this.sourceInstances = fromMap(sourceInstances, false);
        this.targetInstances = fromMap(targetInstances, true);
    }


    private ACache fromMap(Map<String, Instance> map, boolean target){

        ACache result = new MemoryCache();

        for(Reference reference: references){
            result.addInstance(
                    map.get(
                            PrefixHelper.resolveSinglePrefix(
                                    target?reference.getTarget().getUri():reference.getSource().getUri(), prefixes
                            )
                    )
            );
        }

        return result;
    }


    @Override
    public ACache getSource() {
        return sourceInstances;
    }

    @Override
    public ACache getTarget() {
        return targetInstances;
    }

    @Override
    public int score(String prop1, String prop2, MeasureType measure, String methodName, double threshold) {

        int score = 0;
        prop1 = PrefixHelper.resolveSinglePrefix(prop1, prefixes);
        prop2 = PrefixHelper.resolveSinglePrefix(prop2, prefixes);

        IMapper mapper = MapperFactory.createMapper(measure);

        String expr = methodName + "(x."+prop1+",y."+prop2+")";

        AMapping mapping = mapper.getMapping(this.sourceInstances, this.targetInstances, "?x", "?y",
                                             expr, threshold);


        for(Reference reference: references){

            String src = PrefixHelper.resolveSinglePrefix(reference.getSource().getUri(), prefixes);
            String target = PrefixHelper.resolveSinglePrefix(reference.getTarget().getUri(), prefixes);

            if((reference instanceof PositiveReference && mapping.contains(src, target)) ||
                    ((reference instanceof NegativeReference && !mapping.contains(src, target)))){
                score += 1;
            }

        }

        return score;
    }

}
