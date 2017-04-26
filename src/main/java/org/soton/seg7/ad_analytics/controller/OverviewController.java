package org.soton.seg7.ad_analytics.controller;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import org.joda.time.DateTime;
import org.soton.seg7.ad_analytics.model.DBHandler;
import org.soton.seg7.ad_analytics.model.DBQuery;
import org.soton.seg7.ad_analytics.model.Filters;
import org.soton.seg7.ad_analytics.model.Parser;
import org.soton.seg7.ad_analytics.model.exceptions.MongoAuthException;
import org.soton.seg7.ad_analytics.view.MainView;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.math.BigDecimal;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

    private static Map<DateTime, Double> queryData;

    private int ageFilter;
    private int incomeFilter;
    private int genderFilter;
    private int contextFilter;
    private int currentFilter = ageFilter + incomeFilter + genderFilter + contextFilter;

    @FXML
    private AnchorPane background;

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

    ProgressIndicator pi = new ProgressIndicator();
    VBox box = new VBox(pi);

    // Reference to the main application.
    private MainView mainView;

    public OverviewController() {
    }

    @FXML
    private void initialize() {
        ageFilter = 0;
        incomeFilter = 0;
        genderFilter = 0;

        final ToggleGroup toggleGroup = new ToggleGroup();

        queryData = new HashMap<>();

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
            loadGraph(currentGraph.toString());
        });

        endDate.valueProperty().addListener((ov, oldVal, newVal) -> {
            if (startDate.getValue() != null && newVal.compareTo(startDate.getValue())>0)
                DBQuery.setDateRange(DateTime.parse(startDate.getValue().toString()), DateTime.parse(newVal.toString()));
            else
                DBQuery.setDateRange(null, DateTime.parse(newVal.toString()));
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
        
        // Context Dropdown
        
        contextDropdown.getItems().addAll(
        		"All",
        		"Blog",
        		"News",
        		"Shopping",
        		"Social Media"
        		);
        
        contextDropdown.getSelectionModel().selectFirst();
        
        contextDropdown.valueProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue ov, String oldVal, String newVal) {
            	switch(newVal) {
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
                else
                    changeGranularity(newValue);
            }
        });

        toggleGroup.selectedToggleProperty().addListener(
        		(observable, oldValue, newValue) -> {
        			if(currentGraph.equals(Graph.BOUNCE_RATE))
        				loadGraph(currentGraph.toString());
        				
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

        runProgInd();

    	radioBounceTime.setVisible(false);
    	radioBouncePage.setVisible(false);
        bounceSettingsLabel.setVisible(false);

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
        else if (graph.equals(Graph.BOUNCE_RATE.toString())){
            bounceSettingsLabel.setVisible(true);
            radioBounceTime.setVisible(true);
            radioBouncePage.setVisible(true);
            loadBounceRate();
        }
    }

    protected void runProgInd() {
        box.setAlignment(Pos.CENTER);
        // Grey Background
        background.setDisable(true);
        background.getChildren().add(box);

    }

    protected void endProgInd() {
        background.setDisable(false);
        background.getChildren().remove(box);
    }

    private void queryDB(Callable<Boolean> query) {
        //Submits Callable<Boolean> thread to a Future thread
        new Thread(new Runnable() {
            public void run() {
                try {
                    Executors.newSingleThreadExecutor().submit(query).get();
                }catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                } finally {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            endProgInd();
                        }
                    });
                }
            }
        }).start();
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

        queryDB(new Callable<Boolean>() {
            public Boolean call() {
                try {
                    queryData = DBQuery.getTotalCostOverTime(getCurrentFilter());
                } catch (MongoAuthException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

        ArrayList<DateTime> dates = new ArrayList<>(queryData.keySet());
        Collections.sort(dates);

        for (DateTime day : dates)
            series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), queryData.get(day) / 100));

        lineChart.getData().clear();
        lineChart.getData().add(series);
    }


    private void loadBounceRate() {
        currentGraph = Graph.BOUNCE_RATE;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        XYChart.Series<String,Double> series = new XYChart.Series<>();
        lineChart.setTitle("Bounce Rate / " + getGranularityString());

        queryDB(new Callable<Boolean>() {
            public Boolean call() {
                try {
                    if(radioBounceTime.isSelected())
                        queryData = DBQuery.getBounceRateByTime();
                    else
                        queryData = DBQuery.getBounceRateByPage();
                } catch (MongoAuthException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

        ArrayList<DateTime> dates = new ArrayList<>(queryData.keySet());
        Collections.sort(dates);

        for (DateTime day : dates)
            series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), queryData.get(day)));

        lineChart.getData().clear();
        lineChart.getData().add(series);
    }


    private void loadCostPerThousandImpressions() {
        currentGraph = Graph.COST_PER_THOUSAND_IMPRESSIONS;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineChart.setTitle("Cost per Thousand Impressions / Day");

        queryDB(new Callable<Boolean>() {
            public Boolean call() {
                try {
                    queryData = DBQuery.getCostPerThousandImpressionsOverTime(getCurrentFilter()/100);
                } catch (MongoAuthException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

        ArrayList<DateTime> dates = new ArrayList<>(queryData.keySet());
        Collections.sort(dates);

        for (DateTime day : dates)
            series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), queryData.get(day)));

        lineChart.getData().clear();
        lineChart.getData().add(series);
    }

    private void loadTotalCost() {
        currentGraph = Graph.TOTAL_COST;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineChart.setTitle("Total Cost / Day");

        queryDB(new Callable<Boolean>() {
            public Boolean call() {
                try {
                    queryData = DBQuery.getTotalCostOverTime(getCurrentFilter());
                } catch (MongoAuthException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

        ArrayList<DateTime> dates = new ArrayList<>(queryData.keySet());
        Collections.sort(dates);

        for (DateTime day : dates)
            series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), queryData.get(day)/100));

        lineChart.getData().clear();
        lineChart.getData().add(series);
    }

    private void loadNumberOfConversions() {

        currentGraph = Graph.NUMBER_OF_CONVERSIONS;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineChart.setTitle("Number of Conversions / Day");

        queryDB(new Callable<Boolean>() {
            public Boolean call() {
                try {
                    queryData = DBQuery.getNumConversions();
                } catch (MongoAuthException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

        ArrayList<DateTime> days = new ArrayList<>(queryData.keySet());
        Collections.sort(days);

        for (DateTime day : days)
            series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), queryData.get(day)));

        lineChart.getData().clear();
        lineChart.getData().add(series);
    }

    private void loadClickThroughRate() {
        currentGraph = Graph.CLICK_THROUGH_RATE;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineChart.setTitle("Click Through Rate / Day");

        queryDB(new Callable<Boolean>() {
            public Boolean call() {
                try {
                    queryData = DBQuery.getCTROverTime(getCurrentFilter());
                } catch (MongoAuthException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

        ArrayList<DateTime> days = new ArrayList<>(queryData.keySet());
        Collections.sort(days);

        for (DateTime day : days)
            series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), queryData.get(day)));

        lineChart.getData().clear();
        lineChart.getData().add(series);
    }

    private void loadNumberOfClicks() {
        currentGraph = Graph.NUMBER_OF_CLICKS;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineChart.setTitle("Number of Clicks / Day");

        queryDB(new Callable<Boolean>() {
            public Boolean call() {
                try {
                    queryData = DBQuery.getNumClicks();
                } catch (MongoAuthException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

        ArrayList<DateTime> days = new ArrayList<>(queryData.keySet());
        Collections.sort(days);

        for (DateTime day : days)
            series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), queryData.get(day)));

        lineChart.getData().clear();
        lineChart.getData().add(series);
    }

    private void loadNumberOfImpressions() {
        currentGraph = Graph.NUMBER_OF_IMPRESSIONS;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineChart.setTitle("Number of Impressions / Day");

        queryDB(new Callable<Boolean>() {
            public Boolean call() {
                try {
                    queryData = DBQuery.getNumImpressions(getCurrentFilter());
                } catch (MongoAuthException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

        ArrayList<DateTime> days = new ArrayList<>(queryData.keySet());
        Collections.sort(days);

        for (DateTime day : days)
            series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), queryData.get(day)));

        lineChart.getData().clear();
        lineChart.getData().add(series);
    }

    private void loadCostPerClick() {
        currentGraph = Graph.COST_PER_CLICK;
        histogram.setVisible(false);
        lineChart.setVisible(true);

        XYChart.Series<String, Double> series = new XYChart.Series<>();
        lineChart.setTitle("Cost per Click / Day");

        queryDB(new Callable<Boolean>() {
            public Boolean call() {
                try {
                    queryData = DBQuery.getClickCostOverTime();
                } catch (MongoAuthException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

        ArrayList<DateTime> days = new ArrayList<>(queryData.keySet());
        Collections.sort(days);

        for (DateTime day : days)
            series.getData().add(new XYChart.Data<>(day.toString(DBQuery.getDateFormat()), queryData.get(day)/100));

        lineChart.getData().clear();
        lineChart.getData().add(series);
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

}