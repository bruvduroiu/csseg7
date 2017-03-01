package org.soton.seg7.ad_analytics.controller;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FileLoaderController {
	
	private Stage stage;
	
	public void init(Stage primaryStage){
		this.stage = stage;
	}
	
	//function that handles pressing of Click Log button
    @FXML protected void handleLoadClickButtonAction(ActionEvent event) {
        System.out.println("click");
        File clickLog = fileChooser("Choose click log file: ");
    }
    
    //function that handles pressing of Server Log button
    @FXML protected void handleLoadServerButtonAction(ActionEvent event) {
        System.out.println("server");
        File serverLog = fileChooser("Choose server log file: ");

    }
    
    //function that handles pressing of Impression Log button
    @FXML protected void handleLoadImpressionButtonAction(ActionEvent event) {
        System.out.println("impression");
        File impressionLog = fileChooser("Choose impression log file: ");

    }
    
    public File fileChooser(String title){
    	
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Open Resource File");
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
