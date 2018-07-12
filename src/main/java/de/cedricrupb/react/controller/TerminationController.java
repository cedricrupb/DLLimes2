package de.cedricrupb.react.controller;

import com.google.common.eventbus.Subscribe;
import de.cedricrupb.ApplicationContext;
import de.cedricrupb.config.model.*;
import de.cedricrupb.event.config.MappingConfigEvent;
import de.cedricrupb.event.config.SourceInfoEvent;
import de.cedricrupb.event.config.TargetInfoEvent;
import de.cedricrupb.event.learn.*;
import de.cedricrupb.react.model.QualityReport;
import de.cedricrupb.utils.AsyncJoiner;
import de.cedricrupb.utils.ObjectRecorder;
import de.cedricrupb.utils.PrefixHelper;
import org.aksw.limes.core.io.mapping.AMapping;
import org.aksw.limes.core.io.mapping.MappingFactory;
import org.aksw.limes.core.io.mapping.writer.IMappingWriter;
import org.aksw.limes.core.io.mapping.writer.RDFMappingWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.*;

public class TerminationController {

    private static final double epsilon = 0.01;
    private static final String[] JOIN_ON = new String[]{"mapping", "quality"};

    private ApplicationContext ctx;
    private AsyncJoiner<LearningConfig> joiner;

    public TerminationController(ApplicationContext ctx) {
        this.ctx = ctx;
        this.joiner = new AsyncJoiner<>(JOIN_ON, this::jonConsumer);
    }


    private void jonConsumer(Map<String, Object> items){
        QualityReport report = (QualityReport) items.get(JOIN_ON[1]);
        AMapping mapping = (AMapping) items.get(JOIN_ON[0]);
        LearningConfig config = (LearningConfig) items.get("JOINED_ON");
        onFinished(config, mapping, report);
    }

    @Subscribe
    public void onLimesMapping(LimesMappingEvent event){
        joiner.join(event.getConfig(), "mapping", event.getMapping());
    }

    @Subscribe
    public void onReport(QualityReportEvent event){
        joiner.join(event.getConfig(), "quality", event.getReport());
    }


    public void onFinished(LearningConfig config, AMapping mapping,  QualityReport report){
        this.ctx.getObjectRecorder().getCurrentEpoch().newEpoch().record("limes-mapping", mapping);
        Set<Reference> references = fromMapping(mapping, config.getSrcConfig().getInfo().getPrefixes());
        if(shouldTerminate(config, references, report)){
            terminate(config, mapping, report);
        }else {
            continueExec(config, references);
        }
    }

    private boolean shouldTerminate(LearningConfig config,  Set<Reference> mapping, QualityReport report){
        ObjectRecorder recorder = this.ctx.getObjectRecorder().getCurrentEpoch();

        QualityReport oldReport = (QualityReport) recorder.getLastEpoch().get("learn.quality");

        boolean fixpoint = oldReport!=null?Math.abs(report.getPseudoFmeasure() - oldReport.getPseudoFmeasure()) < epsilon: false;

        recorder.record("mapping", mapping);

        if(config.getTerminateConfig().isFixpoint() && fixpoint &&
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

    private void terminate(LearningConfig config, AMapping mapping, QualityReport report){

        for(ObjectRecorder recorder : this.ctx.getObjectRecorder().getEpochs()){
            if(recorder.get("learn.quality") != null) {
                QualityReport r = (QualityReport)recorder.get("learn.quality");
                if(r.getPseudoRecall() - report.getPseudoRecall() > 0.05 ||
                        (r.getPseudoRecall() >= report.getPseudoRecall() && r.getPseudoFmeasure() > report.getPseudoFmeasure())) {
                    mapping = (AMapping) recorder.get("limes-mapping");
                    report = r;
                }
            }
        }

        emitMapping(mapping, config.getTerminateConfig().getOutFile());
        emitReport(report, config.getTerminateConfig().getOutFile()+".log");
        this.ctx.getBus().post(new TerminationEvent(
                config, mapping
        ));
    }

    private void continueExec(LearningConfig config, Set<Reference> mapping){
        this.ctx.getBus().post(new ContinuedExecutionEvent(config, mapping));
    }

    private void emitReport(QualityReport report, String path){

        try {
            PrintWriter writer = new PrintWriter(path);

            NumberFormat format = NumberFormat.getPercentInstance();
            format.setMaximumFractionDigits(2);

            writer.write(String.format("Quality report (epoch %d)", report.getEpoch()));

            String source;

            if(report.getSourceClass().getClassExpression() == null){
                source = report.getSourceClass().getRestriction();
            }else{
                source = report.getSourceClass().getClassExpression().toString();
            }

            writer.write(String.format("Source restriction: %s (certainty %s)",
                    source, format.format(report.getSourceClass().getAccuracy())));

            writer.write("Source coverage: "+ format.format(report.getSourceCoverage()));

            String target;

            if(report.getTargetClass().getClassExpression() == null){
                target = report.getTargetClass().getRestriction();
            }else{
                target = report.getTargetClass().getClassExpression().toString();
            }

            writer.write(String.format("Target restriction: %s (certainty %s)",
                    target, format.format(report.getTargetClass().getAccuracy())));
            writer.write("Target coverage: "+ format.format(report.getTargetCoverage()));

            writer.write(String.format("Pseudo precision: %f", report.getPseudoPrecision()));
            writer.write(String.format("Pseudo recall: %f", report.getPseudoRecall()));
            writer.write(String.format("Pseudo F-measure: %f", report.getPseudoFmeasure()));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


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
