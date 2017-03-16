import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import org.soton.seg7.ad_analytics.model.DBHandler;
import org.soton.seg7.ad_analytics.model.DBQuery;
import org.soton.seg7.ad_analytics.model.Filters;
import org.soton.seg7.ad_analytics.model.Parser;
import org.soton.seg7.ad_analytics.model.exceptions.MongoAuthException;

public class FiltersTest {

	DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH");

	File clickFile = new File(new File("").getAbsolutePath().toString() + "/static/analytics_csv/click_log.csv");
    File impressionsFile = new File(new File("").getAbsolutePath().toString() + "/static/analytics_csv/impression_log.csv");
    File serverFile = new File(new File("").getAbsolutePath().toString() + "/static/analytics_csv/server_log.csv");

    @Test
    public void testINCOME_HIGH_CTR() {
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

            Double result = DBQuery.getTotalCTR(Filters.INCOME_HIGH);

            assertEquals("Correct Num Clicks file:", expectedResult, result);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testGENDER_FEMALE_TotalImpressions() {
    	final Double expectedResult = 486104.0d;

        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("impression_data");
            handler.dropCollection("impression_log");

            Parser.parseCSV(impressionsFile);

            Double result = DBQuery.getTotalNumImpressions(Filters.GENDER_FEMALE);

            assertEquals("Correct Num Clicks file:", expectedResult, result);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testAGE25_34_CostImpressions() {
    	final Float expectedResult = 487.0555f;

        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("click_log");
            handler.dropCollection("click_data");

            Parser.parseCSV(clickFile);

            Float result = new Float(DBQuery.getTotalCostImpressions(Filters.AGE_25_34));

            assertEquals("Correct Num Clicks file:", expectedResult, result);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }
    
    
    @Test
    public void testCONTEXT_BLOG_TotalCostCampaign() {
    	final Double expectedResult = 118097.921223;

        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("click_log");
            handler.dropCollection("click_data");

            Parser.parseCSV(clickFile);

            Double result = DBQuery.getTotalCostCampaign(Filters.CONTEXT_BLOG);

            assertEquals("Correct Num Clicks file:", expectedResult, result);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testCTROverTime() {
        final Map<DateTime, Double> expectedCtrMap = new HashMap<DateTime, Double>(){{
        	put(formatter.parseDateTime("2015-01-02 00"), 0.049980166600555334);put(formatter.parseDateTime("2015-01-07 00"), 0.04892249328204858);put(formatter.parseDateTime("2015-01-12 00"), 0.04943216509952375);put(formatter.parseDateTime("2015-01-01 00"), 0.04893645970338791);put(formatter.parseDateTime("2015-01-06 00"), 0.04903823002220498);put(formatter.parseDateTime("2015-01-11 00"), 0.048602846670157566);put(formatter.parseDateTime("2015-01-05 00"), 0.04885620001118631);put(formatter.parseDateTime("2015-01-10 00"), 0.0488211804605875);put(formatter.parseDateTime("2015-01-04 00"), 0.049258554558907916);put(formatter.parseDateTime("2015-01-09 00"), 0.05013963259972842);put(formatter.parseDateTime("2015-01-14 00"), 0.051361867704280154);put(formatter.parseDateTime("2015-01-03 00"), 0.050230533520433);put(formatter.parseDateTime("2015-01-08 00"), 0.048162740210661734);put(formatter.parseDateTime("2015-01-13 00"), 0.04869660096302095);}};

        
        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("click_log");
            handler.dropCollection("impression_log");
            handler.dropCollection("click_data");
            handler.dropCollection("impression_data");

            Parser.parseCSV(clickFile);
            Parser.parseCSV(impressionsFile);

            Map<DateTime, Double> result = DBQuery.getCTROverTime(Filters.INCOME_MEDIUM);

            assertEquals("Correct Hashmap", expectedCtrMap, result);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

}
