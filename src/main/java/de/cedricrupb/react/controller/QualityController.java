package de.cedricrupb.react.controller;

import com.google.common.eventbus.Subscribe;
import de.cedricrupb.ApplicationContext;
import de.cedricrupb.event.config.MappingConfigEvent;
import de.cedricrupb.event.learn.LimesMappingEvent;
import de.cedricrupb.event.learn.QualityReportEvent;
import de.cedricrupb.event.learn.RestrictionEvent;
import de.cedricrupb.event.learn.observation.KBClassObservationEvent;
import de.cedricrupb.event.learn.observation.KBLongObservationEvent;
import de.cedricrupb.event.learn.observation.KBObservationEvent;
import de.cedricrupb.react.model.LearnedClass;
import de.cedricrupb.react.model.QualityReport;
import de.cedricrupb.utils.ObjectRecorder;
import org.aksw.limes.core.io.mapping.AMapping;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.NumberFormat;


/**
 *
 * Class that records Quality from source, target and records these observationsto report
 *
 * @author Cedric Richter
 */


public class QualityController {

    static Log log = LogFactory.getLog(QualityController.class);

    private ApplicationContext ctx;

    public QualityController(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    @Subscribe
    public void onConfigMapping(MappingConfigEvent event){
        ObjectRecorder recorder = this.ctx.getObjectRecorder().getCurrentEpoch();
        recorder.record("mapping", event.getMapping());
    }

    @Subscribe
    public void onLongObservation(KBLongObservationEvent event){
        if(!event.getKey().equalsIgnoreCase("restriction.entity.count"))return;
        ObjectRecorder recorder = this.ctx.getObjectRecorder().getCurrentEpoch();
        String baseKey;
        if(event.getType() == KBObservationEvent.KBType.SOURCE) {
            baseKey = "source";
        }else{
            baseKey = "target";
        }

        recorder.record(baseKey+".entity.count", event.getObservation());
    }

    @Subscribe
    public void onClassObservation(KBClassObservationEvent event){
        if(!event.getKey().equalsIgnoreCase("dllearner.class"))return;
        ObjectRecorder recorder = this.ctx.getObjectRecorder().getCurrentEpoch();
        String baseKey;
        if(event.getType() == KBObservationEvent.KBType.SOURCE) {
            baseKey = "source";
        }else{
            baseKey = "target";
        }

        recorder.record(baseKey+".class", event.getObservation());
    }


    @Subscribe
    public void onLimesMapping(LimesMappingEvent event){

        AMapping mapping = event.getMapping();

        ObjectRecorder recorder = this.ctx.getObjectRecorder().getCurrentEpoch();

        long allSource = (Long) recorder.get("source.entity.count");
        long allTarget = (Long) recorder.get("target.entity.count");
        LearnedClass sourceClass = (LearnedClass) recorder.get("source.class");
        LearnedClass targetClass = (LearnedClass) recorder.get("target.class");

        QualityReport report = new QualityReport(
                this.ctx.getObjectRecorder().getEpochs().size(),
                sourceClass,
                targetClass,
                mapping,
                allSource,
                allTarget
        );
        recorder.record("learn.quality", report);

        logReport(report);

        this.ctx.getBus().post(new QualityReportEvent(event.getConfig(), report));

    }


    private void logReport(QualityReport report){

        NumberFormat format = NumberFormat.getPercentInstance();
        format.setMaximumFractionDigits(2);

        log.info(String.format("Quality report (epoch %d)", report.getEpoch()));

        String source;

        if(report.getSourceClass().getClassExpression() == null){
            source = report.getSourceClass().getRestriction();
        }else{
            source = report.getSourceClass().getClassExpression().toString();
        }

        log.info(String.format("Source restriction: %s (certainty %s)",
                                source, format.format(report.getSourceClass().getAccuracy())));

        log.info("Source coverage: "+ format.format(report.getSourceCoverage()));

        String target;

        if(report.getTargetClass().getClassExpression() == null){
            target = report.getTargetClass().getRestriction();
        }else{
            target = report.getTargetClass().getClassExpression().toString();
        }

        log.info(String.format("Target restriction: %s (certainty %s)",
                target, format.format(report.getTargetClass().getAccuracy())));
        log.info("Target coverage: "+ format.format(report.getTargetCoverage()));

        log.info(String.format("Pseudo precision: %f", report.getPseudoPrecision()));
        log.info(String.format("Pseudo recall: %f", report.getPseudoRecall()));
        log.info(String.format("Pseudo F-measure: %f", report.getPseudoFmeasure()));


    }



}
