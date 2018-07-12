package de.cedricrupb.react.learner.descriptionLearning;

import de.cedricrupb.config.model.Example;
import org.aksw.limes.core.io.config.KBInfo;

import java.util.*;

public class TruncateOperation extends AExampleOperation {

    private int size;

    public TruncateOperation(int size) {
        this.size = size;
    }

    @Override
    public Set<Example> infer(KBInfo info, Set<Example> examples) {

        if(examples.size() < size)
            return examples;

        List<Example> exampleList = new ArrayList<>(examples);

        return new HashSet<>(exampleList.subList(0, size));
    }
}
