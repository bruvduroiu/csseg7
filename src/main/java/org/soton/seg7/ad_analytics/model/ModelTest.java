import static org.junit.Assert.*;

import org.junit.Test;


public class ModelTest {

	/**
	 * Sam Law, 22/02/2017
	 */
	
	/**
	 * For testing, I've assumed we can load a test .csv file into the model
	 * for which we already know the various values, and test it to see if it
	 * outputs the same values. The test .csv file is just the data from the
	 * first day from the files available on the COMP2211 Notes page
	 * 
	 * Tell me how to correct if wrong!
	 */
	
	public void totalImpressionsTest() {
		Model myModel = new Model("test_impression_log.csv");
		assertEquals("Correct total number of impressions:", 22049, myModel.totalImpressions());
	}
	
	public void costPerClickTest() {
		Model myModel = new Model("test_click_log.csv");
		assertEquals("Correct cost per click:", 4.85, myModel.totalImpressions());
	}
	
	public void totalClicksTest() {
		Model myModel = new Model("test_click_log.csv");
		assertEquals("Correct total number of clicks:", 1079, myModel.totalImpressions());
	}
	
	public void clickThroughRateTest() {
		Model myModel = new Model("test_impression_log.csv", "test_impression_log.csv");
		assertEquals("Correct click-through-rate:", 0.0489, myModel.totalImpressions());
	}
	
	public void totalConversionsTest() {
		Model myModel = new Model("test_server_log.csv");
		assertEquals("Correct total number of conversions:", 95, myModel.totalImpressions());
	}
	
	public void totalCostTest() {
		Model myModel = new Model("test_impression_log.csv", "test_impression_log.csv");
		assertEquals("Correct total cost:", 5250.71, myModel.totalImpressions());
	}
}
