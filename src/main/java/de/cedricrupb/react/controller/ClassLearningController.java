package de.cedricrupb.react.controller;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import de.cedricrupb.ApplicationContext;
import de.cedricrupb.config.model.Example;
import de.cedricrupb.config.model.LearningConfig;
import de.cedricrupb.config.model.LearningDomainConfig;
import de.cedricrupb.config.model.SourceDomainConfig;
import de.cedricrupb.event.ExceptionEvent;
import de.cedricrupb.event.config.KBInfoEvent;
import de.cedricrupb.event.learn.ExampleExpansionEvent;
import de.cedricrupb.event.learn.RestrictionEvent;
import de.cedricrupb.event.learn.observation.KBClassObservationEvent;
import de.cedricrupb.event.learn.observation.KBObservationEvent;
import de.cedricrupb.react.learner.ClassLearner;
import de.cedricrupb.react.model.LearnedClass;

import java.util.Set;

/**
 *
 * Class that performs learning operations on Classes.
 *
 * @author Cedric Richter
 */


public class ClassLearningController {

    private ApplicationContext ctx;

    public ClassLearningController(ApplicationContext ctx) {
        this.ctx = ctx;
        this.ctx.getBus().register(this);
    }

    @Subscribe
    @AllowConcurrentEvents
    public void onInfoEvent(KBInfoEvent event){
        learnClass(event.getConfig(), event.getDomainConfig(), event.getDomainConfig().getExamples());
    }

    @Subscribe
    @AllowConcurrentEvents
    public void onExpansion(ExampleExpansionEvent event){
        learnClass(event.getConfig(), event.getDomain(), event.getExampleSet());
    }

    /**
     *
     * Function that Learns Classes
     */

    public void learnClass(LearningConfig config, LearningDomainConfig domain, Set<Example> examples){
        //long startTime = System.currentTimeMillis();

        ClassLearner learner = new ClassLearner(domain, examples);
        learner.run();



        KBObservationEvent.KBType type;
        if(domain instanceof SourceDomainConfig){
            type = KBObservationEvent.KBType.SOURCE;
        }else{
            type = KBObservationEvent.KBType.TARGET;
        }

        if(learner.hasException()){
            this.ctx.getBus().post(new ExceptionEvent(learner.getException()));
        }else{
            this.ctx.getBus().post(
                    new KBClassObservationEvent(
                            type, "dllearner.class", new LearnedClass(learner.getLearnedExpression(),
                            learner.getRestriction(),
                            learner.getAccuracy())
                    )
            );
            this.ctx.getBus().post(new RestrictionEvent(config, domain,
                    learner.getRestriction(), learner.getAccuracy()));
        }
    }

}
