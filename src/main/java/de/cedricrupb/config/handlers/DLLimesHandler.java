package de.cedricrupb.config.handlers;

import com.google.common.collect.ImmutableSet;
import de.cedricrupb.config.IConfigHandler;
import de.cedricrupb.config.model.*;

import java.util.Map;
import java.util.Set;

/**
 *
 * A handler for the root of DOM tree.
 * It merges all subconfigurations in one model.
 *
 * @author Cedric Richter
 */
public class DLLimesHandler implements IConfigHandler {

    @Override
    public boolean isHandling(String parent, String name) {
        return name.equalsIgnoreCase("DLLimes");
    }

    @Override
    public Object parse(String parent, String name, Map<String, String> attributes, Map<String, Object> childs,
                        Map<String, Object> parserContext) {

        SourceDomainConfig srcConfig = (SourceDomainConfig)childs.get("source");
        TargetDomainConfig targetConfig = (TargetDomainConfig)childs.get("target");

        MLConfig mlConfig;
        if(childs.containsKey("mlalgorithm")) {
            mlConfig = (MLConfig) childs.get("mlalgorithm");
        }else{
            mlConfig = new MLConfig();
        }

        TerminateConfig terminateConfig;

        if(childs.containsKey("terminate")){
            terminateConfig = (TerminateConfig) childs.get("terminate");
        }else{
            terminateConfig = new TerminateConfig();
        }

        Set<Reference> mapping;

        if(childs.containsKey("mapping")){
            mapping = (Set<Reference>) childs.get("mapping");
        }else{
            mapping = ImmutableSet.of();
        }

        return new LearningConfig(srcConfig, targetConfig, mapping, mlConfig, terminateConfig);
    }
}
