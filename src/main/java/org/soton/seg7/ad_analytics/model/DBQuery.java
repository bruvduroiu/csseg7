package org.soton.seg7.ad_analytics.model;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import org.json.JSONObject;
import org.soton.seg7.ad_analytics.model.exceptions.MongoAuthException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;

/**
 * Created by bogdanbuduroiu on 25/02/2017.
 */
public class DBQuery {

    private static final String COL_IMPRESSIONS = "impression_log";
    private static final String COL_CLICKS = "click_log";
    private static final String COL_SERVER = "server_log";

    private static final String GET_TOTAL_NUM = "totalNum";
    private static final String GET_TOTAL_COST = "totalCost";

    private static final String COUNT_METRIC = "dayNum";
    private static final String COST_METRIC = "dayCost";

    private static final String SINGLE_CLICK_COST = "individualCost";

    private static final String BOUNCE_TIME = "dayBounceTime";
    private static final String BOUNCE_PAGE = "dayBouncePage";

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

    public static Map<String, Map<String, Double>> getClickCostOverTime() throws MongoAuthException {
        return getCostMetric(COL_CLICKS);
    }

    public static Map<String, Map<String, Double>> getImpressionCostOverTime() throws MongoAuthException {
        return getCostMetric(COL_IMPRESSIONS);
    }

    public static ArrayList<Double> getAllClickCosts() throws MongoAuthException {
        return getAllCostsMetric(COL_CLICKS);
    }

    private static ArrayList<Double> getAllCostsMetric(String collection) throws MongoAuthException {
        DBHandler handler = DBHandler.getDBConnection();
        fieldModifier = new BasicDBObject();
        fieldModifier.put(SINGLE_CLICK_COST, 1);

        ArrayList<Double> costArr = new ArrayList<>();

        JSONObject jsonResult = new JSONObject(
                handler.sendQuery(
                        ALL_QUERY,
                        fieldModifier,
                        collection
                ).get(0).toString()
        );

        jsonResult.remove("_id");
        jsonResult = jsonResult.getJSONObject(SINGLE_CLICK_COST);

        Iterator<?> keys = jsonResult.keys();

        while (keys.hasNext()) {

            String key = (String) keys.next();
            costArr.add(Double.parseDouble(jsonResult.get(key).toString()));
        }

        System.out.println(costArr);

        return costArr;
    }



    public static Map<String, Map<String, Double>> getTotalCostOverTime() throws MongoAuthException {
        Map<String, Map<String, Double>> costImpressions = getImpressionCostOverTime();
        Map<String, Map<String, Double>> costClicks = getClickCostOverTime();

        Map<String, Map<String, Double>> totalMap  = new HashMap<>();

        for (String day : costImpressions.keySet()) {
            Map<String, Double> hourCostMap = new HashMap<>();
            Map<String, Double> impressionCostHour = costImpressions.get(day);
            Map<String, Double> clickCostHour = costClicks.get(day);

            for (String hour : impressionCostHour.keySet()) {
                hourCostMap.put(
                        hour,
                        /**
                         * ****************************************************************************
                         * This line below replaces a null value in the map with 0.
                         */
                        (clickCostHour.get(hour) == null ? 0d : clickCostHour.get(hour)) +
                                (impressionCostHour.get(hour) == null ? 0d : impressionCostHour.get(hour))
                        //****************************************************************************
                );
            }

            totalMap.put(day, hourCostMap);
        }
        return totalMap;
    }

    public static Map<String, Map<String, Double>> getCTROverTime() throws MongoAuthException {
        Map<String, Map<String, Integer>> numImpressions = getNumImpressions();
        Map<String, Map<String, Integer>> numClicks = getNumClicks();

        Map<String, Map<String, Double>> ctrMap = new HashMap<>();

        for (String day : numImpressions.keySet()) {
            Map<String, Double> hourCtrMap = new HashMap<>();
            Map<String, Integer> impressionsHour = numImpressions.get(day);
            Map<String, Integer> clicksHour = numClicks.get(day);

            for (String hour : impressionsHour.keySet()) {
                if (clicksHour.get(hour) == null) {
                    hourCtrMap.put(hour, 0d);
                } else if (impressionsHour.get(hour) == null) {
                    // Skip
                } else {
                    hourCtrMap.put(hour, Double.parseDouble(clicksHour.get(hour).toString()) / Double.parseDouble(impressionsHour.get(hour).toString()));
                }
            }

            ctrMap.put(day, hourCtrMap);
        }
        return ctrMap;
    }

