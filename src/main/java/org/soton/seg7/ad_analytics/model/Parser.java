package org.soton.seg7.ad_analytics.model;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bogdanbuduroiu on 22/02/2017.
 */
public class Parser {


    public static List<DBObject> parseCSV(String path, String csvFile) {
        String[] headings;
        String line;
        String csvSplitBy = ",";
        List<DBObject> list = new ArrayList<>();
        DBObject jsonFile = new BasicDBObject();

        jsonFile.put("collection", csvFile);
        list.add(jsonFile);

        try {
            BufferedReader br = new BufferedReader(new FileReader(path+csvFile+".csv"));

            headings = br.readLine().split(csvSplitBy);


            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] data = line.split(csvSplitBy);
                DBObject row = new BasicDBObject();
                for (int i=0; i < headings.length;i++)
                    row.put(headings[i], data[i]);

                list.add(row);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    public static void main(String[] args) {

        DBHandler handler = new DBHandler();
        String csvFile = "click_cost";

        // Replace with own file
        List<DBObject> parse = parseCSV("/Users/bogdanbuduroiu/Downloads/2_week_campaign_2 2/",csvFile);
        System.out.println(new JSONObject(JSON.parse(parse.toString())).toString(4));

        handler.openConnection(27017);

        handler.insertData(parse, parse.get(0).get("collection").toString());
        String res = handler.retrieveAllDocuments(parse.get(0).get("collection").toString()).toString(4);

        System.out.println(res);
    }
}
