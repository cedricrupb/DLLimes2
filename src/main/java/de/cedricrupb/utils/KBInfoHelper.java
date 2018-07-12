package de.cedricrupb.utils;

import org.aksw.limes.core.io.config.KBInfo;
import org.aksw.limes.core.io.config.reader.xml.XMLConfigurationReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * This Class contains all methods and operations related to KBinfo.
 *
 * @author Cedric Richter
 */


public class KBInfoHelper {

    public static KBInfo copy(KBInfo info){
        KBInfo copy = new KBInfo();
        copy.setType(info.getType());
        copy.setEndpoint(info.getEndpoint());
        copy.setId(info.getId());
        copy.setPrefixes(info.getPrefixes());
        copy.setPageSize(info.getPageSize());
        copy.setVar(info.getVar());
        copy.setRestrictions(info.getRestrictions());
        copy.setProperties(info.getProperties());
        copy.setOptionalProperties(info.getOptionalProperties());
        copy.setGraph(info.getGraph());
        copy.setFunctions(info.getFunctions());
        return copy;
    }

    public static KBInfo injectRestriction(KBInfo info, String restriction){
        KBInfo copy = copy(info);
        ArrayList<String> restrictions = new ArrayList<>();
        restrictions.add(restriction);
        copy.setRestrictions(restrictions);
        return copy;
    }

    public static KBInfo injectProperties(KBInfo info, Set<String> properties){
        KBInfo copy = copy(info);
        List<String> props = new ArrayList<>();
        copy.setProperties(props);
        for(String prop: properties)
            XMLConfigurationReader.processProperty(copy, prop);
        return copy;
    }

    public static KBInfo injectOptionalProperties(KBInfo info, Set<String> properties){
        KBInfo copy = copy(info);
        List<String> props = new ArrayList<>();
        copy.setProperties(props);
        for(String prop: properties)
            XMLConfigurationReader.processOptionalProperty(copy, prop);
        return copy;
    }

}
