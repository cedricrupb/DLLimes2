package de.cedricrupb.event.learn.observation;

public class KBDoubleObservationEvent extends KBObservationEvent<Double> {
    public KBDoubleObservationEvent(KBType type, String key, Double observation) {
        super(type, key, observation);
    }
}
