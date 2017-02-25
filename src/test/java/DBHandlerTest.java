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

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Created by Sam Law on 25/02/2017.
 */


public class DBHandlerTest {

	@Test
	public void getDBConnectionTest() {
		try {
			DBHandler handler = new DBHandler();
			assertEquals("Connection created successfully",
					new DBHandler(),
					handler.getDBConnection());
		} catch (MongoAuthException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void sendQueryTest() {
		
	}
	
	
	@Test
	public void insertDataTest() {
		
	}
	
	@Test
	public void retrieveAllDocumentsTest() {
		
	}
	
	@Test
	public void dropCollectionTest() {
		try {
			DBHandler handler = new DBHandler();
			String testCollection = "test Collection";
			JSONObject testInsertion = new JSONObject()
            							.put("collection", "impression_log")
            							.put("dayCost", 1)
            							.put("dayNum", 1)
            							.put("data", new JSONArray());
			
			handler.insertData(testInsertion, testCollection);
			handler.dropCollection(testCollection);
		} catch (MongoAuthException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void initializeDatabaseTest() {
		
	}
}
