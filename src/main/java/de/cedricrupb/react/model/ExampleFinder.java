package de.cedricrupb.react.model;

import com.google.common.collect.ImmutableSet;
import de.cedricrupb.config.model.MLConfig;
import de.cedricrupb.config.model.PositiveReference;
import de.cedricrupb.config.model.Reference;
import org.aksw.limes.core.controller.Controller;
import org.aksw.limes.core.controller.ResultMappings;
import org.aksw.limes.core.io.config.Configuration;
import org.aksw.limes.core.io.config.KBInfo;
import org.aksw.limes.core.io.mapping.AMapping;
import org.aksw.limes.core.ml.algorithm.LearningParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ExampleFinder implements Runnable {

    static Logger logger = LoggerFactory.getLogger(ClassLearner.class.getName());
    private static final Set<String> REMOVE_KEYS = ImmutableSet.of("property_min_f_score", "property_min_coverage");

    private KBInfo srcInfo;
    private KBInfo targetInfo;
    private MLConfig ml;
    private Set<Reference> references;

    private Throwable exception;
    private AMapping mapping;

    public ExampleFinder(KBInfo srcInfo, KBInfo targetInfo, MLConfig ml, Set<Reference> references) {
        this.srcInfo = srcInfo;
        this.targetInfo = targetInfo;
        this.ml = ml;
        this.references = references;
    }

    @Override
    public void run() {
        try {
            Configuration configuration = setupLIMESConfig();

            ResultMappings result = Controller.getMapping(configuration);

            mapping = result.getAcceptanceMapping();

        } catch (IOException e) {
            logger.error("Cannot create tmp file", e);
            exception = e;
            return;
        }

    }

    public boolean hasException(){
        return exception != null;
    }

    public Throwable getException(){
        return exception;
    }

    public AMapping getMapping() {
        return mapping;
    }

    private Configuration setupLIMESConfig() throws IOException {
        Configuration config = new Configuration();
        config.getPrefixes().putAll(srcInfo.getPrefixes());
        config.setSourceInfo(srcInfo);

        config.getPrefixes().putAll(targetInfo.getPrefixes());
        config.setTargetInfo(targetInfo);

        config.setMlAlgorithmName(ml.getMlAlgorithmName());
        config.setMlImplementationType(ml.getMlImplementationType());
        config.setMlAlgorithmParameters(cleanParameter(ml.getMlAlgorithmParameters()));

        config.setTrainingDataFile(dumpReferences());

        config.setAcceptanceFile(tempFile("accept", ".nt"));
        config.setAcceptanceThreshold(ml.getThreshold());
        config.setAcceptanceRelation("http://www.w3.org/2002/07/owl#sameAs");

        config.setVerificationFile(tempFile("review", ".nt"));
        config.setVerificationThreshold(0.5);
        config.setVerificationRelation("http://www.w3.org/2002/07/owl#sameAs");

        config.setExecutionEngine("default");
        config.setExecutionPlanner("default");
        config.setExecutionRewriter("default");

        config.setOutputFormat("TAB");

        return config;
    }

    private List<LearningParameter> cleanParameter(List<LearningParameter> params){

        List<LearningParameter> out = new ArrayList<>();

        for(LearningParameter parameter: params)
            if(!REMOVE_KEYS.contains(parameter.getName()))
                out.add(parameter);

        return out;
    }

    private String dumpReferences(){
        try {
            File dumpFile = File.createTempFile("training", ".ttl");

            PrintWriter writer = new PrintWriter(dumpFile);

            for(Reference reference: references){
                if(reference instanceof PositiveReference){
                    String src = reference.getSource().getUri();
                    String target = reference.getTarget().getUri();
                    writer.println(String.format("<%s> <http://www.w3.org/2002/07/owl#sameAs> <%s>.", src, target));
                }
            }

            dumpFile.deleteOnExit();
            return dumpFile.getAbsolutePath();
        } catch (IOException e) {
            return ml.getMlTrainingDataFile();
        }
    }

    private String tempFile(String prefix, String suffix) throws IOException {
        File tmpFile = File.createTempFile(prefix, suffix);
        tmpFile.deleteOnExit();
        return tmpFile.getAbsolutePath();
    }
}
