package org.soton.seg7.ad_analytics.model.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.soton.seg7.ad_analytics.view.Graph;
import org.soton.seg7.ad_analytics.view.MainView;

public class OverviewController {

    @FXML
    private TableView<Graph> graphTable;
    @FXML
    private TableColumn<Graph, String> graphTitleColumn;
    @FXML
    private Label graphTitleLabel;
    @FXML
    private LineChart lineChart;

    // Reference to the main application.
    private MainView mainView;

    public OverviewController() {
    }
    
    @FXML
    private void initialize() {
        graphTitleColumn.setCellValueFactory(cellData -> cellData.getValue().graphTitleProperty());
        showGraphDetails(null);
        // Listen for selection changes and show the person details when changed.
        graphTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showGraphDetails(newValue));
    }

    private void showGraphDetails(Graph graph){
    	if( graph != null){
    		graphTitleLabel.setText(graph.getGraphTitle());
    	}
    }

    public void setMainView(MainView mainView) {
        this.mainView = mainView;
        graphTable.setItems(mainView.getGraphData());
    }
}