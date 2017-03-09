package org.soton.seg7.ad_analytics.controller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

    private enum Graph {
        COST_PER_CLICK("Cost per Click"),
        NUMBER_OF_IMPRESSIONS("Number of Impressions"),
        NUMBER_OF_CLICKS("Number of Clicks"),
        CLICK_THROUGH_RATE("Click through Rate"),
        NUMBER_OF_CONVERSIONS("Number of Conversions"),
        TOTAL_COST("Total Cost"),
        COST_HISTOGRAM("Cost histogram");

        String title;

        Graph (String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private static final int NO_FILTER = 0;

    private static final int FILTER_AGE_25 = 1;
    private static final int FILTER_AGE_25_34 = 2;
    private static final int FILTER_AGE_35_54 = 3;
    private static final int FILTER_AGE_54 = 4;

    private static final int FILTER_INCOME_LOW = 10;
    private static final int FILTER_INCOME_MEDIUM = 20;
    private static final int FILTER_INCOME_HIGH = 30;

    private static final int FILTER_GENDER_MALE = 100;
    private static final int FILTER_GENDER_FEMALE = 200;

    private Graph currentGraph;

    private int currentFilter;

    @FXML
    private ListView<String> graphList;

    @FXML
    private ObservableList<String> list;

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
        if (graph.equals("Cost per Click"))
            loadCostPerClick();
        else if (graph.equals("Number of Impressions"))
            loadNumberOfImpressions();
        else if (graph.equals("Number of Clicks"))
            loadNumberOfClicks();
        else if (graph.equals("Click through Rate"))
            loadClickThroughRate();
        else if (graph.equals("Number of Conversions"))
            loadNumberOfConversions();
        else if (graph.equals("Total Cost"))
            loadTotalCost();
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
        lineChart.setTitle("Total Cost / Day");

        try {
            Map<String, Map<String, Double>> totalCostOverTime = DBQuery.getTotalCostOverTime();
            ArrayList<String> days = new ArrayList<String>(totalCostOverTime.keySet());
            Collections.sort(days);

            for (String day : days)
                series.getData().add(new XYChart.Data<>(day, totalCostOverTime.get(day).values().stream().mapToDouble(Number::doubleValue).sum()));

            lineChart.getData().clear();
            lineChart.getData().add(series);
        }
        catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    private void loadNumberOfConversions() {
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineChart.setTitle("Number of Conversions / Day");

        try {
            Map<String, Map<String, Integer>> conversionsMap = DBQuery.getNumConversions();
            ArrayList<String> days = new ArrayList<String>(conversionsMap.keySet());
            Collections.sort(days);

            for (String day : days)
                series.getData().add(new XYChart.Data<>(day, conversionsMap.get(day).values().stream().mapToDouble(Number::doubleValue).sum()));

            lineChart.getData().clear();
            lineChart.getData().add(series);
        }
        catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    private void loadClickThroughRate() {
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineChart.setTitle("Click Through Rate / Day");

        try {
            Map<String, Map<String, Double>> clickThroughRateMap = DBQuery.getCTROverTime();
            ArrayList<String> days = new ArrayList<String>(clickThroughRateMap.keySet());
            Collections.sort(days);

            for (String day : days)
                series.getData().add(new XYChart.Data<>(day, clickThroughRateMap.get(day).values().stream().mapToDouble(Number::doubleValue).sum()));

            lineChart.getData().clear();
            lineChart.getData().add(series);
        }
        catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    private void loadNumberOfClicks() {
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineChart.setTitle("Number of Clicks / Day");

        try {
            Map<String, Map<String, Integer>> numberOfClicks = DBQuery.getNumClicks();
            ArrayList<String> days = new ArrayList<String>(numberOfClicks.keySet());
            Collections.sort(days);

            for (String day : days)
                series.getData().add(new XYChart.Data<>(day, numberOfClicks.get(day).values().stream().mapToDouble(Number::doubleValue).sum()));

            lineChart.getData().clear();
            lineChart.getData().add(series);
        }
        catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    private void loadNumberOfImpressions() {
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineChart.setTitle("Number of Impressions / Day");

        try {
            Map<String, Map<String, Integer>> numberOfImpressions = DBQuery.getNumImpressions();
            ArrayList<String> days = new ArrayList<String>(numberOfImpressions.keySet());
            Collections.sort(days);

            for (String day : days)
                series.getData().add(new XYChart.Data<>(day, numberOfImpressions.get(day).values().stream().mapToDouble(Number::doubleValue).sum()));

            lineChart.getData().clear();
            lineChart.getData().add(series);
        }
        catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    private void loadCostPerClick() {
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineChart.setTitle("Cost per Click / Day");

        try {
            Map<String, Map<String, Double>> costPerClick = DBQuery.getClickCostOverTime();
            ArrayList<String> days = new ArrayList<String>(costPerClick.keySet());
            Collections.sort(days);

            for (String day : days)
                series.getData().add(new XYChart.Data<>(day, costPerClick.get(day).values().stream().mapToDouble(Number::doubleValue).sum()));

            lineChart.getData().clear();
            lineChart.getData().add(series);
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