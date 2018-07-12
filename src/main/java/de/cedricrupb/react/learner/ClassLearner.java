package de.cedricrupb.react.learner;


import de.cedricrupb.config.model.*;
import de.cedricrupb.react.learner.descriptionLearning.DLComponentFactory;
import de.cedricrupb.react.learner.descriptionLearning.ExampleHandler;
import de.cedricrupb.utils.PrefixHelper;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.*;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.utilities.owl.OWLClassExpressionToSPARQLConverter;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;


import java.net.MalformedURLException;
import java.util.*;


/**
 *
 * Class that Learns from Classes obtained from SPARQL Endpoints, OWLOntologies etc..
 *
 * @author Cedric Richter
 */


public class ClassLearner implements Runnable{

    public static final int SPARQL_RECURSION_DEPTH = 1;


    static Logger logger = LoggerFactory.getLogger(ClassLearner.class.getName());

    private LearningDomainConfig config;
    private Set<Example> examples;

    private Throwable exception;
    private String restriction;
    private CELOE algorithm;


    public ClassLearner(LearningDomainConfig config, Set<Example> examples){
        this.config = config;
        this.examples = examples;
    }


    @Override
    public void run() {
        LearningDomainConfig cfg = config;

        Set<Example> examples = this.examples;

        ExampleHandler handler = ExampleHandler.init(cfg.getInfo(), examples);

        if(cfg.getInfo().getRestrictions() != null &&
                !cfg.getInfo().getRestrictions().isEmpty()){
            handler = handler.inferPositive(cfg.getInfo().getRestrictions()).validate();
        }

        handler = handler.inferNegative(Math.max(examples.size()*2, 1000));

        examples = handler.build();

        DLComponentFactory dlComponentFactory = new DLComponentFactory(cfg.getInfo());

        AbstractReasonerComponent reasoner = null;

        try {
            reasoner = dlComponentFactory.createExampleReasoner(examples);
        } catch (MalformedURLException e) {
            logger.error("Cannot initialize reasoner", e);
            exception = e;
            return;
        } catch (ComponentInitException e) {
            logger.error("Cannot initialize reasoner", e);
            exception = e;
            return;
        }

        if(reasoner == null)
            return;

        AbstractClassExpressionLearningProblem<? extends Score> lp = createProblem(examples);
        lp.setReasoner(reasoner);

        try {
            lp.init();
        } catch (ComponentInitException e) {
            logger.error("Cannot initialize learning problem", e);
            exception = e;
            return;
        }

        CELOE alg = new CELOE();

        alg.setLearningProblem(lp);
        alg.setReasoner(reasoner);
        alg.setMaxExecutionTimeInSecondsAfterImprovement(5);

        try {
            alg.init();
        } catch (ComponentInitException e) {
            logger.error("Cannot initialize algorithm", e);
            exception = e;
            return;
        }

        alg.start();

        algorithm = alg;

        logger.info("Finished execution with accuracy: "+alg.getCurrentlyBestAccuracy());

        restriction = transformExpr(alg.getCurrentlyBestDescription(), cfg.getInfo().getVar());
        restriction = PrefixHelper.revertPrefix(restriction, cfg.getInfo().getPrefixes());



    }

    public boolean hasException(){
        return exception != null;
    }

    public Throwable getException(){
        return exception;
    }

    public String getRestriction(){
        return restriction;
    }

    public double getAccuracy() {
        if(algorithm != null)
            return algorithm.getCurrentlyBestAccuracy();
        if(restriction != null)
            return 1.0;
        return 0.0;
    }


    public OWLClassExpression getLearnedExpression() {
        return algorithm != null? algorithm.getCurrentlyBestDescription() : null;
    }



    private IRI resolvePrefix(String s){
        return IRI.create(PrefixHelper.resolveSinglePrefix(s, this.config.getInfo().getPrefixes()));
    }

    private OWLIndividual fromExample(Example example){
        return new OWLNamedIndividualImpl(resolvePrefix(example.getUri()));
    }

    private AbstractClassExpressionLearningProblem<? extends Score> createProblem(Collection<? extends Example> examples){
        Set<OWLIndividual> pos = new HashSet<>();
        Set<OWLIndividual> neg = new HashSet<>();

        for(Example example: examples) {
            if (example instanceof PositiveExample)
                pos.add(fromExample(example));
            if (example instanceof NegativeExample)
                neg.add(fromExample(example));
        }

        if(neg.isEmpty()){
            PosOnlyLP lp = new PosOnlyLP();
            lp.setPositiveExamples(pos);
            return lp;
        }else {
            PosNegLP lp = new PosNegLPStandard();
            lp.setPositiveExamples(pos);
            lp.setNegativeExamples(neg);
            return lp;
        }
    }

    private String transformExpr(OWLClassExpression expr, String rootVar){

        OWLClassExpressionToSPARQLConverter converter = new OWLClassExpressionToSPARQLConverter();

        String s = converter.convert(rootVar, expr).trim();

        while(s.endsWith(".")){
            s = s.substring(0, s.length()-1).trim();
        }
        return s;
    }

}
