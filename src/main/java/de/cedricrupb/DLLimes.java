package de.cedricrupb;

import com.google.common.eventbus.EventBus;
import de.cedricrupb.config.ConfigurationLoader;
import de.cedricrupb.config.model.LearningConfig;
import de.cedricrupb.evaluation.LimesBasedEvaluator;
import de.cedricrupb.event.config.ConfigLoadingEvent;
import de.cedricrupb.utils.Exception2EventHandler;
import org.aksw.limes.core.datastrutures.GoldStandard;
import org.aksw.limes.core.evaluation.evaluationDataLoader.DataSetChooser;
import org.aksw.limes.core.evaluation.evaluationDataLoader.DataSetChooser2;
import org.aksw.limes.core.evaluation.evaluationDataLoader.EvaluationData;
import org.aksw.limes.core.evaluation.evaluationDataLoader.IDataSet;
import org.aksw.limes.core.evaluation.evaluator.EvaluatorType;
import org.aksw.limes.core.evaluation.qualititativeMeasures.QualitativeMeasuresEvaluator;
import org.aksw.limes.core.io.mapping.AMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 *
 * Class that joins the Functionality of DL-Learner and LIMES
 * Runs on a Dataset, learns from DL-Learner, feeds it to LIMES, obtains FMeasure, Recall, Precision
 * and feeds it back to DL-Learner for further learning.
 *
 * @author Cedric Richter
 */


public class DLLimes {

    static Logger logger = LoggerFactory.getLogger(DLLimes.class);

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, ExecutionException, InterruptedException {

        ApplicationContext ctx = ApplicationContext.createDefault(new EventBus(
                new Exception2EventHandler()
        ));

        if(true) {
            String configPath = "/Users/cedricrichter/IdeaProjects/DLLimes2/src/main/resources/dumpLearning.xml";

            ConfigurationLoader loader = ConfigurationLoader.createDefault();
            LearningConfig config = (LearningConfig) loader.parseFromXML(configPath);

            ctx.getBus().post(new ConfigLoadingEvent(config));
        }else {

            LimesBasedEvaluator limes = new LimesBasedEvaluator(ctx);

            String[] datasets = {"RESTAURANTS"};

            File f = new File("result.txt");
            PrintWriter writer = new PrintWriter(f);
            try {
                for (String d : datasets) {
                    EvaluationData dataset = DataSetChooser2.instance().getData(d);
                    Future<AMapping> future = limes.processDataSet(
                            dataset
                    );
                    logger.info(String.format("Evaluate dataset %s.", dataset.getName()));

                    AMapping mapping = future.get();

                    GoldStandard standard = new GoldStandard(dataset.getReferenceMapping(),
                            dataset.getSourceCache().getAllUris(),
                            dataset.getTargetCache().getAllUris());

                    QualitativeMeasuresEvaluator evaluator = new QualitativeMeasuresEvaluator();

                    Set<EvaluatorType> evalTypes = new HashSet<>();
                    evalTypes.add(EvaluatorType.ACCURACY);
                    evalTypes.add(EvaluatorType.PRECISION);
                    evalTypes.add(EvaluatorType.RECALL);
                    evalTypes.add(EvaluatorType.F_MEASURE);

                    Map<EvaluatorType, Double> quality = evaluator.evaluate(mapping, standard, evalTypes);

                    writer.println(d + ":");
                    for (Map.Entry<EvaluatorType, Double> e : quality.entrySet()) {
                        writer.println(String.format("\t%s: %f", e.getKey().name(), e.getValue()));
                    }
                }
            }finally{
                writer.close();
            }
        }

    }

}
