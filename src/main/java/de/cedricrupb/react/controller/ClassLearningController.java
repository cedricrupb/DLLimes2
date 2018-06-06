package de.cedricrupb.react.controller;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import de.cedricrupb.ApplicationContext;
import de.cedricrupb.event.ExceptionEvent;
import de.cedricrupb.event.config.KBInfoEvent;
import de.cedricrupb.event.learn.RestrictionEvent;
import de.cedricrupb.react.model.ClassLearner;

public class ClassLearningController {

    private ApplicationContext ctx;

    public ClassLearningController(ApplicationContext ctx) {
        this.ctx = ctx;
        this.ctx.getBus().register(this);
    }

    @Subscribe
    @AllowConcurrentEvents
    public void onInfoEvent(KBInfoEvent event){
        ClassLearner learner = new ClassLearner(event.getDomainConfig());
        learner.run();

        if(learner.hasException()){
            this.ctx.getBus().post(new ExceptionEvent(learner.getException()));
        }else{
            this.ctx.getBus().post(new RestrictionEvent(event.getConfig(), event.getDomainConfig(),
                                                        learner.getRestriction(), learner.getAccuracy()));
        }
    }

}
