package edu.bu.metcs622.database;

import static com.mongodb.client.model.Filters.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import edu.bu.metcs622.main.Constants;
import edu.bu.metcs622.scandata.Engine;
import com.mongodb.MongoClientURI;

/**
 * Initializes connection with MongoDB and provides methods for searching database by keyword and date
*/
public class MongoDBInitializer {
	private MongoClient mongoClient;
	private MongoDatabase dbObj;
	private MongoCollection<Document> col = null;
	
	
	/**
	 * Constructor. Initializes connection with collection
	 * @param db
	 */
	public MongoDBInitializer(Engine engine) {
		try {
			
			MongoClientURI uri = new MongoClientURI(
			    Constants.MONGODB_ADDRESS);
			
			this.mongoClient  = new MongoClient(uri);

			this.dbObj = mongoClient.getDatabase("PubMedPal");
			this.col = dbObj.getCollection("pubmeddata");		
		} catch (Exception e) {
			engine.getLogger().writeToErrorLog(e.toString());
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * Constructor. Initializes connection with collection and drops data currently in collection
	 * @param db
	 */
	public MongoDBInitializer(Engine engine, String db) {
		try {
			
			MongoClientURI uri = new MongoClientURI(
			    Constants.MONGODB_ADDRESS);
			
			this.mongoClient  = new MongoClient(uri);

			this.dbObj = mongoClient.getDatabase("PubMedPal");
			this.col = dbObj.getCollection(db);		
		} catch (Exception e) {
			engine.getLogger().writeToErrorLog(e.toString());
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Checks for whether a table exists in MongoDB
	 * @param tableName
	 * @return whether table exists in MongoDB
	 */
	public boolean checkTableExists(String tableName) {
		
		if (this.col.count() > 0) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Creates new record to add to DB
	 * @return Document that is the new record
	 */
	public Document createRecord() {
		Document myDoc = new Document();
		return myDoc;		
	}

	/**
	 * Adds specifies a field and value to add to a document
	 * @param myDoc document to add to
	 * @param field field to add to
	 * @param value value to add
	 */
	public void addToRecord(Document myDoc, String field, String value) {
		// add field value to a new record
		myDoc.put(field, value);
	}
	
	/**
	 * Adds a date to a record - puts date in standard format
	 * @param myDoc Document to add to
	 * @param field field name to add
	 * @param value value to add to field
	 * @throws ParseException
	 */
	public void addDateToRecord(Document myDoc, String field, String value) throws ParseException {
		// add date value to a new record
		if (!value.equals("")) {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			myDoc.put(field, df.parse(value));
		} else {
			myDoc.put(field, value);
		}
		
	}
	
	/**
	 * Finds the number of occurrences of the keyword in the collection
	 * @param keyword the word to search for
	 * @return the number of occurrences
	 */
	public String countKeywordsMongoDB(String keyword) {
		// find number of matches in DB
		String numResults = "";
		if (this.col != null) {
			
			String keywordRegex = "(?i).*" + keyword + ".*";  // create regex with keyword to find
			
			Long num = this.col.count(or(regex("articleTitle", keywordRegex),regex("abstractText", keywordRegex),regex("keywords", keywordRegex)));
			numResults = num.toString();
		}
		
		return numResults;
	}
	
	/**
	 * Finds the number of occurrences of the keyword in the collection between 2 years
	 * @param keyword word to search for
	 * @param beginYear starting year
	 * @param endYear ending year
	 * @return the number of occurrences
	 * @throws ParseException
	 */
	public String countKeywordsMongoDB(String keyword, String beginYear, String endYear) throws ParseException {
		// find number of matches in DB
		String numResults = "";
		if (this.col != null) {
			String keywordRegex = "(?i).*" + keyword + ".*";  // create regex with keyword to find
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			
			Long num = this.col.count(and(
					gte("pubDateString", df.parse(beginYear + "-01-01")), 
					lte("pubDateString", df.parse(endYear + "-12-31")),
					or(regex("articleTitle", keywordRegex),regex("abstractText", keywordRegex),regex("keywords", keywordRegex))));
			
			numResults = num.toString();
		}
		
		return numResults;
	
	}
	
	/**
	 * Finds results for given keyword
	 * @param keyword word to search for
	 * @return an html string with each result as a <li>
	 * @throws ParseException
	 */
	public String keywordSearchMongoDB(String keyword) throws ParseException {
		String results = "";
		
		if (this.col != null) {
			MongoCollection<org.bson.Document> mongoCol = this.col;
			
			String keywordRegex = "(?i).*" + keyword + ".*";  // create regex with keyword to find
			
			// search for records in the date range with the keyword
			Iterator<org.bson.Document> it = mongoCol.find(
					or(regex("articleTitle", keywordRegex),regex("abstractText", keywordRegex),regex("keywords", keywordRegex))
					).iterator();
			
			
			// log results		
			if (it.hasNext()) {
				while(it.hasNext()) {
					org.bson.Document result = it.next();
					results += "<li>" + result.get("articleTitle")+ "</li>";
				}	
			}
		}
		
		return results;
				
	}
	
	
	/**
	 * Finds results for a given keyword between 2 years
	 * @param keyword word to search for
	 * @param beginYear starting year
	 * @param endYear ending year
	 * @return an html string with each result as a <li>
	 * @throws ParseException
	 */
	public String keywordSearchMongoDB(String keyword, String beginYear, String endYear) throws ParseException {

		String results = "";
		if (this.col != null) {
			MongoCollection<org.bson.Document> mongoCol = this.col;
	
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			
			String keywordRegex = "(?i).*" + keyword + ".*";  // create regex with keyword to find
			
			// search for records in the date range with the keyword
			Iterator<org.bson.Document> it = mongoCol.find(and(
					gte("pubDateString", df.parse(beginYear + "-01-01")), 
					lte("pubDateString", df.parse(endYear + "-12-31")),
					or(regex("articleTitle", keywordRegex),regex("abstractText", keywordRegex),regex("keywords", keywordRegex))
					)).iterator();
			
			
			// log results
			while(it.hasNext()) {
				org.bson.Document result = it.next();
				results += "<li>" + result.get("articleTitle") + "</li>";
			}	
		}
		
		return results;
				
	}
		
	/**
	 * add Document with fields to collection
	 * @param record Document to insert
	 */
	public void insertRecord(Document record) {
		// insert the new record into the collection
		if (this.col != null) {
			this.col.insertOne(record);
		}
	}
	
	public MongoCollection<Document> getCol(){
		return this.col;
	}
	
}
