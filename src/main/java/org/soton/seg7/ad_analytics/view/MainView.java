package org.soton.seg7.ad_analytics.view;

//import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.soton.seg7.ad_analytics.controller.OverviewController;
import org.soton.seg7.ad_analytics.model.DBHandler;
import org.soton.seg7.ad_analytics.model.DBQuery;
import org.soton.seg7.ad_analytics.model.Parser;
import org.soton.seg7.ad_analytics.model.exceptions.MongoAuthException;

public class MainView extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;

    private ObservableList<Graph> graphData = FXCollections.observableArrayList();

    public MainView() {
        
    	graphData.add(new LineGraph("Click-through-rate"));
    	graphData.add(new LineGraph("Cost-per-click"));
    	graphData.add(new LineGraph("Number of Clicks"));
    	graphData.add(new LineGraph("Number of Conversions"));
        graphData.add(new LineGraph("Number of Impressions"));
        graphData.add(new LineGraph("Total Cost"));
        graphData.add(new BarGraph("Click Cost Histogram"));
    }

    public ObservableList<Graph> getGraphData() {
        return graphData;
    }

    @Override
    public void start(Stage primaryStage) {
    	this.loadTestData();
        this.primaryStage = primaryStage;

        initRootLayout();

        showOverview();
    }

    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainView.class.getResource("RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   
    public void showOverview() {
        try {
            // Load overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainView.class.getResource("Overview.fxml"));
            AnchorPane overview = (AnchorPane) loader.load();
            
         // Give the controller access to the main app.
            OverviewController controller = loader.getController();
            controller.setMainView(this);

            // Set overview into the center of root layout.
            rootLayout.setCenter(overview);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    
    //function that loads data to database and gets clicks data, and prints the data with description
    // format of printed data: Entry with name: 2015-01-01 has key: 12  and value: 94
    public void loadTestData(){
    	File clickFile = new File(new File("").getAbsolutePath().toString() + "/static/analytics_csv/click_log.csv");
        File impressionsFile = new File(new File("").getAbsolutePath().toString() + "/static/analytics_csv/impression_log.csv");
        File serverFile = new File(new File("").getAbsolutePath().toString() + "/static/analytics_csv/server_log.csv");

    	DBHandler handler;
    	try{
    		//loading data
            handler = DBHandler.getDBConnection();
            handler.dropCollection("impression_log");            
            handler.dropCollection("server_log");
            handler.dropCollection("click_log");
            Parser.parseCSV(impressionsFile);
            Parser.parseCSV(serverFile);
            Parser.parseCSV(clickFile);
            
            //getting data from database
            Map<String, Map<String, Double>> clickCount = DBQuery.getNumClicks();
            
            //printing data
            for (Map.Entry<String, Map<String, Double>> entryMap : clickCount.entrySet()){
            	String name = entryMap.getKey();
            	Map<String, Double> data = entryMap.getValue();
            	for(Map.Entry<String, Double> entry : data.entrySet()){
            		System.out.println("Entry with name: "+name+" has key: "+entry.getKey()+ "  and value: " + entry.getValue());
            	}
            }

        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }
}