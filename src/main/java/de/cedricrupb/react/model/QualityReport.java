package de.cedricrupb.react.model;

import org.aksw.limes.core.datastrutures.GoldStandard;
import org.aksw.limes.core.evaluation.qualititativeMeasures.PseudoPrecision;
import org.aksw.limes.core.io.mapping.AMapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * Class to obtain a quality report from a learned Classes from both source and target as well as mappings
 *
 * The report gives Precision, Fmeasure and Recall.
 *
 * @author Cedric Richter
 */

public class QualityReport {

    private int epoch;

    private LearnedClass sourceClass;
    private LearnedClass targetClass;

    private AMapping mapping;

    private long allSource;
    private long allTarget;

    private double precision = -1;
    private double recall = -1;

    public QualityReport(int epoch, LearnedClass sourceClass, LearnedClass targetClass, AMapping mapping, long allSource, long allTarget) {
        this.epoch = epoch;
        this.sourceClass = sourceClass;
        this.targetClass = targetClass;
        this.mapping = mapping;
        this.allSource = allSource;
        this.allTarget = allTarget;
    }

    public int getEpoch() {
        return epoch;
    }

    public LearnedClass getSourceClass() {
        return sourceClass;
    }

    public LearnedClass getTargetClass() {
        return targetClass;
    }

    public AMapping getMapping(){
        return mapping;
    }

    public double getSourceCoverage(){
        return (double)mapping.getMap().keySet().size()/getAllSource();
    }

    public double getTargetCoverage(){
        Set<String> values = new HashSet<>();
        for (Map.Entry<String, HashMap<String, Double>> e : mapping.getMap().entrySet()) {
            values.addAll(e.getValue().keySet());
        }
        return (double)values.size()/getAllTarget();
    }

    public long getAllSource() {
        return allSource;
    }

    public long getAllTarget() {
        return allTarget;
    }

    public double getPseudoPrecision(){
        if(precision == -1) {
            double p = mapping.getMap().keySet().size();
            double q = 0.0;

            for (String s : mapping.getMap().keySet()) {
                q += mapping.getMap().get(s).size();
            }

            if (q == 0)
                precision = 0.0;
            else
                precision = p/q;
        }
        return precision;
    }

    public double getPseudoRecall(){
        if(recall == -1) {
            double p = mapping.getMap().keySet().size();
            Set<String> values = new HashSet<>();
            for (Map.Entry<String, HashMap<String, Double>> e : mapping.getMap().entrySet()) {
                values.addAll(e.getValue().keySet());
            }

            recall = (p + values.size())/(double)(getAllSource() + getAllTarget());
        }

        return recall;
    }

    public double getPseudoFmeasure(){
        double p = getPseudoPrecision();
        double r = getPseudoRecall();

        if(p == 0 && r == 0)
            return 0.0;

        return (2*p*r)/(p+r);
    }


}
