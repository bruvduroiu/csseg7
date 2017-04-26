import com.mongodb.DB;
import org.junit.Assert;
import org.junit.Test;
import org.soton.seg7.ad_analytics.model.DBHandler;
import org.soton.seg7.ad_analytics.model.DBQuery;
import org.soton.seg7.ad_analytics.model.Filters;
import org.soton.seg7.ad_analytics.model.Parser;
import org.soton.seg7.ad_analytics.model.exceptions.MongoAuthException;

import java.io.File;

/**
 * Created by bogdanbuduroiu on 26/04/2017.
 */
public class PerformanceTest {

    File impressions_file_2_mo = new File(new File("").getAbsolutePath() + "/static/analytics_csv/2_month_data/impression_log.csv");
    File click_file_2_mo = new File(new File("").getAbsolutePath() + "/static/analytics_csv/2_month_data/click_log.csv");
    File server_file_2_mo = new File(new File("").getAbsolutePath() + "/static/analytics_csv/2_month_data/server_log.csv");

    private static final long TARGET_TIME = 10000;
    protected boolean isParsed = true;

    @Test
    public void testAgeFilterPerformance() {
        if (!isParsed)
            handleParsing();

        try {
            final long startTime = System.currentTimeMillis();
            DBQuery.getNumImpressions(Filters.AGE_25_34);
            final long endTime = System.currentTimeMillis();
            Assert.assertTrue(String.format("Age Filtering over target execution time. Time:%d; Target: %d.", (endTime-startTime),TARGET_TIME), (endTime-startTime) <= TARGET_TIME);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }


    }

    @Test
    public void testAgeIncomeFilterPerformance() {
        if (!isParsed)
            handleParsing();
        try {
            final long startTime = System.currentTimeMillis();
            DBQuery.getNumImpressions(Filters.AGE_25_34 + Filters.INCOME_HIGH);
            final long endTime = System.currentTimeMillis();
            Assert.assertTrue(String.format("Age Filtering over target execution time. Time:%d; Target: %d.", (endTime-startTime),TARGET_TIME), (endTime-startTime) <= TARGET_TIME);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAgeIncomeGenderFilterPerformance() {
        if (!isParsed)
            handleParsing();

        try {
            final long startTime = System.currentTimeMillis();
            DBQuery.getNumImpressions(Filters.AGE_25_34 + Filters.INCOME_HIGH + Filters.GENDER_MALE);
            final long endTime = System.currentTimeMillis();
            Assert.assertTrue(String.format("Age Filtering over target execution time. Time:%d; Target: %d.", (endTime-startTime),TARGET_TIME), (endTime-startTime) <= TARGET_TIME);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAgeIncomeGenderContextFilterPerformance() {
        if (!isParsed)
            handleParsing();

        try {
            final long startTime = System.currentTimeMillis();
            DBQuery.getNumImpressions(Filters.AGE_25_34 + Filters.INCOME_HIGH + Filters.GENDER_MALE + Filters.CONTEXT_BLOG);
            final long endTime = System.currentTimeMillis();
            Assert.assertTrue(String.format("Age Filtering over target execution time. Time:%d; Target: %d.", (endTime-startTime),TARGET_TIME), (endTime-startTime) <= TARGET_TIME);
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

    public void testDateFilterPerformance() {
        if (!isParsed)
            handleParsing();

    }

    public void handleParsing() {
        try {
            DBHandler handler = DBHandler.getDBConnection();
            handler.dropDatabase();
            handler.enableSharding();
            handler.shardCollection("impression_data");
            Parser.parseCSV(impressions_file_2_mo);
            Parser.parseCSV(server_file_2_mo);
            Parser.parseCSV(click_file_2_mo);

            DBQuery.indexImpressions();
            isParsed = true;
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }
}
