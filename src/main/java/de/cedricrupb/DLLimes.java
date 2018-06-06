package de.cedricrupb;

import com.google.common.eventbus.EventBus;
import de.cedricrupb.config.ConfigurationLoader;
import de.cedricrupb.config.model.LearningConfig;
import de.cedricrupb.evaluation.LimesBasedEvaluator;
import de.cedricrupb.event.config.ConfigLoadingEvent;
import org.aksw.limes.core.evaluation.evaluationDataLoader.DataSetChooser;
import org.aksw.limes.core.evaluation.evaluationDataLoader.EvaluationData;
import org.aksw.limes.core.io.mapping.AMapping;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DLLimes {

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, ExecutionException, InterruptedException {

        ApplicationContext ctx = ApplicationContext.createDefault(new EventBus());

        if(false) {
            String configPath = "/Users/cedricrichter/IdeaProjects/DLLimes2/src/main/resources/dbpedia.xml";

            ConfigurationLoader loader = ConfigurationLoader.createDefault();
            LearningConfig config = (LearningConfig) loader.parseFromXML(configPath);

            ctx.getBus().post(new ConfigLoadingEvent(config));
        }

        LimesBasedEvaluator evaluator = new LimesBasedEvaluator(ctx);
        Future<AMapping> future = evaluator.processDataSet(
                DataSetChooser.getData("PERSON1")
        );

        AMapping mapping = future.get();

        System.out.println(mapping);

    }

}
