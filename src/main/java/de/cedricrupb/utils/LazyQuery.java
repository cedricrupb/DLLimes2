package de.cedricrupb.utils;

import org.aksw.limes.core.io.config.KBInfo;
import org.apache.jena.query.QuerySolution;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class LazyQuery implements Iterable<QuerySolution> {

    private KBInfo kb;
    private String query;
    private LazyQueryFactory parent;

    LazyQuery(LazyQueryFactory parent, KBInfo kb, String query) {
        this.parent = parent;
        this.kb = kb;
        this.query = query;
    }


    @Override
    public Iterator<QuerySolution> iterator() {
        return new QueryIterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LazyQuery)) return false;
        LazyQuery that = (LazyQuery) o;
        return Objects.equals(kb, that.kb) &&
                Objects.equals(query, that.query);
    }

    @Override
    public int hashCode() {

        return Objects.hash(kb, query);
    }

    private class QueryIterator implements Iterator<QuerySolution>{

        private ResultDescription results = null;
        private boolean moreResults = false;
        private int offset = 0;

        private void queryResults(){

            if(results != null) {
                results.close();
                parent.unregisterDescription(results);
            }

            String q = EndPointHelper.instance().addPrefix(kb,
                    EndPointHelper.instance().addOffset(query, offset, kb.getPageSize()));

            results = EndPointHelper.instance().queryEndpoint(kb, q);
            parent.registerDescription(results);

            offset += kb.getPageSize();
        }


        @Override
        public boolean hasNext() {
            if(results == null || (!results.getResultSet().hasNext() && moreResults)){
                queryResults();
                moreResults = results.getResultSet().hasNext();
            }

            return results.getResultSet().hasNext();
        }

        @Override
        public QuerySolution next() {
            if(hasNext()){
                return results.getResultSet().next();
            }
            throw new NoSuchElementException();
        }
    }
}
