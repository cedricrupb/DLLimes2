package de.cedricrupb.utils;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AsyncJoiner<T> {

    private String[] joinOn;
    private Consumer<Map<String, Object>> joinConsumer;
    private Map<T, Map<String, Object>> joinMap;

    public AsyncJoiner(String[] joinOn, Consumer<Map<String, Object>> joinConsumer, boolean identity) {
        this.joinOn = joinOn;
        this.joinConsumer = joinConsumer;
        if(identity) {
            this.joinMap = new IdentityHashMap<>();
        }else{
            this.joinMap = new HashMap<>();
        }
    }

    public AsyncJoiner(String[] joinOn, Consumer<Map<String, Object>> joinConsumer) {
        this(joinOn, joinConsumer, false);
    }

    public void join(T domain, String key, Object o){
        synchronized (joinMap){
            if(!joinMap.containsKey(domain)){
                joinMap.put(domain, new HashMap<>());
            }
            joinMap.get(domain).put(key, o);
        }
        applyJoinIfPossible(domain);
    }

    private void applyJoinIfPossible(T domain){
        Map<String, Object> values = null;
        synchronized (joinMap) {
            boolean join = true;
            for (String key : joinOn)
                join &= joinMap.get(domain).containsKey(key);

            if(join)values = joinMap.remove(domain);
        }

        if(values != null) {
            values.put("JOINED_ON", domain);
            joinConsumer.accept(values);
        }
    }

}
