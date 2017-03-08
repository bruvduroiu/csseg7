package org.soton.seg7.ad_analytics.controller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.soton.seg7.ad_analytics.model.DBQuery;
import org.soton.seg7.ad_analytics.model.exceptions.MongoAuthException;
import org.soton.seg7.ad_analytics.view.MainView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class OverviewController {

    @FXML
    private ListView<String> graphList;

    @FXML
    private ObservableList<String> list;

    @FXML
    private XYChart<String, Double> metricsChart;

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

    // Reference to the main application.
    private MainView mainView;

    public OverviewController() {
    }

    @FXML
    private void initialize() {
        list = graphList.getItems();
        list.clear();
        list.add("Cost per Click");
        list.add("Number of Impressions");
        list.add("Number of Clicks");
        list.add("Click through Rate");
        list.add("Number of Conversions");
        list.add("Total Cost");
        list.add("Click Cost Histogram");

        graphList.scrollTo(5);
        graphList.getSelectionModel().select(5);

        loadTotalCost();

        loadPieChart();

        try {
            // Display total cost of campaign in proper format
            String totalCampaignCost = String.format("£%.2f", DBQuery.getTotalCostCampaign()/100);
            totalCampaignCostLabel.setText(totalCampaignCost);

            // Display total cost of clicks in proper format
            String totalCostOfClicks = String.format("£%.2f", DBQuery.getTotalCostClicks()/100);
            totalCostOfClicksLabel.setText(totalCostOfClicks);

            // Display total cost of impressions in proper format
            String totalCostOfImpressions = String.format("£%.2f", DBQuery.getTotalCostImpressions()/100);
            totalCostOfImpressionsLabel.setText(totalCostOfImpressions);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }

        // Listen for selection changes and show the person details when changed.
        graphList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> loadGraph(newValue));

    }

    private void loadGraph(String graph){
        metricsChart = (LineChart<String, Double>) metricsChart;
        switch (graph) {
            case "Cost per Click":
                loadCostPerClick();
                break;
            case "Number of Impressions":
                loadNumberOfImpressions();
                break;
            case "Number of Clicks":
                loadNumberOfClicks();
                break;
            case "Click through Rate":
                loadClickThroughRate();
                break;
            case "Number of Conversions":
                loadNumberOfConversions();
                break;
            case "Total Cost":
                loadTotalCost();
                break;
            case "Click Cost Histogram":
                metricsChart = (BarChart<String, Double>) metricsChart;
                loadHistogram();
                break;
        }
    }

    private void loadPieChart() {
        try {
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Total Click Cost", DBQuery.getTotalCostClicks()),
                    new PieChart.Data("Total Impression Cost", DBQuery.getTotalCostImpressions())
            );
            pieChart.getData().clear();
            pieChart.setTitle("Campaign Cost Breakdown");
            pieChart.getData().addAll(pieChartData);

        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    private void loadTotalCost() {
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        metricsChart.setTitle("Total Cost / Day");

        try {
            Map<String, Map<String, Double>> totalCostOverTime = DBQuery.getTotalCostOverTime();
            ArrayList<String> days = new ArrayList<String>(totalCostOverTime.keySet());
            Collections.sort(days);

            for (String day : days)
                series.getData().add(new XYChart.Data<>(day, totalCostOverTime.get(day).values().stream().mapToDouble(Number::doubleValue).sum()));

            metricsChart.getData().clear();
            metricsChart.getData().add(series);
        }
        catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    private void loadNumberOfConversions() {
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        metricsChart.setTitle("Number of Conversions / Day");

        try {
            Map<String, Map<String, Integer>> conversionsMap = DBQuery.getNumConversions();
            ArrayList<String> days = new ArrayList<String>(conversionsMap.keySet());
            Collections.sort(days);

            for (String day : days)
                series.getData().add(new XYChart.Data<>(day, conversionsMap.get(day).values().stream().mapToDouble(Number::doubleValue).sum()));

            metricsChart.getData().clear();
            metricsChart.getData().add(series);
        }
        catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    private void loadClickThroughRate() {
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        metricsChart.setTitle("Click Through Rate / Day");

        try {
            Map<String, Map<String, Double>> clickThroughRateMap = DBQuery.getCTROverTime();
            ArrayList<String> days = new ArrayList<String>(clickThroughRateMap.keySet());
            Collections.sort(days);

            for (String day : days)
                series.getData().add(new XYChart.Data<>(day, clickThroughRateMap.get(day).values().stream().mapToDouble(Number::doubleValue).sum()));

            metricsChart.getData().clear();
            metricsChart.getData().add(series);
        }
        catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    private void loadNumberOfClicks() {
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        metricsChart.setTitle("Number of Clicks / Day");

        try {
            Map<String, Map<String, Integer>> numberOfClicks = DBQuery.getNumClicks();
            ArrayList<String> days = new ArrayList<String>(numberOfClicks.keySet());
            Collections.sort(days);

            for (String day : days)
                series.getData().add(new XYChart.Data<>(day, numberOfClicks.get(day).values().stream().mapToDouble(Number::doubleValue).sum()));

            metricsChart.getData().clear();
            metricsChart.getData().add(series);
        }
        catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    private void loadNumberOfImpressions() {
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        metricsChart.setTitle("Number of Impressions / Day");

        try {
            Map<String, Map<String, Integer>> numberOfImpressions = DBQuery.getNumImpressions();
            ArrayList<String> days = new ArrayList<String>(numberOfImpressions.keySet());
            Collections.sort(days);

            for (String day : days)
                series.getData().add(new XYChart.Data<>(day, numberOfImpressions.get(day).values().stream().mapToDouble(Number::doubleValue).sum()));

            metricsChart.getData().clear();
            metricsChart.getData().add(series);
        }
        catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    private void loadCostPerClick() {
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        metricsChart.setTitle("Cost per Click / Day");

        try {
            Map<String, Map<String, Double>> costPerClick = DBQuery.getClickCostOverTime();
            ArrayList<String> days = new ArrayList<String>(costPerClick.keySet());
            Collections.sort(days);

            for (String day : days)
                series.getData().add(new XYChart.Data<>(day, costPerClick.get(day).values().stream().mapToDouble(Number::doubleValue).sum()));

            metricsChart.getData().clear();
            metricsChart.getData().add(series);
        }
        catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    private void loadHistogram() {
        BarChart.Series<String, Double> series = new BarChart.Series<>();
        metricsChart.setTitle("Distribution of Click Cost");
        ((BarChart<String, Double>) metricsChart).setCategoryGap(0);
        ((BarChart<String, Double>) metricsChart).setBarGap(0);

        //collect all the data
        try {
            Map<String, Map<String, Double>> costPerClick = DBQuery.getClickCostOverTime(); //date->XXX->cost
            ArrayList<Double> costs = null;
            for(String key : costPerClick.keySet()) {
                costs.addAll(costPerClick.get(key).values());
            }
            //Collections.sort(costs); //probably not needed, waste of computation

            double binRange = costs.get(costs.size() -1) / 15;
            int[] group = new int[15];

            // I cba with frequency density so just sorting the costs into frequency bars
            for(double cost : costs) {
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
                }
            }

            //put all the data into the series
            for(int i=0; i<15; i++) {
                series.getData().add(new XYChart.Data(
                        ((binRange * i) + "-" + (binRange * (i+1))), group[i]));
            }

            metricsChart.getData().clear();
            metricsChart.getData().addAll(series);

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
        this.mainView.showLoadStage();
        initialize();
    }
    
}