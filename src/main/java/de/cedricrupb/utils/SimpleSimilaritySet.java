package de.cedricrupb.utils;

import de.cedricrupb.config.model.NegativeReference;
import de.cedricrupb.config.model.PositiveReference;
import de.cedricrupb.config.model.Prefix;
import de.cedricrupb.config.model.Reference;
import org.aksw.limes.core.io.cache.Instance;
import org.aksw.limes.core.measures.measure.IMeasure;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SimpleSimilaritySet implements ISimilaritySet {

    private Map<String, String> prefixes;
    private Collection<? extends Reference> references;
    private Map<String, Instance> sourceInstances;
    private Map<String, Instance> targetInstances;

    public SimpleSimilaritySet(Map<String, String> prefixes, Collection<? extends Reference> references, Map<String, Instance> sourceInstances, Map<String, Instance> targetInstances) {
        this.prefixes = prefixes;
        this.references = references;
        this.sourceInstances = sourceInstances;
        this.targetInstances = targetInstances;
    }

    @Override
    public int score(String prop1, String prop2, IMeasure measure, double threshold) {

        int score = 0;
        prop1 = PrefixHelper.resolveSinglePrefix(prop1, prefixes);
        prop2 = PrefixHelper.resolveSinglePrefix(prop2, prefixes);

        for(Reference reference: references){
            Instance src = sourceInstances.get(
                    PrefixHelper.resolveSinglePrefix(reference.getSource().getUri(), prefixes)
            );
            Instance tar = targetInstances.get(
                    PrefixHelper.resolveSinglePrefix(reference.getTarget().getUri(), prefixes)
            );

            if(src.getAllProperties().contains(prop1) && tar.getAllProperties().contains(prop2)){
                double val = measure.getSimilarity(src, tar, prop1, prop2);

                if((reference instanceof PositiveReference && val >= threshold)
                        || (reference instanceof NegativeReference && val < threshold)
                        )
                    score ++;
            }
        }

        return score;
    }

}
