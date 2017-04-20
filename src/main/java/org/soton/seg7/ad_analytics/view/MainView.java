package org.soton.seg7.ad_analytics.view;

//import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.joda.time.DateTime;
import org.soton.seg7.ad_analytics.controller.OverviewController;
import org.soton.seg7.ad_analytics.model.DBHandler;
import org.soton.seg7.ad_analytics.model.DBQuery;
import org.soton.seg7.ad_analytics.model.Parser;
import org.soton.seg7.ad_analytics.model.exceptions.MongoAuthException;
import sun.applet.Main;

public class MainView extends Application {

    private static Stage primaryStage;
    private BorderPane rootLayout;

    private static final boolean DEBUG_ON = true;

    private ObservableList<Graph> graphData = FXCollections.observableArrayList();

    public MainView() {
    }

    public ObservableList<Graph> getGraphData() {
        return graphData;
    }

    @Override
    public void start(Stage primaryStage) {
//    	this.loadTestData();
        MainView.primaryStage = primaryStage;

        initRootLayout();
        if (DEBUG_ON) {
            loadTestData();
        } else {
            showLoadStage();
        }
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

            primaryStage.setOnCloseRequest(e -> System.exit(0));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   
    public static Stage getPrimaryStage() {
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
            handler.dropCollection("impression_data");
            handler.dropCollection("server_log");
            handler.dropCollection("server_data");
            handler.dropCollection("click_log");
            handler.dropCollection("click_data");
            Parser.parseCSV(impressionsFile);
            Parser.parseCSV(serverFile);
            Parser.parseCSV(clickFile);
            
            //getting data from database
            Map<DateTime, Double> clickCount = DBQuery.getNumClicks();
            
            //printing data
            for (Map.Entry<DateTime, Double> entryMap : clickCount.entrySet()){
            	DateTime name = entryMap.getKey();
            	Double data = entryMap.getValue();
                System.out.println("Entry with name: "+name+" has key: "+name+ "  and value: " + data);
            }

        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    public static void showLoadStage(){
        try {
            // Load overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainView.class.getResource("FileLoaderView.fxml"));
            AnchorPane fileLoad = loader.load();

            Stage loadFileStage = new Stage();
            loadFileStage.setTitle("Load files");
            //window modal -> you can't acces another components in overview
            loadFileStage.initModality(Modality.WINDOW_MODAL);
            loadFileStage.initOwner(MainView.primaryStage);
            Scene scene = new Scene(fileLoad);
            loadFileStage.setScene(scene);
            loadFileStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}