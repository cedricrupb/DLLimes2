package de.cedricrupb.config.handlers;

import de.cedricrupb.config.IConfigHandler;

import java.util.Map;

/**
 *
 * Handles the children of a prefix in DOM tree.
 *
 * @author Cedric Richter
 */
public class ChildPrefixHandler implements IConfigHandler {

    @Override
    public boolean isHandling(String parent, String name) {
        return name.equalsIgnoreCase("namespace") ||
                name.equalsIgnoreCase("label");
    }

    @Override
    public Object parse(String parent, String name, Map<String, String> attributes, Map<String, Object> childs,
                        Map<String, Object> parserContext) {
        return childs.get("TEXT");
    }
}
