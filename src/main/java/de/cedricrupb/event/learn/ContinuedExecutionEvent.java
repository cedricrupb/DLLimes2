package de.cedricrupb.event.learn;

import de.cedricrupb.config.model.LearningConfig;
import de.cedricrupb.config.model.Reference;
import de.cedricrupb.event.ConfigBasedEvent;

import java.util.Set;

public class ContinuedExecutionEvent extends ConfigBasedEvent {

    private Set<Reference> referenceSet;

    public ContinuedExecutionEvent(LearningConfig config, Set<Reference> references) {
        super(config);
        this.referenceSet = references;
    }

    public Set<Reference> getReferenceSet() {
        return referenceSet;
    }
}
