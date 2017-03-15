package org.soton.seg7.ad_analytics.controller;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import org.joda.time.DateTime;
import org.soton.seg7.ad_analytics.model.DBQuery;
import org.soton.seg7.ad_analytics.model.Filters;
import org.soton.seg7.ad_analytics.model.exceptions.MongoAuthException;
import org.soton.seg7.ad_analytics.view.MainView;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.math.BigDecimal;

public class OverviewController {

    private enum Graph {
        COST_PER_CLICK("Cost per Click"),
        NUMBER_OF_IMPRESSIONS("Number of Impressions"),
        NUMBER_OF_CLICKS("Number of Clicks"),
        CLICK_THROUGH_RATE("Click through Rate"),
        NUMBER_OF_CONVERSIONS("Number of Conversions"),
        TOTAL_COST("Total Cost"),
        CLICK_COST_HISTOGRAM("Click Cost Histogram"),
        COST_PER_THOUSAND_IMPRESSIONS("Cost per Thousand Impressions"),
        COST_PER_ACQUISITION("Cost per Acquisition"),
        BOUNCE_RATE("Bounce Rate");

        String title;

        Graph (String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }
    private Graph currentGraph;

    private int ageFilter;
    private int incomeFilter;
    private int genderFilter;
    private int currentFilter = ageFilter + incomeFilter + genderFilter;
    
    @FXML
    private Slider granularitySlider;

    @FXML
    private ListView<String> graphList;

    @FXML
    private ObservableList<String> list;

    @FXML
    private BarChart<String, Double> histogram;

    @FXML
    private LineChart<String, Double> lineChart;

    @FXML
    private Stage stage;

    @FXML
    private Label totalCampaignCostLabel;

    @FXML
    private Label totalCostOfClicksLabel;

    @FXML
    private Label totalCostOfImpressionsLabel;

    @FXML
    private PieChart pieChart;

    @FXML
    private ComboBox<String> ageRangeDropdown, genderDropdown, incomeRangeDropdown;

    @FXML
    private DatePicker startDate;

    @FXML
    private DatePicker endDate;

    // Reference to the main application.
    private MainView mainView;

    public OverviewController() {
    }

    @FXML
    private void initialize() {
        ageFilter = 0;
        incomeFilter = 0;
        genderFilter = 0;

        list = graphList.getItems();
        list.clear();
        list.add(Graph.TOTAL_COST.toString());
        list.add(Graph.COST_PER_CLICK.toString());
        list.add(Graph.NUMBER_OF_IMPRESSIONS.toString());
        list.add(Graph.NUMBER_OF_CLICKS.toString());
        list.add(Graph.CLICK_THROUGH_RATE.toString());
        list.add(Graph.COST_PER_ACQUISITION.toString());
        list.add(Graph.NUMBER_OF_CONVERSIONS.toString());
        list.add(Graph.BOUNCE_RATE.toString());
        list.add(Graph.CLICK_COST_HISTOGRAM.toString());
        list.add(Graph.COST_PER_THOUSAND_IMPRESSIONS.toString());

        graphList.scrollTo(0);
        graphList.getSelectionModel().select(0);
        granularitySlider.setSnapToTicks(true);
        granularitySlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double n) {
                if (n < 0.5) return "Hours";
                if (n < 1.5) return "Days";
                return "Months";
            }

