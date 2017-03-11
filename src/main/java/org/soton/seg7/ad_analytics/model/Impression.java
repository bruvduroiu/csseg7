package org.soton.seg7.ad_analytics.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Created by bogdanbuduroiu on 24/02/2017.
 */
public class Impression {
    @JsonProperty("Date")
    private Date date;

    @JsonProperty("ID")
    private long id;

    @JsonProperty("Gender")
    private char gender;

    @JsonProperty("Age")
    private String age;

    @JsonProperty("Income")
    private String income;

    @JsonProperty("Context")
    private String context;

    @JsonProperty("Impression Cost")
    private Double impressionCost;

    public Impression() { }

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

    public char getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender.charAt(0);
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getIncome() {
        return income;
    }

    public void setIncome(String income) {
        this.income = income;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Double getImpressionCost() {
        return impressionCost;
    }

    public void setImpressionCost(Double impressionCost) {
        this.impressionCost = impressionCost;
    }
}
