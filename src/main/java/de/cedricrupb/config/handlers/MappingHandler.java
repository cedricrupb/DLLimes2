package de.cedricrupb.config.handlers;

import de.cedricrupb.config.IConfigHandler;
import de.cedricrupb.config.model.Reference;
import org.aksw.limes.core.evaluation.evaluationDataLoader.OAEIMappingParser;
import org.aksw.limes.core.io.mapping.AMapping;

import java.sql.Ref;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * A handler for mapping configuration.
 * Mapping defines a relationship between one instance of the source knowledge base
 * and one instance of the target knowledge base.
 *
 * @author Cedric Richter
 */
public class MappingHandler implements IConfigHandler {

    @Override
    public boolean isHandling(String parent, String name) {
        return name.equalsIgnoreCase("MAPPING");
    }

    @Override
    public Object parse(String parent, String name, Map<String, String> attributes, Map<String, Object> childs, Map<String, Object> parserContext) {
        Set<Reference> mapping = new HashSet<>();

        if(childs.containsKey("positive")){
            Object o = childs.get("positive");
            if(o instanceof List){
                mapping.addAll((List<Reference>)o);
            }else{
                mapping.add((Reference)o);
            }
        }

        if(childs.containsKey("negative")){
            Object o = childs.get("negative");
            if(o instanceof List){
                mapping.addAll((List<Reference>)o);
            }else{
                mapping.add((Reference)o);
            }
        }

        return mapping;

    }
}
