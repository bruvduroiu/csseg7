package org.soton.seg7.ad_analytics.controller;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.scene.image.WritableImage;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import org.joda.time.DateTime;
import org.soton.seg7.ad_analytics.model.AudienceSegment;
import org.soton.seg7.ad_analytics.model.DBQuery;
import org.soton.seg7.ad_analytics.model.Filters;
import org.soton.seg7.ad_analytics.model.exceptions.MongoAuthException;
import org.soton.seg7.ad_analytics.view.MainView;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

import javax.imageio.ImageIO;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        NUMBER_OF_BOUNCES("Total Bounces"),
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
    private int contextFilter;
    private int currentFilter = ageFilter + incomeFilter + genderFilter + contextFilter;

    private boolean breakdownHidden = false;

    private ObservableList<AudienceSegment> genderBreakdownData;
    private ObservableList<AudienceSegment> ageBreakdownData;
    private ObservableList<AudienceSegment> incomeBreakdownData;
    private ObservableList<AudienceSegment> contextBreakdownData;

    private XYChart.Series<String, Double> costPerClick;
    private XYChart.Series<String, Double> numberOfImpressions;
    private XYChart.Series<String, Double> numberOfClicks;
    private XYChart.Series<String, Double> clickThroughRate;
    private XYChart.Series<String, Double> numberOfConversions;
    private XYChart.Series<String, Double> totalCost;
    private XYChart.Series<String, Double> costThousandImpressions;
    private XYChart.Series<String, Double> costPerAcquisition;
    private XYChart.Series<String, Double> numberOfBounces;
    private XYChart.Series<String, Double> bounceRate;

    private ExecutorService preemptiveExecutor;
    private Future<Map<DateTime, Double>> future_num_impressions;
    private Future<Map<DateTime, Double>> future_cost_impressions;
    private Future<?> future_breakdown;
    private Map<String, String> breakdown_data;

    private Map<DateTime, Double> num_impressions;
    private Map<DateTime, Double> cost_impressions;

    @FXML
    private Label bounceSettingsLabel;

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
    private ComboBox<String> ageRangeDropdown, genderDropdown, incomeRangeDropdown, contextDropdown;

    @FXML
    private DatePicker startDate;

    @FXML
    private DatePicker endDate;

    @FXML
    private RadioButton radioBounceTime;

    @FXML
    private RadioButton radioBouncePage;


    // FXML References relating to the Breakdown pane

    @FXML
    private AnchorPane breakdownPane;

    @FXML
    private SplitPane splitPane2;

    @FXML
    private TableColumn genderBreakdownCol;

    @FXML
    private TableColumn genderImpBreakdownCol;

    @FXML
    private TableView genderBreakdownTable;

    @FXML
    private TableView ageBreakdownTable;

    @FXML
    private TableColumn ageBreakdownCol;

    @FXML
    private TableColumn ageImpBreakdownCol;

    @FXML
    private TableView incomeBreakdownTable;

    @FXML
    private TableColumn incomeBreakdownCol;

    @FXML
    private TableColumn incomeImpBreakdownCol;

    @FXML
    private TableView contextBreakdownTable;

    @FXML
    private TableColumn contextBreakdownCol;

    @FXML
    private TableColumn contextImpBreakdownCol;

    @FXML
    private Button apply_button;

    // Reference to the main application.
    private MainView mainView;

    // Constructor is obsolete, instead use initialize() below
    public OverviewController() {
    }

    @FXML
    private void initialize() {

        preemptiveExecutor = Executors.newFixedThreadPool(2);
        loadImpressionsPreemptively();

        ageFilter = 0;
        incomeFilter = 0;
        genderFilter = 0;

        final ToggleGroup toggleGroup = new ToggleGroup();

        hideBreakdown();

        radioBounceTime.setToggleGroup(toggleGroup);
        radioBounceTime.setSelected(true);
        radioBouncePage.setToggleGroup(toggleGroup);
        radioBounceTime.setVisible(false);
    	radioBouncePage.setVisible(false);
        bounceSettingsLabel.setVisible(false);

        ValueAxis<Double> yAxis = (ValueAxis<Double>) lineChart.getYAxis();

        yAxis.setTickLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double object) {
                DecimalFormat df = new DecimalFormat("#.00");
                return (currentGraph.toString().contains("Cost") ? "£" : "") + df.format(object);
            }

            @Override
            public Double fromString(String string) {
                return null;
            }
        });

        list = graphList.getItems();
        list.clear();
        list.add(Graph.TOTAL_COST.toString());
        list.add(Graph.COST_PER_CLICK.toString());
        list.add(Graph.NUMBER_OF_IMPRESSIONS.toString());
        list.add(Graph.NUMBER_OF_CLICKS.toString());
        list.add(Graph.CLICK_THROUGH_RATE.toString());
        list.add(Graph.COST_PER_ACQUISITION.toString());
        list.add(Graph.NUMBER_OF_CONVERSIONS.toString());
        list.add(Graph.NUMBER_OF_BOUNCES.toString());
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
            if (endDate.getValue() != null && endDate.getValue().compareTo(newVal)>0)
                DBQuery.setDateRange(DateTime.parse(newVal.toString()), DateTime.parse(endDate.getValue().toString()));
            else
                DBQuery.setDateRange(DateTime.parse(newVal.toString()), null);
            if (oldVal == null || !oldVal.equals(newVal))
                wipeCaches();
            loadGraph(currentGraph.toString());
        });

        endDate.valueProperty().addListener((ov, oldVal, newVal) -> {
            if (startDate.getValue() != null && newVal.compareTo(startDate.getValue())>0)
                DBQuery.setDateRange(DateTime.parse(startDate.getValue().toString()), DateTime.parse(newVal.toString()));
            else
                DBQuery.setDateRange(null, DateTime.parse(newVal.toString()));
            if (oldVal == null || !oldVal.equals(newVal))
                wipeCaches();
            loadGraph(currentGraph.toString());
        });

        ageRangeDropdown.getSelectionModel().selectFirst();

        // Gender Dropdown
        
        genderDropdown.getItems().addAll(
        		"All",
        		"Male",
        		"Female"
        		);
        
        genderDropdown.getSelectionModel().selectFirst();

        // Income Dropdown
        
        incomeRangeDropdown.getItems().addAll(
        		"All",
        		"Low",
        		"Medium",
        		"High"
        		);
        
        incomeRangeDropdown.getSelectionModel().selectFirst();


        // Context Dropdown
        
        contextDropdown.getItems().addAll(
        		"All",
        		"Blog",
        		"News",
        		"Shopping",
        		"Social Media"
        		);
        
        contextDropdown.getSelectionModel().selectFirst();
        
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
        granularitySlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                Alert errorHours = new Alert(Alert.AlertType.INFORMATION);
                errorHours.setHeaderText("Must select a date for which to view metrics by hour");
                errorHours.setContentText("Please select a day for which to view the metrics by hour by using the Start Date Calendar tool.");
                if (startDate.getValue() == null && newValue.intValue() == 0) {
                    granularitySlider.setValue((double)oldValue);
                    errorHours.showAndWait();
                }
                else {
                    if (!oldValue.equals(newValue))
                        wipeCaches();
                    changeGranularity(newValue);
                }
            }
        });

        toggleGroup.selectedToggleProperty().addListener(
        		(observable, oldValue, newValue) -> {
        			if(currentGraph.equals(Graph.BOUNCE_RATE) || currentGraph.equals(Graph.NUMBER_OF_BOUNCES)) {
        			    wipeCaches();
                        loadGraph(currentGraph.toString());
                    }
        				
        		});

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
    	radioBounceTime.setVisible(false);
    	radioBouncePage.setVisible(false);
        bounceSettingsLabel.setVisible(false);

        hideBreakdown();

        if (graph.equals(Graph.COST_PER_CLICK.toString()))
            loadCostPerClick();
        else if (graph.equals(Graph.NUMBER_OF_IMPRESSIONS.toString())) {
            loadNumberOfImpressions();
        }
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
        else if (graph.equals(Graph.BOUNCE_RATE.toString())){
        	bounceSettingsLabel.setVisible(true);
        	radioBounceTime.setVisible(true);
        	radioBouncePage.setVisible(true);
            loadBounceRate();
        }
        else if (graph.equals(Graph.NUMBER_OF_BOUNCES.toString())) {
            bounceSettingsLabel.setVisible(true);
            radioBounceTime.setVisible(true);
            radioBouncePage.setVisible(true);
            loadNumberOfBounces();
        }
    }

    private void loadBreakdown() {
        if (breakdown_data != null) {
            if (breakdownHidden) {
                breakdownHidden = false;
                splitPane2.getItems().add(1, breakdownPane);
                splitPane2.setDividerPosition(0, 0.65);
            }

            genderBreakdownCol.setCellValueFactory(new PropertyValueFactory<>("segmentName"));
            genderImpBreakdownCol.setCellValueFactory(new PropertyValueFactory<>("numberOfImpressions"));

            ageBreakdownCol.setCellValueFactory(new PropertyValueFactory<>("segmentName"));
            ageImpBreakdownCol.setCellValueFactory(new PropertyValueFactory<>("numberOfImpressions"));

            incomeBreakdownCol.setCellValueFactory(new PropertyValueFactory<>("segmentName"));
            incomeImpBreakdownCol.setCellValueFactory(new PropertyValueFactory<>("numberOfImpressions"));

            contextBreakdownCol.setCellValueFactory(new PropertyValueFactory<>("segmentName"));
            contextImpBreakdownCol.setCellValueFactory(new PropertyValueFactory<>("numberOfImpressions"));

            genderBreakdownData = FXCollections.observableArrayList(
                    new AudienceSegment("Male", breakdown_data.get("M")),
                    new AudienceSegment("Female", breakdown_data.get("F"))
            );

            ageBreakdownData = FXCollections.observableArrayList(
                    new AudienceSegment("<25", breakdown_data.get("<25")),
                    new AudienceSegment("25-34", breakdown_data.get("25-34")),
                    new AudienceSegment("35-44", breakdown_data.get("35-44")),
                    new AudienceSegment("45-54", breakdown_data.get("45-54")),
                    new AudienceSegment(">54", breakdown_data.get(">55"))
            );

            incomeBreakdownData = FXCollections.observableArrayList(
                    new AudienceSegment("Low", breakdown_data.get("Low")),
                    new AudienceSegment("Middle", breakdown_data.get("Medium")),
                    new AudienceSegment("High", breakdown_data.get("High"))
            );

            contextBreakdownData = FXCollections.observableArrayList(
                    new AudienceSegment("News", breakdown_data.get("News")),
                    new AudienceSegment("Shopping", breakdown_data.get("Shopping")),
                    new AudienceSegment("Social Media", breakdown_data.get("Social Media")),
                    new AudienceSegment("Blog", breakdown_data.get("Blog")),
                    new AudienceSegment("Hobbies", breakdown_data.get("Hobbies")),
                    new AudienceSegment("Travel", breakdown_data.get("Travel"))
            );

            genderBreakdownTable.setItems(genderBreakdownData);
            ageBreakdownTable.setItems(ageBreakdownData);
            incomeBreakdownTable.setItems(incomeBreakdownData);
            contextBreakdownTable.setItems(contextBreakdownData);
        }
    }

    private void hideBreakdown() {
        if (!breakdownHidden) {
            breakdownHidden = true;
            splitPane2.getItems().remove(1);
        }
    }

    private void loadPieChart() {
        try {
            Double totalCostClicks = DBQuery.getTotalCostClicks();
            Double totalImpressionCost = DBQuery.getTotalCostImpressions(getCurrentFilter());

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Total Click Cost", Double.isNaN(totalCostClicks) ? 0d : totalCostClicks),
                    new PieChart.Data("Total Impression Cost", Double.isNaN(totalImpressionCost) ? 0d : totalImpressionCost)
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
        lineChart.setTitle("Cost per Acquisition / " + getGranularityString());

        if (costPerAcquisition == null )
            try {
                Map<DateTime, Double> costClicks = DBQuery.getClickCostOverTime();
                Map<DateTime, Double> numConversions = DBQuery.getNumConversions();
                Map<DateTime, Double> costImpressions = future_cost_impressions.get();

                Map<DateTime, Double> costPerAcquisition = Stream.concat(costImpressions.keySet().stream(), costClicks.keySet().stream())
                        .distinct()
                        .collect(Collectors.toMap(k->k, k->((!numConversions.containsKey(k) || numConversions.get(k)==0) ? 0
                                    : ((costImpressions.containsKey(k) ? costImpressions.getOrDefault(k,0d) : 0)
                                + ((costClicks.containsKey(k)) ? costClicks.getOrDefault(k,0d) : 0))/numConversions.getOrDefault(k,1d))));
                ArrayList<DateTime> dates = new ArrayList<>(costPerAcquisition.keySet());
                Collections.sort(dates);

                XYChart.Series<String, Double> series = new XYChart.Series<>();
                for (DateTime day : dates)
                    series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), costPerAcquisition.get(day) / 100));

                this.costPerAcquisition = series;
            } catch (MongoAuthException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }

        lineChart.getData().clear();
        lineChart.getData().add(costPerAcquisition);

        for (XYChart.Series<String, Double> s : lineChart.getData()) {
            for (XYChart.Data<String, Double> d : s.getData()) {
                Tooltip.install(d.getNode(), new Tooltip("Date: " +
                        d.getXValue().toString() + "\n" +
                        "Cost: £" + Math.floor(d.getYValue() * 100) / 100));

                //Adding class on hover
                d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add("onHover"));

                //Removing class on exit
                d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove("onHover"));
            }
        }
    }

    private void loadBounceRate() {
        currentGraph = Graph.BOUNCE_RATE;
        histogram.setVisible(false);
        lineChart.setVisible(true);
        lineChart.setTitle("Bounce Rate / " + getGranularityString());

        if (bounceRate != null ) {
            lineChart.getData().clear();
            lineChart.getData().add(bounceRate);
        } else {

            XYChart.Series<String, Double> series = new XYChart.Series<>();

            try {
                Map<DateTime, Double> bounceRate;
                if (radioBounceTime.isSelected())
                    bounceRate = DBQuery.getBounceRateByTime();
                else
                    bounceRate = DBQuery.getBounceRateByPage();
                ArrayList<DateTime> dates = new ArrayList<>(bounceRate.keySet());
                Collections.sort(dates);

                for (DateTime day : dates)
                    series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), bounceRate.get(day)));


                lineChart.getData().clear();
                lineChart.getData().add(series);
                this.bounceRate = series;

                for (XYChart.Series<String, Double> s : lineChart.getData()) {
                    for (XYChart.Data<String, Double> d : s.getData()) {
                        Tooltip.install(d.getNode(), new Tooltip("Date: " +
                                d.getXValue().toString() + "\n" +
                                "Bounces Rate: " + Math.floor(d.getYValue() * 100) / 100));

                        //Adding class on hover
                        d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add("onHover"));

                        //Removing class on exit
                        d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove("onHover"));
                    }
                }

            } catch (MongoAuthException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadNumberOfBounces() {
        currentGraph = Graph.NUMBER_OF_BOUNCES;
        histogram.setVisible(false);
        lineChart.setVisible(true);
        lineChart.setTitle("Number of Bounces / " + getGranularityString());

        if (numberOfBounces != null ) {
            lineChart.getData().clear();
            lineChart.getData().add(numberOfBounces);
        }

        XYChart.Series<String,Double> series = new XYChart.Series<>();

        try {
            Map<DateTime, Double> numBounces;
            if(radioBounceTime.isSelected())
                numBounces = DBQuery.getNumBouncesByTime();
            else
                numBounces = DBQuery.getNumBouncesByPage();
            ArrayList<DateTime> dates = new ArrayList<>(numBounces.keySet());
            Collections.sort(dates);

            for (DateTime day : dates)
                series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), numBounces.get(day)));


            lineChart.getData().clear();
            lineChart.getData().add(series);
            this.numberOfBounces = series;

            for (XYChart.Series<String, Double> s : lineChart.getData()) {
                for (XYChart.Data<String, Double> d : s.getData()) {
                    Tooltip.install(d.getNode(), new Tooltip("Date: " +
                            d.getXValue().toString() + "\n" +
                            "Bounces: " + Math.floor(d.getYValue() * 100) / 100));

                    //Adding class on hover
                    d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add("onHover"));

                    //Removing class on exit
                    d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove("onHover"));
                }
            }

        }
        catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    private void loadCostPerThousandImpressions() {
        currentGraph = Graph.COST_PER_THOUSAND_IMPRESSIONS;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        lineChart.setTitle("Cost per Thousand Impressions / Day");
        if (costThousandImpressions == null )
            try {
                Map<DateTime, Double> totalCost = DBQuery.getClickCostOverTime();
                Map<DateTime, Double> numImpressions = future_num_impressions.get();

                Map<DateTime, Double> costPerThousandImpressionsOverTime = Stream.concat(totalCost.keySet().stream(), numImpressions.keySet().stream())
                        .distinct()
                        .collect(Collectors.toMap(k->k, k->((!numImpressions.containsKey(k)) ? 0 : ((totalCost.containsKey(k)) ? totalCost.getOrDefault(k,0d) : 0)
                                / numImpressions.getOrDefault(k,0d) * 1000)));
                ArrayList<DateTime> dates = new ArrayList<>(costPerThousandImpressionsOverTime.keySet());
                Collections.sort(dates);

                XYChart.Series<String, Double> series = new XYChart.Series<>();
                for (DateTime day : dates)
                    series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), costPerThousandImpressionsOverTime.get(day)));

                this.costThousandImpressions = series;
            } catch (MongoAuthException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }

        lineChart.getData().clear();
        lineChart.getData().add(costThousandImpressions);

        for (XYChart.Series<String, Double> s : lineChart.getData()) {
            for (XYChart.Data<String, Double> d : s.getData()) {
                Tooltip.install(d.getNode(), new Tooltip("Date: " +
                        d.getXValue().toString() + "\n" +
                        "Cost: £" + Math.floor(d.getYValue() * 100) / 100));

                //Adding class on hover
                d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add("onHover"));

                //Removing class on exit
                d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove("onHover"));
            }
        }
    }

    private void loadTotalCost() {
        currentGraph = Graph.TOTAL_COST;
        histogram.setVisible(false);
        lineChart.setVisible(true);
        lineChart.setTitle("Total Cost / Day");

        if (totalCost != null ) {
            lineChart.getData().clear();
            lineChart.getData().add(totalCost);
        } else {

            XYChart.Series<String, Double> series = new XYChart.Series<>();

            try {
                Map<DateTime, Double> totalCostOverTime;
                if ((totalCostOverTime = DBQuery.getTotalCostOverTime(getCurrentFilter())).size() == 0)
                    return;
                ArrayList<DateTime> dates = new ArrayList<>(totalCostOverTime.keySet());
                Collections.sort(dates);

                for (DateTime day : dates)
                    series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), totalCostOverTime.get(day) / 100));


                lineChart.getData().clear();
                lineChart.getData().add(series);
                this.totalCost = series;

                for (XYChart.Series<String, Double> s : lineChart.getData()) {
                    for (XYChart.Data<String, Double> d : s.getData()) {
                        Tooltip.install(d.getNode(), new Tooltip("Date: " +
                                d.getXValue().toString() + "\n" +
                                        "Cost: £" + Math.floor(d.getYValue() * 100) / 100));

                        //Adding class on hover
                        d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add("onHover"));

                        //Removing class on exit
                        d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove("onHover"));
                    }
                }

            } catch (MongoAuthException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadNumberOfConversions() {
        currentGraph = Graph.NUMBER_OF_CONVERSIONS;
        histogram.setVisible(false);
        lineChart.setVisible(true);
        lineChart.setTitle("Number of Conversions / Day");

        if (numberOfConversions != null ) {
            lineChart.getData().clear();
            lineChart.getData().add(numberOfConversions);
        } else {
            XYChart.Series<String, Double> series = new XYChart.Series<>();

            try {
                Map<DateTime, Double> conversionsMap = DBQuery.getNumConversions();
                ArrayList<DateTime> days = new ArrayList<>(conversionsMap.keySet());
                Collections.sort(days);

                for (DateTime day : days)
                    series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), conversionsMap.get(day)));

                lineChart.getData().clear();
                lineChart.getData().add(series);

                for (XYChart.Series<String, Double> s : lineChart.getData()) {
                    for (XYChart.Data<String, Double> d : s.getData()) {
                        Tooltip.install(d.getNode(), new Tooltip("Date: " +
                                d.getXValue().toString() + "\n" +
                                "Conversions: " + Math.floor(d.getYValue() * 100) / 100));

                        //Adding class on hover
                        d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add("onHover"));

                        //Removing class on exit
                        d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove("onHover"));
                    }
                }

            }
            catch (MongoAuthException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadClickThroughRate() {
        currentGraph = Graph.CLICK_THROUGH_RATE;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        lineChart.setTitle("Click Through Rate / Day");

        if (clickThroughRate == null )
            try {
                Map<DateTime, Double> numImpressions = future_num_impressions.get();
                Map<DateTime, Double> numClicks = DBQuery.getNumClicks();

                Map<DateTime, Double> clickThroughRateMap = Stream.concat(numImpressions.keySet().stream(), numClicks.keySet().stream())
                        .distinct()
                        .collect(Collectors.toMap(k -> k, k -> (!numImpressions.containsKey(k)) ? 0 : ((numClicks.containsKey(k)) ? numClicks.getOrDefault(k, 0d) : 0) / numImpressions.getOrDefault(k,1d)));
                ArrayList<DateTime> days = new ArrayList<>(clickThroughRateMap.keySet());
                Collections.sort(days);

                XYChart.Series<String, Double> series = new XYChart.Series<>();
                for (DateTime day : days)
                    series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), clickThroughRateMap.get(day)));

                this.clickThroughRate = series;
            }
            catch (MongoAuthException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        lineChart.getData().clear();
        lineChart.getData().add(clickThroughRate);

        for (XYChart.Series<String, Double> s : lineChart.getData()) {
            for (XYChart.Data<String, Double> d : s.getData()) {
                Tooltip.install(d.getNode(), new Tooltip("Date: " +
                        d.getXValue().toString() + "\n" +
                        "Click-through Rate: " + Math.floor(d.getYValue() * 100) / 100));

                //Adding class on hover
                d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add("onHover"));

                //Removing class on exit
                d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove("onHover"));
            }
        }
    }

    private void loadNumberOfClicks() {
        currentGraph = Graph.NUMBER_OF_CLICKS;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        lineChart.setTitle("Number of Clicks / Day");
        if (numberOfClicks != null ) {
            lineChart.getData().clear();
            lineChart.getData().add(numberOfClicks);
        } else {
            XYChart.Series<String, Double> series = new XYChart.Series<>();

            try {
                Map<DateTime, Double> numberOfClicks = DBQuery.getNumClicks();
                ArrayList<DateTime> days = new ArrayList<>(numberOfClicks.keySet());
                Collections.sort(days);

                for (DateTime day : days)
                    series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), numberOfClicks.get(day)));

                lineChart.getData().clear();
                lineChart.getData().add(series);
                this.numberOfClicks = series;

                for (XYChart.Series<String, Double> s : lineChart.getData()) {
                    for (XYChart.Data<String, Double> d : s.getData()) {
                        Tooltip.install(d.getNode(), new Tooltip("Date: " +
                                d.getXValue().toString() + "\n" +
                                "Clicks: " + Math.floor(d.getYValue() * 100) / 100));

                        //Adding class on hover
                        d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add("onHover"));

                        //Removing class on exit
                        d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove("onHover"));
                    }
                }
            }
            catch (MongoAuthException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadNumberOfImpressions() {
        currentGraph = Graph.NUMBER_OF_IMPRESSIONS;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        lineChart.setTitle("Number of Impressions / Day");
        if (numberOfImpressions == null )
            try {
                XYChart.Series<String, Double> series = new XYChart.Series<>();
                Map<DateTime, Double> numberOfImpressions = future_num_impressions.get();
                ArrayList<DateTime> days = new ArrayList<>(numberOfImpressions.keySet());
                Collections.sort(days);

                for (DateTime day : days)
                    series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), numberOfImpressions.get(day)));


                this.numberOfImpressions = series;
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }

        lineChart.getData().clear();
        lineChart.getData().add(this.numberOfImpressions);

        if (breakdown_data != null)
            loadBreakdown();

        for (XYChart.Series<String, Double> s : lineChart.getData()) {
            for (XYChart.Data<String, Double> d : s.getData()) {
                Tooltip.install(d.getNode(), new Tooltip("Date: " +
                        d.getXValue().toString() + "\n" +
                        "Impressions: " + Math.floor(d.getYValue() * 100) / 100));

                //Adding class on hover
                d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add("onHover"));

                //Removing class on exit
                d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove("onHover"));
            }
        }
    }

    private void loadCostPerClick() {
        currentGraph = Graph.COST_PER_CLICK;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        lineChart.setTitle("Cost per Click / Day");
        if (costPerClick != null ) {
            lineChart.getData().clear();
            lineChart.getData().add(costPerClick);
        } else {
            XYChart.Series<String, Double> series = new XYChart.Series<>();

            try {
                Map<DateTime, Double> costPerClick = DBQuery.getClickCostOverTime();
                ArrayList<DateTime> days = new ArrayList<>(costPerClick.keySet());
                Collections.sort(days);

                for (DateTime day : days)
                    series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), costPerClick.get(day)/100));

                lineChart.getData().clear();
                lineChart.getData().add(series);
                this.costPerClick = series;

                for (XYChart.Series<String, Double> s : lineChart.getData()) {
                    for (XYChart.Data<String, Double> d : s.getData()) {
                        Tooltip.install(d.getNode(), new Tooltip("Date: " +
                                d.getXValue().toString() + "\n" +
                                "Cost: £" + Math.floor(d.getYValue() * 100) / 100));

                        //Adding class on hover
                        d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add("onHover"));

                        //Removing class on exit
                        d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove("onHover"));
                    }
                }
            }
            catch (MongoAuthException e) {
                e.printStackTrace();
            }
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
        MainView.showLoadStage();
        initialize();
    }

    //function that handles pressing of  button
    @FXML
    protected void handleExportButtonAction(ActionEvent event) {
    	WritableImage image = lineChart.snapshot(new SnapshotParameters(), null);


    	FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        //System.out.println(pic.getId());
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image,
                    null), "png", file);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private void wipeCaches() {
        this.numberOfConversions = null;
        this.clickThroughRate = null;
        this.totalCost = null;
        this.costThousandImpressions = null;
        this.numberOfBounces = null;
        this.bounceRate = null;
        this.costPerAcquisition = null;
        this.costPerClick = null;
        this.numberOfImpressions = null;
        this.numberOfClicks = null;
        this.breakdown_data = null;

        loadImpressionsPreemptively();
    }

    private void loadImpressionsPreemptively() {

        if (future_cost_impressions != null) future_cost_impressions.cancel(true);
        if (future_num_impressions != null) future_num_impressions.cancel(true);
        if (future_breakdown != null) future_breakdown.cancel(true);

        future_num_impressions = preemptiveExecutor.submit(() -> {
            try {
                return DBQuery.getNumImpressions(getCurrentFilter());
            } catch (MongoAuthException e) {
                e.printStackTrace();
                return null;
            }
        });

        future_cost_impressions = preemptiveExecutor.submit(() -> {
            try {
                return DBQuery.getImpressionCostOverTime(getCurrentFilter()/100);
            } catch (MongoAuthException e) {
                e.printStackTrace();
                return null;
            }
        });

        Runnable wrappedTask = () -> {
            try {
                try {
                    breakdown_data = Executors.newSingleThreadExecutor().submit(DBQuery::getBreakdown).get();
                } catch (InterruptedException e) {
                } catch (ExecutionException e) {
                }
            } finally {
                try {
                    Platform.runLater(this::loadBreakdown);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        };

        future_breakdown = preemptiveExecutor.submit(wrappedTask);

    }

    private Integer getCurrentFilter() {
        return ageFilter + incomeFilter + genderFilter + contextFilter;
    }

    private String getGranularityString() {
        Integer granularity = DBQuery.getGranularity();
        return (granularity == DBQuery.GRANULARITY_MONTH)
                ? "Month"
                : (granularity == DBQuery.GRANULARITY_DAY)
                ? "Day"
                : "Hour";
    }

    @FXML
    private void applyFilters() {

        int prev_filter = getCurrentFilter();

        String ageRange = ageRangeDropdown.getSelectionModel().getSelectedItem();
        String income = incomeRangeDropdown.getSelectionModel().getSelectedItem();
        String gender = genderDropdown.getSelectionModel().getSelectedItem();
        String context = contextDropdown.getSelectionModel().getSelectedItem();

        switch(ageRange) {
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

        switch(gender) {
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

        switch(income) {
            case "All":
                incomeFilter = 0;
                break;
            case "Low":
                incomeFilter = Filters.INCOME_LOW;
                break;
            case "Medium":
                contextFilter = Filters.INCOME_MEDIUM;
                break;
            case "High":
                contextFilter = Filters.INCOME_HIGH;
                break;
        }

        switch(context) {
            case "All":
                contextFilter = 0;
                break;
            case "Blog":
                contextFilter = Filters.CONTEXT_BLOG;
                break;
            case "News":
                contextFilter = Filters.CONTEXT_NEWS;
                break;
            case "Shopping":
                contextFilter = Filters.CONTEXT_SHOPPING;
                break;
            case "Social Media":
                contextFilter = Filters.CONTEXT_SOCIAL_MEDIA;
                break;
        }

        if (getCurrentFilter() != prev_filter) {
            wipeCaches();
            loadGraph(currentGraph.toString());
        }

    }

}