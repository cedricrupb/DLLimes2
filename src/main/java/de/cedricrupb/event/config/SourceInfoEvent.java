package de.cedricrupb.event.config;

import de.cedricrupb.config.model.LearningConfig;
import de.cedricrupb.config.model.LearningDomainConfig;

public class SourceInfoEvent extends KBInfoEvent {
    public SourceInfoEvent(LearningConfig config, LearningDomainConfig domain) {
        super(config, domain);
    }
}
