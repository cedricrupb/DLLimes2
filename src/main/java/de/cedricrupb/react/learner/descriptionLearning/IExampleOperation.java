package de.cedricrupb.react.learner.descriptionLearning;

import de.cedricrupb.config.model.Example;
import org.aksw.limes.core.io.config.KBInfo;

import java.util.Set;

public interface IExampleOperation {

    public Set<Example> infer(KBInfo info, Set<Example> examples);
}
