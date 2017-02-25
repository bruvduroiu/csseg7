import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.soton.seg7.ad_analytics.model.DBHandler;
import org.soton.seg7.ad_analytics.model.MongoAuthException;
import org.soton.seg7.ad_analytics.model.Parser;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by bogdanbuduroiu on 24/02/2017.
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ParserTest {

    File clickFile = new File(new File("").getAbsolutePath().toString() + "/static/analytics_csv/click_log.csv");
    File impressionsFile = new File(new File("").getAbsolutePath().toString() + "/static/analytics_csv/impression_log.csv");

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
}
