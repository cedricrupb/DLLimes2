package de.cedricrupb.config.model;


public class NegativeExample extends Example {
    NegativeExample(String uri) {
        super(uri);
    }

    @Override
    public String toString(){
        return "not ( "+super.toString()+" )";
    }
}
