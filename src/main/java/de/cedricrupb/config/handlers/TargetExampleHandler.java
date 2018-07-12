package de.cedricrupb.config.handlers;

import de.cedricrupb.config.model.ExampleFactory;

/**
 *
 * A subclass of the example handler. It handles definition of examples for the target
 * knowledge base
 *
 * @author Cedric Richter
 */
public class TargetExampleHandler extends ExampleHandler {
    @Override
    public boolean isHandling(String parent, String name){
        if(parent.equalsIgnoreCase("TARGET")){
            return super.isHandling(parent, name);
        }
        return false;
    }

    @Override
    protected ExampleFactory.ExampleSource getSource() {
        return ExampleFactory.ExampleSource.TARGET;
    }
}
