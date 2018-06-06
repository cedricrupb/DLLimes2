package de.cedricrupb.event;

import de.cedricrupb.config.model.LearningConfig;

import java.util.Objects;

public abstract class ConfigBasedEvent {

    private LearningConfig config;

    public ConfigBasedEvent(LearningConfig config) {
        this.config = config;
    }

    public LearningConfig getConfig() {
        return config;
    }

}
