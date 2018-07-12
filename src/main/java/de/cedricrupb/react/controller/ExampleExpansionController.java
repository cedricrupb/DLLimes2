package de.cedricrupb.react.controller;

import com.google.common.eventbus.Subscribe;
import de.cedricrupb.ApplicationContext;
import de.cedricrupb.config.model.*;
import de.cedricrupb.event.config.MappingConfigEvent;
import de.cedricrupb.event.learn.ContinuedExecutionEvent;
import de.cedricrupb.event.learn.ExampleExpansionEvent;
import de.cedricrupb.event.learn.QualityReportEvent;
import de.cedricrupb.event.learn.RestrictionEvent;
import de.cedricrupb.react.learner.ExampleExpander;
import de.cedricrupb.react.learner.ImproveStrategy;
import de.cedricrupb.react.model.QualityReport;
import de.cedricrupb.utils.AsyncJoiner;
import de.cedricrupb.utils.PrefixHelper;
import org.aksw.limes.core.io.config.KBInfo;

import java.util.*;

public class ExampleExpansionController {

    private static final String[] JOIN_ON = new String[]{"source_restriction", "target_restriction", "continued", "quality"};

    private ApplicationContext ctx;
    private AsyncJoiner<LearningConfig> joiner;

    public ExampleExpansionController(ApplicationContext ctx) {
        this.ctx = ctx;
        this.joiner = new AsyncJoiner<>(JOIN_ON, this::joinConsumer);
    }

    private void joinConsumer(Map<String, Object> map){
        LearningConfig config = (LearningConfig) map.get("JOINED_ON");
        String src = (String) map.get(JOIN_ON[0]);
        String target = (String) map.get(JOIN_ON[1]);
        Set<Reference> references = (Set<Reference>) map.get(JOIN_ON[2]);
        QualityReport report = (QualityReport) map.get(JOIN_ON[3]);
        expandExamples(config, src, target, references, report);
    }


    @Subscribe
    public void onRestriction(RestrictionEvent event){
        String key;

        if(event.getDomainConfig() instanceof SourceDomainConfig){
            key = JOIN_ON[0];
        }else{
            key = JOIN_ON[1];
        }

        joiner.join(event.getConfig(), key, event.getRestriction());
    }


    @Subscribe
    public void onContinuedExecution(ContinuedExecutionEvent event){
        joiner.join(event.getConfig(),"continued", event.getReferenceSet());
    }

    @Subscribe
    public void onQualityReport(QualityReportEvent event){
        joiner.join(event.getConfig(), "quality", event.getReport());
    }


    public void expandExamples(LearningConfig config, String srcRestriction, String targetRestriction, Set<Reference> referenceSet, QualityReport report){

        Set<PositiveExample> srcPossiblePositive = new HashSet<>();
        Set<PositiveExample> targetPossiblePositive = new HashSet<>();

        for(Reference reference: referenceSet){

            Example src = reference.getSource();
            if(src instanceof PositiveExample)
                srcPossiblePositive.add((PositiveExample) src);

            Example target = reference.getTarget();
            if(target instanceof PositiveExample)
                targetPossiblePositive.add((PositiveExample) target);

        }

        Set<Example> srcExamples = buildExamples(report, config.getSrcConfig().getInfo(),
                                                 srcRestriction,
                                                 config.getSrcConfig().getExamples(),
                                                 srcPossiblePositive, 1000);
        Set<Example> targetExamples = buildExamples(report, config.getTargetConfig().getInfo(),
                                                    targetRestriction,
                                                    config.getTargetConfig().getExamples(),
                                                    targetPossiblePositive, 1000);

        referenceSet = buildReferences(unify(config.getSrcConfig().getInfo(), config.getTargetConfig().getInfo(),
                                             referenceSet),
                                        unify(config.getSrcConfig().getInfo(), srcExamples),
                                        unify(config.getTargetConfig().getInfo(), targetExamples));

        this.ctx.getBus().post(new ExampleExpansionEvent(
                config, config.getSrcConfig(), srcExamples
        ));

        this.ctx.getBus().post(new ExampleExpansionEvent(
                config, config.getTargetConfig(), targetExamples
        ));

        this.ctx.getBus().post(new MappingConfigEvent(
                config, referenceSet
        ));
    }


    private Set<Example> buildExamples(QualityReport report, KBInfo info, String restriction, Set<Example> given, Set<PositiveExample> possible, int size){

        ExampleExpander expander = new ExampleExpander(info, restriction, given, possible);

        if(info.getRestrictions()!=null && !info.getRestrictions().isEmpty()){
            return new HashSet<>(given);
        }else{
            ImproveStrategy strategy = new ImproveStrategy(report, expander);
            return strategy.improveExamples(size);
        }

    }

    private Set<Reference> buildReferences(Set<Reference> references, Set<Example> sourceExamples, Set<Example> targetExamples){
        Set<Reference> result = new HashSet<>();
        List<Reference> list = new ArrayList<>();

        for(Reference reference: references){
            if(sourceExamples.contains(reference.getSource()) && targetExamples.contains(reference.getTarget())) {
                result.add(reference);
                list.add(reference);
            }
        }

        ExampleFactory factory = new ExampleFactory(new HashMap<>());

        int aimSize = 2*result.size();
        Random random = new Random();

        while(result.size() < aimSize){

            int ref1 = random.nextInt(list.size());
            int ref2 = random.nextInt(list.size());

            if(ref1 != ref2){
                result.add(factory.createNegativeReference(
                        list.get(ref1).getSource(),
                        list.get(ref2).getTarget()
                ));
            }

        }

        return result;
    }

    private Set<Reference> unify(KBInfo src, KBInfo tar, Set<Reference> set){

        Set<Reference> out = new HashSet<>();

        for(Reference r: set)
            out.add(unify(src, tar, r));

        return out;
    }

    private Reference unify(KBInfo src, KBInfo tar, Reference ref){
        ExampleFactory factory = new ExampleFactory(new HashMap<>());
        if(ref instanceof PositiveReference){
            return factory.createPositiveReference(unify(src, ref.getSource()), unify(tar, ref.getTarget()));
        }
        return factory.createNegativeReference(unify(src, ref.getSource()), unify(tar, ref.getTarget()));
    }

    private Set<Example> unify(KBInfo info, Set<Example> examples){
        Set<Example> out = new HashSet<>();

        for(Example example: examples){
            out.add(unify(info, example));
        }

        return out;
    }

    private Example unify(KBInfo info, Example example){
        ExampleFactory factory = new ExampleFactory(new HashMap<>());
        String t = example.getUri();
        t = PrefixHelper.resolveSinglePrefix(t, info.getPrefixes());
        if(example instanceof PositiveExample)
            return factory.createPositive(ExampleFactory.ExampleSource.SOURCE, t);
        else
            return factory.createNegative(ExampleFactory.ExampleSource.SOURCE, t);
    }


}
