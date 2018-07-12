package de.cedricrupb.logging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.function.Function;

/**
 *
 * Class that iterates logger to keep it running at every iteration.
 *
 * @author Cedric Richter
 */

public class LogIterator<T> implements Iterator<T> {

    private Log logger;
    private int max;
    private int iteration = 0;

    private long startTime = 0;
    private long currentTime = 0;

    private long delay = 0;

    private Iterator<T> delegate;
    private Function<T, String> formatFunction;



    LogIterator(Iterator<T> delegate, Function<T, String> formatFunction, String name, int maxIteration, long delay){
        this.logger = LogFactory.getLog(name);
        this.max = maxIteration;
        this.startTime = System.currentTimeMillis();
        this.delegate = delegate;
        this.formatFunction = formatFunction;
        this.delay = delay;
    }

    LogIterator(Iterator<T> delegate, Function<T, String> formatFunction, String name, int maxIteration){
        this(delegate, formatFunction, name, maxIteration, 0);
    }

    private String formatTime(long time){

        int sec = (int)(time/1000);
        int min = (int)((double)sec/60);
        sec = sec % 60;
        int hour = (int)((double)min/60);
        min = min % 60;

        return (hour>0?String.format("%02d:", hour):"") + String.format("%02d:", min) + String.format("%02d", Math.max(sec, 1));
    }


    private String providePrefix(){
        double process = (double)iteration/max;
        int left = max - iteration;
        long time = currentTime - startTime;
        double avgTime = (double)(time)/iteration;

        long estimatedRuntime = (long)(time + left*avgTime);

        NumberFormat format = NumberFormat.getPercentInstance();
        format.setMaximumFractionDigits(0);

        return String.format("[%s ; %s < %s]", format.format(process), formatTime(time), formatTime(estimatedRuntime));

    }

    @Override
    public boolean hasNext() {
        return this.delegate.hasNext();
    }

    @Override
    public T next() {
        long currentDelay = System.currentTimeMillis() - currentTime;
        currentTime = System.currentTimeMillis();
        iteration++;

        T t = delegate.next();

        if(currentDelay >= delay || iteration >= 0.9*max)
            logger.info(providePrefix() + " " + formatFunction.apply(t));


        return t;
    }
}
