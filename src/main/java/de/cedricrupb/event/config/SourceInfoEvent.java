package de.cedricrupb.event.config;

import de.cedricrupb.config.model.LearningConfig;
import de.cedricrupb.config.model.LearningDomainConfig;

/**
 *
 * Class SourceInfoEvent creates a SourceInfo Event based on Learning Config and LearningDomainConfig
 *
 * @author Cedric Richter
 */

public class SourceInfoEvent extends KBInfoEvent {
    public SourceInfoEvent(LearningConfig config, LearningDomainConfig domain) {
        super(config, domain);
    }
}
