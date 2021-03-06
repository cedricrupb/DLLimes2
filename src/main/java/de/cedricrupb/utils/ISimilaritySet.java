package de.cedricrupb.utils;

import org.aksw.limes.core.io.cache.ACache;
import org.aksw.limes.core.measures.measure.IMeasure;
import org.aksw.limes.core.measures.measure.MeasureType;

/**
 *
 * Interface class implementing ACache methods for SimilaritySet
 *
 * @author Cedric Richter
 */


public interface ISimilaritySet {

    public ACache getSource();

    public ACache getTarget();

    public int score(String prop1, String prop2, MeasureType measure, String methodName, double threshold);


}
