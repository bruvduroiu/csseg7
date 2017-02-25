package org.soton.seg7.ad_analytics.model;

import com.mongodb.*;
import com.mongodb.util.JSON;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
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

    private DBHandler() throws MongoAuthException {
        if ((dbClient = initializeDatabase()) == null)
            throw new MongoAuthException();
    }

    public static DBHandler getDBConnection() throws MongoAuthException{

        if (handler != null)
            return handler;

        return (handler = new DBHandler());
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

    private static MongoClient initializeDatabase() {

        try {

            JSONParser parser = new JSONParser();

            final String object = parser.parse(new FileReader(new File("").getAbsolutePath() + "/static/config.json")).toString();
            final JSONObject config = new JSONObject(object);

            final String HOST = config.get("host").toString();
            final Integer PORT = Integer.parseInt(config.get("port").toString());
            final String USER = config.get("user").toString();
            final String PASS = config.get("pass").toString();
            final String DB_STRING = config.get("db").toString();

            System.out.println(HOST);
            System.out.println(PORT);
            System.out.println(USER);
            System.out.println(PASS);
            System.out.println(DB_STRING);

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

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }
}
