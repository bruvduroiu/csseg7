import static org.junit.Assert.*;

import java.io.File;
import org.junit.Test;
import org.soton.seg7.ad_analytics.model.DBHandler;
import org.soton.seg7.ad_analytics.model.DBQuery;
import org.soton.seg7.ad_analytics.model.Filters;
import org.soton.seg7.ad_analytics.model.Parser;
import org.soton.seg7.ad_analytics.model.exceptions.MongoAuthException;

public class TotalStatsTest {

    File clickFile = new File(new File("").getAbsolutePath().toString() + "/static/analytics_csv/click_log.csv");
    File impressionsFile = new File(new File("").getAbsolutePath().toString() + "/static/analytics_csv/impression_log.csv");
    File serverFile = new File(new File("").getAbsolutePath().toString() + "/static/analytics_csv/server_log.csv");

    @Test
    public void testCTR() {
        final Double expectedResult = 0.049213748498263744;

        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("click_log");
            handler.dropCollection("impression_log");
            handler.dropCollection("click_data");
            handler.dropCollection("impression_data");

            Parser.parseCSV(clickFile);
            Parser.parseCSV(impressionsFile);

            Double result = DBQuery.getTotalCTR(Filters.NO_FILTER);

            assertEquals("Correct Num Clicks file:", expectedResult, result);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testTotalNumClicks() {
    	final Double expectedResult = 23923.0d;

        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("click_log");
            handler.dropCollection("click_data");

            Parser.parseCSV(clickFile);

            Double result = DBQuery.getTotalNumClicks();

            assertEquals("Correct Num Clicks file:", expectedResult, result);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testTotalImpressions() {
    	final Double expectedResult = 486104.0d;

        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("impression_data");
            handler.dropCollection("impression_log");

            Parser.parseCSV(impressionsFile);

            Double result = DBQuery.getTotalNumImpressions(Filters.NO_FILTER);

            assertEquals("Correct Num Impressions:", expectedResult, result);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testTotalCostImpressions() {
    	final Float expectedResult = 487.0555f;

        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("click_log");
            handler.dropCollection("click_data");

            Parser.parseCSV(clickFile);

            Float result = new Float(DBQuery.getTotalCostImpressions(Filters.NO_FILTER));

            assertEquals("Correct Num Clicks file:", expectedResult, result);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testTotalCostClick() {
    	final Double expectedResult = 117610.865725;

        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("click_log");
            handler.dropCollection("click_data");

            Parser.parseCSV(clickFile);

            Double result = DBQuery.getTotalCostClicks();

            assertEquals("Correct Num Clicks file:", expectedResult, result);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testTotalCostCampaign() {
    	final Double expectedResult = 118097.921223;

        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("click_log");
            handler.dropCollection("click_data");

            Parser.parseCSV(clickFile);

            Double result = DBQuery.getTotalCostCampaign(Filters.NO_FILTER);

            assertEquals("Correct Num Clicks file:", expectedResult, result);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

}
