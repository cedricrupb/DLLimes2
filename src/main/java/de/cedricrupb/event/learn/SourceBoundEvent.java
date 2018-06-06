package de.cedricrupb.event.learn;

import de.cedricrupb.config.model.LearningConfig;
import de.cedricrupb.config.model.LearningDomainConfig;
import de.cedricrupb.event.ConfigBasedEvent;

public abstract class SourceBoundEvent extends ConfigBasedEvent {

    private LearningDomainConfig domainConfig;

    public SourceBoundEvent(LearningConfig config, LearningDomainConfig domainConfig) {
        super(config);
        this.domainConfig = domainConfig;
    }

    public LearningDomainConfig getDomainConfig() {
        return domainConfig;
    }

}
