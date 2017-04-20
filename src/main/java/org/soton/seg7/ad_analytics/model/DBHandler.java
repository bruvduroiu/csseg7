package org.soton.seg7.ad_analytics.model;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.soton.seg7.ad_analytics.model.exceptions.MongoAuthException;

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
//            object = parser.parse(
//                    new FileReader(
//                            new File("static/config.json")
//                    )
//            ).toString();
//
//            final JSONObject config = new JSONObject(object);
//
//            System.out.println(object);
//            HOST = config.get("host").toString();
//            PORT = Integer.parseInt(config.get("port").toString());
//            USER = config.get("user").toString();
//            PASS = config.get("pass").toString();
//            DB_STRING = config.get("db").toString();
        HOST = "127.0.0.1";
        PORT = 27017;
        USER = "root";
        PASS = "root";
        DB_STRING = "analytics_data";

        if ((dbClient = initializeDatabase()) == null)
            throw new MongoAuthException();
    }

    public static DBHandler getDBConnection() throws MongoAuthException{

        if (handler != null)
            return handler;

        return (handler = new DBHandler());
    }

    public List<DBObject> sendQuery(DBObject query, DBObject fields, String collection) {

        JSONArray array = new JSONArray();
        List<DBObject> results = new ArrayList<>();

        if (dbClient == null) {
            results.add(new BasicDBObject("error", "database not initialized"));
            return results;
        }

        DB db = dbClient.getDB(DB_STRING);
        DBCollection coll = db.getCollection(collection);

        DBCursor cursor = coll.find(query, fields);
        List<DBObject> it = cursor.toArray();

        return cursor.toArray();
    }

    public DBCollection getCollection(String collection) {
        DB db = dbClient.getDB(DB_STRING);
        return db.getCollection(collection);
    }

    public MongoDatabase getDB() {
        return dbClient.getDatabase(DB_STRING);
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

    @Deprecated
    public JSONObject dropCollection(String collection) {
        if (dbClient == null)
            return new JSONObject().put("error", "database not initialized");

        DB db = dbClient.getDB(DB_STRING);
        db.getCollection(collection).drop();

        return new JSONObject().put("success", "removed " + collection);
    }

    public JSONObject wipeCollection(String collection) {
        if (dbClient == null)
            return new JSONObject().put("error", "database not initialised");

        MongoDatabase db = dbClient.getDatabase(DB_STRING);
        db.getCollection(collection).deleteMany(new BsonDocument());

        return new JSONObject().put("success", String.format("wiped %s", collection));
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

        MongoClient client = new MongoClient("localhost",27017);

        return client;
    }
}
