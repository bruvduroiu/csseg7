package org.soton.seg7.ad_analytics.controller;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
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
    	if (clickLog == null || serverLog == null ||impressionLog == null) {
			System.out.println("There are som null files");
		} else if (Parser.isValidClickLog(clickLog) && Parser.isValidImpressionLog(impressionLog) && Parser.isValidServerLog(serverLog)) {
			DBHandler handler = null;
			try {
				handler = DBHandler.getDBConnection();
				handler.dropCollection("impression_log");
				handler.dropCollection("server_log");
				handler.dropCollection("click_log");
			} catch (MongoAuthException e) {
				e.printStackTrace();
			}
			Parser.parseCSV(clickLog);
			Parser.parseCSV(serverLog);
			Parser.parseCSV(impressionLog);
			// close the dialog.
			Node  source = (Node)  event.getSource();
			Stage stage  = (Stage) source.getScene().getWindow();
			stage.close();
    	} else {
			Alert invalidFileErrorBox = new Alert(Alert.AlertType.ERROR);
			invalidFileErrorBox.setTitle("Invalid File/s Error");
			invalidFileErrorBox.setHeaderText("Something wen't wrong!");
			invalidFileErrorBox.setContentText("One of your files is in invalid format.");
			invalidFileErrorBox.showAndWait();
    	}
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
