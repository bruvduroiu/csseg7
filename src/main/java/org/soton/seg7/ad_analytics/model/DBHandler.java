package org.soton.seg7.ad_analytics.model;

import com.mongodb.*;
import com.mongodb.util.JSON;
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

    public String sendQuery(JSONObject query, String collection) {

        JSONArray array = new JSONArray();

        if (dbClient == null)
            return new JSONObject().append("error", "database not initialized").toString();

        DB db = dbClient.getDB("analytics_data");
        DBCollection coll = db.getCollection(collection);

        DBCursor cursor = coll.find(BasicDBObject.parse(query.toString()));

        while(cursor.hasNext())
            array.put(cursor.next());

        return array.toString();
    }

    public String insertData(JSONObject insertion, String collection) {

        if (dbClient == null)
            return new JSONObject().append("error", "database not initialized").toString();

        DB db = dbClient.getDB("analytics_data");
        DBCollection coll = db.getCollection(collection);

        coll.insert(BasicDBObject.parse(insertion.toString()));

        return new JSONObject()
                .append("status", "ok")
                .append("inserted length:", "1")
                .toString();
    }

    public JSONObject retrieveAllDocuments(String collection) {
        BasicDBList bsonArray = new BasicDBList();
        DBObject allQuery = new BasicDBObject();
        DBObject removeId = new BasicDBObject("_id", 0);

        if (dbClient == null)
            return new JSONObject().put("error", "database not initialized");

        DB db = dbClient.getDB("analytics_data");
        DBCollection coll = db.getCollection(collection);

        DBCursor cursor = coll.find(allQuery, removeId);

        while (cursor.hasNext())
            bsonArray.add(cursor.next());

        return new JSONObject(bsonArray.get(0).toString());
    }

    public JSONObject dropCollection(String collection) {
        if (dbClient == null)
            new JSONObject().put("error", "database not initialized");

        DB db = dbClient.getDB("analytics_data");
        db.getCollection(collection).drop();

        return new JSONObject().put("success", "removed " + collection);
    }
}