            @Override
            public Double fromString(String s) {
                switch (s) {
                    case "Hours":
                        return 0d;
                    case "Days":
                        return 1d;
                    case "Months":
                        return 2d;
                    default:
                        return 1d;
                }
            }
        });
        
        

        // Age Range Dropdown

        ageRangeDropdown.getItems().addAll(
        		"All",
        		"<25",
        		"25-34",
        		"35-44",
        		"45-54",
        		"54>"
        		);

        startDate.valueProperty().addListener((ov,oldVal,newVal) -> {
            DBQuery.setDateRange(DateTime.parse(newVal.toString()), (endDate.getValue() == null) ? null : DateTime.parse(endDate.getValue().toString()));
            loadGraph(currentGraph.toString());
        });

        endDate.valueProperty().addListener((ov, oldVal, newVal) -> {
            DBQuery.setDateRange(DateTime.parse((startDate.getValue() == null) ? null : startDate.getValue().toString()), DateTime.parse(newVal.toString()));
            loadGraph(currentGraph.toString());
        });

        ageRangeDropdown.getSelectionModel().selectFirst();

        ageRangeDropdown.valueProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue ov, String oldVal, String newVal) {
            	switch(newVal) {
            	case "All":
            		ageFilter = 0;
            		break;
            	case "<25":
            		ageFilter = Filters.AGE_25;
            		break;
            	case "25-34":
            		ageFilter = Filters.AGE_25_34;
            		break;
            	case "35-44":
            		ageFilter = Filters.AGE_35_44;
            		break;
            	case "45-54":
            		ageFilter = Filters.AGE_45_54;
            		break;
            	case "54>":
            		ageFilter = Filters.AGE_54;
            		break;
            	}
            	loadGraph(currentGraph.toString());
              }    
          });
        
        // Gender Dropdown
        
        genderDropdown.getItems().addAll(
        		"All",
        		"Male",
        		"Female"
        		);
        
        genderDropdown.getSelectionModel().selectFirst();

        genderDropdown.valueProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue ov, String oldVal, String newVal) {
            	switch(newVal) {
            	case "All":
            		genderFilter = 0;
            		break;
            	case "Male":
            		genderFilter = Filters.GENDER_MALE;
            		break;
            	case "Female":
            		genderFilter = Filters.GENDER_FEMALE;
            		break;
            	}
            	loadGraph(currentGraph.toString());
              }    
          });

        // Income Dropdown
        
        incomeRangeDropdown.getItems().addAll(
        		"All",
        		"Low",
        		"Medium",
        		"High"
        		);
        
        incomeRangeDropdown.getSelectionModel().selectFirst();

        incomeRangeDropdown.valueProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue ov, String oldVal, String newVal) {
            	switch(newVal) {
            	case "All":
            		incomeFilter = 0;
            		break;
            	case "Low":
            		incomeFilter = Filters.INCOME_LOW;
            		break;
            	case "Medium":
            		incomeFilter = Filters.INCOME_MEDIUM;
            		break;
            	case "High":
            		incomeFilter = Filters.INCOME_HIGH;
            		break;
            	}
            	loadGraph(currentGraph.toString());
            	
              }    
          });
        

        // Load the total cost stats and pie chart

        loadTotalCost();

        loadPieChart();

        try {
            // Display total cost of campaign in proper format
            String totalCampaignCost = String.format("£%.2f", DBQuery.getTotalCostCampaign(getCurrentFilter())/100);
            totalCampaignCostLabel.setText(totalCampaignCost);

            // Display total cost of clicks in proper format
            String totalCostOfClicks = String.format("£%.2f", DBQuery.getTotalCostClicks()/100);
            totalCostOfClicksLabel.setText(totalCostOfClicks);

            // Display total cost of impressions in proper format
            String totalCostOfImpressions = String.format("£%.2f", DBQuery.getTotalCostImpressions(getCurrentFilter())/100);
            totalCostOfImpressionsLabel.setText(totalCostOfImpressions);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }

        // Listen for selection changes and show the person details when changed.
        graphList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> loadGraph(newValue));
        
        // Listen for time granularity change
        granularitySlider.valueProperty().addListener(
        		(observable, oldValue, newValue) -> changeGranularity(newValue));
    }
    
    private void changeGranularity(Number granularity){
    	if(granularity.intValue() == 0)
    		DBQuery.setGranularity(3);
    	else if(granularity.intValue() == 1)
    		DBQuery.setGranularity(2);
    	else if(granularity.intValue() == 2)
    		DBQuery.setGranularity(1);
    	loadGraph(currentGraph.toString());
    }

    private void loadGraph(String graph) {

        if (graph.equals(Graph.COST_PER_CLICK.toString()))
            loadCostPerClick();
        else if (graph.equals(Graph.NUMBER_OF_IMPRESSIONS.toString()))
            loadNumberOfImpressions();
        else if (graph.equals(Graph.NUMBER_OF_CLICKS.toString()))
            loadNumberOfClicks();
        else if (graph.equals(Graph.CLICK_THROUGH_RATE.toString()))
            loadClickThroughRate();
        else if (graph.equals(Graph.NUMBER_OF_CONVERSIONS.toString()))
            loadNumberOfConversions();
        else if (graph.equals(Graph.TOTAL_COST.toString()))
            loadTotalCost();
        else if (graph.equals(Graph.CLICK_COST_HISTOGRAM.toString()))
            loadHistogram();
        else if (graph.equals(Graph.COST_PER_THOUSAND_IMPRESSIONS.toString()))
            loadCostPerThousandImpressions();
        else if (graph.equals(Graph.COST_PER_ACQUISITION.toString()))
            loadCostPerAcquisition();
        else if (graph.equals(Graph.BOUNCE_RATE.toString()))
            loadBounceRate();
    }
    

    private void loadPieChart() {
        try {
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Total Click Cost", DBQuery.getTotalCostClicks()),
                    new PieChart.Data("Total Impression Cost", DBQuery.getTotalCostImpressions(getCurrentFilter()))
            );
            pieChart.getData().clear();
            pieChart.setTitle("Campaign Cost Breakdown");
            pieChart.getData().addAll(pieChartData);

        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    private void loadCostPerAcquisition() {
        currentGraph = Graph.COST_PER_ACQUISITION;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        XYChart.Series<String,Double> series = new XYChart.Series<>();
        lineChart.setTitle("Cost per Acquisition / " + getGranularityString());

        try {
            Map<DateTime, Double> costPerAcquisition = DBQuery.getCPAOverTime(getCurrentFilter());
            ArrayList<DateTime> dates = new ArrayList<>(costPerAcquisition.keySet());
            Collections.sort(dates);

            for (DateTime day : dates)
                series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), costPerAcquisition.get(day)));


            lineChart.getData().clear();
            lineChart.getData().add(series);
        }
        catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    private void loadBounceRate() {
        currentGraph = Graph.BOUNCE_RATE;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        XYChart.Series<String,Double> series = new XYChart.Series<>();
        lineChart.setTitle("Bounce Rate / " + getGranularityString());

        try {
            Map<DateTime, Double> bounceRate = DBQuery.getBounceRateByTime();
            ArrayList<DateTime> dates = new ArrayList<>(bounceRate.keySet());
            Collections.sort(dates);

            for (DateTime day : dates)
                series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), bounceRate.get(day)));


            lineChart.getData().clear();
            lineChart.getData().add(series);
        }
        catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    private void loadCostPerThousandImpressions() {
        currentGraph = Graph.COST_PER_THOUSAND_IMPRESSIONS;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineChart.setTitle("Cost per Thousand Impressions / Day");

        try {
            Map<DateTime, Double> costPerThousandImpressionsOverTime = DBQuery.getCostPerThousandImpressionsOverTime(getCurrentFilter());
            ArrayList<DateTime> dates = new ArrayList<>(costPerThousandImpressionsOverTime.keySet());
            Collections.sort(dates);

            for (DateTime day : dates)
                series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), costPerThousandImpressionsOverTime.get(day)));


            lineChart.getData().clear();
            lineChart.getData().add(series);
        }
        catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    private void loadTotalCost() {
        currentGraph = Graph.TOTAL_COST;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineChart.setTitle("Total Cost / Day");

        try {
            Map<DateTime, Double> totalCostOverTime = DBQuery.getTotalCostOverTime(getCurrentFilter());
            ArrayList<DateTime> dates = new ArrayList<>(totalCostOverTime.keySet());
            Collections.sort(dates);

            for (DateTime day : dates)
                series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), totalCostOverTime.get(day)));


            lineChart.getData().clear();
            lineChart.getData().add(series);
        }
        catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    private void loadNumberOfConversions() {
        currentGraph = Graph.NUMBER_OF_CONVERSIONS;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineChart.setTitle("Number of Conversions / Day");

        try {
            Map<DateTime, Double> conversionsMap = DBQuery.getNumConversions();
            ArrayList<DateTime> days = new ArrayList<>(conversionsMap.keySet());
            Collections.sort(days);

            for (DateTime day : days)
                series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), conversionsMap.get(day)));

            lineChart.getData().clear();
            lineChart.getData().add(series);
        }
        catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    private void loadClickThroughRate() {
        currentGraph = Graph.CLICK_THROUGH_RATE;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineChart.setTitle("Click Through Rate / Day");

        try {
            Map<DateTime, Double> clickThroughRateMap = DBQuery.getCTROverTime(getCurrentFilter());
            ArrayList<DateTime> days = new ArrayList<>(clickThroughRateMap.keySet());
            Collections.sort(days);

            for (DateTime day : days)
                series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), clickThroughRateMap.get(day)));

            lineChart.getData().clear();
            lineChart.getData().add(series);
        }
        catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    private void loadNumberOfClicks() {
        currentGraph = Graph.NUMBER_OF_CLICKS;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineChart.setTitle("Number of Clicks / Day");

        try {
            Map<DateTime, Double> numberOfClicks = DBQuery.getNumClicks();
            ArrayList<DateTime> days = new ArrayList<>(numberOfClicks.keySet());
            Collections.sort(days);

            for (DateTime day : days)
                series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), numberOfClicks.get(day)));

            lineChart.getData().clear();
            lineChart.getData().add(series);
        }
        catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    private void loadNumberOfImpressions() {
        currentGraph = Graph.NUMBER_OF_IMPRESSIONS;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineChart.setTitle("Number of Impressions / Day");

        try {
            Map<DateTime, Double> numberOfImpressions = DBQuery.getNumImpressions(getCurrentFilter());
            ArrayList<DateTime> days = new ArrayList<>(numberOfImpressions.keySet());
            Collections.sort(days);

            for (DateTime day : days)
                series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), numberOfImpressions.get(day)));

            lineChart.getData().clear();
            lineChart.getData().add(series);
        }
        catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    private void loadCostPerClick() {
        currentGraph = Graph.COST_PER_CLICK;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineChart.setTitle("Cost per Click / Day");

        try {
            Map<DateTime, Double> costPerClick = DBQuery.getClickCostOverTime();
            ArrayList<DateTime> days = new ArrayList<>(costPerClick.keySet());
            Collections.sort(days);

            for (DateTime day : days)
                series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), costPerClick.get(day)));

            lineChart.getData().clear();
            lineChart.getData().add(series);
        }
        catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }


    private void loadHistogram() {
        currentGraph = Graph.CLICK_COST_HISTOGRAM;
        histogram.setVisible(true);
        lineChart.setVisible(false);

        BarChart.Series<String, Double> series = new BarChart.Series<>();
        histogram.setTitle("Distribution of Click Cost");
        histogram.setCategoryGap(0);
        histogram.setBarGap(0);

        //collect all the data
        try {

            ArrayList<Double> clickCosts = DBQuery.getAllClickCosts();
            Collections.sort(clickCosts);

            double binRange = clickCosts.get(clickCosts.size() -1) / 15;
            int[] group = new int[15];

            // I cba with frequency density so just sorting the costs into frequency bars
            for(double cost : clickCosts) {
                if(cost <= binRange) {
                    group[0]++;
                }else if(cost <= binRange * 2) {
                    group[1]++;
                }else if(cost <= binRange * 3) {
                    group[2]++;
                }else if(cost <= binRange * 4) {
                    group[3]++;
                }else if(cost <= binRange * 5) {
                    group[4]++;
                }else if(cost <= binRange * 6) {
                    group[5]++;
                }else if(cost <= binRange * 7) {
                    group[6]++;
                }else if(cost <= binRange * 8) {
                    group[7]++;
                }else if(cost <= binRange * 9) {
                    group[8]++;
                }else if(cost <= binRange * 10) {
                    group[9]++;
                }else if(cost <= binRange * 11) {
                    group[10]++;
                }else if(cost <= binRange * 12) {
                    group[11]++;
                }else if(cost <= binRange * 13) {
                    group[12]++;
                }else if(cost <= binRange * 14) {
                    group[13]++;
                }else if(cost <= binRange * 15) {
                    group[14]++;
                } else {
                    System.err.println("The histogram has loaded incorrectly, cost (" + cost + ") > upper bracket (" + binRange * 15 + ")");
                }
            }

            //put all the data into the series
            for(int i=0; i<15; i++) {
                series.getData().add(new XYChart.Data(
                        (
                                (new BigDecimal(binRange * i).setScale(2, RoundingMode.HALF_UP).doubleValue())
                                + "-"
                                + (new BigDecimal(binRange * (i+1)).setScale(2, RoundingMode.HALF_UP).doubleValue())),
                        group[i]));
            }

            histogram.getData().clear();
            histogram.getData().addAll(series);

        }
        catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }


    public void setMainView(MainView mainView) {
        this.mainView = mainView;

    }


    //function that handles pressing of Change Campain button
    @FXML
    protected void handleChangeCampainButtonAction(ActionEvent event) {
        //this.mainView.showLoadStage();
        //initialize();
        /** do nothing */
    }


    private Integer getCurrentFilter() {
        return ageFilter + incomeFilter + genderFilter;
    }

    private String getGranularityString() {
        Integer granularity = DBQuery.getGranularity();
        return (granularity == DBQuery.GRANULARITY_MONTH)
                ? "Month"
                : (granularity == DBQuery.GRANULARITY_DAY)
                ? "Day"
                : "Hour";
    }

}