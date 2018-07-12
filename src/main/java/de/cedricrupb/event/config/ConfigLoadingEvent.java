package de.cedricrupb.event.config;

import de.cedricrupb.config.model.LearningConfig;
import de.cedricrupb.event.ConfigBasedEvent;

/**
 *
 * Class ConfigLoadingEvent creates a Loading Event based on Learning Config
 *
 * @author Cedric Richter
 */

public class ConfigLoadingEvent extends ConfigBasedEvent {
    public ConfigLoadingEvent(LearningConfig config) {
        super(config);
    }
}
