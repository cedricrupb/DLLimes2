package de.cedricrupb.config.handlers;

import de.cedricrupb.config.IConfigHandler;
import de.cedricrupb.config.model.TerminateConfig;

import java.util.Map;

public class TerminateHandler implements IConfigHandler {

    @Override
    public boolean isHandling(String parent, String name) {
        return name.equalsIgnoreCase("terminate");
    }

    @Override
    public Object parse(String parent, String name, Map<String, String> attributes, Map<String, Object> childs, Map<String, Object> parserContext) {
        int iterations = Integer.parseInt((String)childs.get("iteration"));
        boolean fix = Boolean.parseBoolean((String)childs.get("fixpoint"));
        String file = (String)childs.get("file");

        return new TerminateConfig(iterations, fix, file);
    }
}
