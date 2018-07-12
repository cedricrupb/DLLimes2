package de.cedricrupb.config;

import java.util.Map;


/**
 *
 * Interface Class implementing methods of SAXConfigParses and Handler Classes of Source & Target
 *
 * @author Cedric Richter
 */


public interface IConfigHandler {

    public boolean isHandling(String parent, String name);

    public Object parse(String parent, String name, Map<String, String> attributes, Map<String, Object> childs,
                        Map<String, Object> parserContext);

}
