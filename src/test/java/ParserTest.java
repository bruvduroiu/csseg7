import org.json.JSONObject;
import org.junit.Test;
import org.soton.seg7.ad_analytics.model.DBHandler;
import org.soton.seg7.ad_analytics.model.DBQuery;
import org.soton.seg7.ad_analytics.model.Filters;
import org.soton.seg7.ad_analytics.model.exceptions.MongoAuthException;
import org.soton.seg7.ad_analytics.model.Parser;
import java.io.File;

import static org.junit.Assert.*;

/**
 * Rewritten by Adam Kantorik on 15/03/2017.
 */

public class ParserTest {	

    File clickFile = new File(new File("").getAbsolutePath().toString() + "/static/analytics_csv/click_log.csv");
    File impressionsFile = new File(new File("").getAbsolutePath().toString() + "/static/analytics_csv/impression_log.csv");
    File serverFile = new File(new File("").getAbsolutePath().toString() + "/static/analytics_csv/server_log.csv");
    File wrongFile = new File(new File("").getAbsolutePath().toString() + "/README.md");

    @Test
    public void testClickLogValidator() {
        assertEquals(true, Parser.isValidClickLog(clickFile));
    }

    @Test
    public void testServerLogValidator() {
        assertEquals(true, Parser.isValidServerLog(serverFile));
    }

    @Test
    public void testImpressionLogValidator() {
        assertEquals(true, Parser.isValidImpressionLog(impressionsFile));
    }

    @Test
    public void testClickParse() {

        DBHandler handler = null;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("click_log");

            JSONObject parse = Parser.parseCSV(clickFile);
            Double numComputed = DBQuery.getTotalNumClicks();
            Double numInserted = parse.getDouble("numdoc");

            assertEquals("Correct num clicks", numComputed, numInserted);
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
            Double numComputed = DBQuery.getTotalNumImpressions(Filters.NO_FILTER);
            Double numInserted = parse.getDouble("numdoc");

            assertEquals("Correct JSON file", numComputed, numInserted);

        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testWrongFileParse() {

        DBHandler handler = null;

        try {
            handler = DBHandler.getDBConnection();
            handler.dropCollection("impression_log");
            handler.dropCollection("click_log");

            boolean passed1 = Parser.isValidImpressionLog(wrongFile);
            boolean passed2 = Parser.isValidClickLog(impressionsFile);


            assertFalse("Readme as impression log : ", passed1);
            assertFalse("Impression log as click log : ", passed2);

        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }
}
