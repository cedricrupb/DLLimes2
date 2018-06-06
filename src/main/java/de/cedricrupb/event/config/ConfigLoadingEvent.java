package de.cedricrupb.event.config;

import de.cedricrupb.config.model.LearningConfig;
import de.cedricrupb.event.ConfigBasedEvent;

public class ConfigLoadingEvent extends ConfigBasedEvent {
    public ConfigLoadingEvent(LearningConfig config) {
        super(config);
    }
}
