package org.soton.seg7.ad_analytics.model;

import com.mongodb.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by bogdanbuduroiu on 22/02/2017.
 */
public class DBHandler {


    private static MongoClient dbClient;
    private static DBHandler handler;

    private DBHandler(int port) {
        dbClient = new MongoClient("127.0.0.1", port);
    }

    public static DBHandler getDBConnection(int port) {

        if (handler != null)
            return handler;

        return (handler = new DBHandler(port));
    }

    public String sendQuery(DBObject query, String collection) {

        JSONArray array = new JSONArray();

        if (dbClient == null)
            return new JSONObject().append("error", "database not initialized").toString();

        DB db = dbClient.getDB("analytics_data");
        DBCollection coll = db.getCollection(collection);

        DBCursor cursor = coll.find(query);

        while(cursor.hasNext())
            array.put(cursor.next());

        return array.toString();
    }

    public String insertData(List<DBObject> insertion, String collection) {

        if (dbClient == null)
            return new JSONObject().append("error", "database not initialized").toString();

        DB db = dbClient.getDB("analytics_data");
        DBCollection coll = db.getCollection(collection);

        coll.insert(insertion);

        return new JSONObject()
                .append("status", "ok")
                .append("inserted length:", "1")
                .toString();
    }

    public JSONArray retrieveAllDocuments(String collection) {
        JSONArray array = new JSONArray();

        if (dbClient == null)
            return new JSONArray().put(new JSONObject().append("error", "database not initialized"));

        DB db = dbClient.getDB("analytics_data");
        DBCollection coll = db.getCollection(collection);

        DBCursor cursor = coll.find();

        while (cursor.hasNext())
            array.put(cursor.next());

        return array;
    }
}
