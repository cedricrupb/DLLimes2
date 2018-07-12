package de.cedricrupb.config.model;

import org.aksw.limes.core.io.config.KBInfo;

import java.util.Set;

/**
 *
 * Class extended from DomainLearningConfig to obtain source learning domain config
 *
 * @author Cedric Richter
 */


public class SourceDomainConfig extends LearningDomainConfig {
    public SourceDomainConfig(KBInfo info, Set<Example> examples) {
        super(info, examples);
    }
}
