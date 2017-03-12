package org.soton.seg7.ad_analytics.model;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.soton.seg7.ad_analytics.model.exceptions.MongoAuthException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by bogdanbuduroiu on 25/02/2017.
 */
public class DBQuery {

    private static final String COL_IMPRESSIONS = "impression_log";
    private static final String COL_CLICKS = "click_log";
    private static final String COL_SERVER = "server_log";
    private static final String DATA_IMPRESSIONS = "impression_data";
    private static final String DATA_CLICKS = "click_data";
    private static final String DATA_SERVER = "server_data";

    private static final String COST_METRIC = "cost";
    private static final String COUNT_METRIC = "num";

    private static final String COST_AGGREGATE = "$cost";
    private static final String COUNT_AGGREGATE = "$num";

    private static final String DATE = "date";

    private static final String OP_SUM = "$sum";
    private static final String OP_COUNT = "$count";

    private static final String BOUNCE_COND_PAGE = "bounceRatePage";
    private static final String BOUNCE_COND_TIME = "bounceRateTime";


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

    public static Map<String, Map<String, Double>> getTotalCostOverTime() throws MongoAuthException {
        Map<String, Map<String, Double>> clickCost = getClickCostOverTime();
        Map<String, Map<String, Double>> impressionCost = getImpressionCostOverTime();
        Map<String, Map<String, Double>> totalCost = new HashMap<>();

        for (String date : impressionCost.keySet()) {
            Map<String, Double> hourClickCost = clickCost.get(date);
            Map<String, Double> hourImpressionCost = impressionCost.get(date);

            Map<String, Double> hourTotalCost = Stream.concat(hourClickCost.keySet().stream(), hourImpressionCost.keySet().stream())
                    .distinct()
                    .collect(Collectors.toMap(k -> k, k -> hourClickCost.getOrDefault(k,0d) + hourImpressionCost.getOrDefault(k,0d)));

            totalCost.put(date, hourTotalCost);
        }
        return totalCost;
    }

    public static Map<String, Map<String, Double>> getCTROverTime() throws MongoAuthException {
        Map<String, Map<String, Integer>> numImpressions = getNumImpressions();
        Map<String, Map<String, Integer>> numClicks = getNumClicks();

        Map<String, Map<String, Double>> ctrMap = new HashMap<>();

        for (String day : numImpressions.keySet()) {
            Map<String, Integer> impressionsHour = numImpressions.get(day);
            Map<String, Integer> clicksHour = numClicks.get(day);

            Map<String, Double> ctrHour = Stream.concat(clicksHour.keySet().stream(), impressionsHour.keySet().stream())
                    .distinct()
                    .collect(Collectors.toMap(k -> k, k -> ((double)clicksHour.getOrDefault(k,0)) / ((double) impressionsHour.getOrDefault(k,0))));

            ctrMap.put(day, ctrHour);
        }
        return ctrMap;
    }

    public static Map<String, Map<String, Double>> getBounceRate(String condition) throws MongoAuthException {
        DBHandler handler = DBHandler.getDBConnection();
        fieldModifier = new BasicDBObject();
        fieldModifier.put(condition, 1);

        Map<String, Map<String, Double>> dayMap = new HashMap<>();
        Map<String, Double> hourMap = new HashMap<>();

        JSONArray jsonArrayResult =
                handler.sendQuery(
                        ALL_QUERY,
                        fieldModifier,
                        COL_SERVER
                );

        for (Object entry : jsonArrayResult){
            BasicDBObject bson = (BasicDBObject) entry;
            DateTime dateKey = new DateTime(new Date(bson.getDate("date").toString()));

            if (!dayMap.containsKey(dateKey.toLocalDate().toString()))
                hourMap = new HashMap<>();

            hourMap.put(dateKey.getHourOfDay()+"", bson.getDouble(condition));
            dayMap.put(dateKey.toLocalDate().toString(), hourMap);
        }

        return dayMap;
    }

    public static Map<String, Map<String, Double>> getBounceRateByTime() throws MongoAuthException {
        return getBounceRate("");
    }

    public static Map<String, Map<String, Double>> getBounceRateByPage() throws MongoAuthException {
        return getBounceRate("");
    }

    public static Double getTotalCTR() throws MongoAuthException {
        double clicks = getTotalNumClicks();
        double impressions = getTotalNumImpressions();

        return clicks/impressions;
    }

    public static Double getTotalNumClicks() throws MongoAuthException {
        return getTotalMetric(OP_COUNT, COUNT_AGGREGATE, COL_CLICKS);
    }

    public static Double getTotalNumImpressions() throws MongoAuthException {
        return getTotalMetric(OP_COUNT, COUNT_AGGREGATE, COL_IMPRESSIONS);
    }

    public static Double getTotalCostImpressions() throws MongoAuthException {
        return getTotalMetric(OP_SUM, COST_AGGREGATE, COL_IMPRESSIONS);
    }

    public static Double getTotalCostClicks() throws MongoAuthException {
        return getTotalMetric(OP_SUM, COST_AGGREGATE, COL_CLICKS);
    }

