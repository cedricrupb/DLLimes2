package de.cedricrupb.react.model;

import de.cedricrupb.config.model.Reference;
import de.cedricrupb.utils.LazyQueryFactory;
import de.cedricrupb.utils.PrefixHelper;
import org.aksw.limes.core.io.config.KBInfo;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Resource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PropertyCoverageFilter implements Runnable {

    private KBInfo kb;
    private double threshold;

    private Set<String> properties;

    public PropertyCoverageFilter(KBInfo info, double threshold) {
        this.kb = info;
        this.threshold = threshold;
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

        long count = 0;
        try {

            for (QuerySolution solution : factory.create(kb, queryFull)) {
                count = solution.getLiteral("?c").getLong();
            }

            String cov = createCoverageQuery(kb.getVar(), res);
            for (QuerySolution covSol : factory.create(kb, cov)) {
                Resource r = covSol.getResource("?p");
                long c = covSol.getLiteral("?count").getLong();

                double coverage = c / count;

                if (coverage < threshold) {
                    break;
                }

                prop.add(r.getURI());
            }

        }finally{
            factory.close();
        }

        properties = new HashSet<>();

        for(String p: prop){
            properties.add(PrefixHelper.revertSinglePrefix(p, kb.getPrefixes()));
        }

    }

    public Set<String> getProperties() {
        return properties;
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
        return "SELECT DISTINCT (count("+var+") AS ?c) WHERE {"+res+"}";
    }
}
