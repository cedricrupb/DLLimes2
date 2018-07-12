package de.cedricrupb.utils;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import de.cedricrupb.event.ExceptionEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * This class handles any exception and throws an exception event.
 * @author Cedric Richter
 */


public class Exception2EventHandler implements SubscriberExceptionHandler {

    static Log log = LogFactory.getLog("Exception");

    @Override
    public void handleException(Throwable throwable, SubscriberExceptionContext subscriberExceptionContext) {
        log.error("A missed exception occurred while processing", throwable);
        subscriberExceptionContext.getEventBus().post(new ExceptionEvent(throwable));
    }
}
