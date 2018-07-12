package de.cedricrupb.config.model;

/**
 *
 * Class extended from Reference Class to obtain negative references
 *
 * @author Cedric Richter
 */


public class NegativeReference extends Reference {
    NegativeReference(Example source, Example target) {
        super(source, target);
    }
}
