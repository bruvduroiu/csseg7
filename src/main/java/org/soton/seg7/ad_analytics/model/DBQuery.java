package org.soton.seg7.ad_analytics.model;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.soton.seg7.ad_analytics.model.exceptions.MongoAuthException;

import java.util.HashMap;
import java.util.Iterator;
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

    public static Map<String, Map<String, Integer>> getNumImpressions() throws MongoAuthException {
        return getCountMetric(COL_IMPRESSIONS);
    }

    public static Map<String, Map<String, Integer>> getNumClicks() throws MongoAuthException {
        return getCountMetric(COL_CLICKS);
    }

    public static Map<String, Map<String, Integer>> getNumConversions() throws MongoAuthException {
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
        fieldModifier.put(metric, 1);

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

    private static Map<String, Map<String, Integer>> getCountMetric(String collection) throws MongoAuthException {
        DBHandler handler = DBHandler.getDBConnection();
        fieldModifier = new BasicDBObject();
        fieldModifier.put("dayNum", 1);

        Map<String, Map<String, Integer>> countMap = new HashMap<>();

        JSONObject jsonResult = new JSONObject(
                handler.sendQuery(
                        ALL_QUERY,
                        fieldModifier,
                        collection
                ).get(0).toString()
        );

        jsonResult.remove("_id");
        jsonResult = jsonResult.getJSONObject("dayNum");

        Iterator<?> keys = jsonResult.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            String value = jsonResult.get(key).toString();
            value = value.substring(1, value.length()-1);           //remove curly brackets
            String[] keyValuePairs = value.split(",");              //split the string to creat key-value pairs
            Map<String,Integer> map = new HashMap<>();

            for(String pair : keyValuePairs)                        //iterate over the pairs
            {
                String[] entry = pair.split(":");                   //split the pairs to get key and value
                map.put(entry[0].trim().replace("\"",""), Integer.parseInt(entry[1].trim()));          //add them to the hashmap and trim whitespaces
            }
            countMap.put(key, map);
        }

        System.out.println(countMap);

        return countMap;

    }

    public static Map<String, Map<String, Double>> getCTROverTime() throws MongoAuthException {
        Map<String, Map<String, Integer>> numImpressions = getNumImpressions();
        Map<String, Map<String, Integer>> numClicks = getNumClicks();

        Map<String, Map<String, Double>> ctrMap = new HashMap<>();

        for (String day : numImpressions.keySet()) {
            Map<String, Double> hourCtrMap = new HashMap<>();
            Map<String, Integer> impressionsHour = numImpressions.get(day);
            Map<String, Integer> clicksHour = numClicks.get(day);

            for (String hour : impressionsHour.keySet())
                hourCtrMap.put(hour, Double.parseDouble(clicksHour.get(hour).toString())/Double.parseDouble(impressionsHour.get(hour).toString()));

            ctrMap.put(day, hourCtrMap);
        }
        return ctrMap;
    }
}
