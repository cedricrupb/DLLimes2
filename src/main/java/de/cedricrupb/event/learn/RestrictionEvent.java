package de.cedricrupb.event.learn;

import de.cedricrupb.config.model.LearningConfig;
import de.cedricrupb.config.model.LearningDomainConfig;
import de.cedricrupb.event.ConfigBasedEvent;

public class RestrictionEvent extends SourceBoundEvent {

    private String restriction;
    private double accuracy;

    public RestrictionEvent(LearningConfig config, LearningDomainConfig cfg, String restriction, double accuracy) {
        super(config, cfg);
        this.restriction = restriction;
        this.accuracy = accuracy;
    }

    public String getRestriction() {
        return restriction;
    }

    public double getAccuracy() {
        return accuracy;
    }


}
