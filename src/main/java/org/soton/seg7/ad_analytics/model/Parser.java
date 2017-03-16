package org.soton.seg7.ad_analytics.model;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.connection.Server;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;
import org.mongojack.JacksonDBCollection;
import org.soton.seg7.ad_analytics.model.exceptions.MongoAuthException;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Created by bogdanbuduroiu on 22/02/2017.
 */
public class Parser {

    /**
     *  This class is responsible for turning CSV files into a Collection
     *  of BSON DBObjects ready to be inserted into the Database.
     *
     *  The parse is returned as a Collection (List) because the Java MongoDB
     *  driver allows for inserting collections in a single DB statement.
     *
     *  The parse automatically generates the keys to the key-value pairs
     *  from the header of the CSV file.
     *
     *  Also, the parser generates by-day and by-hour totals (only click cost atm),
     *  and stores them into the MongoDB database in the form:
     *
     *      {
     *          'date1': {
     *              'hour1': 'total1',
     *              'hour2': 'total2',
     *              ...
     *          },
     *
     *          'date2': {
     *              'hour1': 'total1',
     *              'hour2': 'total2',
     *              ...
     *          },
     *
     *          ...
     *      }
     */

    // No need to have an instance of the Parser
    private Parser() { }

    private static final String CSV_DELIMITER = ",";
    private static final Integer BOUNCE_MINUTES = 2;
    private static final Integer BOUNCE_PAGES = 1;

