package de.cedricrupb.react.learner.descriptionLearning;

import de.cedricrupb.config.model.Example;
import de.cedricrupb.config.model.ExampleFactory;
import org.aksw.limes.core.io.config.KBInfo;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderOWL;
import org.semanticweb.owlapi.model.OWLIndividual;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

public class OWLNegativeInferOperation extends ANegativeInferOperation {


    public OWLNegativeInferOperation(int size) {
        super(size);
    }

    @Override
    public Set<Example> infer(KBInfo info, Set<Example> examples) {

        SortedSet<OWLIndividual> instances = this.getInstances(info, examples);

        ClosedWorldReasoner cwr;

        try {
            cwr = (ClosedWorldReasoner)this.getReasoner(info, examples);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return examples;
        } catch (ComponentInitException e) {
            e.printStackTrace();
            return examples;
        }

        AutomaticNegativeExampleFinderOWL negativeFinder = new AutomaticNegativeExampleFinderOWL(
                instances,
                cwr
        );

        Set<Example> out = new HashSet<>(examples);

        ExampleFactory factory = new ExampleFactory(new HashMap<>());

        for(OWLIndividual individual: negativeFinder.getNegativeExamples(this.size, true)){
            out.add(factory.createNegative(ExampleFactory.ExampleSource.SOURCE, individual.toStringID()));
        }

        return out;
    }
}
