package de.cedricrupb.config.handlers;

import de.cedricrupb.config.model.ExampleFactory;

/**
 *
 * A subclass of the example handler. It handles definition of examples for the source
 * knowledge base.
 *
 * @author Cedric Richter
 */
public class SourceExampleHandler extends ExampleHandler {

    @Override
    public boolean isHandling(String parent, String name){
        if(parent.equalsIgnoreCase("SOURCE")){
            return super.isHandling(parent, name);
        }
        return false;
    }

    @Override
    protected ExampleFactory.ExampleSource getSource() {
        return ExampleFactory.ExampleSource.SOURCE;
    }

}
