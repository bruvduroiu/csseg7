package org.soton.seg7.ad_analytics.controller;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.soton.seg7.ad_analytics.view.MainView;
public class OverviewController {
    @FXML
    private ListView<String> graphList;
    @FXML
    private Label graphTitleLabel;
    @FXML
    private ObservableList<String> list;

    // Reference to the main application.
    private MainView mainView;
    public OverviewController() {
    }

    @FXML
    private void initialize() {
        list = graphList.getItems();
        list.add("Cost per Click");
        list.add("Number of Impressions");
        list.add("Number of Clicks");
        list.add("Click through Rate");
        list.add("Number of Conversions");
        list.add("Total Cost");


        // Listen for selection changes and show the person details when changed.
        graphList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> loadGraph(newValue));

    }
    private void loadGraph(String graph){
        switch (graph) {
            case "Cost per Click":
                loadCostPerClick();
                break;
            case "Number of Imrpessions":
                loadNumberOfImpressions();
                break;
            case "Number of Clicks":
                loadNumberOfClicks();
                break;
            case "Click through Rate":
                loadClickThroughRate();
                break;
            case "NumberOfConversions":
                loadNumberOfConversions();
                break;
            case "Total Cost":
                loadTotalCost();
                break;
        }
    }
    private void loadTotalCost() {
        // TODO Auto-generated method stub

    }
    private void loadNumberOfConversions() {
        // TODO Auto-generated method stub

    }
    private void loadClickThroughRate() {
        // TODO Auto-generated method stub

    }
    private void loadNumberOfClicks() {
        // TODO Auto-generated method stub

    }
    private void loadNumberOfImpressions() {
        // TODO Auto-generated method stub

    }
    private void loadCostPerClick() {
        // TODO Auto-generated method stub

    }
    public void setMainView(MainView mainView) {
        this.mainView = mainView;

    }
}