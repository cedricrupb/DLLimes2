package de.cedricrupb.utils;

import org.aksw.limes.core.measures.measure.IMeasure;

public interface ISimilaritySet {

    public int score(String prop1, String prop2, IMeasure measure, double threshold);


}
