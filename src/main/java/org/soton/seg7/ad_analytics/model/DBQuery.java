package org.soton.seg7.ad_analytics.model;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bogdanbuduroiu on 25/02/2017.
 */
public class DBQuery {

    private static final String COL_IMPRESSIONS = "impression_log";
    private static final String COL_CLICKS = "click_log";
    private static final String COL_SERVER = "server_log";

    private static final String GET_TOTAL_NUM = "totalNum";
    private static final String GET_TOTAL_COST = "totalCost";

    private static final DBObject ALL_QUERY = new BasicDBObject();
    private static DBObject fieldModifier;

    public static Map<String, Object> getNumImpressions() throws MongoAuthException{
        return getCountMetric(COL_IMPRESSIONS);
    }

    public static Map<String, Object> getNumClicks() throws MongoAuthException {
        return getCountMetric(COL_CLICKS);
    }

    public static Map<String, Object> getNumConversions() throws MongoAuthException {
        return getCountMetric(COL_SERVER);
    }

    public static Double getTotalCTR() throws MongoAuthException {
        double clicks = getTotalClicks();
        double impressions = getTotalImpressions();

        return clicks/impressions;
    }

    public static Integer getTotalClicks() throws MongoAuthException {
        return getTotalMetric(COL_CLICKS, GET_TOTAL_NUM);
    }

    public static Integer getTotalImpressions() throws MongoAuthException {
        return getTotalMetric(COL_IMPRESSIONS, GET_TOTAL_NUM);
    }

    public static Double getTotalCostImpressions() throws MongoAuthException {
        return Double.parseDouble(getTotalMetric(COL_IMPRESSIONS, GET_TOTAL_COST).toString());
    }

    public static Double getTotalCostClicks() throws MongoAuthException {
        return Double.parseDouble(getTotalMetric(COL_CLICKS, GET_TOTAL_COST).toString());
    }

    public static Double getTotalCostCampaign() throws MongoAuthException {
        return getTotalCostImpressions() + getTotalCostClicks();
    }

    private static Integer getTotalMetric(String collection, String metric) throws MongoAuthException {
        DBHandler handler = DBHandler.getDBConnection();
        fieldModifier = new BasicDBObject();
        fieldModifier.put("total", 1);

        JSONObject jsonResult = new JSONObject(
                handler.sendQuery(
                        ALL_QUERY,
                        fieldModifier,
                        collection
                ).get(0).toString()
        );

        jsonResult.remove("_id");
        return jsonResult.getInt(metric);
    }

    private static Map<String, Object> getCountMetric(String collection) throws MongoAuthException {
        DBHandler handler = DBHandler.getDBConnection();
        fieldModifier = new BasicDBObject();
        fieldModifier.put("dayNum", 1);

        JSONObject jsonResult = new JSONObject(
                handler.sendQuery(
                        ALL_QUERY,
                        fieldModifier,
                        collection
                ).get(0).toString()
        );

        jsonResult.remove("_id");
        jsonResult = jsonResult.getJSONObject("dayNum");

        return jsonResult.toMap();
    }

    public static Map<String, Object> getCTROverTime() throws MongoAuthException {
        Map<String, Object> numImpressions = getNumImpressions();
        Map<String, Object> numClicks = getNumClicks();

        Map<String, Map<String, Double>> ctrMap = new HashMap<>();
        Map<String, Double> hourCtrMap = new HashMap<>();

        for (String day : numImpressions.keySet()) {

        }
        return null;
    }
}
