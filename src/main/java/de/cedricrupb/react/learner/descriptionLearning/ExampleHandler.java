package de.cedricrupb.react.learner.descriptionLearning;

import de.cedricrupb.config.model.Example;
import de.cedricrupb.config.model.ExampleFactory;
import de.cedricrupb.utils.LazyQueryFactory;
import org.aksw.limes.core.io.config.KBInfo;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Resource;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderOWL;
import org.semanticweb.owlapi.model.OWLIndividual;

import java.util.*;

public class ExampleHandler {

    public static ExampleHandler init(KBInfo info, Set<Example> base){
        return new ExampleHandler(info, base);
    }


    private KBInfo info;
    private Set<Example> base;
    private List<IExampleOperation> operations;

    ExampleHandler(KBInfo info, Set<Example> examples){
        this.info = info;
        this.base = examples;
        this.operations = new ArrayList<>();
    }

    ExampleHandler(ExampleHandler base, IExampleOperation operation){
        this.info = base.info;
        this.base = new HashSet<>(base.base);
        this.operations = new ArrayList<>(base.operations);
        this.operations.add(operation);
    }


    public ExampleHandler inferPositive(List<String> restriction){
        return new ExampleHandler(this, new PositiveInferOperation(restriction));
    }

    public ExampleHandler inferNegative(int size){
        if(new DLComponentFactory(info).useSparqlReasoner())
            return new ExampleHandler(this, new SparqlNegativeInferOperation(size));
        return new ExampleHandler(this, new OWLNegativeInferOperation(size));
    }

    public ExampleHandler validate(){
        return new ExampleHandler(this, new ValidateExampleOperation());
    }

    public ExampleHandler truncate(int size){
        return new ExampleHandler(this, new TruncateOperation(size));
    }

    public Set<Example> build(){

        Set<Example> out = base;

        for(IExampleOperation op: this.operations)
            out = op.infer(info, out);

        return out;
    }

}
