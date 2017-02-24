import static org.junit.Assert.*;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.junit.Test;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ModelTest {
	
	/**
	 * Adam Kantorik, 24/02/2017
	 */
	
	/**
	 * Needs JUnitTest 4.11 or higher - ordering of tests.
	 * Changed test to load data just once. A at the beginning of loading test makes sure
	 * that tests that load data will run first. I also corrected test format.
	 */

	Model myModel = new Model();
	
	@Test
	public void AloadImpressionLogTest(){
		assertTrue(myModel.loadImpressionLog("test_impression_log.csv"));
	}
	
	@Test
	public void AloadClickLogTest(){
		assertTrue(myModel.loadClickLog("test_click_log.csv"));
	}
	
	@Test
	public void AloadServerLog(){
		assertTrue(myModel.loadServerLog("test_click_log.csv"));
	}
	
	@Test
	public void totalImpressionsTest() {
		assertEquals("Correct total number of impressions:", 22049, myModel.totalImpressions());
	}
	
	@Test
	public void costPerClickTest() {
		assertEquals("Correct cost per click:", 4.85, myModel.costPerClick());
	}
	
	@Test
	public void totalClicksTest() {
		assertEquals("Correct total number of clicks:", 1079, myModel.totalClicks());
	}
	
	@Test
	public void clickThroughRateTest() {
		assertEquals("Correct click-through-rate:", 0.0489, myModel.clickThroughRate());
	}
	
	@Test
	public void totalConversionsTest() {
		assertEquals("Correct total number of conversions:", 95, myModel.totalConversions());
	}
	
	@Test
	public void totalCostTest() {
		assertEquals("Correct total cost:", 5250.71, myModel.totalCost());
	}

}
