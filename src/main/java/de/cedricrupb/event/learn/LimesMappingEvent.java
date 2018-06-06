package de.cedricrupb.event.learn;

import de.cedricrupb.config.model.LearningConfig;
import de.cedricrupb.config.model.Reference;
import de.cedricrupb.event.ConfigBasedEvent;
import org.aksw.limes.core.io.mapping.AMapping;

import java.util.Set;

public class LimesMappingEvent extends ConfigBasedEvent {

    private AMapping mapping;

    public LimesMappingEvent(LearningConfig config, AMapping mapping) {
        super(config);
        this.mapping = mapping;
    }

    public AMapping getMapping() {
        return mapping;
    }
}
