package de.cedricrupb.react.learner.descriptionLearning;

import de.cedricrupb.config.model.Example;
import de.cedricrupb.config.model.ExampleFactory;
import org.aksw.limes.core.io.config.KBInfo;
import org.dllearner.core.ComponentInitException;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL2;
import org.semanticweb.owlapi.model.OWLIndividual;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

public class SparqlNegativeInferOperation extends ANegativeInferOperation {
    public SparqlNegativeInferOperation(int size) {
        super(size);
    }

    @Override
    public Set<Example> infer(KBInfo info, Set<Example> examples) {

        SortedSet<OWLIndividual> instances = this.getInstances(info, examples);

        SPARQLReasoner reasoner;

        try {
            reasoner = (SPARQLReasoner) this.getReasoner(info, examples);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return examples;
        } catch (ComponentInitException e) {
            e.printStackTrace();
            return examples;
        }

        AutomaticNegativeExampleFinderSPARQL2 negativeFinder = new AutomaticNegativeExampleFinderSPARQL2(reasoner);

        Set<Example> out = new HashSet<>(examples);

        ExampleFactory factory = new ExampleFactory(new HashMap<>());

        for(OWLIndividual individual: negativeFinder.getNegativeExamples(instances, size)){
            out.add(factory.createNegative(ExampleFactory.ExampleSource.SOURCE, individual.toStringID()));
        }

        return out;
    }
}
