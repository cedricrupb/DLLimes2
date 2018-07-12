package de.cedricrupb.config.handlers;

import de.cedricrupb.config.IConfigHandler;
import de.cedricrupb.config.model.MLConfig;
import org.aksw.limes.core.ml.algorithm.LearningParameter;
import org.aksw.limes.core.ml.algorithm.MLAlgorithmFactory;

import java.util.List;
import java.util.Map;

/**
 *
 * A handler for the machine learning configuration.
 * Refer to LIMES for a detailed description
 *
 * @author Cedric Richter
 */
public class MLHandler implements IConfigHandler {

    @Override
    public boolean isHandling(String parent, String name) {
        return name.equalsIgnoreCase("mlalgorithm");
    }

    @Override
    public Object parse(String parent, String name, Map<String, String> attributes, Map<String, Object> childs, Map<String, Object> parserContext) {
        MLConfig config = new MLConfig((String)childs.get("name"),
                Double.parseDouble((String)childs.get("threshold")));

        if(childs.containsKey("type")){
            config.setMlImplementationType(
                    MLAlgorithmFactory.getImplementationType((String)childs.get("type"))
            );
        }

        if(childs.containsKey("training")){
            config.setMlTrainingDataFile(
                    (String)childs.get("training")
            );
        }

        if(childs.containsKey("parameter")){
            Object o = childs.get("parameter");
            if(o instanceof List){
                for(LearningParameter p: (List<LearningParameter>)o){
                    config.getMlAlgorithmParameters().add(p);
                }
            }else if(o instanceof LearningParameter){
                config.getMlAlgorithmParameters().add((LearningParameter)o);
            }
        }

        return config;
    }
}
