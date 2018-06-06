package de.cedricrupb.utils;

import org.aksw.limes.core.io.config.KBInfo;
import org.aksw.limes.core.io.query.ModelRegistry;
import org.aksw.limes.core.io.query.QueryModuleFactory;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class EndPointHelper {

    public static ResultDescription queryEndpoint(KBInfo kb, String query){

        QueryModuleFactory.getQueryModule(kb.getType(), kb);

        Query sparqlQuery = QueryFactory.create(query, Syntax.syntaxARQ);
        QueryExecution qexec;

        // take care of graph issues. Only takes one graph. Seems like some
        // sparql endpoint do
        // not like the FROM option.
        if (!kb.getType().equalsIgnoreCase("sparql")) {
            Model model = ModelRegistry.getInstance().getMap().get(kb.getEndpoint());
            if (model == null) {
                throw new RuntimeException("No model with id '" + kb.getEndpoint() + "' registered");
            }
            qexec = QueryExecutionFactory.create(sparqlQuery, model);
        } else {
            if (kb.getGraph() != null) {
                qexec = QueryExecutionFactory.sparqlService(kb.getEndpoint(), sparqlQuery, kb.getGraph());
            } //
            else {
                qexec = QueryExecutionFactory.sparqlService(kb.getEndpoint(), sparqlQuery);
            }
        }
        return new ResultDescription(qexec.execSelect(), qexec);
    }


    public static String addPrefix(KBInfo kb, String s){
        Iterator<String> iter = kb.getPrefixes().keySet().iterator();
        String key, query = "";
        while (iter.hasNext()) {
            key = iter.next();
            query = query + "PREFIX " + key + ": <" + kb.getPrefixes().get(key) + ">\n";
        }
        return query+s;
    }

    public static String addOffset(String s, int offset, int limit){
        return s +" OFFSET "+offset+" LIMIT "+limit;
    }

    public static String genPropertyQuery(String resVar, String propVar, String var, List<String> res, Set<String> properties){
        String s =  "select distinct "+resVar+" "+propVar+" "+var+" where { values "+propVar+" {";

        for(String p: properties)
            s += p+" ";

        s += "} values "+resVar+" {";

        for(String p: res)
            s += p+" ";

        s += "} "+resVar+" "+propVar+" "+var+". } ORDER BY "+resVar+" "+propVar+" "+var;

        return s;
    }


}
