package de.cedricrupb.event;

import de.cedricrupb.config.model.LearningConfig;

import java.util.Objects;

/**
 *
 * Class that activates an ConfigurationBased Event from LearningConfig
 *
 * @author Cedric Richter
 */

public abstract class ConfigBasedEvent {

    private LearningConfig config;

    public ConfigBasedEvent(LearningConfig config) {
        this.config = config;
    }

    public LearningConfig getConfig() {
        return config;
    }

}
