package de.cedricrupb.utils;


import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;

public class ResultDescription {

    private ResultSet set;
    private QueryExecution exec;

    public ResultDescription(ResultSet set, QueryExecution exec) {
        this.set = set;
        this.exec = exec;
    }

    public ResultSet getResultSet() {
        return set;
    }

    public void close(){
        exec.close();
    }

}
