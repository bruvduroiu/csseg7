package org.soton.seg7.ad_analytics.controller;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.soton.seg7.ad_analytics.model.DBHandler;
import org.soton.seg7.ad_analytics.model.Parser;
import org.soton.seg7.ad_analytics.model.exceptions.MongoAuthException;

//controller of file loader
//TODO pass overview controller and pass chosen files in handleSartLoadingButtonAction() function
//TODO make tests if user chose right files(correct format)
public class FileLoaderController {
	
	private Stage stage;
	
	@FXML 
	private VBox bx;
	@FXML 
	private StackPane root;
	
	//variables for labels
	@FXML
	private Label ClickLogT;
	@FXML
	private Label ServerLogT;
	@FXML
	private Label ImpressionLogT;
	
	File clickLog;
	File serverLog;
	File impressionLog;
	
	ProgressIndicator pi = new ProgressIndicator();
    VBox box = new VBox(pi);
	
	public void init(Stage primaryStage){
		this.stage = stage;
	}
	
	//function that handles pressing of Click Log button
    @FXML
	protected void handleLoadClickButtonAction(ActionEvent event) {
        clickLog = fileChooser("Choose click log file: ");
        ClickLogT.setText(""+clickLog.getName());
    }
    
    //function that handles pressing of Server Log button
    @FXML
	protected void handleLoadServerButtonAction(ActionEvent event) {
        serverLog = fileChooser("Choose server log file: ");
        ServerLogT.setText(""+serverLog.getName());
    }
    
    //function that handles pressing of Impression Log button
    @FXML
	protected void handleLoadImpressionButtonAction(ActionEvent event) {
        impressionLog = fileChooser("Choose impression log file: ");
        ImpressionLogT.setText(""+impressionLog.getName());
    }
    
    //TODO pass values to overview controller
    @FXML
	protected void handleStartLoadingButtonAction(ActionEvent event) {
    	
    	runProgInd();
    	
    	
    	if (clickLog == null || serverLog == null ||impressionLog == null) {

			
    		Alert nullFilesErrorBox = new Alert(Alert.AlertType.ERROR);
			nullFilesErrorBox.setTitle("Missing File/s Error");
			nullFilesErrorBox.setHeaderText("Something wen't wrong!");
			nullFilesErrorBox.setContentText("Please upload an impression log, server log and click log.");		
			nullFilesErrorBox.showAndWait();
			
			endProgInd();
			
		} else if (Parser.isValidClickLog(clickLog) && Parser.isValidImpressionLog(impressionLog) && Parser.isValidServerLog(serverLog)) {
			
			DBHandler handler = null;
			try {
				handler = DBHandler.getDBConnection();
				handler.dropCollection("impression_log");
				handler.dropCollection("server_log");
				handler.dropCollection("click_log");
				handler.dropCollection("impression_data");
				handler.dropCollection("server_data");
				handler.dropCollection("click_data");
			} catch (MongoAuthException e) {
				e.printStackTrace();
			}

			
			Parser.parseCSV(clickLog);
			Parser.parseCSV(serverLog);
			Parser.parseCSV(impressionLog);
			
			endProgInd();
			
			
			// close the dialog.
			Node  source = (Node)  event.getSource();
			Stage stage  = (Stage) source.getScene().getWindow();
			
			stage.close();
    	} else {
    		
    		Alert invalidFileErrorBox = new Alert(Alert.AlertType.ERROR);
			invalidFileErrorBox.setTitle("Invalid File/s Error");
			invalidFileErrorBox.setHeaderText("Something wen't wrong!");

			invalidFileErrorBox.setContentText("Some files are in invalid format: ");
			if (!Parser.isValidClickLog(clickLog)) invalidFileErrorBox.setContentText(invalidFileErrorBox.getContentText() + " Click Log, ");
			if (!Parser.isValidServerLog(serverLog)) invalidFileErrorBox.setContentText(invalidFileErrorBox.getContentText() + " Server Log, ");
			if (!Parser.isValidImpressionLog(impressionLog)) invalidFileErrorBox.setContentText(invalidFileErrorBox.getContentText() + " Impressions Log, ");

			invalidFileErrorBox.showAndWait();
			
			endProgInd();
    	}
    }
    
    
    protected void runProgInd(){
        box.setAlignment(Pos.CENTER);
        // Grey Background
        bx.setDisable(true);
        root.getChildren().add(box);
    }
    
    protected void endProgInd(){
        bx.setDisable(false);
        root.getChildren().remove(box);
    }
    
    //runs file chooser
    //takes title of file explorer window as argument
    public File fileChooser(String title){
    	
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle(title);
    	fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
            ); 
    	File file = fileChooser.showOpenDialog(stage);
    	
    	if(file != null){
    		System.out.println("Chosen file: " + file);
    	}

    	return file;
    }

}
