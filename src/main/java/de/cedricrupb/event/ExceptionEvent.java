package de.cedricrupb.event;

/**
 *
 * Class that activates an Exception Event in case any exception occurs
 *
 * @author Cedric Richter
 */

public class ExceptionEvent {

    public Throwable exception;

    public ExceptionEvent(Throwable exception) {
        this.exception = exception;
    }

    public Throwable getException() {
        return exception;
    }

}
