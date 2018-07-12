package de.cedricrupb.config.model;

import org.aksw.limes.core.io.config.KBInfo;

import java.util.Set;

/**
 *
 * Class extended from DomainLearningConfig to obtain Target learning domain config.
 *
 * @author Cedric Richter
 */


public class TargetDomainConfig extends LearningDomainConfig {
    public TargetDomainConfig(KBInfo info, Set<Example> examples) {
        super(info, examples);
    }
}
