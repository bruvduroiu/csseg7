package org.soton.seg7.ad_analytics.view;
import java.io.IOException;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.soton.seg7.ad_analytics.controller.OverviewController;
import org.soton.seg7.ad_analytics.controller.FileLoaderController;
public class MainView extends Application {
    private static Stage primaryStage;
    private BorderPane rootLayout;

    public MainView() {

    }
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initRootLayout();
        showOverview();
        showLoadStage();
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
    
    //shows window for loading files
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
            loadFileStage.initOwner(primaryStage);
            Scene scene = new Scene(fileLoad);
            loadFileStage.setScene(scene);
            loadFileStage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}