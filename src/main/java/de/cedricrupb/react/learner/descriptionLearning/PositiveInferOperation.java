package de.cedricrupb.react.learner.descriptionLearning;

import de.cedricrupb.config.model.Example;
import de.cedricrupb.config.model.ExampleFactory;
import de.cedricrupb.utils.LazyQueryFactory;
import org.aksw.limes.core.io.config.KBInfo;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Resource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PositiveInferOperation implements IExampleOperation {

    private List<String> restrictions;

    public PositiveInferOperation(List<String> restrictions) {
        this.restrictions = restrictions;
    }

    @Override
    public Set<Example> infer(KBInfo info, Set<Example> examples) {
        Set<Example> set = new HashSet<>(examples);
        String res = createRestriction(restrictions);
        String query = "select "+info.getVar()+" where {"+res+"}";

        LazyQueryFactory factory = new LazyQueryFactory();
        ExampleFactory exampleFactory = new ExampleFactory(new HashMap<>());

        for(QuerySolution solution: factory.create(info, query)){

            Resource r = solution.getResource(info.getVar());
            if(r != null)
                set.add(exampleFactory.createPositive(
                        ExampleFactory.ExampleSource.SOURCE, r.getURI()
                ));

        }

        return set;
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
