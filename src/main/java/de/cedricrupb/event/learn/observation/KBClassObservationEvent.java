package de.cedricrupb.event.learn.observation;

import de.cedricrupb.react.model.LearnedClass;

public class KBClassObservationEvent extends KBObservationEvent<LearnedClass> {
    public KBClassObservationEvent(KBType type, String key, LearnedClass observation) {
        super(type, key, observation);
    }
}
