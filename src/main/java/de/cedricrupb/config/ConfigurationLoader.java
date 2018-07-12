package de.cedricrupb.config;

import de.cedricrupb.config.handlers.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Class Loads all the Configuartions from Package "Handlers" and adds them to a list
 *
 * Brings all the configs from ChildPrefixHandler, PrefixHandler, KBInfoChildHandler, KBInfoHandler, SourceExampleHandler,
 * TargetExampleHandler , MappingHandler, MLChildHandler, MLHandler, TerminateChildHandler,TerminateHandler,
 * DLLimesHandler, MappingHandler, SameHandler, SameChildHandler to be used again.
 *
 * @author Cedric Richter
 */


public class ConfigurationLoader {

    public static ConfigurationLoader createDefault(){
        List<IConfigHandler> list = new ArrayList<>();
        list.add(new ChildPrefixHandler());
        list.add(new PrefixHandler());
        list.add(new KBInfoChildHandler());
        list.add(new KBInfoHandler());
        list.add(new SourceExampleHandler());
        list.add(new TargetExampleHandler());
        list.add(new MappingHandler());
        list.add(new MLChildHandler());
        list.add(new MLHandler());
        list.add(new TerminateChildHandler());
        list.add(new TerminateHandler());
        list.add(new DLLimesHandler());
        list.add(new MappingHandler());
        list.add(new SameHandler());
        list.add(new SameChildHandler());
        return new ConfigurationLoader(list);
    }

    private List<IConfigHandler> handlers;

    public ConfigurationLoader(List<IConfigHandler> handlers) {
        this.handlers = handlers;
    }

    /**
     *
     * Parse object from XML files
     *
     * @return handler on root
     */
    public Object parseFromXML(String path) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        SAXConfigParser handler = new SAXConfigParser(handlers);
        saxParser.parse(path, handler);
        return handler.getRoot();
    }

}
