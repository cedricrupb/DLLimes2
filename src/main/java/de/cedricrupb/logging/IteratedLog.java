package de.cedricrupb.logging;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

/**
 *
 * Class that records and logs every iteration.
 *
 * @author Cedric Richter
 */

public class IteratedLog {

    private static final String DEFAULT_NAME = "Iterate";

    private static <T> Function<T, String> makeDefault(){
        return (x -> String.format("Next: %s", x.toString()));
    }

    public static <T> Iterator<T> iterate(Iterator<T> iterator, int max, String name, Function<T, String> format){
        return new LogIterator<>(iterator, format, name, max);
    }

    public static <T> Iterable<T> iterate(Iterable<T> iterable, int max, String name, Function<T, String> format){
        return new Iterable<T>() {
            @NotNull
            @Override
            public Iterator<T> iterator() {
                return iterate(iterable.iterator(), max, name, format);
            }
        };
    }

    public static <T> Iterable<T> iterate(Collection<T> collection, String name, Function<T, String> format){
        return iterate(collection, collection.size(), name, format);
    }

    public static <T> Iterator<T> iterate(Iterator<T> iterator, int max, String name){
        return iterate(iterator, max, name, makeDefault());
    }

    public static <T> Iterator<T> iterate(Iterator<T> iterator, int max){
        return iterate(iterator, max, DEFAULT_NAME);
    }

    public static <T> Iterable<T> iterate(Iterable<T> iterable, int max, String name){
        return iterate(iterable, max, name, makeDefault());
    }

    public static <T> Iterable<T> iterate(Iterable<T> iterable, int max){
        return iterate(iterable, max, DEFAULT_NAME);
    }

    public static <T> Iterable<T> iterate(Collection<T> collection, String name){
        return iterate(collection, collection.size(), name, makeDefault());
    }

    public static <T> Iterable<T> iterate(Collection<T> collection){
        return iterate(collection, DEFAULT_NAME);
    }



}
