package org.soton.seg7.ad_analytics.model;

import com.mongodb.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.soton.seg7.ad_analytics.model.exceptions.MongoAuthException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bogdanbuduroiu on 22/02/2017.
 */
public class DBHandler {


    private static MongoClient dbClient;
    private static DBHandler handler;

    private String HOST;
    private int PORT;
    private String USER;
    private String PASS;
    private String DB_STRING;


    private DBHandler() throws MongoAuthException {
        JSONParser parser = new JSONParser();

        final String object;
        try {
            object = parser.parse(
                    new FileReader(
                            new File("").getAbsolutePath() + "/static/config.json"
                    )
            ).toString();

            final JSONObject config = new JSONObject(object);

            HOST = config.get("host").toString();
            PORT = Integer.parseInt(config.get("port").toString());
            USER = config.get("user").toString();
            PASS = config.get("pass").toString();
            DB_STRING = config.get("db").toString();

        if ((dbClient = initializeDatabase()) == null)
            throw new MongoAuthException();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public static DBHandler getDBConnection() throws MongoAuthException{

        if (handler != null)
            return handler;

        return (handler = new DBHandler());
    }

    public JSONArray sendQuery(DBObject query, DBObject fields, String collection) {

        JSONArray array = new JSONArray();

        if (dbClient == null)
            return new JSONArray().put(new JSONObject().put("error", "database not initialized"));

        DB db = dbClient.getDB(DB_STRING);
        DBCollection coll = db.getCollection(collection);

        DBCursor cursor = coll.find(query, fields);

        while(cursor.hasNext())
            array.put(cursor.next());

        return array;
    }

    public String insertData(JSONObject insertion, String collection) {

        if (dbClient == null)
            return new JSONObject().append("error", "database not initialized").toString();

        DB db = dbClient.getDB(DB_STRING);
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

        DB db = dbClient.getDB(DB_STRING);
        DBCollection coll = db.getCollection(collection);

        DBCursor cursor = coll.find(allQuery, removeId);

        while (cursor.hasNext())
            bsonArray.add(cursor.next());

        return new JSONObject(bsonArray.get(0).toString());
    }

    public JSONObject dropCollection(String collection) {
        if (dbClient == null)
            new JSONObject().put("error", "database not initialized");

        DB db = dbClient.getDB(DB_STRING);
        db.getCollection(collection).drop();

        return new JSONObject().put("success", "removed " + collection);
    }

    private MongoClient initializeDatabase() {

        List<ServerAddress> addresses = new ArrayList<>();
        addresses.add(new ServerAddress(HOST, PORT));

        List<MongoCredential> credentials = new ArrayList<>();
        credentials.add(
                MongoCredential.createCredential(
                        USER,
                        DB_STRING,
                        PASS.toCharArray()
                )
        );

        MongoClient client = new MongoClient(addresses, credentials);

        return client;
    }
}
