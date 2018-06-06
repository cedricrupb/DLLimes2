package de.cedricrupb.config.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    public PositiveExample createPositive(ExampleSource source, String uri){
        Set<Example> examples = getExamples(getKey(source));
        PositiveExample example = new PositiveExample(uri);
        examples.add(example);
        return example;
    }

    public NegativeExample createNegative(ExampleSource source, String uri){
        Set<Example> examples = getExamples(getKey(source));
        NegativeExample example = new NegativeExample(uri);
        examples.add(example);
        return example;
    }

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
