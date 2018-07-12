package de.cedricrupb.event.learn.observation;


/**
 *
 * Class KBIntClassObservationEvent creates an event based on KBType, Key and Integer as datatype of Observation
 *
 * @author Cedric Richter
 */

public class KBIntOberservationEvent extends KBObservationEvent<Integer> {
    public KBIntOberservationEvent(KBType type, String key, Integer observation) {
        super(type, key, observation);
    }
}
