package de.cedricrupb.utils;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontendImpl;
import org.aksw.jena_sparql_api.cache.file.CacheBackendFile;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.aksw.limes.core.io.cache.Instance;
import org.aksw.limes.core.io.cache.MemoryCache;
import org.aksw.limes.core.io.config.KBInfo;
import org.aksw.limes.core.io.describe.IConnectionConfig;
import org.aksw.limes.core.io.query.CsvQueryModule;
import org.aksw.limes.core.io.query.FileQueryModule;
import org.aksw.limes.core.io.query.ModelRegistry;
import org.aksw.limes.core.io.query.QueryModuleFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.core.DatasetDescription;


import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class EndPointHelper {

    public static final String CACHE_FOLDER = "cache/";

    private static EndPointHelper instance;

    public static EndPointHelper instance(){

        if(instance == null){
            instance = new EndPointHelper();
        }

        return instance;
    }

    private Map<KBIndex, QueryExecutionFactory> factories = new HashMap<>();


    private Model processCSV(KBInfo info){
        CsvQueryModule module = new CsvQueryModule(info);
        MemoryCache cache = new MemoryCache();
        module.fillAllInCache(cache);

        Model model = ModelFactory.createDefaultModel();

        for(Instance instance: cache.getAllInstances()){
            Resource resource = model.createResource(instance.getUri());
            for(String prop: instance.getAllProperties()) {
                Property property = model.createProperty("", prop);
                TreeSet<String> properties = instance.getProperty(prop);

                if(properties.size() == 1){
                    for(String s: properties) {
                        Literal literal = model.createLiteral(s, false);
                        model.add(
                                model.createStatement(resource, property, literal)
                        );
                    }
                }else{
                    List<RDFNode> list = new ArrayList<>();
                    for(String s: properties){
                        list.add(model.createLiteral(s, false));
                    }
                    model.add(
                            model.createStatement(resource, property, model.createList(list.iterator()))
                    );
                }

            }
        }


        return model;
    }


    private FluentQueryExecutionFactory<?> initFactory(KBInfo info){
        String name = info.getType();
        if(name.equalsIgnoreCase("N3") || name.toLowerCase().startsWith("nt") ||
                name.toLowerCase().startsWith("n-triple") ||
                name.toLowerCase().startsWith("turtle") || name.toLowerCase().startsWith("ttl") ||
                name.toLowerCase().startsWith("rdf") || name.toLowerCase().startsWith("xml")) {
            new FileQueryModule(info);
            Model model = ModelRegistry.getInstance().getMap().get(info.getEndpoint());
            return FluentQueryExecutionFactory.from(model);
        }else if(name.equalsIgnoreCase("csv")){
            Model model = processCSV(info);
            return FluentQueryExecutionFactory.from(model);
        }else{
            DatasetDescription dd = new DatasetDescription();
            if(info.getGraph() != null) {
                dd.addDefaultGraphURI(info.getGraph());
            }

            SparqlServiceReference ssr = new SparqlServiceReference(info.getEndpoint(), dd);

            return FluentQueryExecutionFactory.http(ssr);
        }
    }

    protected QueryExecutionFactory initQueryExecution(KBInfo kbInfo, IConnectionConfig config) {
        org.aksw.jena_sparql_api.core.QueryExecutionFactory qef;

        qef =   initFactory(kbInfo)
                .config()
                .withDelay(config.getRequestDelayInMs(), TimeUnit.MILLISECONDS)
                .end()
                .create();
        return qef;
    }

    protected QueryExecutionFactory wrapCachedQueryExecution(QueryExecutionFactory qef,
                                                                                           CacheFrontend frontend){
        return new QueryExecutionFactoryCacheEx(qef, frontend);

    }


    private QueryExecutionFactory initMappedFactory(KBInfo info){
        KBIndex index = new KBIndex(info.getEndpoint(), info.getGraph());
        if(!factories.containsKey(index)){

            CacheFrontend frontend = new CacheFrontendImpl(
                    new CacheBackendFile(
                            new File(CACHE_FOLDER), 24l * 60l * 60l * 1000l
                    )
            );

            QueryExecutionFactory factory = initFactory(info).config()
                                                .withRetry(3, 2000, TimeUnit.MILLISECONDS)
                                            .end().create();
            factory = wrapCachedQueryExecution(factory, frontend);
            factories.put(index, factory);

        }
        return factories.get(index);
    }



    public ResultDescription queryEndpoint(KBInfo kb, String query){

        QueryExecutionFactory qef = initMappedFactory(kb);

        //QueryModuleFactory.getQueryModule(kb.getType(), kb);

        Query sparqlQuery = QueryFactory.create(query, Syntax.syntaxARQ);
        QueryExecution qexec = qef.createQueryExecution(sparqlQuery);

        return new ResultDescription(qexec.execSelect(), qexec);
    }


    public String addPrefix(KBInfo kb, String s){
        Iterator<String> iter = kb.getPrefixes().keySet().iterator();
        String key, query = "";
        while (iter.hasNext()) {
            key = iter.next();
            query = query + "PREFIX " + key + ": <" + kb.getPrefixes().get(key) + ">\n";
        }
        return query+s;
    }

    public String addOffset(String s, int offset, int limit){
        return s +" OFFSET "+offset+" LIMIT "+limit;
    }

    public String genPropertyQuery(String resVar, String propVar, String var, List<String> res, Set<String> properties){
        String s =  "select distinct "+resVar+" "+propVar+" "+var+" where { "+resVar+" "+propVar+" "+var+". values "+propVar+" {";

        for(String p: properties)
            s += p+" ";

        s += "} values "+resVar+" {";

        for(String p: res)
            s += p+" ";

        s += "} } ORDER BY "+resVar+" "+propVar+" "+var+"";

        return s;
    }

    private class KBIndex {

        private String endpoint;
        private String graph;

        public KBIndex(String endpoint, String graph) {
            this.endpoint = endpoint;
            this.graph = graph;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            KBIndex kbIndex = (KBIndex) o;
            return Objects.equals(endpoint, kbIndex.endpoint) &&
                    Objects.equals(graph, kbIndex.graph);
        }

        @Override
        public int hashCode() {

            return Objects.hash(endpoint, graph);
        }
    }

}
