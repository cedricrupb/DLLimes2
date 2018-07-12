package de.cedricrupb.config.handlers;

import de.cedricrupb.config.IConfigHandler;

import java.util.Map;


/**
 *
 * Class to terminate ChildHandler Operations after a fixpoint/iterations.
 *
 * @author Cedric Richter
 */


public class TerminateChildHandler implements IConfigHandler {

    @Override
    public boolean isHandling(String parent, String name) {
        return parent.equalsIgnoreCase("terminate") && (
                name.equalsIgnoreCase("iteration") ||
                        name.equalsIgnoreCase("fixpoint") ||
                        name.equalsIgnoreCase("file")
                );
    }

    @Override
    public Object parse(String parent, String name, Map<String, String> attributes, Map<String, Object> childs, Map<String, Object> parserContext) {
        return childs.get("TEXT");
    }
}
