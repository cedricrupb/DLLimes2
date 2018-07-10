package de.cedricrupb.react.learner;

import de.cedricrupb.utils.LazyQueryFactory;
import de.cedricrupb.utils.PrefixHelper;
import org.aksw.limes.core.io.config.KBInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Resource;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PropertyCoverageFilter implements Runnable {

    static Log log = LogFactory.getLog(PropertyCoverageFilter.class);

    List<String> propFilter = new ArrayList<>();

    private KBInfo kb;
    private double threshold;

    private Set<String> properties;

    private long entityCount = -1;

    public PropertyCoverageFilter(KBInfo info, double threshold) {
        this.kb = info;
        this.threshold = threshold;
        propFilter.add("(.*)wikiPage(.*)");
    }


    @Override
    public void run() {
        Set<String> prop = new HashSet<>();

        if(!kb.getProperties().isEmpty()){
            properties = new HashSet<>(kb.getProperties());
            return;
        }

        LazyQueryFactory factory = new LazyQueryFactory();

        String res = createRestriction(kb.getRestrictions());
        String queryFull = createCountQuery(kb.getVar(), res);

        try {
            log.info("Start coverage calculation ( "+threshold+" threshold): "+kb.getRestrictions());
            for (QuerySolution solution : factory.create(kb, queryFull)) {
                entityCount = solution.getLiteral("?c").getLong();
            }

            NumberFormat format = NumberFormat.getPercentInstance();

            if(entityCount > 0) {
                String cov = createCoverageQuery(kb.getVar(), res);
                for (QuerySolution covSol : factory.create(kb, cov)) {
                    Resource r = covSol.getResource("?p");
                    long c = covSol.getLiteral("?count").getLong();

                    double coverage = entityCount > 0 ? c / entityCount : 1.0;

                    if (coverage < threshold) {
                        break;
                    }

                    log.info(r.getURI()+" is covered by "+format.format(coverage)+" instances.");

                    prop.add(r.getURI());
                }
            }

        }finally{
            factory.close();
        }

        properties = new HashSet<>();

        for(String p: prop){
            if(!filter(p))
                properties.add(PrefixHelper.revertSinglePrefix(p, kb.getPrefixes(), true));
        }

    }

    public Set<String> getProperties() {
        return properties;
    }

    public long getEntityCount() {
        return entityCount;
    }

    private boolean filter(String s){

        boolean filter = false;

        for(String regex: propFilter){

            filter |= s.matches(regex);

        }

        return filter;

    }

    private String createRestriction(List<String> restrictions){
        String query = "", where;

        for (int i = 0; i < restrictions.size(); i++) {
            where = restrictions.get(i).trim();
            if (where.length() > 3) {
                query = query + where + " .\n";
            }
        }

        return query;
    }

    private String createCoverageQuery(String var, String res){
        return "SELECT DISTINCT ?p (count(?p) AS ?count) WHERE {"+res+" "+var+" ?p [].}" +
                " GROUP BY ?p ORDER BY DESC(?count)";
    }

    private String createCountQuery(String var, String res){
        return "SELECT DISTINCT (count(distinct "+var+") AS ?c) WHERE {"+res+"}";
    }
}
