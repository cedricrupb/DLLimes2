package de.cedricrupb.event.learn.observation;


/**
 *
 * Class KBIntClassObservationEvent creates an event based on KBType, Key and Long as datatype of Observation
 *
 * @author Cedric Richter
 */

public class KBLongObservationEvent extends KBObservationEvent<Long> {
    public KBLongObservationEvent(KBType type, String key, Long observation) {
        super(type, key, observation);
    }
}
