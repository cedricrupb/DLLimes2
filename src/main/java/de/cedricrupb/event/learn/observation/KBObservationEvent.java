package de.cedricrupb.event.learn.observation;

import java.util.Objects;

public class KBObservationEvent<T> {

    public enum KBType{
        SOURCE, TARGET
    }

    private KBType type;
    private String key;
    private T observation;

    public KBObservationEvent(KBType type, String key, T observation) {
        this.type = type;
        this.key = key;
        this.observation = observation;
    }

    public KBType getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public T getObservation() {
        return observation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KBObservationEvent<?> that = (KBObservationEvent<?>) o;
        return type == that.type &&
                Objects.equals(key, that.key) &&
                Objects.equals(observation, that.observation);
    }

    @Override
    public int hashCode() {

        return Objects.hash(type, key, observation);
    }
}
