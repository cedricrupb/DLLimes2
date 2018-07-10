package de.cedricrupb.react.model;

import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.Objects;

public class LearnedClass {

    private OWLClassExpression classExpression;
    private String restriction;
    private double accuracy;


    public LearnedClass(OWLClassExpression classExpression, String restriction, double accuracy) {
        this.classExpression = classExpression;
        this.restriction = restriction;
        this.accuracy = accuracy;
    }

    public OWLClassExpression getClassExpression() {
        return classExpression;
    }

    public String getRestriction() {
        return restriction;
    }

    public double getAccuracy() {
        return accuracy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnedClass that = (LearnedClass) o;
        return Double.compare(that.accuracy, accuracy) == 0 &&
                Objects.equals(classExpression, that.classExpression) &&
                Objects.equals(restriction, that.restriction);
    }

    @Override
    public int hashCode() {

        return Objects.hash(classExpression, restriction, accuracy);
    }


}
