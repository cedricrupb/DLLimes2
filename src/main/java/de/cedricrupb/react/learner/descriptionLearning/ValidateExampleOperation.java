package de.cedricrupb.react.learner.descriptionLearning;

import de.cedricrupb.config.model.Example;
import de.cedricrupb.utils.LazyQueryFactory;
import org.aksw.limes.core.io.config.KBInfo;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ValidateExampleOperation extends AExampleOperation {

    static Logger logger = LoggerFactory.getLogger(ValidateExampleOperation.class.getName());

    @Override
    public Set<Example> infer(KBInfo info, Set<Example> examples) {

        Map<String, Example> index = new HashMap<>();

        for(Example example: examples){
            index.put(this.resolvePrefix(info, example.getUri()).toString(), example);
        }

        String query = validationQuery(info, index.keySet());

        Set<Example> out = new HashSet<>();
        LazyQueryFactory factory = new LazyQueryFactory();

        for(QuerySolution solution: factory.create(info, query)){

            Resource x = solution.getResource(info.getVar());

            if(x == null)
                continue;

            if(index.containsKey(x.getURI())) {
                out.add(index.remove(x.getURI()));
            }

        }
        if(!index.isEmpty())
            logger.info("Didn't find "+new ArrayList<>(index.keySet()));

        return out;
    }


    private String validationQuery(KBInfo info, Set<String> examples){

        String query = "select distinct "+info.getVar()+" where { "+createRestriction(info.getRestrictions());

        query += " values "+info.getVar()+" { ";

        for(String example: examples)
            query = query +"<"+example+"> ";

        query += "}}";
        return query;
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
}
