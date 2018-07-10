package de.cedricrupb.react.controller;

import com.google.common.eventbus.Subscribe;
import de.cedricrupb.ApplicationContext;
import de.cedricrupb.config.model.LearningConfig;
import de.cedricrupb.config.model.MLConfig;
import de.cedricrupb.config.model.Reference;
import de.cedricrupb.config.model.SourceDomainConfig;
import de.cedricrupb.event.config.MappingConfigEvent;
import de.cedricrupb.event.learn.CoveredPropertyEvent;
import de.cedricrupb.event.learn.MatchedPropertyEvent;
import de.cedricrupb.event.learn.RestrictionEvent;
import de.cedricrupb.event.learn.observation.KBLongObservationEvent;
import de.cedricrupb.event.learn.observation.KBObservationEvent;
import de.cedricrupb.react.learner.PropertyCoverageFilter;
import de.cedricrupb.react.learner.PropertyLearner;
import de.cedricrupb.utils.AsyncJoiner;
import de.cedricrupb.utils.KBInfoHelper;
import org.aksw.limes.core.io.config.KBInfo;
import org.aksw.limes.core.ml.algorithm.LearningParameter;


import java.util.Map;
import java.util.Set;

public class PropertyLearningController {

    private static final String[] JOIN_ON = new String[]{"source", "target", "mapping"};

    private ApplicationContext ctx;
    private AsyncJoiner<LearningConfig> joiner;

    public PropertyLearningController(ApplicationContext ctx) {
        this.ctx = ctx;
        this.ctx.getBus().register(this);
        this.joiner = new AsyncJoiner<>(JOIN_ON, this::joinConsumer);
    }

    private void joinConsumer(Map<String, Object> items){
        KBInfo source = (KBInfo) items.get("source");
        KBInfo target = (KBInfo) items.get("target");
        Set<Reference> mapping = (Set<Reference>) items.get("mapping");
        LearningConfig config = (LearningConfig) items.get("JOINED_ON");
        onJoin(config, source, target, mapping);
    }

    private KBInfo buildInfo(KBInfo info, String restriction, Set<String> properties){
        info = KBInfoHelper.injectRestriction(info, restriction);
        info = KBInfoHelper.injectProperties(info, properties);
        return info;
    }



    @Subscribe
    public void onRestriction(RestrictionEvent event){
        double threshold = 0.6;

        String key;
        KBObservationEvent.KBType type;
        if(event.getDomainConfig() instanceof SourceDomainConfig){
            key = "source";
            type = KBObservationEvent.KBType.SOURCE;
        }else{
            key = "target";
            type = KBObservationEvent.KBType.TARGET;
        }


        MLConfig ml = event.getConfig().getMlConfig();
        for(LearningParameter parameter: ml.getMlAlgorithmParameters())
            if(parameter.getName().equalsIgnoreCase("property_min_coverage"))
                threshold = Double.parseDouble((String)parameter.getValue());

        KBInfo info = KBInfoHelper.injectRestriction(event.getDomainConfig().getInfo(),
                                                     event.getRestriction());

        PropertyCoverageFilter filter = new PropertyCoverageFilter(
            info, threshold
        );
        filter.run();

        this.ctx.getBus().post(new KBLongObservationEvent(
                type, "restriction.entity.count", filter.getEntityCount()
        ));

        this.ctx.getBus().post(new CoveredPropertyEvent(event.getConfig(), event.getDomainConfig(),
                                                        filter.getProperties()));



        this.joiner.join(event.getConfig(), key,
                    buildInfo(event.getDomainConfig().getInfo(), event.getRestriction(), filter.getProperties())
                );
    }

    @Subscribe
    public void onMapping(MappingConfigEvent mapping){
        this.joiner.join(
                mapping.getConfig(), "mapping", mapping.getMapping()
        );
    }


    public void onJoin(LearningConfig cfg, KBInfo source, KBInfo target, Set<Reference> mapping){
        double threshold = 0.6;

        MLConfig ml = cfg.getMlConfig();
        for(LearningParameter parameter: ml.getMlAlgorithmParameters())
            if(parameter.getName().equalsIgnoreCase("property_min_f_score"))
                threshold = Double.parseDouble((String)parameter.getValue());

        PropertyLearner learner = new PropertyLearner(source, target, mapping, threshold);
        learner.run();

        this.ctx.getBus().post(
                new MatchedPropertyEvent(
                        cfg, learner.getSrcProperties(), learner.getTargetProperties()
                )
        );

    }


}
