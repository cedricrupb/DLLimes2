package de.cedricrupb.evaluation;

import de.cedricrupb.config.model.*;
import org.aksw.limes.core.evaluation.evaluationDataLoader.DataSetChooser;
import org.aksw.limes.core.evaluation.evaluationDataLoader.EvaluationData;
import org.aksw.limes.core.io.mapping.AMapping;

import java.util.*;

/**
 *
 * Class to load DataSets from LIMES to describe learning configuration from source, target and GoldStarndard,
 * depending on these reference of Positive/Negative Examples are generated.
 *
 * @author Cedric Richter
 */


public class LimesDataSetLoader {

    public static LearningConfig getData(EvaluationData data, MLConfig ml, TerminateConfig terminateConfig){

        Set<Reference> mapping = sampleFromGold(data.getReferenceMapping(), 0.1);

        Set<Example> srcExamples = new HashSet<>();
        Set<Example> targetExamples = new HashSet<>();

        for(Reference reference: mapping){
            srcExamples.add(reference.getSource());
            targetExamples.add(reference.getTarget());
        }

        SourceDomainConfig srcConfig = new SourceDomainConfig(
                data.getConfigReader().getConfiguration().getSourceInfo(),
                srcExamples
        );

        srcConfig.getInfo().setProperties(new ArrayList<>());
        srcConfig.getInfo().setRestrictions(new ArrayList<>());

        TargetDomainConfig targetConfig = new TargetDomainConfig(
                data.getConfigReader().getConfiguration().getTargetInfo(),
                targetExamples
        );

        targetConfig.getInfo().setProperties(new ArrayList<>());
        //targetConfig.getInfo().setRestrictions(new ArrayList<>());


        return new LearningConfig(
                srcConfig,
                targetConfig,
                mapping,
                ml,
                terminateConfig
        );

    }

    /**
     *
     * @return Source & Target Tuples
     */
    private static Set<Reference> sampleFromGold(AMapping standard, double relativeSize){

        HashMap<String, HashMap<String, Double>> map = standard.getMap();

        List<StringTuple> tuples = new ArrayList<>();
        for(String s: map.keySet()){
            for(String p: map.get(s).keySet()){
                if(map.get(s).get(p) == 1.0){
                    tuples.add(new StringTuple(s, p));
                }
            }
        }

        Collections.shuffle(tuples);

        int length = (int)(relativeSize*tuples.size());

        Set<Reference> out = new HashSet<>();
        ExampleFactory factory = new ExampleFactory(new HashMap<>());

        for(int i = 0; i < length && i < tuples.size(); i++){
            out.add(
                    factory.createPositiveReference(
                            factory.createPositive(ExampleFactory.ExampleSource.SOURCE, tuples.get(i).first),
                            factory.createPositive(ExampleFactory.ExampleSource.TARGET, tuples.get(i).second)
                    )
            );
        }

        return out;
    }

    private static class StringTuple{

        private String first;
        private String second;

        public StringTuple(String first, String second) {
            this.first = first;
            this.second = second;
        }
    }

}
