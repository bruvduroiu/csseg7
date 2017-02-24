import com.mongodb.DBObject;
import org.json.JSONArray;
import org.junit.Test;
import org.soton.seg7.ad_analytics.model.DBHandler;
import org.soton.seg7.ad_analytics.model.Parser;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

/**
 * Created by bogdanbuduroiu on 24/02/2017.
 */

public class ParserTest {

    @Test
    public void testParse() {
        List<DBObject> parse = Parser.parseCSV("click_log");


        assertEquals("Correct number of rows parsed", 23925, parse.size());
    }

    @Test
    public void testInsertion() {
        DBHandler handler = DBHandler.getDBConnection(27017);

        List<DBObject> parse = Parser.parseCSV("click_log");

        handler.insertData(parse, parse.get(0).get("collection").toString());
        JSONArray res = handler.retrieveAllDocuments(parse.get(0).get("collection").toString());

        assertEquals("Correct number of insertions", parse.size(), res.length());
    }
}
