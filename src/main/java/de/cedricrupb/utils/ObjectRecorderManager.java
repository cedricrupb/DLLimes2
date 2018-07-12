package de.cedricrupb.utils;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Class that manages object recorder and registers and object recorder.
 *
 * @author Cedric Richter
 */


public class ObjectRecorderManager {

    private List<ObjectRecorder> epochs = new ArrayList<>();


    public List<ObjectRecorder> getEpochs(){
        return ImmutableList.copyOf(epochs);
    }

    public ObjectRecorder getCurrentEpoch(){
        synchronized (epochs) {
            return epochs.get(epochs.size() - 1);
        }
    }

    public void registerObjectRecorder(ObjectRecorder recorder){
        synchronized (epochs){
            if(recorder.getEpoch() >= epochs.size()){
                epochs.add(recorder);
            }else{
                ObjectRecorder last = epochs.get(recorder.getEpoch());
                last.merge(recorder);
            }
        }
    }

}
