package de.cedricrupb.event.learn.observation;

public class KBIntOberservationEvent extends KBObservationEvent<Integer> {
    public KBIntOberservationEvent(KBType type, String key, Integer observation) {
        super(type, key, observation);
    }
}
