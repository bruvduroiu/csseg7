package org.soton.seg7.ad_analytics.model;

import org.json.JSONArray;
import org.json.JSONObject;
import org.soton.seg7.ad_analytics.model.exceptions.MongoAuthException;

import java.io.*;
import java.util.*;

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

    private static String csvDelimiter = ",";

    public static boolean isValidImpressionLog(File csvFile) {

        String[] headers;

        if (!csvFile.getName().equals("impression_log.csv")) return false;

        try {
            BufferedReader br = new BufferedReader(new FileReader(csvFile));

            headers = br.readLine().split(csvDelimiter);

            if (!headers[0].equals("Date")) return false;
            if (!headers[1].equals("ID")) return false;
            if (!headers[2].equals("Gender")) return false;
            if (!headers[3].equals("Age")) return false;
            if (!headers[4].equals("Income")) return false;
            if (!headers[5].equals("Context")) return false;
            if (!headers[6].equals("Impression Cost")) return false;

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

            headers = br.readLine().split(csvDelimiter);

            if (!headers[0].equals("Entry Date")) return false;
            if (!headers[1].equals("ID")) return false;
            if (!headers[2].equals("Exit Date")) return false;
            if (!headers[3].equals("Pages Viewed")) return false;
            if (!headers[4].equals("Conversion")) return false;

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

            headers = br.readLine().split(csvDelimiter);

            if (!headers[0].equals("Date")) return false;
            if (!headers[1].equals("ID")) return false;
            if (!headers[2].equals("Click Cost")) return false;

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

        String line;
        int totalClicks = 0;
        double totalCost = 0;
        JSONObject jsonObject;

        // Total click-cost map of day -> hour -> total
        Map<String, Map<String, Float>> dayTotalCosts = new HashMap<>();

        // Total num clicks map of day -> hour -> total
        Map<String, Map<String, Integer>> dayClicks = new HashMap<>();

        // Total click-cost map of hour -> total
        Map<String, Float> hourTotalCosts = new HashMap<>();

        // Total num clicks map of hour -> total
        Map<String, Integer> hourClicks = new HashMap<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(csvFile));

            br.readLine();

            final long initTime = System.currentTimeMillis();

            while ((line = br.readLine()) != null) {


                // use comma as separator
                String[] data = line.split(csvDelimiter);

                String day = data[0].split(":")[0].split(" ")[0];
                String hour = data[0].split(":")[0].split(" ")[1];

                // Whenever the parser finds a new date, reset the sums in the hashmap
                if (!dayTotalCosts.containsKey(day))
                    hourTotalCosts = new HashMap<>();

                if (!dayClicks.containsKey(day))
                    hourClicks = new HashMap<>();

                // Get total cost of ads per hour
                hourTotalCosts.put(
                        hour,
                        hourTotalCosts.containsKey(hour)
                                ? hourTotalCosts.get(hour) + new Float(data[2])
                                : new Float(data[2])
                );

                hourClicks.put(
                        hour,
                        hourClicks.containsKey(hour)
                                ? hourClicks.get(hour) + 1
                                : 1
                );

                dayTotalCosts.put(day, hourTotalCosts);
                dayClicks.put(day, hourClicks);

                totalClicks++;
                totalCost += Double.parseDouble(data[2]);
            }

            jsonObject = new JSONObject()
                    .put("collection", "click_log")
                    .put("dayCost", dayTotalCosts)
                    .put("dayNum", dayClicks)
                    .put("totalNum", totalClicks)
                    .put("totalCost", totalCost);

            insertIntoDB(jsonObject);

            final long finalTime = System.currentTimeMillis();

            System.out.printf("[DEBUG][PARSER] Parsed %s in %d sec.", csvFile.getName(), (finalTime-initTime)/1000);
            System.out.println();

            return jsonObject;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }

        return new JSONObject().put("error", "exception occurred while parsing the csv file");
    }

    private static JSONObject parseImpressions(File csvFile) {

        String line;
        JSONObject jsonObject;
        int totalImpressions = 0;
        double totalCost = 0;

        Map<String, Map<String, Float>> dayTotalCost = new HashMap<>();

        Map<String, Map<String, Integer>> dayTotalImpressions = new HashMap<>();

        Map<String, Float> hourTotalCost = new HashMap<>();

        Map<String, Integer> hourTotalImpressions = new HashMap<>();

        try {

            BufferedReader br = new BufferedReader(new FileReader(csvFile));

            br.readLine();

            final long initTime = System.currentTimeMillis();

            while((line = br.readLine()) != null) {
                String[] data = line.split(csvDelimiter);

                String day = data[0].split(":")[0].split(" ")[0];
                String hour = data[0].split(":")[0].split(" ")[1];

                // Reset the hour HashMaps for every new date
                if (!dayTotalCost.containsKey(day))
                    hourTotalCost = new HashMap<>();

                // Reset the hour HashMaps for every new day
                if (!dayTotalImpressions.containsKey(day))
                    hourTotalImpressions = new HashMap<>();

                // Get total cost of displaying ads per hour
                hourTotalCost.put(
                        hour,
                        hourTotalCost.containsKey(hour)
                                ? hourTotalCost.get(hour) + new Float(data[data.length - 1])
                                : new Float(data[data.length - 1])
                );

                // Get total number of impressions per hour
                hourTotalImpressions.put(
                        hour,
                        hourTotalImpressions.containsKey(hour)
                                ? hourTotalImpressions.get(hour) + 1
                                : 1
                );

                dayTotalCost.put(day, hourTotalCost);
                dayTotalImpressions.put(day, hourTotalImpressions);

                totalImpressions++;
                totalCost += Double.parseDouble(data[data.length - 1]);
            }

            jsonObject = new JSONObject()
                    .put("collection", "impression_log")
                    .put("dayCost", dayTotalCost)
                    .put("dayNum", dayTotalImpressions)
                    .put("totalNum", totalImpressions)
                    .put("totalCost", totalCost);

            insertIntoDB(jsonObject);

            final long finalTime = System.currentTimeMillis();

            System.out.printf("[DEBUG][PARSER] Parsed %s in %d sec.", csvFile.getName(), (finalTime-initTime)/1000);
            System.out.println();
            return jsonObject;
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }

        return new JSONObject().put("error", "exception occurred while parsing the csv file");
    }

    private static JSONObject parseServer(File csvFile) {
        String line;

        Map<String, Map<String, Integer>> dayConversions = new HashMap<>();
        Map<String, Integer> hourConversions = new HashMap<>();

        JSONObject jsonObject;

        try {
            BufferedReader br = new BufferedReader(new FileReader(csvFile));

            br.readLine();

            final long initTime = System.currentTimeMillis();

            while ((line = br.readLine()) != null) {
                String[] data = line.split(csvDelimiter);

                String day = data[0].split(":")[0].split(" ")[0];
                String hour = data[0].split(":")[0].split(" ")[1];

                if (!dayConversions.containsKey(day))
                    hourConversions = new HashMap<>();

                if (data[data.length-1].equals("Yes"))
                    hourConversions.put(
                            hour,
                            hourConversions.containsKey(hour)
                                    ? hourConversions.get(hour) + 1
                                    : 1
                    );

                dayConversions.put(day, hourConversions);

            }

            jsonObject = new JSONObject()
                    .put("collection", "server_log")
                    .put("dayNum", dayConversions);

            insertIntoDB(jsonObject);

            final long finalTime = System.currentTimeMillis();

            System.out.printf("[DEBUG][PARSER] Parsed %s in %d sec.", csvFile.getName(), (finalTime-initTime)/1000);
            System.out.println();

            return jsonObject;


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MongoAuthException e) {
            e.printStackTrace();
        }
        return new JSONObject().put("error", "exception occurred while parsing the csv file");
    }

    private static void insertIntoDB(JSONObject jsonObject) throws MongoAuthException {

        DBHandler handler = DBHandler.getDBConnection();

        final String collection = jsonObject.getString("collection");
        jsonObject.remove("collection");

        handler.insertData(jsonObject, collection);
    }
}
