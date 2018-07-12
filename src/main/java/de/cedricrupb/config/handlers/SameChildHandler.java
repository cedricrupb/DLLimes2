package de.cedricrupb.config.handlers;

import de.cedricrupb.config.IConfigHandler;

import java.util.Map;

/**
 *
 * A handler for children of the sameAs reference configuration in DOM tree
 *
 * @author Cedric Richter
 */
public class SameChildHandler implements IConfigHandler {

    @Override
    public boolean isHandling(String parent, String name) {
        return (parent.equalsIgnoreCase("POSITIVE") ||
                parent.equalsIgnoreCase("NEGATIVE")) &&
                (name.equalsIgnoreCase("SOURCE") ||
                 name.equalsIgnoreCase("TARGET"));
    }

    @Override
    public Object parse(String parent, String name, Map<String, String> attributes, Map<String, Object> childs, Map<String, Object> parserContext) {
        return childs.get("TEXT");
    }
}
