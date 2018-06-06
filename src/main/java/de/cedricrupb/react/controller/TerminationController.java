package de.cedricrupb.react.controller;

import com.google.common.eventbus.Subscribe;
import de.cedricrupb.ApplicationContext;
import de.cedricrupb.config.model.*;
import de.cedricrupb.event.config.MappingConfigEvent;
import de.cedricrupb.event.config.SourceInfoEvent;
import de.cedricrupb.event.config.TargetInfoEvent;
import de.cedricrupb.event.learn.LimesMappingEvent;
import de.cedricrupb.event.learn.RestrictionEvent;
import de.cedricrupb.event.learn.TerminationEvent;
import de.cedricrupb.utils.ObjectRecorder;
import de.cedricrupb.utils.PrefixHelper;
import org.aksw.limes.core.io.mapping.AMapping;
import org.aksw.limes.core.io.mapping.MappingFactory;
import org.aksw.limes.core.io.mapping.writer.IMappingWriter;
import org.aksw.limes.core.io.mapping.writer.RDFMappingWriter;

import java.io.IOException;
import java.util.*;

public class TerminationController {

    private ApplicationContext ctx;

    public TerminationController(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    @Subscribe
    public void onConfigMapping(MappingConfigEvent event){
        ObjectRecorder recorder = this.ctx.getObjectRecorder().getCurrentEpoch();
        recorder.record("mapping", event.getMapping());
    }

    @Subscribe
    public void onRestrictionEvent(RestrictionEvent event){
        ObjectRecorder recorder = this.ctx.getObjectRecorder().getCurrentEpoch();
        if(event.getDomainConfig() instanceof SourceDomainConfig){
            recorder.record("source_restriction_accuracy", event.getAccuracy());
        }else{
            recorder.record("target_restriction_accuracy", event.getAccuracy());
        }
    }

    @Subscribe
    public void onLimesMapping(LimesMappingEvent event){
        Set<Reference> references = fromMapping(event.getMapping(), event.getConfig().getSrcConfig().getInfo().getPrefixes());
        if(shouldTerminate(event.getConfig(), references)){
            terminate(event.getConfig(), event.getMapping());
        }else{
            continueExec(event.getConfig(), references);
        }
    }

    private boolean shouldTerminate(LearningConfig config, Set<Reference> mapping){
        ObjectRecorder recorder = this.ctx.getObjectRecorder().getCurrentEpoch().newEpoch();
        recorder.record("mapping", mapping);

        if(config.getTerminateConfig().isFixpoint() &&
                isFixpoint((Set<Reference>)recorder.getLastEpoch().get("mapping"),
                            mapping)
                )
        {
            return true;
        }

        Integer it = findInHistory("iteration", recorder, Integer.class);
        int iteration = 1;

        if(it != null)
            iteration = it + 1;

        recorder.record("iteration", iteration);

        if(config.getTerminateConfig().getIteration() >= 0
                && config.getTerminateConfig().getIteration() <= iteration){
            return true;
        }

        return false;
    }

    private <T> T findInHistory(String key, ObjectRecorder recorder, Class<T> clazz){
        while (recorder != null && recorder.get(key) == null)
            recorder = recorder.getLastEpoch();
        if(recorder == null)
            return null;
        return (T) recorder.get(key);
    }

    private boolean isFixpoint(Set<Reference> oldReferences, Set<Reference> newReferences){
        for(Reference reference: newReferences){
            if(!oldReferences.contains(reference))return false;
        }
        return true;
    }

    private void terminate(LearningConfig config, AMapping mapping){
        emitMapping(mapping, config.getTerminateConfig().getOutFile());
        this.ctx.getBus().post(new TerminationEvent(
                config, mapping
        ));
    }

    private void continueExec(LearningConfig config, Set<Reference> mapping){
        this.ctx.getBus().post(new SourceInfoEvent(config, config.getSrcConfig()));
        this.ctx.getBus().post(new TargetInfoEvent(config, config.getTargetConfig()));
        this.ctx.getBus().post(new MappingConfigEvent(config, mapping));
    }


    private void emitMapping(AMapping mapping, String path){

        mapping.setPredicate("http://www.w3.org/2002/07/owl#sameAs");

        IMappingWriter writer = new RDFMappingWriter();

        try {
            writer.write(mapping, path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Set<Reference> fromMapping(AMapping mapping, Map<String, String> prefix){
        ExampleFactory factory = new ExampleFactory(new HashMap<>());

        Set<Reference> references = new HashSet<>();

        for(String srcURI: mapping.getMap().keySet()){
            String srcPrefixed = PrefixHelper.revertSinglePrefix(srcURI, prefix);
            Example srcExample = factory.createPositive(ExampleFactory.ExampleSource.SOURCE, srcPrefixed);
            for(String targetURI: mapping.getMap().get(srcURI).keySet()){
                String targetPrefixed = PrefixHelper.revertSinglePrefix(targetURI, prefix);
                Example targetExample = factory.createPositive(ExampleFactory.ExampleSource.TARGET, targetPrefixed);
                references.add(factory.createPositiveReference(srcExample, targetExample));
            }
        }

        return references;
    }

}
