package org.soton.seg7.ad_analytics.model;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     *  and stores them into the MongoDB databse in the form:
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

    public static List<DBObject> parseCSV(String path, String csvFile) {
        String[] headings;
        String line;
        String csvSplitBy = ",";
        List<DBObject> list = new ArrayList<>();
        DBObject jsonFile = new BasicDBObject();

        // Total click-cost map of day -> hour -> total
        Map<String, Map<String, Float>> dayTotals = new HashMap<>();

        // Total click-cost map of hour -> total
        Map<String, Float> hourTotals = new HashMap<>();

        jsonFile.put("collection", csvFile);
        list.add(jsonFile);

        try {
            final String PATH = path + csvFile + ".csv";
            BufferedReader br = new BufferedReader(new FileReader(PATH));

            headings = br.readLine().split(csvSplitBy);


            while ((line = br.readLine()) != null) {


                // use comma as separator
                String[] data = line.split(csvSplitBy);
                DBObject row = new BasicDBObject();
                String daydate = data[0].split(":")[0];
                String day = daydate.split(" ")[0];
                String hour = daydate.split(" ")[1];

                System.out.println("Day: " + day +"; Hour: " + hour);

                System.out.println(data[0].split(":")[0]);
                for (int i=0; i < headings.length;i++) {
                    row.put(headings[i], data[i]);
                }

                // Whenever the parser finds a new date, reset the sums in the hashmap
                if (!dayTotals.containsKey(day))
                    hourTotals = new HashMap<>();

                // Get total cost of ads per hour
                hourTotals.put(
                        hour,
                        hourTotals.containsKey(hour)
                            ? hourTotals.get(hour) + new Float(data[2])
                            : new Float(data[2])
                );

                dayTotals.put(day, hourTotals);

                list.add(row);
            }
            list.add(new BasicDBObject(dayTotals));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }
}
