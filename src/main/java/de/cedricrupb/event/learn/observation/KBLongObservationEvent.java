package de.cedricrupb.event.learn.observation;

public class KBLongObservationEvent extends KBObservationEvent<Long> {
    public KBLongObservationEvent(KBType type, String key, Long observation) {
        super(type, key, observation);
    }
}
