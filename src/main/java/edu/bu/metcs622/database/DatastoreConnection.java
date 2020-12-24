package edu.bu.metcs622.database;

import java.io.IOException;
import java.text.ParseException;
import org.w3c.dom.Document;
import edu.bu.metcs622.lucene.LuceneSearcher;
import edu.bu.metcs622.scandata.BruteForceSearcher;
import edu.bu.metcs622.scandata.Engine;

/**
 * Builds and holds references to 4 search methods:
 *   1) Brute force searcher
 *   2) Lucene index
 *   3) MongoDB
 *   4) MySQL
 */
public class DatastoreConnection {
	private MongoDBInitializer mongoDb;
	private MySQLDBInitializer mysql;
	private LuceneSearcher lucene;
	private BruteForceSearcher bfSearch;
	
	/**
	 * Constructor
	 * @param doc Document for building databases and index
	 * @throws IOException
	 * @throws ParseException
	 */
	public DatastoreConnection (Engine engine, Document doc) throws IOException, ParseException {  // will be a w3c Document
		
		// initialize databases
		String files = "pubmeddata";
		mysql = new MySQLDBInitializer(engine);
		mongoDb = new MongoDBInitializer(engine);
		
		
		System.out.println("GATHERING DATA...");
		try {
			// initialize lucene index
			lucene = new LuceneSearcher(engine);
		} catch (org.apache.lucene.queryparser.classic.ParseException e) {
			engine.getLogger().writeToErrorLog(e.toString());
			e.printStackTrace();
		}

		
		
		// initialize brute force searcher with data
		
		
		bfSearch = new BruteForceSearcher(doc);
		System.out.println("GATHERING DATA FINISHED!");

	} // end constructor
	
	
	
	public MongoDBInitializer getMongoDb() {
		return this.mongoDb;
	}
	
	public MySQLDBInitializer getMysql() {
		return this.mysql;
	}
	
	public LuceneSearcher getLucene() {
		return this.lucene;
	}


	public BruteForceSearcher getBfSearch() {
		return bfSearch;
	}



} // end BuildDatabase class


