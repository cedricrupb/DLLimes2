package de.cedricrupb.event.learn;

import de.cedricrupb.config.model.LearningConfig;
import de.cedricrupb.event.ConfigBasedEvent;
import de.cedricrupb.react.model.QualityReport;

public class QualityReportEvent extends ConfigBasedEvent {

    private QualityReport report;

    public QualityReportEvent(LearningConfig config, QualityReport report) {
        super(config);
        this.report = report;
    }


    public QualityReport getReport() {
        return report;
    }

}
