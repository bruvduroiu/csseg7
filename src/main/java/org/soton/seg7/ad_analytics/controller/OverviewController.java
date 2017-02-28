package org.soton.seg7.ad_analytics.controller;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.soton.seg7.ad_analytics.model.DBQuery;
import org.soton.seg7.ad_analytics.model.exceptions.MongoAuthException;
import org.soton.seg7.ad_analytics.view.MainView;

import java.util.Map;

public class OverviewController {
    @FXML
    private ListView<String> graphList;
    @FXML
    private Label graphTitleLabel;
    @FXML
    private ObservableList<String> list;
    @FXML
    private LineChart<String, Double> lineChart;
    @FXML
    private Stage stage;

    // Reference to the main application.
    private MainView mainView;
    public OverviewController() {
    }

    @FXML
    private void initialize() {
        list = graphList.getItems();
        list.add("Cost per Click");
        list.add("Number of Impressions");
        list.add("Number of Clicks");
        list.add("Click through Rate");
        list.add("Number of Conversions");
        list.add("Total Cost");


        // Listen for selection changes and show the person details when changed.
        graphList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> loadGraph(newValue));

    }
    private void loadGraph(String graph){
        switch (graph) {
            case "Cost per Click":
                loadCostPerClick();
                break;
            case "Number of Imrpessions":
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
        }
    }
    private void loadTotalCost() {
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        try {
            //TODO Implement Cost over time code
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void loadNumberOfConversions() {
        // TODO Auto-generated method stub
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineChart.setTitle("Number of Conversions / day");

        try {
            Map<String, Map<String, Integer>> conversionsMap = DBQuery.getNumConversions();
            for (String day : conversionsMap.keySet())
                series.getData().add(new XYChart.Data<>(day, conversionsMap.get(day).values().stream().mapToDouble(Number::doubleValue).sum()));


            lineChart.getData().add(series);
        }
        catch (MongoAuthException e) {
            e.printStackTrace();
        }

    }
    private void loadClickThroughRate() {
        // TODO Auto-generated method stub

    }
    private void loadNumberOfClicks() {
        // TODO Auto-generated method stub

    }
    private void loadNumberOfImpressions() {
        // TODO Auto-generated method stub

    }
    private void loadCostPerClick() {
        // TODO Auto-generated method stub

    }
    public void setMainView(MainView mainView) {
        this.mainView = mainView;

    }
}