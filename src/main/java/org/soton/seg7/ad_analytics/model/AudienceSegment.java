package org.soton.seg7.ad_analytics.model;

import javafx.beans.property.SimpleStringProperty;

public class AudienceSegment {

    private final SimpleStringProperty segmentName, numberOfImpressions;

    public AudienceSegment (String segmentName, String numberOfImpressions) {
        this.segmentName = new SimpleStringProperty(segmentName);
        this.numberOfImpressions = new SimpleStringProperty(numberOfImpressions);
    }

    public String getSegmentName() {
        return this.segmentName.get();
    }

    public String getNumberOfImpressions() {
        return this.numberOfImpressions.get();
    }

}
