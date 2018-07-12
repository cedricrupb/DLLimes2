package de.cedricrupb.config.handlers;

import de.cedricrupb.config.IConfigHandler;
import de.cedricrupb.config.model.Prefix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A handler for prefix configuration. It allows to define a new prefix for
 * both source and target endpoint
 *
 * @author Cedric Richter
 */
public class PrefixHandler implements IConfigHandler {

    @Override
    public boolean isHandling(String parent, String name) {
        return name.equalsIgnoreCase("prefix");
    }

    @Override
    public Object parse(String parent, String name, Map<String, String> attributes, Map<String, Object> childs,
                        Map<String, Object> parserContext) {

        Prefix prefix = new Prefix((String)childs.get("namespace"), (String)childs.get("label"));

        if(!parserContext.containsKey("prefixes")){
            parserContext.put("prefixes", new HashMap<String, String>());
        }
        ((Map<String, String>)parserContext.get("prefixes")).put(prefix.getNamespace(), prefix.getLabel());

        return prefix;
    }
}
