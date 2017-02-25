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
    public void testClickDayNum() {
        final Map<String, Object> expectedJsonMap = new JSONObject("{ \"2015-01-05\" : { \"00\" : 19, \"11\" : 92, \"22\" : 58, \"01\" : 5, \"12\" : 95, \"23\" : 31, \"02\" : 7, \"13\" : 108, \"03\" : 6, \"14\" : 100, \"04\" : 26, \"15\" : 117, \"05\" : 33, \"16\" : 108, \"06\" : 49, \"17\" : 122, \"07\" : 69, \"18\" : 113, \"08\" : 82, \"19\" : 127, \"09\" : 116, \"20\" : 98, \"10\" : 98, \"21\" : 68 }, \"2015-01-06\" : { \"00\" : 18, \"11\" : 111, \"22\" : 51, \"01\" : 5, \"12\" : 114, \"23\" : 45, \"02\" : 4, \"13\" : 122, \"03\" : 5, \"14\" : 112, \"04\" : 14, \"15\" : 118, \"05\" : 26, \"16\" : 94, \"06\" : 59, \"17\" : 105, \"07\" : 91, \"18\" : 122, \"08\" : 109, \"19\" : 105, \"09\" : 116, \"20\" : 99, \"10\" : 116, \"21\" : 72 }, \"2015-01-03\" : { \"00\" : 16, \"11\" : 108, \"22\" : 56, \"01\" : 3, \"12\" : 93, \"23\" : 33, \"02\" : 4, \"13\" : 98, \"03\" : 3, \"14\" : 95, \"04\" : 10, \"15\" : 120, \"05\" : 31, \"16\" : 126, \"06\" : 69, \"17\" : 104, \"07\" : 76, \"18\" : 108, \"08\" : 103, \"19\" : 122, \"09\" : 89, \"20\" : 90, \"10\" : 122, \"21\" : 75 }, \"2015-01-14\" : { \"00\" : 18, \"11\" : 142, \"01\" : 4, \"02\" : 5, \"03\" : 1, \"04\" : 20, \"05\" : 48, \"06\" : 54, \"07\" : 77, \"08\" : 104, \"09\" : 126, \"10\" : 127 }, \"2015-01-04\" : { \"00\" : 19, \"11\" : 93, \"22\" : 46, \"01\" : 4, \"12\" : 93, \"23\" : 33, \"02\" : 8, \"13\" : 90, \"03\" : 5, \"14\" : 93, \"04\" : 16, \"15\" : 100, \"05\" : 36, \"16\" : 107, \"06\" : 51, \"17\" : 116, \"07\" : 72, \"18\" : 107, \"08\" : 73, \"19\" : 116, \"09\" : 88, \"20\" : 97, \"10\" : 85, \"21\" : 83 }, \"2015-01-01\" : { \"22\" : 49, \"12\" : 94, \"23\" : 34, \"13\" : 96, \"14\" : 115, \"15\" : 105, \"16\" : 110, \"17\" : 98, \"18\" : 102, \"19\" : 104, \"20\" : 88, \"21\" : 84 }, \"2015-01-12\" : { \"00\" : 16, \"11\" : 151, \"22\" : 80, \"01\" : 2, \"12\" : 128, \"23\" : 33, \"02\" : 6, \"13\" : 135, \"03\" : 6, \"14\" : 129, \"04\" : 12, \"15\" : 140, \"05\" : 25, \"16\" : 138, \"06\" : 61, \"17\" : 101, \"07\" : 85, \"18\" : 128, \"08\" : 107, \"19\" : 115, \"09\" : 127, \"20\" : 96, \"10\" : 121, \"21\" : 82 }, \"2015-01-02\" : { \"00\" : 12, \"11\" : 79, \"22\" : 72, \"01\" : 8, \"12\" : 117, \"23\" : 47, \"02\" : 3, \"13\" : 116, \"03\" : 4, \"14\" : 97, \"04\" : 12, \"15\" : 101, \"05\" : 26, \"16\" : 90, \"06\" : 41, \"17\" : 98, \"07\" : 65, \"18\" : 93, \"08\" : 86, \"19\" : 95, \"09\" : 92, \"20\" : 108, \"10\" : 100, \"21\" : 76 }, \"2015-01-13\" : { \"00\" : 16, \"11\" : 143, \"22\" : 50, \"01\" : 10, \"12\" : 116, \"23\" : 30, \"02\" : 8, \"13\" : 127, \"03\" : 5, \"14\" : 138, \"04\" : 14, \"15\" : 124, \"05\" : 49, \"16\" : 120, \"06\" : 61, \"17\" : 136, \"07\" : 92, \"18\" : 107, \"08\" : 131, \"19\" : 148, \"09\" : 138, \"20\" : 92, \"10\" : 137, \"21\" : 61 }, \"2015-01-10\" : { \"00\" : 18, \"11\" : 112, \"22\" : 59, \"01\" : 9, \"12\" : 110, \"23\" : 36, \"02\" : 8, \"13\" : 97, \"03\" : 4, \"14\" : 84, \"04\" : 10, \"15\" : 133, \"05\" : 44, \"16\" : 101, \"06\" : 63, \"17\" : 107, \"07\" : 79, \"18\" : 127, \"08\" : 99, \"19\" : 105, \"09\" : 91, \"20\" : 80, \"10\" : 122, \"21\" : 87 }, \"2015-01-11\" : { \"00\" : 12, \"11\" : 134, \"22\" : 58, \"01\" : 3, \"12\" : 127, \"23\" : 43, \"02\" : 4, \"13\" : 119, \"03\" : 6, \"14\" : 127, \"04\" : 16, \"15\" : 126, \"05\" : 42, \"16\" : 128, \"06\" : 69, \"17\" : 117, \"07\" : 89, \"18\" : 102, \"08\" : 104, \"19\" : 147, \"09\" : 141, \"20\" : 120, \"10\" : 117, \"21\" : 91 }, \"2015-01-09\" : { \"00\" : 15, \"11\" : 134, \"22\" : 67, \"01\" : 4, \"12\" : 115, \"23\" : 41, \"02\" : 6, \"13\" : 130, \"03\" : 2, \"14\" : 132, \"04\" : 16, \"15\" : 122, \"05\" : 32, \"16\" : 101, \"06\" : 76, \"17\" : 140, \"07\" : 84, \"18\" : 125, \"08\" : 106, \"19\" : 109, \"09\" : 115, \"20\" : 89, \"10\" : 113, \"21\" : 83 }, \"2015-01-07\" : { \"00\" : 18, \"11\" : 111, \"22\" : 64, \"01\" : 5, \"12\" : 106, \"23\" : 43, \"02\" : 5, \"13\" : 122, \"03\" : 6, \"14\" : 130, \"04\" : 13, \"15\" : 101, \"05\" : 42, \"16\" : 126, \"06\" : 68, \"17\" : 112, \"07\" : 77, \"18\" : 107, \"08\" : 86, \"19\" : 107, \"09\" : 101, \"20\" : 106, \"10\" : 114, \"21\" : 87 }, \"2015-01-08\" : { \"00\" : 15, \"11\" : 74, \"22\" : 54, \"01\" : 4, \"12\" : 121, \"23\" : 34, \"02\" : 7, \"13\" : 115, \"03\" : 6, \"14\" : 113, \"04\" : 13, \"15\" : 108, \"05\" : 34, \"16\" : 97, \"06\" : 51, \"17\" : 129, \"07\" : 83, \"18\" : 114, \"08\" : 105, \"19\" : 112, \"09\" : 103, \"20\" : 97, \"10\" : 111, \"21\" : 97 } }".replace(" ", "")).toMap();

        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("click_log");

            Parser.parseCSV(clickFile);
            Map<String, Map<Integer, Integer>> resNumClicks = DBQuery.getNumClicks();

            assertEquals("Correct Num Clicks file:", expectedJsonMap, resNumClicks);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testImpressionDayNum() {
        final Map<String, Object> expectedJsonMap = new JSONObject("{2015-01-01:{16:2126, 17:2116, 18:2143, 19:2128, 20:1915, 21:1472, 22:1100, 23:733, 12:2126, 13:2026, 14:2073, 15:2091}, 2015-01-02:{0:320, 1:117, 2:114, 3:112, 4:290, 5:635, 6:1034, 7:1324, 8:1671, 9:1834, 10:499}}".replace(" ", "")).toMap();

        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("impression_log");

            Parser.parseCSV(impressionsFile);
            Map<String, Map<Integer, Integer>> resNumImpressions = DBQuery.getNumImpressions();

            assertEquals("Correct Num Impressions in file", expectedJsonMap, resNumImpressions);
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
            Map<String, Map<Integer, Integer>> resNumConversions = DBQuery.getNumConversions();

            assertEquals("Correct num of Conversions", expectedJsonMap, resNumConversions);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCTR() {
        final Double expectedResult = 0.797459915330511;

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
    public void testCTROverTime() {
        final Map<String, Map<Integer, Double>> expectedCtrMap = new HashMap<>();

        DBHandler handler;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("click_log");
            handler.dropCollection("impression_log");

            Parser.parseCSV(clickFile);
            Parser.parseCSV(impressionsFile);

            Map<String, Map<Integer, Double>> result = DBQuery.getCTROverTime();

            assertEquals("Correct Hashmap", expectedCtrMap, result);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }
}
