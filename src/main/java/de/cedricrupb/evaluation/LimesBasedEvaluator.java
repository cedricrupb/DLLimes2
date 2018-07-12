package de.cedricrupb.evaluation;

import com.google.common.eventbus.Subscribe;
import de.cedricrupb.ApplicationContext;
import de.cedricrupb.config.model.LearningConfig;
import de.cedricrupb.config.model.MLConfig;
import de.cedricrupb.config.model.TerminateConfig;
import de.cedricrupb.event.ExceptionEvent;
import de.cedricrupb.event.config.ConfigLoadingEvent;
import de.cedricrupb.event.learn.TerminationEvent;
import org.aksw.limes.core.evaluation.evaluationDataLoader.EvaluationData;
import org.aksw.limes.core.io.mapping.AMapping;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 *
 * Class that bases on LIMES logic/functionality to Evaluate
 *
 * @author Cedric Richter
 */

public class LimesBasedEvaluator {

    private ApplicationContext ctx;

    private boolean interuppt = false;
    private IdentityHashMap<LearningConfig, EventBasedCallable> callback = new IdentityHashMap<>();

    public LimesBasedEvaluator(ApplicationContext ctx) {
        this.ctx = ctx;
        this.ctx.getBus().register(this);
    }

    public Future<AMapping> processDataSet(EvaluationData data){

        try {
            Path p = Files.createTempFile("dataSetOut", ".nt");

            TerminateConfig term = new TerminateConfig(
                    1, true, p.toString()
            );

            LearningConfig cfg = LimesDataSetLoader.getData(data, new MLConfig(), term);

            EventBasedCallable callable = new EventBasedCallable();
            callback.put(cfg, callable);

            this.ctx.getBus().post(new ConfigLoadingEvent(cfg));

            FutureTask<AMapping> task =  new FutureTask<>(callable);
            new Thread(task).run();
            return task;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    /**
     *
     * Calls termination event
     */
    @Subscribe
    public void onTermination(TerminationEvent event){
        if(callback.containsKey(event.getConfig())){
            EventBasedCallable callable = callback.remove(event.getConfig());
            synchronized (callable) {
                callable.mapping = event.getTerminationMapping();
                callable.notifyAll();
            }
        }
    }

    /**
     *
     * Calls exception event
     */
    @Subscribe
    public void onException(ExceptionEvent event){
        interuppt = true;
        for(EventBasedCallable callable: callback.values()){
            synchronized (callable){
                callable.notifyAll();
            }
        }
    }


    private class EventBasedCallable implements Callable<AMapping> {

        private AMapping mapping = null;

        @Override
        public AMapping call() throws Exception {
            synchronized (this) {
                while (!interuppt && mapping == null) {
                    this.wait();
                }
            }
            return mapping;
        }
    }

}
