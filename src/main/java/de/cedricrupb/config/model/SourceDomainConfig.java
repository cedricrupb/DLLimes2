package de.cedricrupb.config.model;

import org.aksw.limes.core.io.config.KBInfo;

import java.util.Set;

public class SourceDomainConfig extends LearningDomainConfig {
    public SourceDomainConfig(KBInfo info, Set<Example> examples) {
        super(info, examples);
    }
}
