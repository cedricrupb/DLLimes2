package de.cedricrupb.react.learner.descriptionLearning;

import de.cedricrupb.config.model.Example;
import de.cedricrupb.config.model.NegativeExample;
import org.aksw.limes.core.io.config.KBInfo;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class ANegativeInferOperation extends AExampleOperation {


    protected int size;

    public ANegativeInferOperation(int size) {
        this.size = size;
    }


    public AbstractReasonerComponent getReasoner(KBInfo info, Set<Example> examples) throws MalformedURLException, ComponentInitException {

        DLComponentFactory factory = new DLComponentFactory(info);

        SortedSet<OWLIndividual> instances = this.getInstances(info, examples);
        Set<String> strInstances = new HashSet<>();

        for(OWLIndividual i: instances)
            strInstances.add(i.toStringID());

        return factory.createReasoner(strInstances);

    }

}
