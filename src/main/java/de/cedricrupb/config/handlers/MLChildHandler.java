package de.cedricrupb.config.handlers;

import de.cedricrupb.config.IConfigHandler;
import org.aksw.limes.core.ml.algorithm.LearningParameter;

import java.util.Map;

/**
 *  This class handles children of the machine learning configuration in DOM tree.
 *  Refer to LIMES for a detailed description.
 *
 * @author Cedric Richter
 */
public class MLChildHandler implements IConfigHandler {

    @Override
    public boolean isHandling(String parent, String name) {
        return (parent.equalsIgnoreCase("MLALGORITHM") ||
                parent.equalsIgnoreCase("PARAMETER"))&& (
                name.equalsIgnoreCase("NAME") ||
                        name.equalsIgnoreCase("THRESHOLD") ||
                        name.equalsIgnoreCase("TYPE") ||
                        name.equalsIgnoreCase("VALUE") ||
                        name.equalsIgnoreCase("PARAMETER")
                );
    }
    /**
     * Parses the object to learning parameter
     * @return child
     */
    @Override
    public Object parse(String parent, String name, Map<String, String> attributes, Map<String, Object> childs, Map<String, Object> parserContext) {
        if(name.equalsIgnoreCase("PARAMETER")){
            LearningParameter param = new LearningParameter();

            param.setName((String)childs.get("name"));
            param.setValue(childs.get("value"));
            return param;
        }
        return childs.get("TEXT");
    }
}
