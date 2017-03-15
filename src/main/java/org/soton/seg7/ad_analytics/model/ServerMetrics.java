package org.soton.seg7.ad_analytics.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Created by bogdanbuduroiu on 11/03/2017.
 */
public class ServerMetrics {

    private Date date;
    private Double conversionRate;
    private Double bounceRatePage;
    private Double bounceRateTime;
    private Integer num;
    private Double views;

    public ServerMetrics(Date date, Double conversionRate, Double bounceRatePage, Double bounceRateTime, Integer num, Double views) {
        this.date = date;
        this.conversionRate = conversionRate;
        this.bounceRatePage = bounceRatePage;
        this.bounceRateTime = bounceRateTime;
        this.num = num;
        this.views = views;
    }

    @JsonProperty("Date")
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(Double conversionRate) {
        this.conversionRate = conversionRate;
    }

    public Double getBounceRatePage() {
        return bounceRatePage;
    }

    public void setBounceRatePage(Double bounceRatePage) {
        this.bounceRatePage = bounceRatePage;
    }

    public Double getBounceRateTime() {
        return bounceRateTime;
    }

    public void setBounceRateTime(Double bounceRateTime) {
        this.bounceRateTime = bounceRateTime;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public Double getViews() {
        return views;
    }

    public void setViews(Double views) {
        this.views = views;
    }
}
