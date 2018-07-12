package de.cedricrupb.react.learner;

import de.cedricrupb.config.model.Example;
import de.cedricrupb.react.model.QualityReport;

import java.util.Set;

/**
 * Implements a strategy to seed either negative or positive examples
 *
 * @author Cedric Richter
 */
public class ImproveStrategy {

    private double learningRate = 0.01;
    private double negative = 0.5;

    private QualityReport report;
    private ExampleExpander expander;

    public ImproveStrategy(QualityReport report, ExampleExpander expander) {
        this.report = report;
        this.expander = expander;
    }

    public Set<Example> improveExamples(int size){

        if(report.getPseudoRecall() < 1){
            return improveRecall(size);
        }

        return expander.findExamples(size, negative);
    }

    private Set<Example> improveRecall(int size){
        return expander.findExamples(size, negative - learningRate, learningRate);
    }

}
