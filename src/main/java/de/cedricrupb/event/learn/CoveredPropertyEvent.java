package de.cedricrupb.event.learn;

import de.cedricrupb.config.model.LearningConfig;
import de.cedricrupb.config.model.LearningDomainConfig;

import java.util.Set;

/**
 *
 * Class that creates an Event from Learned CoveredProperties based on LearningConfig and Learning Domain Properties
 *
 * @author Cedric Richter
 */


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
