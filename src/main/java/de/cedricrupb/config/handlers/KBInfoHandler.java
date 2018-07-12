package de.cedricrupb.config.handlers;

import de.cedricrupb.config.IConfigHandler;
import de.cedricrupb.config.model.*;
import org.aksw.limes.core.io.config.KBInfo;
import org.aksw.limes.core.io.config.reader.xml.XMLConfigurationReader;

import java.util.*;

/**
 * A handler for the knowledge base configuration.
 * A knowledge base defines the endpoint which should be queried
 *
 * @author Cedric Richter
 */
public class KBInfoHandler implements IConfigHandler {

    @Override
    public boolean isHandling(String parent, String name) {
        return parent.equalsIgnoreCase("DLLIMES") && (name.equalsIgnoreCase("source") ||
                name.equalsIgnoreCase("target"));
    }

    @Override
    public Object parse(String parent, String name, Map<String, String> attributes, Map<String, Object> childs, Map<String, Object> parserContext) {
        Map<String, String> prefixes;
        if(parserContext.containsKey("prefixes")){
            prefixes = (Map<String, String>)parserContext.get("prefixes");
        }else{
            prefixes = new HashMap<>();
            parserContext.put("prefixes", prefixes);
        }

        KBInfo src = new KBInfo();
        src.setPrefixes(prefixes);
        src.setId((String)childs.get("id"));
        src.setEndpoint((String)childs.get("endpoint"));
        src.setVar((String)childs.get("var"));
        if(childs.containsKey("pagesize"))
            src.setPageSize(Integer.parseInt((String)childs.get("pagesize")));
        if(childs.containsKey("restriction")) {
            src.setRestrictions(
                    new ArrayList<String>(
                            Arrays.asList(new String[]{(String) childs.get("restriction")})
                    )
            );
        }
        if(childs.containsKey("property")) {
            for(String prop: iterate(childs.get("property"), String.class))
                XMLConfigurationReader.processProperty(src, prop);
        }
        if(childs.containsKey("optional_property")) {
            for(String prop: iterate(childs.get("optional_property"), String.class))
                XMLConfigurationReader.processOptionalProperty(src, prop);
        }

        if(childs.containsKey("type")){
            src.setType((String)childs.get("type"));
        }

        new ExampleFactory(parserContext);
        ExampleFactory.ExampleSource source;

        if(name.equalsIgnoreCase("source")){
            source = ExampleFactory.ExampleSource.SOURCE;
        }else{
            source = ExampleFactory.ExampleSource.TARGET;
        }

        Set<Example> examples = (Set<Example>)parserContext.get(ExampleFactory.getKey(source));

        if(name.equalsIgnoreCase("source"))
            return new SourceDomainConfig(src, examples);
        else
            return new TargetDomainConfig(src, examples);
    }


    private <T> Iterable<? extends T> iterate(Object o, Class<? extends T> clazz){

        if(o instanceof Iterable){
            return (Iterable<? extends T>)o;
        }

        List<T> list = new ArrayList<>();
        list.add((T)o);
        return list;
    }
}
