package de.cedricrupb.config.handlers;

import de.cedricrupb.config.IConfigHandler;
import de.cedricrupb.config.model.Example;
import de.cedricrupb.config.model.ExampleFactory;

import java.util.Map;

/**
 * This handler allows to define two instances which are the same (positive) or
 * are not the same (negative).
 *
 * @author Cedric Richter
 */
public class SameHandler implements IConfigHandler {

    @Override
    public boolean isHandling(String parent, String name) {
        return parent.equalsIgnoreCase("MAPPING") &&
                (name.equalsIgnoreCase("POSITIVE") ||
                 name.equalsIgnoreCase("NEGATIVE"));
    }


    @Override
    public Object parse(String parent, String name, Map<String, String> attributes, Map<String, Object> childs, Map<String, Object> parserContext) {
        ExampleFactory factory = new ExampleFactory(parserContext);
        if(name.equalsIgnoreCase("POSITIVE")){
            Example source = factory.createPositive(ExampleFactory.ExampleSource.SOURCE, (String)childs.get("source"));
            Example target = factory.createPositive(ExampleFactory.ExampleSource.TARGET, (String)childs.get("target"));
            return factory.createPositiveReference(source, target);
        }else {
            Example source = factory.createNeutral(ExampleFactory.ExampleSource.SOURCE, (String)childs.get("source"));
            Example target = factory.createNeutral(ExampleFactory.ExampleSource.TARGET, (String)childs.get("target"));
            return factory.createNegativeReference(source, target);
        }
    }
}
