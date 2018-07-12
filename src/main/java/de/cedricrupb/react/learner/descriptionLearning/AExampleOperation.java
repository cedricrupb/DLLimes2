package de.cedricrupb.react.learner.descriptionLearning;

import de.cedricrupb.config.model.Example;
import org.aksw.limes.core.io.config.KBInfo;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLOntologyKnowledgeSource;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.SPARQLReasoner;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class AExampleOperation implements IExampleOperation {

    public IRI resolvePrefix(KBInfo info, String s){
        String[] split = s.split(":");
        if(split.length >= 2){
            String prefix = split[0];

            String rest = "";

            for(int i = 1; i < split.length; i++){
                rest += split[i]+":";
            }
            rest = rest.substring(0, rest.length()-1);

            Map<String, String> prefixes = info.getPrefixes();

            if(prefixes.containsKey(prefix)){
                prefix = prefixes.get(prefix);
                return IRI.create(prefix + rest);
            }
        }
        return IRI.create(s);
    }

    private OWLIndividual fromExample(KBInfo info, Example example){
        return new OWLNamedIndividualImpl(resolvePrefix(info, example.getUri()));
    }

    public SortedSet<OWLIndividual> getInstances(KBInfo info, Set<Example> examples){

        SortedSet<OWLIndividual> instances = new TreeSet<>();

        for(Example example: examples){

            IRI iri = resolvePrefix(info, example.getUri());

            instances.add(new OWLNamedIndividualImpl(
                    iri
            ));
        }

        return instances;
    }


}
