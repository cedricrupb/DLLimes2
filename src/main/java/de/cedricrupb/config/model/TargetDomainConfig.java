package de.cedricrupb.config.model;

import org.aksw.limes.core.io.config.KBInfo;

import java.util.Set;

public class TargetDomainConfig extends LearningDomainConfig {
    public TargetDomainConfig(KBInfo info, Set<Example> examples) {
        super(info, examples);
    }
}