    public static Double getTotalCostCampaign() throws MongoAuthException {
        return getTotalCostImpressions() + getTotalCostClicks();
    }

    private static Map<String, Map<String, Integer>> getCountMetric(String collection) throws MongoAuthException {
        DBHandler handler = DBHandler.getDBConnection();
        fieldModifier = new BasicDBObject()
                .append(DATE,1)
                .append(COUNT_METRIC, 1);

        JSONArray jsonArrayResult = handler.sendQuery(
                ALL_QUERY,
                fieldModifier,
                collection
        );

        Map<String, Map<String,Integer>> mapCount = new HashMap<>();

        for (Object entry : jsonArrayResult) {
            BasicDBObject bson = (BasicDBObject) entry;
            DateTime dateKey = buildDateKey(bson, filter);

            if (mapCount.containsKey(dateKey.toLocalDate().toString()))
                mapCount.get(dateKey.toLocalDate().toString()).put(dateKey.getHourOfDay()+"", bson.getInt("num"));
            else {
                Map<String, Integer> map = new HashMap<>();
                map.put(dateKey.getHourOfDay()+"", bson.getInt("num"));
                mapCount.put(dateKey.toLocalDate().toString(), map);
            }
        }

        return mapCount;
    }

    private static Map<String, Map<String, Double>> getCostMetric(String collection) throws MongoAuthException {
        DBHandler handler = DBHandler.getDBConnection();
        fieldModifier = new BasicDBObject();
        fieldModifier.put(COST_METRIC, 1);
        fieldModifier.put(DATE, 1);

        Map<String, Map<String, Double>> mapCost = new HashMap<>();
        Map<String, Double> hourCost = new HashMap<>();

        JSONArray jsonArrayResult = handler.sendQuery(
                ALL_QUERY,
                fieldModifier,
                collection
        );

        for (Object entry : jsonArrayResult) {
            BasicDBObject bson = (BasicDBObject) entry;
            DateTime dateKey = buildDateKey(bson, filter);

            if (mapCost.containsKey(dateKey.toLocalDate().toString()))
                mapCost.get(dateKey.toLocalDate().toString()).put(dateKey.getHourOfDay()+"", bson.getDouble("cost"));
            else {
                Map<String, Double> map = new HashMap<>();
                map.put(dateKey.getHourOfDay()+"", bson.getDouble("cost"));
                mapCost.put(dateKey.toLocalDate().toString(), map);
            }
        }

        return mapCost;
    }

    private static Double getTotalMetric(String op, String metric, String collection) throws MongoAuthException {
        DBHandler handler = DBHandler.getDBConnection();
        List<DBObject> pipeline = new ArrayList<>();
        DBObject group_metric = new BasicDBObject();
        if (op.equals(OP_SUM))
            group_metric = new BasicDBObject(
                    "$group", new BasicDBObject("_id", null).append(
                    "total", new BasicDBObject(op, metric)
            ));
        else if (op.equals(OP_COUNT))
            group_metric = new BasicDBObject(op, "total");

        pipeline.add(group_metric);

        Iterable<DBObject> results = handler.getCollection(collection).aggregate(Arrays.asList(
                new BasicDBObject("$match", getQueryFilter(filter)),
                group_metric
        )).results();

        BasicDBObject result = (BasicDBObject) results.iterator().next();

        return result.getDouble("total");
    }

    private static DBObject getQueryFilter(Integer filter) {
        Integer age, income, gender;
        BasicDBObject query = new BasicDBObject();

        if (filter == 0)
            return ALL_QUERY;

        age = filter % 10;
        filter /= 10;
        income = (filter % 10) * 10;
        filter /= 10;
        gender = (filter % 10) * 100;

        if (age != 0)
            query.append("Age", (age == Filters.AGE_25) ? "<25"
                    : (age == Filters.AGE_25_34) ? "25-34"
                    : (age == Filters.AGE_35_54) ? "35-54"
                    : (age == Filters.AGE_54) ? ">54"
                    : "null");
        if (income != 0)
            query.append("Income", (income == Filters.INCOME_LOW) ? "Low"
                    : (income == Filters.INCOME_MEDIUM) ? "Medium"
                    : (income == Filters.INCOME_HIGH) ? "High"
                    : "null");
        if (gender != 0)
            query.append("Gender", (gender == Filters.GENDER_MALE) ? 'M'
                    : (gender == Filters.GENDER_FEMALE) ? 'F'
                    : "null");

        return query;
    }

    private static DateTime buildDateKey(BasicDBObject bson, Integer filter) {
        DateTime dateKey;
        if (filter == 0)
            dateKey = new DateTime(new Date(bson.getDate("date").toString()));
        else {
            BasicDBObject dateObj = (BasicDBObject) bson.get("_id");
            String dateString = String.format("%s-%s-%s",
                    dateObj.getString("year"),
                    dateObj.getString("month"),
                    dateObj.getString("day"));
            dateKey = new DateTime(new Date(dateString));
        }

        return dateKey;
    }
}
