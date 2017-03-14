package org.soton.seg7.ad_analytics.model;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.joda.time.DateTime;
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

    public static ArrayList<Double> getAllClickCosts() throws MongoAuthException {
        ArrayList<Double> allClickCosts = new ArrayList<Double>();

        DBHandler handler = DBHandler.getDBConnection();
        fieldModifier = new BasicDBObject()
                .append("Click Cost", 1);

        List<DBObject> results = handler.sendQuery(
                ALL_QUERY,
                fieldModifier,
                DATA_CLICKS
        );

        for(DBObject val : results) {
            String cost = val.toString().split(",")[1].split(" ")[4].split("}")[0];
            allClickCosts.add(Double.parseDouble(cost));
        }

        return allClickCosts;
    }

    public static Map<String, Map<String, Double>> getNumImpressions(Integer filter) throws MongoAuthException {
        if (filter == Filters.NO_FILTER)
            return getCountMetric(COL_IMPRESSIONS);

        DBHandler handler = DBHandler.getDBConnection();
        DBObject query = getQueryFilter(filter);
        List<DBObject> results =  new ArrayList<>();
        handler.getCollection(DATA_IMPRESSIONS).aggregate(Arrays.asList(
                new BasicDBObject("$match", query),
                new BasicDBObject("$group",
                        new BasicDBObject("_id", new BasicDBObject("year", new BasicDBObject("$year", "$Date"))
                                .append("month", new BasicDBObject("$month", "$Date"))
                                .append("day", new BasicDBObject("$dayOfMonth", "$Date"))
                                .append("hour", new BasicDBObject("$hour", "$Date")))
                                .append("num", new BasicDBObject("$sum", 1)))))
                .results().forEach(results::add);

        return buildResultsMap(results, filter, COUNT_METRIC);
    }

    public static Map<String, Map<String, Double>> getCPAOverTime(Integer filter) throws MongoAuthException {
        Map<String, Map<String, Double>> costImpressions = getImpressionCostOverTime(filter);
        Map<String, Map<String, Double>> costClicks = getClickCostOverTime();
        Map<String, Map<String, Double>> numConversions = getNumConversions();
        Map<String, Map<String, Double>> cpa = new HashMap<>();

        for (String day : costImpressions.keySet()) {
            Map<String, Double> hourImpression = costImpressions.get(day);
            Map<String, Double> hourClicks = costClicks.get(day);
            Map<String, Double> hourConversions = numConversions.get(day);

            cpa.put(day,
                    Stream.concat(hourImpression.keySet().stream(), hourClicks.keySet().stream())
                            .distinct()
                            .collect(Collectors.toMap(k->k ,(k -> (hourImpression.getOrDefault(k,0d) + hourClicks.getOrDefault(k,0d))/hourConversions.getOrDefault(k,1d) ))));
        }

        return cpa;
    }

    public static Map<String, Map<String, Double>> getNumClicks() throws MongoAuthException {
        return getCountMetric(COL_CLICKS);
    }

    public static Map<String, Map<String, Double>> getNumConversions() throws MongoAuthException {
        return getCountMetric(COL_SERVER);
    }

    public static Map<String, Map<String, Double>> getClickCostOverTime() throws MongoAuthException {
        return getCostMetric(COL_CLICKS);
    }

    public static Map<String, Map<String, Double>> getImpressionCostOverTime(Integer filter) throws MongoAuthException {
        if (filter == Filters.NO_FILTER)
            return getCostMetric(COL_IMPRESSIONS);

        DBHandler handler = DBHandler.getDBConnection();
        DBObject query = getQueryFilter(filter);
        List<DBObject> results =  new ArrayList<>();
        handler.getCollection(DATA_IMPRESSIONS).aggregate(Arrays.asList(
                new BasicDBObject("$match", query),
                new BasicDBObject("$group",
                        new BasicDBObject("_id", new BasicDBObject("year", new BasicDBObject("$year", "$Date"))
                                .append("month", new BasicDBObject("$month", "$Date"))
                                .append("day", new BasicDBObject("$dayOfMonth", "$Date"))
                                .append("hour", new BasicDBObject("$hour", "$Date")))
                                .append("cost", new BasicDBObject("$sum", "$Impression Cost")))))
                .results().forEach(results::add);

        return buildResultsMap(results, filter, COST_METRIC);
    }

    public static Map<String, Map<String, Double>> getTotalCostOverTime(Integer filter) throws MongoAuthException {
        Map<String, Map<String, Double>> clickCost = getClickCostOverTime();
        Map<String, Map<String, Double>> impressionCost = getImpressionCostOverTime(filter);
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

    public static Map<String, Map<String, Double>> getCTROverTime(Integer filter) throws MongoAuthException {
        Map<String, Map<String, Double>> numImpressions = getNumImpressions(filter);
        Map<String, Map<String, Double>> numClicks = getNumClicks();

        Map<String, Map<String, Double>> ctrMap = new HashMap<>();

        for (String day : numImpressions.keySet()) {
            Map<String, Double> impressionsHour = numImpressions.get(day);
            Map<String, Double> clicksHour = numClicks.get(day);

            Map<String, Double> ctrHour = Stream.concat(clicksHour.keySet().stream(), impressionsHour.keySet().stream())
                    .distinct()
                    .collect(Collectors.toMap(k -> k, k -> clicksHour.getOrDefault(k,0d) / impressionsHour.getOrDefault(k,0d)));

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

        List<DBObject> results =
                handler.sendQuery(
                        ALL_QUERY,
                        fieldModifier,
                        COL_SERVER
                );

        for (DBObject entry : results){
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

    public static Double getTotalCTR(Integer filter) throws MongoAuthException {
        double clicks = getTotalNumClicks();
        double impressions = getTotalNumImpressions(filter);

        return clicks/impressions;
    }

    public static Double getTotalNumClicks() throws MongoAuthException {
        return getTotalMetric(OP_COUNT, COUNT_AGGREGATE, COL_CLICKS, Filters.NO_FILTER);
    }

    public static Double getTotalNumImpressions(Integer filter) throws MongoAuthException {
        return getTotalMetric(OP_COUNT, COUNT_AGGREGATE, COL_IMPRESSIONS, filter);
    }

    public static Double getTotalCostImpressions(Integer filter) throws MongoAuthException {
        return getTotalMetric(OP_SUM, COST_AGGREGATE, COL_IMPRESSIONS, filter);
    }

    public static Double getTotalCostClicks() throws MongoAuthException {
        return getTotalMetric(OP_SUM, COST_AGGREGATE, COL_CLICKS, Filters.NO_FILTER);
    }

    public static Double getTotalCostCampaign(Integer filter) throws MongoAuthException {
        return getTotalCostImpressions(filter) + getTotalCostClicks();
    }

    private static Map<String, Map<String, Double>> getCountMetric(String collection) throws MongoAuthException {
        DBHandler handler = DBHandler.getDBConnection();
        fieldModifier = new BasicDBObject()
                .append(DATE,1)
                .append(COUNT_METRIC, 1);

        List<DBObject> results = handler.sendQuery(
                ALL_QUERY,
                fieldModifier,
                collection
        );

       return buildResultsMap(results, Filters.NO_FILTER, COUNT_METRIC);
    }

    private static Map<String, Map<String, Double>> getCostMetric(String collection) throws MongoAuthException {
        DBHandler handler = DBHandler.getDBConnection();
        fieldModifier = new BasicDBObject();
        fieldModifier.put(COST_METRIC, 1);
        fieldModifier.put(DATE, 1);

        Map<String, Map<String, Double>> mapCost = new HashMap<>();
        Map<String, Double> hourCost = new HashMap<>();

        List<DBObject> results = handler.sendQuery(
                ALL_QUERY,
                fieldModifier,
                collection
        );
        return buildResultsMap(results, Filters.NO_FILTER, COST_METRIC);
    }

    private static Double getTotalMetric(String op, String metric, String collection, Integer filter) throws MongoAuthException {
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
        Integer age, income, gender, context;
        BasicDBObject query = new BasicDBObject();

        if (filter == 0)
            return ALL_QUERY;

        age = filter % 10;
        filter /= 10;
        income = (filter % 10) * 10;
        filter /= 10;
        gender = (filter % 10) * 100;
        filter /= 10;
        context = (filter % 10) * 1000;

        if (age != 0)
            query.append("Age", (age == Filters.AGE_25) ? "<25"
                    : (age == Filters.AGE_25_34) ? "25-34"
                    : (age == Filters.AGE_35_44) ? "35-44"
                    : (age == Filters.AGE_45_54) ? "45-54"
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
        if (context != 0)
            query.append("Context", (context == Filters.CONTEXT_BLOG) ? "Blog"
                    : (context == Filters.CONTEXT_NEWS) ? "News"
                    : (context == Filters.CONTEXT_SHOPPING) ? "Shopping"
                    : (context == Filters.CONTEXT_SOCIAL_MEDIA) ? "Social Media"
                    : "null");

        return query;
    }

    private static DateTime buildDateKey(BasicDBObject bson, Integer filter) throws java.text.ParseException {
        DateTime dateKey;
        if (filter == 0)
            dateKey = new DateTime(new Date(bson.getDate("date").toString()));
        else {
            String month;
            String day;
            String hour;
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            BasicDBObject dateObj = (BasicDBObject) bson.get("_id");
            String dateString = String.format("%s-%s-%s %s:00:00",
                    dateObj.getString("year"),
                    ((month = dateObj.getString("month")).length()==1)
                            ? "0" + month
                            : month,
                    ((day = dateObj.getString("day")).length()==1)
                            ? "0" + day
                            : day,
                    ((hour = dateObj.getString("hour")).length()==1)
                            ? "0" + hour
                            : hour);
            Date date = df.parse(dateString);
            dateKey = new DateTime(date);
        }

        return dateKey;
    }

    private static Map<String, Map<String, Double>> buildResultsMap(List<DBObject> results, Integer filter, String metric) {
        Map<String, Map<String, Double>> mapCost = new HashMap<>();
        for (DBObject entry : results) {
            BasicDBObject bson = (BasicDBObject) entry;
            DateTime dateKey = null;
            try {
                dateKey = buildDateKey(bson, filter);

                if (mapCost.containsKey(dateKey.toLocalDate().toString()))
                    mapCost.get(dateKey.toLocalDate().toString()).put(dateKey.getHourOfDay()+"", bson.getDouble(metric));
                else {
                    Map<String, Double> map = new HashMap<>();
                    map.put(dateKey.getHourOfDay()+"", bson.getDouble(metric));
                    mapCost.put(dateKey.toLocalDate().toString(), map);
                }
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
        }
        return mapCost;
    }
}
