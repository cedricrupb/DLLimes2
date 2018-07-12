package de.cedricrupb.event.learn.observation;


/**
 *
 * Class KBDoubleClassObservationEvent creates an event based on KBType, Key and Double as dataType of Observation
 *
 * @author Cedric Richter
 */

public class KBDoubleObservationEvent extends KBObservationEvent<Double> {
    public KBDoubleObservationEvent(KBType type, String key, Double observation) {
        super(type, key, observation);
    }
}
