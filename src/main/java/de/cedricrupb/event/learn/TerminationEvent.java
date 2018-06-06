package de.cedricrupb.event.learn;

import de.cedricrupb.config.model.LearningConfig;
import de.cedricrupb.event.ConfigBasedEvent;
import org.aksw.limes.core.io.mapping.AMapping;

public class TerminationEvent extends ConfigBasedEvent {

    private AMapping terminationMapping;

    public TerminationEvent(LearningConfig config, AMapping terminationMapping) {
        super(config);
        this.terminationMapping = terminationMapping;
    }

    public AMapping getTerminationMapping() {
        return terminationMapping;
    }

}
