package de.cedricrupb.event.config;


import de.cedricrupb.config.model.LearningConfig;
import de.cedricrupb.config.model.LearningDomainConfig;

/**
 *
 * Class TargetInfoEvent creates a TargetInfo Event based on Learning Config and LearningDomainConfig
 *
 * @author Cedric Richter
 */

public class TargetInfoEvent extends KBInfoEvent {
    public TargetInfoEvent(LearningConfig config, LearningDomainConfig domain) {
        super(config, domain);
    }
}
