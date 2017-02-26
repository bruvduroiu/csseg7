package controller.model;


import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;



public abstract class Graph {

    private final StringProperty graphTitle;
    

    
    public Graph() {
        this(null);
    }

   
    public Graph(String graphTitle) {
        this.graphTitle = new SimpleStringProperty(graphTitle);
        
    }

    public String getGraphTitle() {
        return graphTitle.get();
    }

    public void setGraphTitle(String graphTitle) {
        this.graphTitle.set(graphTitle);
    }

    public StringProperty graphTitleProperty() {
        return graphTitle;
    }


	public abstract ObservableList getData();

    
}