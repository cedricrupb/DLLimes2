package de.cedricrupb.event.learn;

import de.cedricrupb.config.model.LearningConfig;
import de.cedricrupb.event.ConfigBasedEvent;

import java.util.Set;


/**
 *
 * Class that activates an MatchedProperty Event from LearningConfig, sourceProperties and TargetProperties
 *
 * @author Cedric Richter
 */

public class MatchedPropertyEvent extends ConfigBasedEvent {

    private Set<String> sourceProperties;
    private Set<String> targetProperties;

    public MatchedPropertyEvent(LearningConfig config, Set<String> sourceProperties, Set<String> targetProperties) {
        super(config);
        this.sourceProperties = sourceProperties;
        this.targetProperties = targetProperties;
    }

    public Set<String> getSourceProperties() {
        return sourceProperties;
    }

    public Set<String> getTargetProperties() {
        return targetProperties;
    }


}