    public static boolean isValidImpressionLog(File csvFile) {

        String[] headers;

        if (!csvFile.getName().equals("impression_log.csv")) return false;

        try {
            BufferedReader br = new BufferedReader(new FileReader(csvFile));

            headers = br.readLine().split(CSV_DELIMITER);

            if (!headers[0].equals("Date")) return false;
            if (!headers[1].equals("ID")) return false;
            if (!headers[2].equals("Gender")) return false;
            if (!headers[3].equals("Age")) return false;
            if (!headers[4].equals("Income")) return false;
            if (!headers[5].equals("Context")) return false;
            if (!headers[6].equals("Impression Cost")) return false;

            Runtime.getRuntime().exec("sed -i.bak '1d' " +csvFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;
    }

    public static boolean isValidServerLog(File csvFile) {

        String[] headers;

        if (!csvFile.getName().equals("server_log.csv")) return false;

        try {
            BufferedReader br = new BufferedReader(new FileReader(csvFile));

            headers = br.readLine().split(CSV_DELIMITER);

            if (!headers[0].equals("Entry Date")) return false;
            if (!headers[1].equals("ID")) return false;
            if (!headers[2].equals("Exit Date")) return false;
            if (!headers[3].equals("Pages Viewed")) return false;
            if (!headers[4].equals("Conversion")) return false;

            Runtime.getRuntime().exec("sed -i.bak '1d' " +csvFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public static boolean isValidClickLog(File csvFile) {

        String[] headers;

        if (!csvFile.getName().equals("click_log.csv")) return false;

        try {
            BufferedReader br = new BufferedReader(new FileReader(csvFile));

            headers = br.readLine().split(CSV_DELIMITER);

            if (!headers[0].equals("Date")) return false;
            if (!headers[1].equals("ID")) return false;
            if (!headers[2].equals("Click Cost")) return false;

            Runtime.getRuntime().exec("sed -i.bak '1d' " +csvFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public static JSONObject parseCSV(File csvFile) {
        if (csvFile.getName().equals("click_log.csv"))
            return parseClicks(csvFile);
        else if (csvFile.getName().equals("impression_log.csv"))
            return parseImpressions(csvFile);
        else if (csvFile.getName().equals("server_log.csv"))
            return parseServer(csvFile);
        else
            return new JSONObject().put("error", "something happened");
    }

    private static JSONObject parseClicks(File csvFile) {

        Map<DateTime, Double> clickCost = new HashMap<>();
        Map<DateTime, Integer> numClicks = new HashMap<>();

        List<Click> clicks = new ArrayList<>();
        List<ClickMetrics> metrics = new ArrayList<>();

        try {

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            CsvMapper mapper = new CsvMapper();
            mapper.setDateFormat(df);

            CsvSchema schema = CsvSchema.emptySchema()
                    .withHeader();

            ObjectReader reader = mapper
                    .readerFor(Click.class)
                    .with(schema);

            MappingIterator<Click> it = reader.readValues(csvFile);

            final long initTime = System.currentTimeMillis();

            while (it.hasNext()) {
                Click click = it.next();

                DateTime dateKey = new DateTime(click.getDate())
                        .withMinuteOfHour(0)
                        .withSecondOfMinute(0);

                clickCost.merge(dateKey, click.getClickCost(), (oldVal, cost) -> oldVal + cost);

                numClicks.merge(dateKey, 1, (oldVal, one) -> oldVal + one);

                clicks.add(click);
            }

            for (DateTime key : numClicks.keySet())
                metrics.add(new ClickMetrics(
                        key.toDate(),
                        clickCost.get(key),
                        numClicks.get(key)
                ));

            DBCollection click_data = DBHandler.getDBConnection().getCollection("click_data");
            DBCollection click_log = DBHandler.getDBConnection().getCollection("click_log");

            JacksonDBCollection<Click, String> dataColl = JacksonDBCollection.wrap(click_data, Click.class, String.class);
            JacksonDBCollection<ClickMetrics, String> logColl = JacksonDBCollection.wrap(click_log, ClickMetrics.class, String.class);

            dataColl.insert(clicks);
            logColl.insert(metrics);

            final long finalTime = System.currentTimeMillis();

            System.out.printf("[DEBUG][PARSER] Parsed %s in %d sec.", csvFile.getName(), (finalTime-initTime)/1000);
            System.out.println();

            return new JSONObject()
                    .put("insert", "ok")
                    .put("numdoc", clicks.size());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }

        return new JSONObject().put("error", "exception occurred while parsing the csv file");
    }

    private static JSONObject parseImpressions(File csvFile) {

        Map<DateTime, Integer> numImpressions = new HashMap<>();
        Map<DateTime, Double> dayCost = new HashMap<>();

        List<Impression> impressions = new ArrayList<>();
        List<ImpressionMetrics> metrics = new ArrayList<>();

        long numParsed = 0;

        try {

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            CsvMapper mapper = new CsvMapper();
            mapper.setDateFormat(df);

            CsvSchema schema = CsvSchema.emptySchema()
                    .withHeader();

            ObjectReader reader = mapper.readerFor(Impression.class).with(schema);
            MappingIterator<Impression> it = reader.readValues(csvFile);

            final long initTime = System.currentTimeMillis();

            while (it.hasNext()) {
                Impression impression = it.next();

                DateTime dateKey = new DateTime(impression.getDate())
                        .withMinuteOfHour(0)
                        .withSecondOfMinute(0);

                dayCost.merge(dateKey, impression.getImpressionCost(), (oldVal, cost) -> oldVal + cost);

                numImpressions.merge(dateKey, 1, (oldVal, one) -> oldVal + one);

                numParsed++;

                impressions.add(impression);
                if (impressions.size() == 300000) {
                    DBCollection impression_data = DBHandler.getDBConnection().getCollection("impression_data");
                    JacksonDBCollection<Impression, String> dataColl = JacksonDBCollection.wrap(impression_data, Impression.class, String.class);
                    dataColl.insert(impressions);

                    impressions = new ArrayList<>();
                }
            }


            for (DateTime key : numImpressions.keySet())
                metrics.add(new ImpressionMetrics(
                        key.toDate(),
                        dayCost.get(key),
                        numImpressions.get(key)
                ));

            DBCollection impression_log = DBHandler.getDBConnection().getCollection("impression_log");
            DBCollection impression_data = DBHandler.getDBConnection().getCollection("impression_data");
            JacksonDBCollection<ImpressionMetrics, String> logColl = JacksonDBCollection.wrap(impression_log, ImpressionMetrics.class, String.class);
            JacksonDBCollection<Impression, String> dataColl = JacksonDBCollection.wrap(impression_data, Impression.class, String.class);
            dataColl.insert(impressions);
            logColl.insert(metrics);

            final long finalTime = System.currentTimeMillis();
            System.out.printf("[DEBUG][PARSER] Parsed %s in %d sec.", csvFile.getName(), (finalTime-initTime)/1000);
            System.out.println();

            return new JSONObject()
                    .put("insert", "ok")
                    .put("numdoc", numParsed);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }

        return new JSONObject().put("error", "exception occurred while parsing the csv file");
    }

    private static JSONObject parseServer(File csvFile) {

        Map<DateTime, Double> numConversions = new HashMap<>();
        Map<DateTime, Double> numPageViews = new HashMap<>();
        Map<DateTime, Double> numBouncesTime = new HashMap<>();
        Map<DateTime, Double> numBouncesPage = new HashMap<>();

        List<ServerEntry> entries = new ArrayList<>();
        List<ServerMetrics> metrics = new ArrayList<>();

        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            CsvMapper mapper = new CsvMapper();
            mapper.setDateFormat(df);

            CsvSchema schema = CsvSchema.emptySchema()
                    .withHeader()
                    .withNullValue("n/a");

            ObjectReader reader = mapper.readerFor(ServerEntry.class).with(schema);
            MappingIterator<ServerEntry> it = reader.readValues(csvFile);

            final long initTime = System.currentTimeMillis();

            while (it.hasNext()) {
                ServerEntry entry = it.next();

                DateTime entry_date = new DateTime(entry.getEntryDate());
                DateTime exit_date = entry.getExitDate() != null
                        ? new DateTime(entry.getExitDate())
                        : new DateTime(new Date());
                DateTime dateKey = entry_date
                        .withMinuteOfHour(0)
                        .withSecondOfMinute(0);

                if (Minutes.minutesBetween(entry_date,exit_date).getMinutes() < BOUNCE_MINUTES)
                    numBouncesTime.merge(dateKey, 1d, (oldVal, one) -> oldVal + one);

                if (entry.getPagesViewed() <= BOUNCE_PAGES)
                    numBouncesPage.merge(dateKey, 1d, (oldVal, one) -> oldVal + one);

                if (entry.isConversion())
                    numConversions.merge(dateKey, 1d, (oldVal, one) -> oldVal + one);

                numPageViews.merge(dateKey, 1d, (oldVal, one) -> oldVal + one);

                entries.add(entry);
            }

            for (Map.Entry<DateTime, Double> entry : numPageViews.entrySet()) {
                metrics.add(new ServerMetrics(
                        entry.getKey().toDate(),
                        (numConversions.get(entry.getKey()) == null) ? 0 : numConversions.get(entry.getKey()) / entry.getValue(),
                        (numBouncesPage.get(entry.getKey()) == null) ? 0 : numBouncesPage.get(entry.getKey()) / entry.getValue(),
                        (numBouncesTime.get(entry.getKey()) == null) ? 0 : numBouncesTime.get(entry.getKey()) / entry.getValue(),
                        (numConversions.get(entry.getKey()) == null) ? 0 : numConversions.get(entry.getKey()).intValue(),
                        entry.getValue()
                ));
            }

            DBCollection server_data = DBHandler.getDBConnection().getCollection("server_data");
            DBCollection server_log = DBHandler.getDBConnection().getCollection("server_log");
            JacksonDBCollection<ServerEntry, String> coll = JacksonDBCollection.wrap(server_data, ServerEntry.class, String.class);
            JacksonDBCollection<ServerMetrics, String> collMetrics = JacksonDBCollection.wrap(server_log, ServerMetrics.class, String.class);
            coll.insert(entries);
            collMetrics.insert(metrics);


            final long finalTime = System.currentTimeMillis();

            System.out.printf("[DEBUG][PARSER] Parsed %s in %d sec.", csvFile.getName(), (finalTime-initTime)/1000);
            System.out.println();

            return new JSONObject()
                    .put("insert", "ok")
                    .put("numdocs", entries.size());


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
        return new JSONObject().put("error", "exception occurred while parsing the csv file");
    }

}
