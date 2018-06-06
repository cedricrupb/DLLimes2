package de.cedricrupb.config.handlers;

import de.cedricrupb.config.IConfigHandler;

import java.util.Map;


/**
 *
 * Handles the children of a knowledge base configuration in DOM tree.
 *
 * @author Cedric Richter
 */
public class KBInfoChildHandler implements IConfigHandler {

    @Override
    public boolean isHandling(String parent, String name) {
        return name.equalsIgnoreCase("ID") ||
                name.equalsIgnoreCase("ENDPOINT") ||
                name.equalsIgnoreCase("VAR") ||
                name.equalsIgnoreCase("PAGESIZE")||
                name.equalsIgnoreCase("GRAPH") ||
                name.equalsIgnoreCase("TYPE") ||
                name.equalsIgnoreCase("RESTRICTION")||
                name.equalsIgnoreCase("PROPERTY")||
                name.equalsIgnoreCase("OPTIONAL_PROPERTY");
    }

    @Override
    public Object parse(String parent, String name, Map<String, String> attributes, Map<String, Object> childs, Map<String, Object> parserContext) {
        return childs.get("TEXT");
    }
}
