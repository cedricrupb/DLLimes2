package de.cedricrupb.event.config;


import de.cedricrupb.config.model.LearningConfig;
import de.cedricrupb.config.model.LearningDomainConfig;

public class TargetInfoEvent extends KBInfoEvent {
    public TargetInfoEvent(LearningConfig config, LearningDomainConfig domain) {
        super(config, domain);
    }
}
