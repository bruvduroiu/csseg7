import org.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.soton.seg7.ad_analytics.model.DBHandler;
import org.soton.seg7.ad_analytics.model.DBQuery;
import org.soton.seg7.ad_analytics.model.exceptions.MongoAuthException;
import org.soton.seg7.ad_analytics.model.Parser;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bogdanbuduroiu on 24/02/2017.
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ParserTest {

    File clickFile = new File(new File("").getAbsolutePath().toString() + "/static/analytics_csv/click_log.csv");
    File impressionsFile = new File(new File("").getAbsolutePath().toString() + "/static/analytics_csv/impression_log.csv");
    File serverFile = new File(new File("").getAbsolutePath().toString() + "/static/analytics_csv/server_log.csv");

    
    @Test
    public void testImpressionDayNum() {
        final Map<String, Object> expectedJsonMap = new JSONObject("{2015-01-01:{12:2126, 13:2026, 14:2073, 15:2091, 16:1683}}".replace(" ", "")).toMap();

        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("impression_log");

            Parser.parseCSV(impressionsFile);
            Map<String, Map<String, Integer>> resNumImpressions = DBQuery.getNumImpressions();

            assertEquals("Correct Num Impressions in file", expectedJsonMap, resNumImpressions);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testClickDayNum() {
        final Map<String, Object> expectedJsonMap = new JSONObject("{2015-01-01:{12:94, 13:96, 14:115, 15:105, 16:89}}").toMap();

        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("click_log");

            Parser.parseCSV(clickFile);
            Map<String, Map<String, Integer>> resNumClicks = DBQuery.getNumClicks();

            assertEquals("Correct Num Clicks file:", expectedJsonMap, resNumClicks);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testServerConversions() {
        final Map<String, Object> expectedJsonMap = new JSONObject("{\"2015-01-05\" : { \"00\" : 1, \"11\" : 5, \"22\" : 3, \"01\" : 1, \"12\" : 9, \"23\" : 4, \"02\" : 1, \"13\" : 4, \"14\" : 10, \"04\" : 2, \"15\" : 3, \"05\" : 2, \"16\" : 7, \"06\" : 5, \"17\" : 10, \"07\" : 5, \"18\" : 12, \"08\" : 8, \"19\" : 9, \"09\" : 15, \"20\" : 5, \"10\" : 7, \"21\" : 10 }, \"2015-01-06\" : { \"11\" : 12, \"22\" : 7, \"01\" : 1, \"12\" : 6, \"23\" : 2, \"13\" : 7, \"03\" : 1, \"14\" : 9, \"15\" : 11, \"05\" : 3, \"16\" : 8, \"06\" : 1, \"17\" : 7, \"07\" : 5, \"18\" : 8, \"08\" : 11, \"19\" : 5, \"09\" : 12, \"20\" : 4, \"10\" : 10, \"21\" : 8 }, \"2015-01-03\" : { \"00\" : 3, \"11\" : 10, \"22\" : 5, \"12\" : 7, \"23\" : 2, \"02\" : 2, \"13\" : 7, \"03\" : 1, \"14\" : 13, \"04\" : 1, \"15\" : 12, \"05\" : 5, \"16\" : 6, \"06\" : 5, \"17\" : 7, \"07\" : 7, \"18\" : 11, \"08\" : 5, \"19\" : 4, \"09\" : 2, \"20\" : 8, \"10\" : 10, \"21\" : 10 }, \"2015-01-14\" : { \"00\" : 1, \"11\" : 8, \"04\" : 1, \"05\" : 3, \"06\" : 4, \"07\" : 7, \"08\" : 17, \"09\" : 9, \"10\" : 10 }, \"2015-01-04\" : { \"00\" : 2, \"11\" : 8, \"22\" : 7, \"01\" : 1, \"12\" : 11, \"23\" : 2, \"02\" : 2, \"13\" : 12, \"14\" : 9, \"15\" : 6, \"05\" : 3, \"16\" : 12, \"06\" : 2, \"17\" : 6, \"07\" : 5, \"18\" : 9, \"08\" : 8, \"19\" : 9, \"09\" : 5, \"20\" : 6, \"10\" : 7, \"21\" : 7 }, \"2015-01-01\" : { \"22\" : 3, \"12\" : 5, \"13\" : 12, \"14\" : 14, \"15\" : 10, \"16\" : 9, \"17\" : 10, \"18\" : 2, \"19\" : 11, \"20\" : 10, \"21\" : 9 }, \"2015-01-12\" : { \"00\" : 3, \"11\" : 13, \"22\" : 11, \"12\" : 11, \"23\" : 4, \"13\" : 13, \"03\" : 1, \"14\" : 15, \"04\" : 1, \"15\" : 22, \"05\" : 2, \"16\" : 13, \"06\" : 5, \"17\" : 8, \"07\" : 9, \"18\" : 12, \"08\" : 8, \"19\" : 10, \"09\" : 15, \"20\" : 5, \"10\" : 10, \"21\" : 8 }, \"2015-01-02\" : { \"00\" : 1, \"11\" : 8, \"22\" : 5, \"12\" : 12, \"23\" : 3, \"13\" : 14, \"14\" : 7, \"04\" : 2, \"15\" : 9, \"05\" : 7, \"16\" : 11, \"06\" : 4, \"17\" : 10, \"07\" : 3, \"18\" : 11, \"08\" : 8, \"19\" : 7, \"09\" : 6, \"20\" : 10, \"10\" : 11, \"21\" : 7 }, \"2015-01-13\" : { \"00\" : 2, \"11\" : 14, \"22\" : 4, \"12\" : 11, \"23\" : 3, \"13\" : 4, \"14\" : 11, \"15\" : 8, \"05\" : 3, \"16\" : 7, \"06\" : 7, \"17\" : 10, \"07\" : 6, \"18\" : 10, \"08\" : 11, \"19\" : 11, \"09\" : 7, \"20\" : 11, \"10\" : 8, \"21\" : 3 }, \"2015-01-10\" : { \"00\" : 1, \"11\" : 9, \"22\" : 10, \"12\" : 16, \"23\" : 2, \"13\" : 8, \"03\" : 1, \"14\" : 9, \"04\" : 2, \"15\" : 11, \"05\" : 2, \"16\" : 10, \"06\" : 4, \"17\" : 7, \"07\" : 6, \"18\" : 10, \"08\" : 11, \"19\" : 11, \"09\" : 8, \"20\" : 9, \"10\" : 6, \"21\" : 10 }, \"2015-01-11\" : { \"11\" : 11, \"22\" : 7, \"12\" : 12, \"23\" : 4, \"13\" : 13, \"03\" : 1, \"14\" : 3, \"15\" : 13, \"05\" : 5, \"16\" : 10, \"06\" : 3, \"17\" : 9, \"07\" : 3, \"18\" : 13, \"08\" : 5, \"19\" : 10, \"09\" : 7, \"20\" : 15, \"10\" : 16, \"21\" : 8 }, \"2015-01-09\" : { \"00\" : 1, \"11\" : 12, \"22\" : 5, \"01\" : 2, \"12\" : 8, \"23\" : 4, \"02\" : 1, \"13\" : 8, \"14\" : 15, \"04\" : 1, \"15\" : 9, \"05\" : 4, \"16\" : 9, \"06\" : 8, \"17\" : 11, \"07\" : 8, \"18\" : 14, \"08\" : 5, \"19\" : 12, \"09\" : 8, \"20\" : 7, \"10\" : 10, \"21\" : 5 }, \"2015-01-07\" : { \"11\" : 9, \"22\" : 3, \"12\" : 8, \"23\" : 3, \"13\" : 4, \"14\" : 13, \"04\" : 1, \"15\" : 6, \"05\" : 4, \"16\" : 15, \"06\" : 5, \"17\" : 3, \"07\" : 4, \"18\" : 19, \"08\" : 8, \"19\" : 10, \"09\" : 9, \"20\" : 7, \"10\" : 4, \"21\" : 12 }, \"2015-01-08\" : { \"00\" : 1, \"11\" : 5, \"22\" : 5, \"12\" : 6, \"23\" : 3, \"13\" : 11, \"03\" : 1, \"14\" : 11, \"04\" : 1, \"15\" : 11, \"05\" : 1, \"16\" : 7, \"06\" : 3, \"17\" : 13, \"07\" : 9, \"18\" : 8, \"08\" : 6, \"19\" : 13, \"09\" : 13, \"20\" : 12, \"10\" : 11, \"21\" : 11 } }".replace(" ", "")).toMap();

        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("server_log");

            Parser.parseCSV(serverFile);
            Map<String, Map<String, Integer>> resNumConversions = DBQuery.getNumConversions();

            assertEquals("Correct num of Conversions", expectedJsonMap, resNumConversions);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCTR() {
        final Double expectedResult = 0.0499049904990499;

        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("click_log");
            handler.dropCollection("impression_log");

            Parser.parseCSV(clickFile);
            Parser.parseCSV(impressionsFile);

            Double result = DBQuery.getTotalCTR();

            assertEquals("Correct Num Clicks file:", expectedResult, result);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testTotalClicks() {
    	final Double expectedResult = new Double(499);

        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("click_log");

            Parser.parseCSV(clickFile);

            Double result = DBQuery.getTotalClicks();

            assertEquals("Correct Num Clicks file:", expectedResult, result);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testTotalImpressions() {
    	final Double expectedResult = new Double(9999);

        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("impression_log");

            Parser.parseCSV(impressionsFile);

            Double result = DBQuery.getTotalImpressions();

            assertEquals("Correct Num Clicks file:", expectedResult, result);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testTotalCostImpressions() {
    	final Float expectedResult = 10.067485f;

        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("click_log");

            Parser.parseCSV(clickFile);

            Float result = new Float(DBQuery.getTotalCostImpressions());

            assertEquals("Correct Num Clicks file:", expectedResult, result);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testTotalCostClick() {
    	final Double expectedResult = 2460.598919;

        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("click_log");

            Parser.parseCSV(clickFile);

            Double result = DBQuery.getTotalCostClicks();

            assertEquals("Correct Num Clicks file:", expectedResult, result);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testTotalCostCampaign() {
    	final Double expectedResult = 2470.666404;

        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("click_log");

            Parser.parseCSV(clickFile);

            Double result = DBQuery.getTotalCostCampaign();

            assertEquals("Correct Num Clicks file:", expectedResult, result);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testClickParse() {

        DBHandler handler = null;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("click_log");

            JSONObject parse = Parser.parseCSV(clickFile);
            String parseAnalytics = parse.toString();
            String resAnalytics = handler.retrieveAllDocuments("click_log").toString();

            assertEquals("Correct JSON file", resAnalytics, parseAnalytics);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testImpressionsParse() {

        DBHandler handler = null;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("impression_log");

            JSONObject parse = Parser.parseCSV(impressionsFile);
            String parseAnalytics = parse.toString();
            String resAnalytics = handler.retrieveAllDocuments("impression_log").toString();

            assertEquals("Correct JSON file", resAnalytics, parseAnalytics);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testCTROverTime() {
        final Map<String, Object> expectedCtrMap = new JSONObject("{2015-01-01:{12:0.04421448730009407, 13:0.04738400789733465, 14:0.05547515677761698, 15:0.05021520803443329, 16:0.052881758764111705}}").toMap();

        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("click_log");
            handler.dropCollection("impression_log");

            Parser.parseCSV(clickFile);
            Parser.parseCSV(impressionsFile);

            Map<String, Map<String, Double>> result = DBQuery.getCTROverTime();

            assertEquals("Correct Hashmap", expectedCtrMap, result);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }
}
