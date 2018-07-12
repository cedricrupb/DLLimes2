package de.cedricrupb.config.model;

import java.util.Objects;

/**
 *
 * A Class to Create Example from URIs.
 *
 * @author Cedric Richter
 */


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
        if (o == null) return false;
        if(o instanceof Example) {
            Example that = (Example) o;
            return Objects.equals(uri, that.uri);
        }
        return false;
    }

    @Override
    public int hashCode() {

        return Objects.hash(uri);
    }

    @Override
    public String toString(){
        return uri;
    }

}
