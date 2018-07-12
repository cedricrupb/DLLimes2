package de.cedricrupb.config.model;

import com.google.common.collect.ImmutableSet;

import java.util.Objects;
import java.util.Set;

/**
 *
 * Class to Describe the Configuartion for Learning Purpose.
 *
 * @author Cedric Richter
 */


public class LearningConfig {

    private SourceDomainConfig srcConfig;
    private TargetDomainConfig targetConfig;
    private Set<Reference> mapping;
    private MLConfig mlConfig;
    private TerminateConfig terminateConfig;

    public LearningConfig(SourceDomainConfig srcConfig, TargetDomainConfig targetConfig, Set<Reference> mapping, MLConfig mlConfig, TerminateConfig terminateConfig) {
        this.srcConfig = srcConfig;
        this.targetConfig = targetConfig;
        this.mapping = ImmutableSet.copyOf(mapping);
        this.mlConfig = mlConfig;
        this.terminateConfig = terminateConfig;
    }
    /**
     *
     * @return source configuration
     */
    public SourceDomainConfig getSrcConfig() {
        return srcConfig;
    }

    /**
     *
     * @return target configuration
     */
    public TargetDomainConfig getTargetConfig() {
        return targetConfig;
    }

    /**
     *
     * @return mapping
     */
    public Set<Reference> getMapping() {
        return mapping;
    }

    /**
     *
     * @return machine learning configuration
     */
    public MLConfig getMlConfig() {
        return mlConfig;
    }

    /**
     *
     * @return termination configuration
     */
    public TerminateConfig getTerminateConfig() {
        return terminateConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearningConfig that = (LearningConfig) o;
        return Objects.equals(srcConfig, that.srcConfig) &&
                Objects.equals(targetConfig, that.targetConfig) &&
                Objects.equals(mapping, that.mapping) &&
                Objects.equals(mlConfig, that.mlConfig) &&
                Objects.equals(terminateConfig, that.terminateConfig);
    }

    @Override
    public int hashCode() {

        return Objects.hash(srcConfig, targetConfig, mapping, mlConfig, terminateConfig);
    }
}
