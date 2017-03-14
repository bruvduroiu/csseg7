package org.soton.seg7.ad_analytics.model;

import java.util.Date;

/**
 * Created by bogdanbuduroiu on 11/03/2017.
 */
public class ImpressionMetrics {

    private Date Date;
    private Double cost;
    private Integer num;

    public ImpressionMetrics(Date date, Double cost, Integer num) {
        this.Date = date;
        this.cost = cost;
        this.num = num;
    }

    public Date getDate() {
        return Date;
    }

    public void setDate(Date date) {
        this.Date = date;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }
}
