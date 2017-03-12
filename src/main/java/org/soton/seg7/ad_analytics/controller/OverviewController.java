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
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import org.soton.seg7.ad_analytics.model.DBQuery;
import org.soton.seg7.ad_analytics.model.Filters;
import org.soton.seg7.ad_analytics.model.exceptions.MongoAuthException;
import org.soton.seg7.ad_analytics.view.MainView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private Graph currentGraph;

    private int ageFilter;
    private int incomeFilter;
    private int genderFilter;
    private int currentFilter = ageFilter + incomeFilter + genderFilter;

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

    @FXML
    private MenuButton ageRangeDropdown, genderDropdown, incomeRangeDropdown;

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
        list.add("Cost per Click");
        list.add("Number of Impressions");
        list.add("Number of Clicks");
        list.add("Click through Rate");
        list.add("Number of Conversions");
        list.add("Total Cost");

        graphList.scrollTo(5);
        graphList.getSelectionModel().select(5);

        // Age Range Dropdown

        List<MenuItem> ageRangeDropdownItems = new ArrayList<>();

        MenuItem ageRange25 = new MenuItem("<25");
        ageRange25.setOnAction(e -> {
            ageFilter = Filters.AGE_25;
            loadGraph(currentGraph.toString());
        });
        MenuItem ageRange25_34 = new MenuItem("25-34");
        ageRange25_34.setOnAction(e -> {
            ageFilter = Filters.AGE_25_34;
            loadGraph(currentGraph.toString());
        });
        MenuItem ageRange35_44 = new MenuItem("35-44");
        ageRange35_44.setOnAction(e -> {
            ageFilter = Filters.AGE_35_44;
            loadGraph(currentGraph.toString());
        });
        MenuItem ageRange45_54 = new MenuItem("45-54");
        ageRange45_54.setOnAction(e -> {
            ageFilter = Filters.AGE_45_54;
            loadGraph(currentGraph.toString());
        });
        MenuItem ageRange54 = new MenuItem("54>");
        ageRange54.setOnAction(e -> {
            ageFilter = Filters.AGE_54;
            loadGraph(currentGraph.toString());
        });

        ageRangeDropdownItems.add(ageRange25);
        ageRangeDropdownItems.add(ageRange25_34);
        ageRangeDropdownItems.add(ageRange35_44);
        ageRangeDropdownItems.add(ageRange45_54);
        ageRangeDropdownItems.add(ageRange54);

        ageRangeDropdown.getItems().addAll(ageRangeDropdownItems);

        // Gender Dropdown

        List<MenuItem> genderDropdownItems = new ArrayList<>();

        MenuItem genderMale = new MenuItem("Male");
        genderMale.setOnAction(e -> {
            genderFilter = Filters.GENDER_MALE;
            loadGraph(currentGraph.toString());
        });
        MenuItem genderFemale = new MenuItem("Female");
        genderFemale.setOnAction(e -> {
            genderFilter = Filters.GENDER_FEMALE;
            loadGraph(currentGraph.toString());
        });

        genderDropdownItems.add(genderMale);
        genderDropdownItems.add(genderFemale);

        genderDropdown.getItems().addAll(genderDropdownItems);

        // Income Dropdown

        List<MenuItem> incomeRangeDropdownItems = new ArrayList<>();

        MenuItem incomeLow = new MenuItem("Low");
        incomeLow.setOnAction(e -> {
            incomeFilter = Filters.INCOME_LOW;
            loadGraph(currentGraph.toString());
        });
        MenuItem incomeMedium = new MenuItem("Medium");
        incomeMedium.setOnAction(e -> {
            incomeFilter = Filters.INCOME_MEDIUM;
            loadGraph(currentGraph.toString());
        });
        MenuItem incomeHigh = new MenuItem("High");
        incomeHigh.setOnAction(e -> {
            incomeFilter = Filters.INCOME_HIGH;
            loadGraph(currentGraph.toString());
        });

        incomeRangeDropdownItems.add(incomeLow);
        incomeRangeDropdownItems.add(incomeMedium);
        incomeRangeDropdownItems.add(incomeHigh);

        incomeRangeDropdown.getItems().addAll(incomeRangeDropdownItems);


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
    }

    private void loadGraph(String graph) {

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
                    new PieChart.Data("Total Impression Cost", DBQuery.getTotalCostImpressions(getCurrentFilter()))
            );
            pieChart.getData().clear();
            pieChart.setTitle("Campaign Cost Breakdown");
            pieChart.getData().addAll(pieChartData);

        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    private void loadTotalCost() {
        currentGraph = Graph.TOTAL_COST;
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineChart.setTitle("Total Cost / Day");

        try {
            Map<String, Map<String, Double>> totalCostOverTime = DBQuery.getTotalCostOverTime(getCurrentFilter());
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
        currentGraph = Graph.NUMBER_OF_CONVERSIONS;
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineChart.setTitle("Number of Conversions / Day");

        try {
            Map<String, Map<String, Double>> conversionsMap = DBQuery.getNumConversions();
            ArrayList<String> days = new ArrayList<>(conversionsMap.keySet());
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
        currentGraph = Graph.CLICK_THROUGH_RATE;
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineChart.setTitle("Click Through Rate / Day");

        try {
            Map<String, Map<String, Double>> clickThroughRateMap = DBQuery.getCTROverTime(getCurrentFilter());
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
        currentGraph = Graph.NUMBER_OF_CLICKS;
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineChart.setTitle("Number of Clicks / Day");

        try {
            Map<String, Map<String, Double>> numberOfClicks = DBQuery.getNumClicks();
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
        currentGraph = Graph.NUMBER_OF_IMPRESSIONS;
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineChart.setTitle("Number of Impressions / Day");

        try {
            Map<String, Map<String, Double>> numberOfImpressions = DBQuery.getNumImpressions(getCurrentFilter());
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
        currentGraph = Graph.COST_PER_CLICK;
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

    private Integer getCurrentFilter() {
        return ageFilter + incomeFilter + genderFilter;
    }
    
}