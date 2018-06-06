package de.cedricrupb.event.learn;

import de.cedricrupb.config.model.LearningConfig;
import de.cedricrupb.config.model.LearningDomainConfig;

import java.util.Set;

public class CoveredPropertyEvent extends SourceBoundEvent {

    private Set<String> coveredProperties;

    public CoveredPropertyEvent(LearningConfig config, LearningDomainConfig domainConfig, Set<String> coveredProperties) {
        super(config, domainConfig);
        this.coveredProperties = coveredProperties;
    }

    public Set<String> getCoveredProperties() {
        return coveredProperties;
    }


}
