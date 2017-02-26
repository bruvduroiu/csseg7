package controller;

import java.io.IOException;

import controller.model.Graph;
import controller.model.LineGraph;

import controller.view.OverviewController;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

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
    }

    public ObservableList<Graph> getGraphData() {
        return graphData;
    }

   

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        initRootLayout();

        showOverview();
    }

   
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainView.class.getResource("view/RootLayout.fxml"));
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
            loader.setLocation(MainView.class.getResource("view/Overview.fxml"));
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
}