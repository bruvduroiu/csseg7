package org.soton.seg7.ad_analytics.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Created by bogdanbuduroiu on 24/02/2017.
 */

public class ServerEntry {

    @JsonProperty("Entry Date")
    private Date entryDate;

    @JsonProperty("Exit Date")
    private Date exitDate;

    @JsonProperty("ID")
    private long id;

    @JsonProperty("Pages Viewed")
    private int pages_viewed;

    @JsonProperty("Conversion")
    private boolean conversion;

    public ServerEntry() {
    }

    public Date getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(Date entryDate) {
        this.entryDate = entryDate;
    }

    public Date getExitDate() {
        return exitDate;
    }

    public void setExitDate(Date exitDate) {
        this.exitDate = exitDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getPagesViewed() {
        return pages_viewed;
    }

    public void setPages_viewed(int pages_viewed) {
        this.pages_viewed = pages_viewed;
    }

    public boolean isConversion() {
        return conversion;
    }

    public void setConversion(String conversion) {
        this.conversion = conversion.equals("Yes");
    }
}
