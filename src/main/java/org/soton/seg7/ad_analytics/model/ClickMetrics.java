package org.soton.seg7.ad_analytics.model;

import java.util.Date;

/**
 * Created by bogdanbuduroiu on 11/03/2017.
 */
public class ClickMetrics {

    private Date date;
    private Double cost;
    private Integer num;

    public ClickMetrics(Date date, Double cost, Integer num) {
        this.date = date;
        this.cost = cost;
        this.num = num;
    }

    public Date getDate() {
        return date;
    }

    public Double getCost() {
        return cost;
    }

    public Integer getNum() {
        return num;
    }
}
