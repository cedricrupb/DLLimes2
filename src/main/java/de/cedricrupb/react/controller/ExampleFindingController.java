package de.cedricrupb.react.controller;

import com.google.common.eventbus.Subscribe;
import de.cedricrupb.ApplicationContext;
import de.cedricrupb.config.model.*;
import de.cedricrupb.event.ExceptionEvent;
import de.cedricrupb.event.config.MappingConfigEvent;
import de.cedricrupb.event.learn.LimesMappingEvent;
import de.cedricrupb.event.learn.MatchedPropertyEvent;
import de.cedricrupb.event.learn.RestrictionEvent;
import de.cedricrupb.react.learner.ExampleFinder;
import de.cedricrupb.utils.AsyncJoiner;
import de.cedricrupb.utils.KBInfoHelper;
import org.aksw.limes.core.io.config.KBInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * Class that finds examples and joins them on with Source & Target Property, Source & Target Restriction & Mapping
 *
 * @author Cedric Richter
 */


public class ExampleFindingController {

    private static final String[] JOIN_ON = new String[]{"source_restriction", "target_restriction", "mapping",
                                                         "source_property", "target_property"};

    private ApplicationContext ctx;
    private AsyncJoiner<LearningConfig> joiner;

    public ExampleFindingController(ApplicationContext ctx) {
        this.ctx = ctx;
        this.joiner = new AsyncJoiner<>(JOIN_ON, this::joinConsumer);
    }

    private void joinConsumer(Map<String, Object> map){
        String sourceRes = (String) map.get("source_restriction");
        String targetRes = (String) map.get("target_restriction");
        Set<String> sourceProp = (Set<String>) map.get("source_property");
        Set<String> targetProp = (Set<String>) map.get("target_property");
        Set<Reference> mapping = (Set<Reference>) map.get("mapping");
        LearningConfig config = (LearningConfig)map.get("JOINED_ON");
        onJoin(config, sourceRes, targetRes, sourceProp, targetProp, mapping);
    }

    @Subscribe
    public void onRestriction(RestrictionEvent event){
        if(event.getDomainConfig() instanceof SourceDomainConfig)
            joiner.join(event.getConfig(), "source_restriction", event.getRestriction());
        else
            joiner.join(event.getConfig(), "target_restriction", event.getRestriction());
    }

    @Subscribe
    private void onProperty(MatchedPropertyEvent event){
        joiner.join(event.getConfig(), "source_property", event.getSourceProperties());
        joiner.join(event.getConfig(), "target_property", event.getTargetProperties());
    }

    @Subscribe
    public void onMapping(MappingConfigEvent event){
        joiner.join(event.getConfig(), "mapping", event.getMapping());
    }

    public void onJoin(LearningConfig config, String sourceRestriction, String targetRestriction,
                       Set<String> sourceProperties, Set<String> targetProperties, Set<Reference> mapping){

        KBInfo source = buildInfo(config.getSrcConfig().getInfo(), sourceRestriction, sourceProperties);
        KBInfo target = buildInfo(config.getTargetConfig().getInfo(), targetRestriction, targetProperties);

        ExampleFinder finder = new ExampleFinder(source, target, config.getMlConfig(), mapping);
        finder.run();

        Map<String, String> prefix = new HashMap<>(source.getPrefixes());
        prefix.putAll(target.getPrefixes());

        if(!finder.hasException()){
            this.ctx.getBus().post(new LimesMappingEvent(
                    config, finder.getMapping()
            ));
        }else{
            this.ctx.getBus().post(new ExceptionEvent(finder.getException()));
        }

    }

    private KBInfo buildInfo(KBInfo org, String restriction, Set<String> properties){
        org = KBInfoHelper.injectRestriction(org, restriction);
        return KBInfoHelper.injectProperties(org, properties);
    }

}
