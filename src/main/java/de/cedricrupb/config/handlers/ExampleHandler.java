package de.cedricrupb.config.handlers;

import de.cedricrupb.config.IConfigHandler;
import de.cedricrupb.config.model.ExampleFactory;

import java.util.Map;

/**
 *
 * Abstract handler for examples. It can handle both postive and negative examples.
 *
 * @author Cedric Richter
 */
public abstract class ExampleHandler implements IConfigHandler {

    /**
     *
     * Handles the namespace, label as Negative & Positive examples
     * @return  Namespace and Label
     */

    @Override
    public boolean isHandling(String parent, String name) {
        return name.equalsIgnoreCase("POSITIVE-EXAMPLE") ||
                name.equalsIgnoreCase("NEGATIVE-EXAMPLE");
    }

    /**
     * Parses the objects
     *
     * @return Positive/Negative Examples
     *
     */
    @Override
    public Object parse(String parent, String name, Map<String, String> attributes, Map<String, Object> childs, Map<String, Object> parserContext) {
        ExampleFactory factory = new ExampleFactory(parserContext);
        if(name.equalsIgnoreCase("POSITIVE-EXAMPLE")){
            return factory.createPositive(getSource(), (String)childs.get("TEXT"));
        }
        return factory.createNegative(getSource(), (String)childs.get("TEXT"));
    }


    /**
     * It is important to declare if an example is available in the source endpoint
     * or in the target endpoint.
     * @return the source of the example
     */
    protected abstract ExampleFactory.ExampleSource getSource();

}
