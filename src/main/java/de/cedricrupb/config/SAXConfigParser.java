package de.cedricrupb.config;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;


/**
 *
 * Class SAXConfigParser creates a config based on StartElement, EndElement, Root and SAXContext.
 *
 * @author Cedric Richter
 */


public class SAXConfigParser extends DefaultHandler {

    private List<IConfigHandler> handlers;

    private Map<String, Object> parserContext = new HashMap<>();
    private Stack<SAXContext> contextStack = new Stack<>();

    private Object root;

    public SAXConfigParser(List<IConfigHandler> handlers) {
        this.handlers = handlers;
        contextStack.push(new SAXContext("ROOT", new HashMap<>()));
    }

    public void startElement(String uri, String localName,String qName,
                             Attributes attributes) throws SAXException {

        Map<String, String> attrs = new HashMap<>();

        for(int i = 0; i < attributes.getLength(); i++){
            attrs.put(attributes.getQName(i), attributes.getValue(i));
        }

        contextStack.push(
               new SAXContext(qName, attrs)
        );

    }

    public void characters(char ch[], int start, int length) throws SAXException {
        String txt = new String(ch, start, length);

        Map<String, Object> childs = contextStack.peek().childs;

        if(childs.containsKey("TEXT")){
            childs.put("TEXT", childs.get("TEXT")+txt);
        }else{
            childs.put("TEXT", txt);
        }
    }

    public void endElement(String uri, String localName,
                           String qName) throws SAXException {

        SAXContext ctx = contextStack.pop();
        SAXContext parentCtx = contextStack.peek();

        if(!ctx.name.equals(qName)){
            throw new SAXNotRecognizedException("End tag mismatch of "+qName);
        }

        qName = qName.toLowerCase();

        Object o = null;

        for(IConfigHandler handler: handlers){
            if(handler.isHandling(parentCtx.name, qName)){
                o = handler.parse(parentCtx.name, qName, ctx.attributes, ctx.childs, parserContext);
                break;
            }
        }

        if(o != null) {

            if(parentCtx.name.equals("ROOT")){
                root = o;
                return;
            }

            Map<String, Object> childs = parentCtx.childs;

            Object out = o;
            if(childs.containsKey(qName)) {
                Object act = childs.get(qName);

                if(act instanceof List){
                    ((List) act).add(o);
                    out = act;
                }else{
                    List<Object> list = new ArrayList<>();
                    list.add(act);
                    list.add(o);
                    out = list;
                }

            }

            childs.put(qName, out);
        }else {
            throw new SAXNotRecognizedException();
        }

    }

    public Object getRoot(){
        return root;
    }


    private class SAXContext{
        String name;
        Map<String, String> attributes;

        Map<String, Object> childs = new HashMap<>();

        public SAXContext(String name, Map<String, String> attributes) {
            this.name = name;
            this.attributes = attributes;
        }
    }
}
