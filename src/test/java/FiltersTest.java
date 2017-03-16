import static org.junit.Assert.*;

import java.io.File;
import java.text.DecimalFormat;
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
        final Double expectedResult = 0.2463596482194715;

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
    	final Double expectedResult = 324635.0d;

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
    	final Float expectedResult = 122.1276f;

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
    	final Double expectedResult = 117680.673926;

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
    public void testCONTEXT_SHOPPING_CTROverTime() {
        final Map<DateTime, Double> expectedCtrMap = new HashMap<DateTime, Double>(){{
        	put(formatter.parseDateTime("2015-01-07 00"), 0.17044515832950896);
        	put(formatter.parseDateTime("2015-01-02 00"), 0.17242105263157895);
        	put(formatter.parseDateTime("2015-01-12 00"), 0.1738383578115606);
        	put(formatter.parseDateTime("2015-01-01 00"), 0.17350056279144557);
        	put(formatter.parseDateTime("2015-01-06 00"), 0.17162921348314605);
        	put(formatter.parseDateTime("2015-01-11 00"), 0.17066443794400335);
        	put(formatter.parseDateTime("2015-01-10 00"), 0.17109172817022908);
        	put(formatter.parseDateTime("2015-01-05 00"), 0.16964459118275393);
        	put(formatter.parseDateTime("2015-01-04 00"), 0.17117968094038624);
        	put(formatter.parseDateTime("2015-01-09 00"), 0.176242795389049);
        	put(formatter.parseDateTime("2015-01-14 00"), 0.17690058479532164);
        	put(formatter.parseDateTime("2015-01-08 00"), 0.16692986530422665);
        	put(formatter.parseDateTime("2015-01-03 00"), 0.17763824184727567);
        	put(formatter.parseDateTime("2015-01-13 00"), 0.16761920313520576);}};

        
        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("click_log");
            handler.dropCollection("impression_log");
            handler.dropCollection("click_data");
            handler.dropCollection("impression_data");

            Parser.parseCSV(clickFile);
            Parser.parseCSV(impressionsFile);

            Map<DateTime, Double> result = DBQuery.getCTROverTime(Filters.CONTEXT_SHOPPING);
            
            DecimalFormat df = new DecimalFormat("#.############");
            
            assertEquals("Same number of elements : ", expectedCtrMap.size(), result.size());
            for (Map.Entry<DateTime, Double> entry : result.entrySet())
			{
            	assertEquals("Correct value of HashMap : ", df.format(expectedCtrMap.get(entry.getKey())), df.format(entry.getValue()));
            }
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

}
