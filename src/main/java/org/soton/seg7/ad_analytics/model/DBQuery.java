package org.soton.seg7.ad_analytics.model;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.soton.seg7.ad_analytics.model.exceptions.MongoAuthException;

import javax.persistence.Basic;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.logging.Filter;
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
    private static final String OP_AVG = "$avg";

    private static final String BOUNCE_COND_PAGE = "bounceRatePage";
    private static final String BOUNCE_COND_TIME = "bounceRateTime";

    public static final int GRANULARITY_HOUR = 3;
    public static final int GRANULARITY_DAY = 2;
    public static final int GRANULARITY_MONTH = 1;
    private static int granularity = GRANULARITY_DAY;

    private static DateTime startDate;
    private static DateTime endDate;

    public static void setDateRange(DateTime startDate, DateTime endDate) {
        DBQuery.startDate = startDate;
        DBQuery.endDate = endDate;
    }

    public static int getGranularity() {
        return granularity;
    }
    
    public static void setGranularity(int granularity) {
	    if (granularity > GRANULARITY_HOUR || granularity < GRANULARITY_MONTH)
	        return;
	    DBQuery.granularity = granularity;
	}

    private static final BasicDBObject ALL_QUERY = new BasicDBObject();
    private static BasicDBObject fieldModifier;

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
            BasicDBObject bson = (BasicDBObject) val;
            allClickCosts.add(bson.getDouble("Click Cost"));
        }

        return allClickCosts;
    }

    public static Map<DateTime, Double> getNumImpressions(Integer filter) throws MongoAuthException {
        if (filter == Filters.NO_FILTER)
            return getCountMetric(COL_IMPRESSIONS);

        List<DBObject> results =  new ArrayList<>();

        DBHandler handler = DBHandler.getDBConnection();

        List<BasicDBObject> query = new ArrayList<>();
        query.add(getQueryFilter(filter));
        getDateFilterQuery().forEach(query::add);

        handler.getCollection(DATA_IMPRESSIONS).aggregate(Arrays.asList(
                new BasicDBObject("$match", new BasicDBObject("$and", query)),
                new BasicDBObject("$group",
                        new BasicDBObject("_id", getGranularityAggregate())
                                .append("num", new BasicDBObject("$sum", 1)))))
                .results().forEach(results::add);

        return buildResultsMap(results, COUNT_METRIC);
    }

    @Deprecated
    public static Map<DateTime, Double> getCPAOverTime(Integer filter) throws MongoAuthException {
        Map<DateTime, Double> costImpressions = getImpressionCostOverTime(filter);
        Map<DateTime, Double> costClicks = getClickCostOverTime();
        Map<DateTime, Double> numConversions = getNumConversions();

        return Stream.concat(costImpressions.keySet().stream(), costClicks.keySet().stream())
                .distinct()
                .collect(Collectors.toMap(k->k, k->((numConversions.get(k)==0) ? 0 :(costImpressions.getOrDefault(k,0d) + costClicks.getOrDefault(k,0d))/numConversions.getOrDefault(k,1d))));
    }

    public static Map<DateTime, Double> getNumClicks() throws MongoAuthException {
        return getCountMetric(COL_CLICKS);
    }

    public static Map<DateTime, Double> getNumConversions() throws MongoAuthException {
        return getCountMetric(COL_SERVER);
    }

    public static Map<DateTime, Double> getClickCostOverTime() throws MongoAuthException {
        return getCostMetric(COL_CLICKS);
    }

    public static Map<DateTime, Double> getImpressionCostOverTime(Integer filter) throws MongoAuthException {
        if (filter == Filters.NO_FILTER)
            return getCostMetric(COL_IMPRESSIONS);

        DBHandler handler = DBHandler.getDBConnection();
        List<BasicDBObject> query = new ArrayList<>();
        query.add(getQueryFilter(filter));
        getDateFilterQuery().forEach(query::add);

        List<DBObject> results =  new ArrayList<>();

        handler.getCollection(DATA_IMPRESSIONS).aggregate(Arrays.asList(
                new BasicDBObject("$match", new BasicDBObject("$and", query)),
                new BasicDBObject("$group",
                        new BasicDBObject("_id", getGranularityAggregate())
                                .append("cost", new BasicDBObject("$sum", "$Impression Cost")))))
                .results().forEach(results::add);

        return buildResultsMap(results, COST_METRIC);
    }

    public static Map<DateTime, Double> getTotalCostOverTime(Integer filter) throws MongoAuthException {
        Map<DateTime, Double> clickCost = getClickCostOverTime();
        Map<DateTime, Double> impressionCost = getImpressionCostOverTime(filter);

        return Stream.concat(clickCost.keySet().stream(), impressionCost.keySet().stream())
                .distinct()
                .collect(Collectors.toMap(k->k, k->clickCost.getOrDefault(k,0d) + impressionCost.getOrDefault(k,0d)));

    }

    @Deprecated
    public static Map<DateTime, Double> getCostPerThousandImpressionsOverTime(Integer filter) throws MongoAuthException {
        Map<DateTime, Double> totalCost = getClickCostOverTime();
        Map<DateTime, Double> numImpressions = getNumImpressions(filter);

        return Stream.concat(totalCost.keySet().stream(), numImpressions.keySet().stream())
                .distinct()
                .collect(Collectors.toMap(k->k, k->(totalCost.getOrDefault(k,0d) / numImpressions.getOrDefault(k,0d) * 1000)));

    }

    @Deprecated
    public static Map<DateTime, Double> getCTROverTime(Integer filter) throws MongoAuthException {
        Map<DateTime, Double> numImpressions = getNumImpressions(filter);
        Map<DateTime, Double> numClicks = getNumClicks();

        return Stream.concat(numImpressions.keySet().stream(), numClicks.keySet().stream())
                .distinct()
                .collect(Collectors.toMap(k -> k, k -> numClicks.getOrDefault(k, 0d) / numImpressions.getOrDefault(k,1d)));
    }

    public static Map<DateTime, Double> getBounceMetric(String operation, String condition) throws MongoAuthException {
        DBHandler handler = DBHandler.getDBConnection();

        List<DBObject> results =  new ArrayList<>();

        handler.getCollection(COL_SERVER).aggregate(Arrays.asList(
                new BasicDBObject("$match", new BasicDBObject("$and", getDateFilterQuery())),
                new BasicDBObject("$group",
                        new BasicDBObject("_id", getGranularityAggregate())
                                .append(condition, new BasicDBObject(operation, "$"+condition)))))
                .results().forEach(results::add);

        return buildResultsMap(results, condition);
    }

    public static Map<DateTime, Double> getBounceRateByTime() throws MongoAuthException {
        return getBounceMetric(OP_AVG, BOUNCE_COND_TIME);
    }

    public static Map<DateTime, Double> getBounceRateByPage() throws MongoAuthException {
        return getBounceMetric(OP_AVG, BOUNCE_COND_PAGE);
    }

    public static Map<DateTime, Double> getNumBouncesByTime() throws MongoAuthException {
        return getBounceMetric(OP_SUM, BOUNCE_COND_TIME);
    }

    public static Map<DateTime, Double> getNumBouncesByPage() throws MongoAuthException {
        return getBounceMetric(OP_SUM, BOUNCE_COND_PAGE);
    }

    public static Double getTotalCTR(Integer filter) throws MongoAuthException {
        double clicks = getTotalNumClicks();
        double impressions = getTotalNumImpressions(filter);

        return clicks/impressions;
    }

    public static Double getTotalNumClicks() throws MongoAuthException {
        return getTotalMetric(OP_SUM, COUNT_AGGREGATE, COL_CLICKS, Filters.NO_FILTER);
    }

    public static Double getTotalNumImpressions(Integer filter) throws MongoAuthException {
        return (filter == Filters.NO_FILTER)
                ? getTotalMetric(OP_SUM, COUNT_METRIC, COL_IMPRESSIONS, filter)
                : getTotalCountMetric(DATA_IMPRESSIONS, filter);
    }

    public static Double getTotalCostImpressions(Integer filter) throws MongoAuthException {
        String aggregate = (filter == Filters.NO_FILTER) ? COST_AGGREGATE : "$Impression Cost";
        String collection = (filter == Filters.NO_FILTER) ? COL_IMPRESSIONS : DATA_IMPRESSIONS;
        return getTotalMetric(OP_SUM, aggregate, collection, filter);
    }

    public static Double getTotalCostClicks() throws MongoAuthException {
        return getTotalMetric(OP_SUM, COST_AGGREGATE, COL_CLICKS, Filters.NO_FILTER);
    }

    public static Double getTotalCostCampaign(Integer filter) throws MongoAuthException {
        return getTotalCostImpressions(filter) + getTotalCostClicks();
    }

    private static Map<DateTime, Double> getCountMetric(String collection) throws MongoAuthException {
        DBHandler handler = DBHandler.getDBConnection();
        List<DBObject> results =  new ArrayList<>();

        handler.getCollection(collection).aggregate(Arrays.asList(
                new BasicDBObject("$match", new BasicDBObject("$and", getDateFilterQuery())),
                new BasicDBObject("$group",
                        new BasicDBObject("_id", getGranularityAggregate())
                                .append("num", new BasicDBObject("$sum", "$num")))))
                .results().forEach(results::add);

        return buildResultsMap(results, COUNT_METRIC);
    }

    private static Map<DateTime, Double> getCostMetric(String collection) throws MongoAuthException {
        DBHandler handler = DBHandler.getDBConnection();
        List<DBObject> results =  new ArrayList<>();

        handler.getCollection(collection).aggregate(Arrays.asList(
                new BasicDBObject("$match", new BasicDBObject("$and", getDateFilterQuery())),
                new BasicDBObject("$group",
                        new BasicDBObject("_id", getGranularityAggregate())
                                .append("cost", new BasicDBObject("$sum", "$cost")))))
                .results().forEach(results::add);

        return buildResultsMap(results, COST_METRIC);
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

        if (!results.iterator().hasNext())
            return Double.NaN;
        BasicDBObject result = (BasicDBObject) results.iterator().next();

        return result.getDouble("total");
    }

    private static Double getTotalCountMetric(String collection, Integer filter) throws MongoAuthException {
        DBHandler handler = DBHandler.getDBConnection();
        Iterable<DBObject> results = handler.getCollection(collection).aggregate(Arrays.asList(
                new BasicDBObject("$match", getQueryFilter(filter)),
                new BasicDBObject("$count", "num")
        )).results();

        BasicDBObject result = (BasicDBObject) results.iterator().next();

        return result.getDouble("num");
    }

    private static BasicDBObject getQueryFilter(Integer filter) {
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

    private static DateTime buildDateKey(BasicDBObject bson) throws java.text.ParseException {
        DateTime dateKey;
        String month;
        String day;
        String hour;
        BasicDBObject dateObj = (BasicDBObject) bson.get("_id");
        String dateString = null;
        DateFormat df = null;
        if (granularity == GRANULARITY_HOUR) {
            df = new SimpleDateFormat("HH:mm");
            dateString = String.format("%s:00:00",
                    ((hour = dateObj.getString("hour")).length() == 1)
                            ? "0" + hour
                            : hour);
        } else if (granularity == GRANULARITY_DAY) {
            df = new SimpleDateFormat("yyyy-MM-dd");
            dateString = String.format("%s-%s-%s",
                    dateObj.getString("year"),
                    ((month = dateObj.getString("month")).length() == 1)
                            ? "0" + month
                            : month,
                    ((day = dateObj.getString("day")).length() == 1)
                            ? "0" + day
                            : day);
        } else {
            df = new SimpleDateFormat("yyyy-MM");
            dateString = String.format("%s-%s",
                    dateObj.getString("year"),
                    ((month = dateObj.getString("month")).length() == 1)
                            ? "0" + month
                            : month);
        }
        Date date = df.parse(dateString);
        dateKey = new DateTime(date);

        return dateKey;
    }

    private static Map<DateTime, Double> buildResultsMap(List<DBObject> results, String metric) {
        Map<DateTime, Double> mapCost = new HashMap<>();
        for (DBObject entry : results) {
            BasicDBObject bson = (BasicDBObject) entry;
            DateTime dateKey = null;
            try {
                dateKey = buildDateKey(bson);
                mapCost.put(dateKey, bson.getDouble(metric));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return mapCost;
    }

    private static BasicDBObject getGranularityAggregate() {
        BasicDBObject timeGranularity = new BasicDBObject("year", new BasicDBObject("$year", "$Date"));

        if (granularity >= GRANULARITY_MONTH)
            timeGranularity.append("month", new BasicDBObject("$month", "$Date"));
        if (granularity >= GRANULARITY_DAY)
            timeGranularity.append("day", new BasicDBObject("$dayOfMonth", "$Date"));
        if (granularity >= GRANULARITY_HOUR)
            timeGranularity.append("hour", new BasicDBObject("$hour", "$Date"));

        return timeGranularity;
    }
    
    public static DateTimeFormatter getDateFormat() {
        if (granularity == GRANULARITY_MONTH)
            return DateTimeFormat.forPattern("yyyy-MM");
        else if (granularity == GRANULARITY_DAY)
            return DateTimeFormat.forPattern("yyyy-MM-dd");
        else
            return DateTimeFormat.forPattern("HH:mm");
    }

    private static List<BasicDBObject> getDateFilterQuery() {
        List<BasicDBObject> dateQuery = new ArrayList<>();

        if (startDate != null)
            dateQuery.add(new BasicDBObject("Date", new BasicDBObject("$gte", startDate.toDate())));
        else
            dateQuery.add(new BasicDBObject("Date", new BasicDBObject("$gte", Date.from(Instant.EPOCH))));

        if (granularity == GRANULARITY_HOUR)
            dateQuery.add(new BasicDBObject("Date", new BasicDBObject("$lte", startDate.plusHours(23).plusMinutes(59).plusSeconds(59).toDate())));
        else if (endDate != null)
            dateQuery.add(new BasicDBObject("Date", new BasicDBObject("$lte", endDate.toDate())));
        else
            dateQuery.add(new BasicDBObject("Date", new BasicDBObject("$lte", new Date())));

        return dateQuery;
    }

    public static void indexImpressions() {
        try {
            System.out.println("[DEBUG][INDEXING] Starting indexing process.");
            DBCollection impression_data = DBHandler.getDBConnection().getCollection("impression_data");

            String[] indexes = {"Age", "Income", "Gender", "Context"};

            Arrays.asList(indexes).forEach((index) -> impression_data.createIndex(
                    new BasicDBObject(index,1),
                    new BasicDBObject("background", true)));
            System.out.println("[DEBUG][INDEXING] Created indexes.");
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
    }

	public static Map<DateTime, Double> getNumUniques() throws MongoAuthException {
		DBHandler handler = DBHandler.getDBConnection();
		List<DBObject> results =  new ArrayList<>();
		
        handler.getCollection(COL_CLICKS).aggregate(Arrays.asList(
                new BasicDBObject("$match", new BasicDBObject("$and", getDateFilterQuery())),
                new BasicDBObject("$group",
                        new BasicDBObject("_id", getGranularityAggregate()
                                .append("ID", "$ID"))
                                .append("num", new BasicDBObject("$sum", 1))),
                new BasicDBObject("$group",
                        new BasicDBObject("_id", "$_id")
                        .append("num", new BasicDBObject("$sum", "$num")))
                ))
                .results().forEach(results::add);
        
        
        return buildResultsMap(results, COUNT_METRIC);
	}
}