    public static Map<String, Map<String, Double>> getBounceRate(String condition) throws MongoAuthException {
        DBHandler handler = DBHandler.getDBConnection();
        fieldModifier = new BasicDBObject();
        fieldModifier.put(condition, 1);

        Map<String, Map<String, Double>> bounceMap = new HashMap<>();

        JSONObject jsonResult = new JSONObject(
                handler.sendQuery(
                        ALL_QUERY,
                        fieldModifier,
                        COL_SERVER
                ).get(0).toString()
        );

        jsonResult.remove("_id");
        jsonResult = jsonResult.getJSONObject(condition);

        Iterator<?> keys = jsonResult.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            String value = jsonResult.get(key).toString();
            value = value.substring(1, value.length()-1);           //remove curly brackets
            String[] keyValuePairs = value.split(",");              //split the string to creat key-value pairs
            Map<String,Double> map = new HashMap<>();

            for(String pair : keyValuePairs)                        //iterate over the pairs
            {
                String[] entry = pair.split(":");                   //split the pairs to get key and value
                map.put(entry[0].trim().replace("\"",""), Double.parseDouble(entry[1].trim()));          //add them to the hashmap and trim whitespaces
            }
            bounceMap.put(key, map);
        }

        System.out.println(bounceMap);

        return bounceMap;
    }

    public static Map<String, Map<String, Double>> getBounceRateByTime() throws MongoAuthException {
        return getBounceRate(BOUNCE_TIME);
    }

    public static Map<String, Map<String, Double>> getBounceRateByPage() throws MongoAuthException {
        return getBounceRate(BOUNCE_PAGE);
    }

    public static Double getTotalCTR() throws MongoAuthException {
        double clicks = getTotalNumClicks();
        double impressions = getTotalNumImpressions();

        return clicks/impressions;
    }

    public static Double getTotalNumClicks() throws MongoAuthException {
        return getTotalMetric(COL_CLICKS, GET_TOTAL_NUM);
    }

    public static Double getTotalNumImpressions() throws MongoAuthException {
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

    private static Map<String, Map<String, Integer>> getCountMetric(String collection) throws MongoAuthException {
        DBHandler handler = DBHandler.getDBConnection();
        fieldModifier = new BasicDBObject();
        fieldModifier.put(COUNT_METRIC ,1);

        Map<String, Map<String, Integer>> countMap = new HashMap<>();

        JSONObject jsonResult = new JSONObject(
                handler.sendQuery(
                        ALL_QUERY,
                        fieldModifier,
                        collection
                ).get(0).toString()
        );

        jsonResult.remove("_id");
        jsonResult = jsonResult.getJSONObject(COUNT_METRIC);

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

    private static Map<String, Map<String, Double>> getCostMetric(String collection) throws MongoAuthException {
        DBHandler handler = DBHandler.getDBConnection();
        fieldModifier = new BasicDBObject();
        fieldModifier.put(COST_METRIC, 1);

        Map<String, Map<String, Double>> countMap = new HashMap<>();

        JSONObject jsonResult = new JSONObject(
                handler.sendQuery(
                        ALL_QUERY,
                        fieldModifier,
                        collection
                ).get(0).toString()
        );

        jsonResult.remove("_id");
        jsonResult = jsonResult.getJSONObject(COST_METRIC);

        Iterator<?> keys = jsonResult.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            String value = jsonResult.get(key).toString();
            value = value.substring(1, value.length()-1);           //remove curly brackets
            String[] keyValuePairs = value.split(",");              //split the string to creat key-value pairs
            Map<String,Double> map = new HashMap<>();

            for(String pair : keyValuePairs)                        //iterate over the pairs
            {
                String[] entry = pair.split(":");                   //split the pairs to get key and value
                map.put(entry[0].trim().replace("\"",""), Double.parseDouble(entry[1].trim()));          //add them to the hashmap and trim whitespaces
            }
            countMap.put(key, map);
        }

        System.out.println(countMap);

        return countMap;
    }

    private static Double getTotalMetric(String collection, String metric) throws MongoAuthException {
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
        return Double.parseDouble(jsonResult.get(metric).toString());
    }
}
