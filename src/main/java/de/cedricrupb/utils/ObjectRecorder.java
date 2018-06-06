package de.cedricrupb.utils;


import com.github.jsonldjava.utils.Obj;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ObjectRecorder {

    private int epoch = 0;
    private ObjectRecorder lastEpoch = null;
    private Consumer<ObjectRecorder> epochConsumer;
    private Map<String, Object> record = new ConcurrentHashMap<>();

    private ObjectRecorder(Consumer<ObjectRecorder> epochConsumer, ObjectRecorder lastEpoch){
        this.lastEpoch = lastEpoch;
        this.epochConsumer = epochConsumer;
        if(lastEpoch != null)epoch = lastEpoch.epoch + 1;
    }

    public ObjectRecorder(Consumer<ObjectRecorder> epochConsumer){
        this(epochConsumer, null);
    }

    public ObjectRecorder(){
        this(null);
    }

    public ObjectRecorder newEpoch(){
        ObjectRecorder newEpoch = new ObjectRecorder(epochConsumer, this);
        if(epochConsumer != null)
            epochConsumer.accept(newEpoch);
        return newEpoch;
    }

    public ObjectRecorder record(String key, Object o){
        if(!record.containsKey(key)){
            o = makeImmutableIfPossible(o);
            record.put(key, o);
        }
        return this;
    }

    public Object get(String key){
        return record.get(key);
    }

    private Object makeImmutableIfPossible(Object o){
        if(o instanceof Set)
            return makeImmutableIfPossible((Set<?>)o);
        if(o instanceof Map)
            return makeImmutableIfPossible((Map<?, ?>)o);
        if(o instanceof List)
            return makeImmutableIfPossible((List<?>)o);
        return o;
    }

    private Object makeImmutableIfPossible(Set<?> o){
        return ImmutableSet.copyOf(o);
    }

    private Object makeImmutableIfPossible(List<?> o){
        return ImmutableList.copyOf(o);
    }

    private Object makeImmutableIfPossible(Map<?, ?> o){
        return ImmutableMap.copyOf(o);
    }

    public int getEpoch() {
        return epoch;
    }

    public ObjectRecorder getLastEpoch() {
        return lastEpoch;
    }

    void merge(ObjectRecorder o){
        for(Map.Entry<String, Object> e: o.record.entrySet()){
            this.record(e.getKey(), e.getValue());
        }
    }

}
