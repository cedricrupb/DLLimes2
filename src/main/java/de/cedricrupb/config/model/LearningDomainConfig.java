package de.cedricrupb.config.model;

import org.aksw.limes.core.io.config.KBInfo;

import java.util.Objects;
import java.util.Set;

/**
 *
 * Class to Learn the Configuration from Domain from KBInfo and Examples.
 *
 * @author Cedric Richter
 */


public class LearningDomainConfig {

    private KBInfo info;
    private Set<Example> examples;

    public LearningDomainConfig(KBInfo info, Set<Example> examples) {
        this.info = info;
        this.examples = examples;
    }

    public KBInfo getInfo() {
        return info;
    }

    public Set<Example> getExamples() {
        return examples;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearningDomainConfig that = (LearningDomainConfig) o;
        return Objects.equals(info, that.info) &&
                Objects.equals(examples, that.examples);
    }

    @Override
    public int hashCode() {

        return Objects.hash(info, examples);
    }

}
