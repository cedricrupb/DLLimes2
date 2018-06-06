package de.cedricrupb.config;

import java.util.Map;

public interface IConfigHandler {

    public boolean isHandling(String parent, String name);

    public Object parse(String parent, String name, Map<String, String> attributes, Map<String, Object> childs,
                        Map<String, Object> parserContext);

}
