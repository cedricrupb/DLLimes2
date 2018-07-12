package de.cedricrupb.react.learner;

import de.cedricrupb.config.model.Example;
import de.cedricrupb.config.model.ExampleFactory;
import de.cedricrupb.config.model.NegativeExample;
import de.cedricrupb.config.model.PositiveExample;
import de.cedricrupb.react.learner.descriptionLearning.ExampleHandler;
import de.cedricrupb.utils.LazyQuery;
import de.cedricrupb.utils.LazyQueryFactory;
import de.cedricrupb.utils.PrefixHelper;
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
        this.validatedExamples = unify(info, validatedExamples);
        this.possibleExamples = unify(info, possibleExamples);
        this.restriction = restriction;
    }


    private <T extends Example> Set<T> unify(KBInfo info, Set<T> examples){
        Set<T> out = new HashSet<>();
        ExampleFactory factory = new ExampleFactory(new HashMap<>());

        for(Example e: examples){
            String t = e.getUri();
            t = PrefixHelper.resolveSinglePrefix(t, info.getPrefixes());
            if(e instanceof PositiveExample)
                out.add((T) factory.createPositive(ExampleFactory.ExampleSource.SOURCE, t));
            else
                out.add((T) factory.createNegative(ExampleFactory.ExampleSource.SOURCE, t));
        }

        return out;
    }


    public Set<Example> findExamples(int size, double negativeRatio){
        return findExamples(size, negativeRatio, 0.0);
    }

    public Set<Example> findExamples(int size, double negativeRatio, double seed){

        Set<Example> positive = new HashSet<>();
        Set<Example> negativeExamples = new HashSet<>();

        for(Example example: validatedExamples){
            if(example instanceof PositiveExample)
                positive.add(example);
            if(example instanceof NegativeExample)
                negativeExamples.add(example);
        }


        int pos = (int)((1 - negativeRatio - seed)*size);
        int neg = (int)(negativeRatio*size) - negativeExamples.size();

        positive.addAll(possibleExamples);
        Set<Example> out = ExampleHandler.init(info, positive).truncate(pos).build();

        size = Math.min(out.size(), size);
        int seeding = (int)Math.ceil(seed*size);

        Set<Example> negative = new HashSet<>();

        negative.addAll(negativeExamples);
        negative.addAll(negative(neg));

        Set<Example> S = seed(seeding, negative);
        negative.removeAll(S);

        out.addAll(negative);
        out.addAll(S);

        return out;

    }

    private Set<Example> negative(int size){
        if(size == 0)return new HashSet<>();
        return ExampleHandler.init(info, new HashSet<>()).inferNegative(size).build();
    }


    private Set<Example> seed(int size, Set<Example> negative){
        if(size == 0)return new HashSet<>();
        List<String> restriction = new ArrayList<>();
        restriction.add(this.restriction);
        Set<Example> examples =  ExampleHandler.init(info, new HashSet<>())
                                                .inferPositive(restriction)
                                                .build();
        examples.removeAll(validatedExamples);
        examples.removeAll(possibleExamples);

        Set<Example> out = new HashSet<>();
        if(!examples.isEmpty()) {

            examples = ExampleHandler.init(info, examples).truncate(size).build();

            ExampleFactory factory = new ExampleFactory(new HashMap<>());

            for (Example ex : examples)
                out.add(factory.createNegative(ExampleFactory.ExampleSource.SOURCE, ex.getUri()));

        }else{
            examples = ExampleHandler.init(info, negative).truncate(size).build();

            ExampleFactory factory = new ExampleFactory(new HashMap<>());

            for (Example ex : examples)
                out.add(factory.createPositive(ExampleFactory.ExampleSource.SOURCE, ex.getUri()));
        }


        return out;
    }

}
