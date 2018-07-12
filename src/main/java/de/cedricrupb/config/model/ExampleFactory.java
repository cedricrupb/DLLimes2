package de.cedricrupb.config.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * Class That Returns ExampleSet of Source/Target based on Context
 * Positive,Negative and Neutral Examples are mapped and URIs are added in this class
 *
 * @author Cedric Richter
 */


public class ExampleFactory {

    public enum ExampleSource{
        SOURCE, TARGET
    }

    private Map<String, Object> context;

    public ExampleFactory(Map<String, Object> context) {
        this.context = context;
        getExamples(getKey(ExampleSource.SOURCE));
        getExamples(getKey(ExampleSource.TARGET));
    }

    private Set<Example> getExamples(String key){
        if(!context.containsKey(key)){
            context.put(key, new HashSet<>());
        }
        return (Set<Example>) context.get(key);
    }

    public static String getKey(ExampleSource source){
        if(source == ExampleSource.TARGET){
            return "target_examples";
        }
        return "source_examples";
    }

    /**
     *
     * Create Positive Example.
     * @return  Positive example
     */

    public PositiveExample createPositive(ExampleSource source, String uri){
        Set<Example> examples = getExamples(getKey(source));
        PositiveExample example = new PositiveExample(uri);
        examples.add(example);
        return example;
    }

     /**
     *
     * Create Nagative Example
     * @return  Negative example
     */
    public NegativeExample createNegative(ExampleSource source, String uri){
        Set<Example> examples = getExamples(getKey(source));
        NegativeExample example = new NegativeExample(uri);
        examples.add(example);
        return example;
    }

    /**
     *
     * Create Neutral Example
     * @return  Neutral example
     */
    public Example createNeutral(ExampleSource source, String uri){
        return new Example(uri);
    }

    public PositiveReference createPositiveReference(Example source, Example target){
        return new PositiveReference(source, target);
    }

    public NegativeReference createNegativeReference(Example source, Example target){
        return new NegativeReference(source, target);
    }

}
