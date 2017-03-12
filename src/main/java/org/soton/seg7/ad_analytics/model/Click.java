package org.soton.seg7.ad_analytics.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Created by bogdanbuduroiu on 24/02/2017.
 */
public class Click {

    @JsonProperty("Date")
    private Date date;

    @JsonProperty("ID")
    private long id;

    @JsonProperty("Click Cost")
    private Double clickCost;

    public Click() {
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Double getClickCost() {
        return clickCost;
    }

    public void setClickCost(Double clickCost) {
        this.clickCost = clickCost;
    }
}
