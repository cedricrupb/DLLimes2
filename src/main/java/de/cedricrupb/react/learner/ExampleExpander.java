package de.cedricrupb.react.learner;

import de.cedricrupb.config.model.Example;
import de.cedricrupb.config.model.ExampleFactory;
import de.cedricrupb.config.model.PositiveExample;
import de.cedricrupb.utils.LazyQuery;
import de.cedricrupb.utils.LazyQueryFactory;
import javafx.geometry.Pos;
import org.aksw.limes.core.io.config.KBInfo;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Resource;

import java.util.*;

/**
 *
 * Class that finds Examples, Expands them from Restrictions, ValidatedExamples and PossibleExamples.
 *
 *
 * @author Cedric Richter
 */

public class ExampleExpander {

    private KBInfo info;
    private String restriction;

    private Set<Example> validatedExamples;
    private Set<PositiveExample> possibleExamples;

    public ExampleExpander(KBInfo info, String restriction, Set<Example> validatedExamples, Set<PositiveExample> possibleExamples) {
        this.info = info;
        this.validatedExamples = validatedExamples;
        this.possibleExamples = possibleExamples;
        this.restriction = restriction;
    }


    public Set<Example> findExamples(int size, double negativeRatio){

        Set<Example> result = new HashSet<>(size);

        int posSize = (int)((1 - negativeRatio)*size);
        int negSize = (int) (negativeRatio*size);

        for(Example example: validatedExamples){

            if(posSize <= 0 && negSize <= 0)
                break;

            if(example instanceof PositiveExample){
                if(posSize > 0 && result.add(example)){
                    posSize--;
                }
            }else{
                if(negSize > 0 && result.add(example)){
                    negSize --;
                }
            }


        }

        List<Example> sampling = new ArrayList<>(possibleExamples);
        //sampling.addAll(sampleNegative(negSize));

        Collections.shuffle(sampling);

        for(Example example: sampling){

            if(posSize <= 0 && negSize <= 0)
                break;

            if(example instanceof PositiveExample){
                if(posSize > 0 && result.add(example)){
                    posSize--;
                }
            }else{
                if(negSize > 0 && result.add(example)){
                    negSize --;
                }
            }

        }

        return result;
    }

    private List<Example> sampleNegative(int size){

        String var = info.getVar();
        String restriction = this.restriction;
        String query = "select "+var+" where {"+restriction+"}";

        ExampleFactory exampleFactory = new ExampleFactory(new HashMap<>());
        LazyQueryFactory queryFactory = new LazyQueryFactory();

        int load = 2*size;

        List<Example> list = new ArrayList<>(load);

        for(QuerySolution solution : queryFactory.create(info, query)){

            Resource x = solution.getResource(var);

            Example example = exampleFactory.createNegative(ExampleFactory.ExampleSource.SOURCE, x.getURI());

            if(!validatedExamples.contains(example) && !possibleExamples.contains(example)) {
                list.add(example);
            }

            if(list.size() >= load)
                break;
        }

        Collections.shuffle(list);

        return list.subList(0, Math.min(size, list.size()));

    }


}
