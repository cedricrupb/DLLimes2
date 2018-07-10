package de.cedricrupb.event.learn;

import de.cedricrupb.config.model.Example;
import de.cedricrupb.config.model.LearningConfig;
import de.cedricrupb.config.model.LearningDomainConfig;
import de.cedricrupb.event.ConfigBasedEvent;

import java.util.Set;

public class ExampleExpansionEvent extends ConfigBasedEvent {

    private LearningDomainConfig domain;
    private Set<Example> exampleSet;

    public ExampleExpansionEvent(LearningConfig config, LearningDomainConfig domain, Set<Example> exampleSet) {
        super(config);
        this.domain = domain;
        this.exampleSet = exampleSet;
    }

    public LearningDomainConfig getDomain() {
        return domain;
    }

    public Set<Example> getExampleSet() {
        return exampleSet;
    }
}
