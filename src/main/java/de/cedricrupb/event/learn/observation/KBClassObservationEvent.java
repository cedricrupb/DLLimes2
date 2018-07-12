package de.cedricrupb.event.learn.observation;

import de.cedricrupb.react.model.LearnedClass;

/**
 *
 * Class KBClassObservationEvent creates an KBClassObservation Event based on KBType, Key and LearnedClass
 *
 * @author Cedric Richter
 */

public class KBClassObservationEvent extends KBObservationEvent<LearnedClass> {
    public KBClassObservationEvent(KBType type, String key, LearnedClass observation) {
        super(type, key, observation);
    }
}
