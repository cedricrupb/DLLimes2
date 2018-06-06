package de.cedricrupb.config.model;

import java.util.Objects;

public class Example {

    private String uri;

    Example(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Example that = (Example) o;
        return Objects.equals(uri, that.uri);
    }

    @Override
    public int hashCode() {

        return Objects.hash(uri);
    }

}
