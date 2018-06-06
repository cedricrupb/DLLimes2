package de.cedricrupb.config.model;

import java.util.Objects;

public class Reference {

    private Example source;
    private Example target;

    Reference(Example source, Example target) {
        this.source = source;
        this.target = target;
    }

    public Example getSource() {
        return source;
    }

    public Example getTarget() {
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reference reference = (Reference) o;
        return Objects.equals(source, reference.source) &&
                Objects.equals(target, reference.target);
    }

    @Override
    public int hashCode() {

        return Objects.hash(source, target);
    }

}
