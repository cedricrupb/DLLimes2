package de.cedricrupb.event.config;

import de.cedricrupb.config.model.LearningConfig;
import de.cedricrupb.config.model.Reference;
import de.cedricrupb.event.ConfigBasedEvent;

import java.util.Set;

/**
 *
 * Class MappingConfigEvent creates a MappingConfig Event based on Mapping.
 *
 * @author Cedric Richter
 */

public class MappingConfigEvent extends ConfigBasedEvent {

    private Set<Reference> mapping;

    public MappingConfigEvent(LearningConfig config, Set<Reference> mapping) {
        super(config);
        this.mapping = mapping;
    }

    public Set<Reference> getMapping() {
        return mapping;
    }
}
