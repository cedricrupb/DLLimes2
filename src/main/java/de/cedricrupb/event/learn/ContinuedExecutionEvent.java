package de.cedricrupb.event.learn;

import de.cedricrupb.config.model.LearningConfig;
import de.cedricrupb.config.model.Reference;
import de.cedricrupb.event.ConfigBasedEvent;

import java.util.Set;


/**
 *
 * Class that creates an continued execution event based on set of references and Learning Config
 *
 * @author Cedric Richter
 */

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
