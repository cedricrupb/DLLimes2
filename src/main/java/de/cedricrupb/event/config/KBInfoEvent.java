package de.cedricrupb.event.config;

import de.cedricrupb.config.model.LearningConfig;
import de.cedricrupb.config.model.LearningDomainConfig;
import de.cedricrupb.event.ConfigBasedEvent;

/**
 *
 * Class KBinfoEvent creates a KBInfo Event based on LearningDomainConfig.
 *
 * @author Cedric Richter
 */


public class KBInfoEvent extends ConfigBasedEvent {

    private LearningDomainConfig domain;

    public KBInfoEvent(LearningConfig config, LearningDomainConfig domain) {
        super(config);
        this.domain = domain;
    }


    public LearningDomainConfig getDomainConfig() {
        return domain;
    }

}
