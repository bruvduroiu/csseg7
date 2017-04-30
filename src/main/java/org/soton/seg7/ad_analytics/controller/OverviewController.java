package org.soton.seg7.ad_analytics.controller;
import com.sun.javaws.progress.Progress;
import com.sun.media.jfxmedia.events.PlayerStateEvent;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import sun.tools.serialver.SerialVer;

import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

import javax.imageio.ImageIO;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.math.BigDecimal;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OverviewController {

    private enum Graph {
        HOME("Home"),
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
        BOUNCE_RATE("Bounce Rate"),
    	NUMBER_OF_UNIQUES("Unique clicks");

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
    private Graph defaultGraph;

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
    private XYChart.Series<String, Double> numberOfUniques;

    private ExecutorService preemptiveExecutor;
    private Future<Map<DateTime, Double>> future_num_impressions;
    private Future<Map<DateTime, Double>> future_cost_impressions;
    private Future<?> future_breakdown;
    private Map<String, String> breakdown_data;

    private Map<DateTime, Double> num_impressions;
    private Map<DateTime, Double> cost_impressions;

    @FXML
    private Button setDefaultGraph;

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
    
    @FXML
    private AnchorPane graphPane;


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

    @FXML
    private StackPane background;

    private VBox box;

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

        apply_button.setOnAction((ActionEvent event) -> initializeProgressIndicator());

        list = graphList.getItems();
        list.clear();
        list.add(Graph.HOME.toString());
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
        list.add(Graph.NUMBER_OF_UNIQUES.toString());

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
        readDefaultGraphPref();
        loadGraph(Graph.HOME.toString());
        loadPieChart();
        loadBreakdown();

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
                (observable, oldValue, newValue) -> {
                    initializeProgressIndicator();
                    loadGraph(newValue);
                });
        
        // Listen for time granularity change
        granularitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
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
        });

        toggleGroup.selectedToggleProperty().addListener(
        		(observable, oldValue, newValue) -> {
        			if(currentGraph.equals(Graph.BOUNCE_RATE) || currentGraph.equals(Graph.NUMBER_OF_BOUNCES)) {
        			    wipeCaches();
                        loadGraph(currentGraph.toString());
                    }
        				
        		});

    }

    private void readDefaultGraphPref(){
        try {
            BufferedReader reader = new BufferedReader(new FileReader("preferredGraph.txt"));
            String graphPref = reader.readLine();
                if (graphPref.equals("Total Cost"))
                    defaultGraph = Graph.TOTAL_COST;
                else if (graphPref.equals("Bounce Rate"))
                    defaultGraph = Graph.BOUNCE_RATE;
                else if (graphPref.equals("Click Cost Histogram"))
                    defaultGraph = Graph.CLICK_COST_HISTOGRAM;
                else if (graphPref.equals("Click Through Rate"))
                    defaultGraph = Graph.CLICK_THROUGH_RATE;
                else if (graphPref.equals("Cost per Acquisition"))
                    defaultGraph = Graph.COST_PER_ACQUISITION;
                else if (graphPref.equals("Cost per Click"))
                    defaultGraph = Graph.COST_PER_CLICK;
                else if (graphPref.equals("Cost per Thousand Impressions"))
                    defaultGraph = Graph.COST_PER_THOUSAND_IMPRESSIONS;
                else if (graphPref.equals("Number of Clicks"))
                    defaultGraph = Graph.NUMBER_OF_CLICKS;
                else if (graphPref.equals("Number of Conversions"))
                    defaultGraph = Graph.NUMBER_OF_CONVERSIONS;
                else if (graphPref.equals("Number of Impressions"))
                    defaultGraph = Graph.NUMBER_OF_IMPRESSIONS;
                else
                    defaultGraph = Graph.TOTAL_COST;
        } catch (IOException x) {
            defaultGraph = Graph.NUMBER_OF_BOUNCES;
        }
    }

    @FXML
    private void setDefaultGraph(ActionEvent event){
        defaultGraph = currentGraph;
        try{
            PrintWriter writer = new PrintWriter("preferredGraph.txt", "UTF-8");
            writer.println(currentGraph.toString());
            writer.close();
        } catch (IOException e) {
            // do something
        }
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
        } else if (graph.equals(Graph.HOME.toString())){
            loadGraph(defaultGraph.toString());
        }
        else if (graph.equals(Graph.NUMBER_OF_BOUNCES.toString())) {
            bounceSettingsLabel.setVisible(true);
            radioBounceTime.setVisible(true);
            radioBouncePage.setVisible(true);
            loadNumberOfBounces();
        }
        else if (graph.equals(Graph.NUMBER_OF_UNIQUES.toString()))
        	loadNumberOfUniques();
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
                    new AudienceSegment(">54", breakdown_data.get(">54"))
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

    private void initializeProgressIndicator() {
        ProgressIndicator pi = new ProgressIndicator();
        box = new VBox(pi);
        box.setAlignment(Pos.CENTER);
        background.setDisable(true);
        background.getChildren().add(box);
    }

    private void stopProgressIndicator() {
        background.setDisable(false);
        background.getChildren().remove(box);
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

            for (PieChart.Data d : pieChart.getData()) {
                Tooltip.install(d.getNode(), new Tooltip(d.getName() + ": £" + String.format("%.2f",d.getPieValue()/100)));
            }

        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    private void loadCostPerAcquisition() {
        currentGraph = Graph.COST_PER_ACQUISITION;
        histogram.setVisible(false);
        lineChart.setVisible(true);
        lineChart.setTitle("Cost per Acquisition / " + getGranularityString());

        Service<XYChart.Series> loaderService = new Service<XYChart.Series>() {
            @Override
            protected Task<XYChart.Series> createTask() {
                return new Task<XYChart.Series>() {
                    @Override
                    protected XYChart.Series call() throws Exception {
                        if (costPerAcquisition == null )
                            try {
                                Map<DateTime, Double> costClicks = DBQuery.getClickCostOverTime();
                                Map<DateTime, Double> numConversions = DBQuery.getNumConversions();
                                Map<DateTime, Double> costImpressions = future_cost_impressions.get();

                                Map<DateTime, Double> costPAcquisition = Stream.concat(costImpressions.keySet().stream(), costClicks.keySet().stream())
                                        .distinct()
                                        .collect(Collectors.toMap(k->k, k->((!numConversions.containsKey(k) || numConversions.get(k)==0) ? 0
                                                : ((costImpressions.containsKey(k) ? costImpressions.getOrDefault(k,0d) : 0)
                                                + ((costClicks.containsKey(k)) ? costClicks.getOrDefault(k,0d) : 0))/numConversions.getOrDefault(k,1d))));
                                ArrayList<DateTime> dates = new ArrayList<>(costPAcquisition.keySet());
                                Collections.sort(dates);

                                XYChart.Series<String, Double> series = new XYChart.Series<>();
                                for (DateTime day : dates)
                                    series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), costPAcquisition.get(day) / 100));

                                costPerAcquisition = series;
                            } catch (MongoAuthException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                            } catch (ExecutionException e) {
                            }

                        return costPerAcquisition;
                    }
                };
            }
        };

        loaderService.setOnSucceeded(e -> Platform.runLater(() -> {
            stopProgressIndicator();

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
        }));

        loaderService.restart();

    }

    private void loadBounceRate() {
        currentGraph = Graph.BOUNCE_RATE;
        histogram.setVisible(false);
        lineChart.setVisible(true);
        lineChart.setTitle("Bounce Rate / " + getGranularityString());

        Service<XYChart.Series> loaderService = new Service<XYChart.Series>() {
            @Override
            protected Task<XYChart.Series> createTask() {
                return new Task<XYChart.Series>() {
                    @Override
                    protected XYChart.Series call() throws Exception {
                        if (bounceRate == null)
                            try {
                                XYChart.Series<String, Double> series = new XYChart.Series<>();
                                Map<DateTime, Double> bncRate;
                                if (radioBounceTime.isSelected())
                                    bncRate = DBQuery.getBounceRateByTime();
                                else
                                    bncRate = DBQuery.getBounceRateByPage();
                                ArrayList<DateTime> dates = new ArrayList<>(bncRate.keySet());
                                Collections.sort(dates);

                                for (DateTime day : dates)
                                    series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), bncRate.get(day)));


                                bounceRate = series;

                            } catch (MongoAuthException e) {
                                e.printStackTrace();
                            }
                        return bounceRate;
                    }
                };
            }
        };

        loaderService.setOnSucceeded(e -> Platform.runLater(() -> {
           stopProgressIndicator();

            lineChart.getData().clear();
            lineChart.getData().add(loaderService.getValue());

            for (XYChart.Series<String, Double> s : lineChart.getData()) {
                for (XYChart.Data<String, Double> d : s.getData()) {
                    Tooltip.install(d.getNode(), new Tooltip("Date: " +
                            d.getXValue() + "\n" +
                            "Bounces Rate: " + Math.floor(d.getYValue() * 100) / 100));

                    //Adding class on hover
                    d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add("onHover"));

                    //Removing class on exit
                    d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove("onHover"));
                }
            }
        }));

        loaderService.restart();
    }

    private void loadNumberOfBounces() {
        currentGraph = Graph.NUMBER_OF_BOUNCES;
        histogram.setVisible(false);
        lineChart.setVisible(true);
        lineChart.setTitle("Number of Bounces / " + getGranularityString());


        Service<XYChart.Series> loaderService = new Service<XYChart.Series>() {
            @Override
            protected Task<XYChart.Series> createTask() {
                return new Task<XYChart.Series>() {
                    @Override
                    protected XYChart.Series call() throws Exception {
                        if (numberOfBounces == null)
                            try {
                                XYChart.Series<String,Double> series = new XYChart.Series<>();
                                Map<DateTime, Double> numBounces;
                                if(radioBounceTime.isSelected())
                                    numBounces = DBQuery.getNumBouncesByTime();
                                else
                                    numBounces = DBQuery.getNumBouncesByPage();
                                ArrayList<DateTime> dates = new ArrayList<>(numBounces.keySet());
                                Collections.sort(dates);

                                for (DateTime day : dates)
                                    series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), numBounces.get(day)));


                                numberOfBounces = series;

                            }
                            catch (MongoAuthException e) {
                                e.printStackTrace();
                            }
                        return numberOfBounces;
                    }
                };
            }
        };


        loaderService.setOnSucceeded(e -> Platform.runLater(() -> {
            stopProgressIndicator();
            lineChart.getData().clear();
            lineChart.getData().add(loaderService.getValue());

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
        }));

        loaderService.restart();
    }

    private void loadCostPerThousandImpressions() {
        currentGraph = Graph.COST_PER_THOUSAND_IMPRESSIONS;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        lineChart.setTitle("Cost per Thousand Impressions / Day");

        Service<XYChart.Series> loaderService = new Service<XYChart.Series>() {
            @Override
            protected Task<XYChart.Series> createTask() {
                return new Task<XYChart.Series>() {
                    @Override
                    protected XYChart.Series call() throws Exception {
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

                                costThousandImpressions = series;
                            } catch (MongoAuthException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                            } catch (ExecutionException e) {
                            }


                        return costThousandImpressions;
                    }
                };
            }
        };

        loaderService.setOnSucceeded(e -> Platform.runLater(() -> {
            lineChart.getData().clear();
            lineChart.getData().add(costThousandImpressions);

            for (XYChart.Series<String, Double> s : lineChart.getData()) {
                for (XYChart.Data<String, Double> d : s.getData()) {
                    Tooltip.install(d.getNode(), new Tooltip("Date: " +
                            d.getXValue() + "\n" +
                            "Cost: £" + Math.floor(d.getYValue() * 100) / 100));

                    //Adding class on hover
                    d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add("onHover"));

                    //Removing class on exit
                    d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove("onHover"));
                }
            }
        }));

        loaderService.restart();

    }

    private void loadTotalCost() {
        currentGraph = Graph.TOTAL_COST;
        histogram.setVisible(false);
        lineChart.setVisible(true);
        lineChart.setTitle("Total Cost / Day");

        Service<XYChart.Series> loaderService = new Service<XYChart.Series>() {
            @Override
            protected Task<XYChart.Series> createTask() {
                return new Task<XYChart.Series>() {
                    @Override
                    protected XYChart.Series call() throws Exception {
                        if (totalCost == null)
                            try {
                                XYChart.Series<String, Double> series = new XYChart.Series<>();
                                Map<DateTime, Double> totalCostOverTime;
                                if ((totalCostOverTime = DBQuery.getTotalCostOverTime(getCurrentFilter())).size() == 0)
                                    totalCost = new XYChart.Series<>();
                                ArrayList<DateTime> dates = new ArrayList<>(totalCostOverTime.keySet());
                                Collections.sort(dates);

                                for (DateTime day : dates)
                                    series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), totalCostOverTime.get(day) / 100));

                                totalCost = series;
                            } catch (MongoAuthException e) {
                                e.printStackTrace();
                            }

                        return totalCost;
                    }
                };
            }
        };

        loaderService.setOnSucceeded(e -> Platform.runLater(() -> {
            stopProgressIndicator();

            lineChart.getData().clear();
            lineChart.getData().add(loaderService.getValue());

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
        }));

        loaderService.restart();
    }

    private void loadNumberOfConversions() {
        currentGraph = Graph.NUMBER_OF_CONVERSIONS;
        histogram.setVisible(false);
        lineChart.setVisible(true);
        lineChart.setTitle("Number of Conversions / Day");

        Service<XYChart.Series> loaderService = new Service<XYChart.Series>() {
            @Override
            protected Task<XYChart.Series> createTask() {
                return new Task<XYChart.Series>() {
                    @Override
                    protected XYChart.Series call() throws Exception {
                        if (numberOfConversions == null)
                            try {
                                XYChart.Series<String, Double> series = new XYChart.Series<>();
                                Map<DateTime, Double> conversionsMap = DBQuery.getNumConversions();
                                ArrayList<DateTime> days = new ArrayList<>(conversionsMap.keySet());
                                Collections.sort(days);

                                for (DateTime day : days)
                                    series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), conversionsMap.get(day)));

                                numberOfConversions = series;
                            }
                            catch (MongoAuthException e) {
                                e.printStackTrace();

                            }
                        return numberOfConversions;
                    }
                };
            }

        };

        loaderService.setOnSucceeded(e -> Platform.runLater(() -> {
            stopProgressIndicator();
            lineChart.getData().clear();
            lineChart.getData().add(loaderService.getValue());

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
        }));

        loaderService.restart();
    }

    private void loadClickThroughRate() {
        currentGraph = Graph.CLICK_THROUGH_RATE;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        lineChart.setTitle("Click Through Rate / Day");

        Service<XYChart.Series> loaderService = new Service<XYChart.Series>() {
            @Override
            protected Task<XYChart.Series> createTask() {
                return new Task<XYChart.Series>() {
                    @Override
                    protected XYChart.Series call() throws Exception {
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

                                clickThroughRate = series;
                            }
                            catch (MongoAuthException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }

                        return clickThroughRate;
                    }
                };
            }
        };

        loaderService.setOnSucceeded(e -> Platform.runLater(() -> {
            stopProgressIndicator();
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
        }));

        loaderService.restart();
    }

    private void loadNumberOfClicks() {
        currentGraph = Graph.NUMBER_OF_CLICKS;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        lineChart.setTitle("Number of Clicks / Day");
        Service<XYChart.Series> loaderService = new Service<XYChart.Series>() {
            @Override
            protected Task<XYChart.Series> createTask() {
                return new Task<XYChart.Series>() {
                    @Override
                    protected XYChart.Series call() throws Exception {
                        XYChart.Series<String, Double> series = new XYChart.Series<>();

                        try {
                            Map<DateTime, Double> numClicks = DBQuery.getNumClicks();
                            ArrayList<DateTime> days = new ArrayList<>(numClicks.keySet());
                            Collections.sort(days);

                            for (DateTime day : days)
                                series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), numClicks.get(day)));

                            numberOfClicks = series;

                        }
                        catch (MongoAuthException e) {
                            e.printStackTrace();
                        }
                        return numberOfClicks;
                    }
                };
            }
        };

        loaderService.setOnSucceeded(e -> Platform.runLater(() -> {
            stopProgressIndicator();

            lineChart.getData().clear();
            lineChart.getData().add(loaderService.getValue());

            for (XYChart.Series<String, Double> s : lineChart.getData()) {
                for (XYChart.Data<String, Double> d : s.getData()) {
                    Tooltip.install(d.getNode(), new Tooltip("Date: " +
                            d.getXValue() + "\n" +
                            "Clicks: " + Math.floor(d.getYValue() * 100) / 100));

                    //Adding class on hover
                    d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add("onHover"));

                    //Removing class on exit
                    d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove("onHover"));
                }
            }
        }));

        loaderService.restart();
    }

    private void loadNumberOfImpressions() {
        currentGraph = Graph.NUMBER_OF_IMPRESSIONS;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        lineChart.setTitle("Number of Impressions / Day");

        Service<XYChart.Series> loaderService = new Service<XYChart.Series>() {
            @Override
            protected Task<XYChart.Series> createTask() {
                return new Task<XYChart.Series>() {
                    @Override
                    protected XYChart.Series call() throws Exception {
                        XYChart.Series<String, Double> series = new XYChart.Series<>();
                        if (numberOfImpressions == null)
                            try {
                                Map<DateTime, Double> numOfImpressions = future_num_impressions.get();
                                ArrayList<DateTime> days = new ArrayList<>(numOfImpressions.keySet());
                                Collections.sort(days);

                                for (DateTime day : days)
                                    series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), numOfImpressions.get(day)));


                                numberOfImpressions = series;
                            } catch (InterruptedException e) {
                            } catch (ExecutionException e) {
                            }

                        return numberOfImpressions;
                    }
                };
            }
        };

        loaderService.setOnSucceeded(event -> Platform.runLater(() -> {
            stopProgressIndicator();

            lineChart.getData().clear();
            lineChart.getData().add(loaderService.getValue());
            for (XYChart.Series<String, Double> s : lineChart.getData()) {
                for (XYChart.Data<String, Double> d : s.getData()) {
                    Tooltip.install(d.getNode(), new Tooltip("Date: " +
                            d.getXValue().toString() + "\n" +
                            "Impressions: " + Math.floor(d.getYValue() * 100) / 100));

                    //Adding class on hover
                    d.getNode().setOnMouseEntered(e -> d.getNode().getStyleClass().add("onHover"));

                    //Removing class on exit
                    d.getNode().setOnMouseExited(e -> d.getNode().getStyleClass().remove("onHover"));
                }
            }
        }));

        loaderService.start();


    }

    private void loadCostPerClick() {
        currentGraph = Graph.COST_PER_CLICK;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        lineChart.setTitle("Cost per Click / Day");

        Service<XYChart.Series> loaderService = new Service<XYChart.Series>() {
            @Override
            protected Task<XYChart.Series> createTask() {
                return new Task<XYChart.Series>() {
                    @Override
                    protected XYChart.Series call() throws Exception {
                        if (costPerClick == null) {
                            XYChart.Series<String, Double> series = new XYChart.Series<>();

                            try {
                                Map<DateTime, Double> cPerClk = DBQuery.getClickCostOverTime();
                                ArrayList<DateTime> days = new ArrayList<>(cPerClk.keySet());
                                Collections.sort(days);

                                for (DateTime day : days)
                                    series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), cPerClk.get(day)/100));
                                costPerClick = series;

                            }
                            catch (MongoAuthException e) {
                                e.printStackTrace();
                            }
                        }

                        return costPerClick;
                    }
                };
            }
        };

        loaderService.setOnSucceeded(event -> Platform.runLater(() -> {
            stopProgressIndicator();

            lineChart.getData().clear();
            lineChart.getData().add(loaderService.getValue());

            for (XYChart.Series<String, Double> s : lineChart.getData()) {
                for (XYChart.Data<String, Double> d : s.getData()) {
                    Tooltip.install(d.getNode(), new Tooltip("Date: " +
                            d.getXValue().toString() + "\n" +
                            "Cost: £" + Math.floor(d.getYValue() * 100) / 100));

                    //Adding class on hover
                    d.getNode().setOnMouseEntered(e -> d.getNode().getStyleClass().add("onHover"));

                    //Removing class on exit
                    d.getNode().setOnMouseExited(e -> d.getNode().getStyleClass().remove("onHover"));
                }
            }
        }));

        loaderService.restart();
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
    
    private void loadNumberOfUniques() {
        currentGraph = Graph.NUMBER_OF_UNIQUES;
        histogram.setVisible(false);
        lineChart.setVisible(true);
        lineChart.setTitle("Number of clicks from unique visitors");

        Service<XYChart.Series> loaderService = new Service<XYChart.Series>() {
            @Override
            protected Task<XYChart.Series> createTask() {
                return new Task<XYChart.Series>() {
                    @Override
                    protected XYChart.Series call() throws Exception {
                        if (numberOfUniques == null)
                            try {
                                XYChart.Series<String, Double> series = new XYChart.Series<>();
                                Map<DateTime, Double> numUniques = DBQuery.getNumUniques();
                                ArrayList<DateTime> days = new ArrayList<>(numUniques.keySet());
                                Collections.sort(days);

                                for (DateTime day : days)
                                    series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), numUniques.get(day)));
                                numberOfUniques = series;

                            } catch (MongoAuthException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                        return numberOfUniques;
                    }
                };
            }
        };


        loaderService.setOnSucceeded(e -> Platform.runLater(() -> {
            stopProgressIndicator();

            lineChart.getData().clear();
            lineChart.getData().add(loaderService.getValue());
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
        }));

        loaderService.restart();
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

    //function that handles pressing of Export button
    @FXML
    protected void handleExportButtonAction(ActionEvent event) {
    	WritableImage image = splitPane2.snapshot(new SnapshotParameters(), null);


    	FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        File cFile = fileChooser.showSaveDialog(stage);
        File file = new File(cFile.getAbsolutePath()+".png");
        if (file != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image,
                    null), "png", file);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
    
    //function that handles pressing of ExportAll button
    @FXML
    protected void handleExportAllButtonAction(ActionEvent event){
    	FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        File file = fileChooser.showSaveDialog(stage);
    	Document document = new Document(PageSize.A4, 20, 20, 20, 20); 
		try {
			PdfWriter.getInstance(document, new FileOutputStream(file));
		} catch (FileNotFoundException | DocumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		document.open();
		WritableImage image = null;
    	BufferedImage png = null;
    	ByteArrayOutputStream baos = null;
    	Image iTextImage = null;
    	for(String x : list){
    		loadGraph(x);
    		
        	image = splitPane2.snapshot(new SnapshotParameters(), null);
        	png = SwingFXUtils.fromFXImage(image, null);
        	baos = new ByteArrayOutputStream();
        	try {
				ImageIO.write(png, "png", baos);
				iTextImage = Image.getInstance(baos.toByteArray());
				iTextImage.scaleToFit(PageSize.A4.getWidth(), PageSize.A4.getHeight());
	        	document.add(iTextImage);
			} catch (IOException | DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	document.close();
    }
    
    @FXML
    protected void handlePrintButtonAction(ActionEvent event) {
    	WritableImage image = splitPane2.snapshot(new SnapshotParameters(), null);

        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(new Printable() {
          @Override
          public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            // Get the upper left corner that it printable
            int x = (int) Math.ceil(pageFormat.getImageableX());
            int y = (int) Math.ceil(pageFormat.getImageableY());
            if (pageIndex != 0) {
              return NO_SUCH_PAGE;
            }
            graphics.drawImage(bufferedImage, x, y, bufferedImage.getWidth(), bufferedImage.getHeight(), null);
            return PAGE_EXISTS;
          }
        });
        try {
          printJob.print();
        } catch (PrinterException e1) {
          e1.printStackTrace();
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

        if (breakdown_data == null && future_breakdown == null)
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
                incomeFilter = Filters.INCOME_MEDIUM;
                break;
            case "High":
                incomeFilter = Filters.INCOME_HIGH;
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
