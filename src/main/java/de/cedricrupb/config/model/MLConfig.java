package de.cedricrupb.config.model;

import org.aksw.limes.core.ml.algorithm.LearningParameter;
import org.aksw.limes.core.ml.algorithm.MLImplementationType;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Class to set Configuration of MachineLearningAlgorithm
 *
 * @author Cedric Richter
 */


public class MLConfig {

    private String mlAlgorithmName = new String();
    private double threshold = 0.9;
    private List<LearningParameter> mlAlgorithmParameters = new ArrayList<>();
    private MLImplementationType mlImplementationType = MLImplementationType.UNSUPERVISED;
    private String mlTrainingDataFile = new String();

    public MLConfig(String mlAlgorithmName, double threshold) {
        this.mlAlgorithmName = mlAlgorithmName;
        this.threshold = threshold;
    }

    public MLConfig(){
        this("wombat simple", 0.9);
    }


    public void setMlAlgorithmParameters(List<LearningParameter> mlAlgorithmParameters) {
        this.mlAlgorithmParameters = mlAlgorithmParameters;
    }

    public void setMlImplementationType(MLImplementationType mlImplementationType) {
        this.mlImplementationType = mlImplementationType;
    }

    public void setMlTrainingDataFile(String mlTrainingDataFile) {
        this.mlTrainingDataFile = mlTrainingDataFile;
    }

    public String getMlAlgorithmName() {
        return mlAlgorithmName;
    }

    public List<LearningParameter> getMlAlgorithmParameters() {
        return mlAlgorithmParameters;
    }

    public MLImplementationType getMlImplementationType() {
        return mlImplementationType;
    }

    public String getMlTrainingDataFile() {
        return mlTrainingDataFile;
    }

    public double getThreshold() {
        return threshold;
    }

}
