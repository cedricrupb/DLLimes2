package de.cedricrupb.config.model;

import java.util.Objects;

/**
 *
 * Class to Define and Set Prefixes.
 *
 * @author Cedric Richter
 */


public class Prefix {

    private String namespace;
    private String label;

    public Prefix(String namespace, String label) {
        this.namespace = namespace;
        this.label = label;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prefix prefix = (Prefix) o;
        return Objects.equals(namespace, prefix.namespace) &&
                Objects.equals(label, prefix.label);
    }

    @Override
    public int hashCode() {

        return Objects.hash(namespace, label);
    }
}
