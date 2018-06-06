package de.cedricrupb.event;

public class ExceptionEvent {

    public Throwable exception;

    public ExceptionEvent(Throwable exception) {
        this.exception = exception;
    }

    public Throwable getException() {
        return exception;
    }

}
