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

public class DataFormatTest {

	DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH");

    File clickFile = new File(new File("").getAbsolutePath().toString() + "/static/analytics_csv/click_log.csv");
    File impressionsFile = new File(new File("").getAbsolutePath().toString() + "/static/analytics_csv/impression_log.csv");
    File serverFile = new File(new File("").getAbsolutePath().toString() + "/static/analytics_csv/server_log.csv");
	
	@Test
    public void testImpressionDayNum() {
        final Map<DateTime, Double> expectedJsonMap = new HashMap<DateTime, Double>(){{
        	put(formatter.parseDateTime("2015-01-12 00"), 40945.0);put(formatter.parseDateTime("2015-01-07 00"), 37958.0);put(formatter.parseDateTime("2015-01-02 00"), 32773.0);put(formatter.parseDateTime("2015-01-01 00"), 22049.0);put(formatter.parseDateTime("2015-01-11 00"), 42014.0);put(formatter.parseDateTime("2015-01-06 00"), 37379.0);put(formatter.parseDateTime("2015-01-10 00"), 36562.0);put(formatter.parseDateTime("2015-01-05 00"), 35758.0);put(formatter.parseDateTime("2015-01-04 00"), 33111.0);put(formatter.parseDateTime("2015-01-14 00"), 14135.0);put(formatter.parseDateTime("2015-01-09 00"), 39031.0);put(formatter.parseDateTime("2015-01-13 00"), 42159.0);put(formatter.parseDateTime("2015-01-08 00"), 37311.0);put(formatter.parseDateTime("2015-01-03 00"), 34919.0);}};
        
        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("impression_log");
            handler.dropCollection("impression_data");

            Parser.parseCSV(impressionsFile);
            Map<DateTime, Double> resNumImpressions = DBQuery.getNumImpressions(Filters.NO_FILTER);

            assertEquals("Correct Num Impressions in file", expectedJsonMap, resNumImpressions);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testClickDayNum() {
        final Map<DateTime, Double> expectedJsonMap = new HashMap<DateTime, Double>(){{
        	put(formatter.parseDateTime("2015-01-12 00"), 2024.0);put(formatter.parseDateTime("2015-01-07 00"), 1857.0);put(formatter.parseDateTime("2015-01-02 00"), 1638.0);put(formatter.parseDateTime("2015-01-01 00"), 1079.0);put(formatter.parseDateTime("2015-01-11 00"), 2042.0);put(formatter.parseDateTime("2015-01-06 00"), 1833.0);put(formatter.parseDateTime("2015-01-10 00"), 1785.0);put(formatter.parseDateTime("2015-01-05 00"), 1747.0);put(formatter.parseDateTime("2015-01-04 00"), 1631.0);put(formatter.parseDateTime("2015-01-14 00"), 726.0);put(formatter.parseDateTime("2015-01-09 00"), 1957.0);put(formatter.parseDateTime("2015-01-13 00"), 2053.0);put(formatter.parseDateTime("2015-01-08 00"), 1797.0);put(formatter.parseDateTime("2015-01-03 00"), 1754.0);}};
        		
        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("click_log");
            handler.dropCollection("click_data");

            Parser.parseCSV(clickFile);
            Map<DateTime, Double> resNumClicks = DBQuery.getNumClicks();

            assertEquals("Correct Num Clicks file:", expectedJsonMap, resNumClicks);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testServerConversions() {
        final Map<DateTime, Double> expectedJsonMap = new HashMap<DateTime, Double>(){{
        	put(formatter.parseDateTime("2015-01-12 00"), 199.0);put(formatter.parseDateTime("2015-01-07 00"), 147.0);put(formatter.parseDateTime("2015-01-02 00"), 156.0);put(formatter.parseDateTime("2015-01-01 00"), 95.0);put(formatter.parseDateTime("2015-01-11 00"), 168.0);put(formatter.parseDateTime("2015-01-06 00"), 138.0);put(formatter.parseDateTime("2015-01-10 00"), 163.0);put(formatter.parseDateTime("2015-01-05 00"), 138.0);put(formatter.parseDateTime("2015-01-04 00"), 139.0);put(formatter.parseDateTime("2015-01-14 00"), 60.0);put(formatter.parseDateTime("2015-01-09 00"), 167.0);put(formatter.parseDateTime("2015-01-13 00"), 151.0);put(formatter.parseDateTime("2015-01-08 00"), 162.0);put(formatter.parseDateTime("2015-01-03 00"), 143.0);}};
        
        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("server_log");
            handler.dropCollection("server_data");

            Parser.parseCSV(serverFile);
            Map<DateTime, Double> resNumConversions = DBQuery.getNumConversions();

            assertEquals("Correct num of Conversions", expectedJsonMap, resNumConversions);
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

            Map<DateTime, Double> result = DBQuery.getCTROverTime(Filters.NO_FILTER);

            assertEquals("Correct Hashmap", expectedCtrMap, result);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testBouncePage() {
			final Map<DateTime, Double> expectedMap = new HashMap<DateTime, Double>(){{
				put(formatter.parseDateTime("2015-01-12 00"), 0.3543064978101073);
				put(formatter.parseDateTime("2015-01-07 00"), 0.3650475735845285);
				put(formatter.parseDateTime("2015-01-02 00"), 0.3716156140250623);
				put(formatter.parseDateTime("2015-01-01 00"), 0.35663507111452036);
				put(formatter.parseDateTime("2015-01-11 00"), 0.3697624036079537);
				put(formatter.parseDateTime("2015-01-06 00"), 0.3892625082889752);
				put(formatter.parseDateTime("2015-01-10 00"), 0.35233610961639156);
				put(formatter.parseDateTime("2015-01-05 00"), 0.37488096095899914);
				put(formatter.parseDateTime("2015-01-04 00"), 0.37390582312259907);
				put(formatter.parseDateTime("2015-01-14 00"), 0.32359057708051275);
				put(formatter.parseDateTime("2015-01-09 00"), 0.34104517478558716);
				put(formatter.parseDateTime("2015-01-13 00"), 0.4009792521545559);
				put(formatter.parseDateTime("2015-01-08 00"), 0.36002325087311465);
				put(formatter.parseDateTime("2015-01-03 00"), 0.33557926095288054);}};
        
			DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("server_log");
            handler.dropCollection("server_data");

            Parser.parseCSV(serverFile);

            Map<DateTime, Double> result = DBQuery.getBounceRateByPage();
			
            DecimalFormat df = new DecimalFormat("#.############");
            
            assertEquals("Same number of elements : ", expectedMap.size(), result.size());
            for (Map.Entry<DateTime, Double> entry : result.entrySet())
			{
            	assertEquals("Correct value of HashMap : ", df.format(expectedMap.get(entry.getKey())), df.format(entry.getValue()));
            }
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testBounceTime() {
        Map<DateTime, Double> expectedMap = new HashMap<DateTime, Double>(){{
        	put(formatter.parseDateTime("2015-01-12 00"), 0.46918027959485337);
        	put(formatter.parseDateTime("2015-01-07 00"), 0.49163540023824454);
        	put(formatter.parseDateTime("2015-01-02 00"), 0.5231196964658601);
        	put(formatter.parseDateTime("2015-01-01 00"), 0.5313349973128622);
        	put(formatter.parseDateTime("2015-01-11 00"), 0.49388121776152266);
        	put(formatter.parseDateTime("2015-01-06 00"), 0.5276460969356462);
        	put(formatter.parseDateTime("2015-01-10 00"), 0.49401092705330424);
        	put(formatter.parseDateTime("2015-01-05 00"), 0.4948748282123761);
        	put(formatter.parseDateTime("2015-01-04 00"), 0.4880127944874278);
        	put(formatter.parseDateTime("2015-01-14 00"), 0.59800228387399378);
        	put(formatter.parseDateTime("2015-01-09 00"), 0.49819335524947733);
        	put(formatter.parseDateTime("2015-01-13 00"), 0.5256880823330287);
        	put(formatter.parseDateTime("2015-01-08 00"), 0.5146174894034906);
        	put(formatter.parseDateTime("2015-01-03 00"), 0.484213489272829);}};

        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("server_log");
            handler.dropCollection("server_data");

            Parser.parseCSV(serverFile);

            Map<DateTime, Double> result = DBQuery.getBounceRateByTime();
			
            DecimalFormat df = new DecimalFormat("#.############");
            
            assertEquals("Same number of elements : ", expectedMap.size(), result.size());
            for (Map.Entry<DateTime, Double> entry : result.entrySet())
			{
            	assertEquals("Correct value of HashMap : ", df.format(expectedMap.get(entry.getKey())), df.format(entry.getValue()));
            }
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

}
