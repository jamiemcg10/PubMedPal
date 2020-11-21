package edu.bu.metcs622.scandata;

//import java.io.File;
//import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.bu.metcs622.database.DatastoreBuilder;
import edu.bu.metcs622.database.MongoDBInitializer;
import edu.bu.metcs622.database.MySQLDBInitializer;
import edu.bu.metcs622.lucene.LuceneSearcher;
import edu.bu.metcs622.main.Logger;

/**
 * Controls execution
 *
 */
public class Engine {

	private MySQLDBInitializer mysql = null;
	private MongoDBInitializer mongodb;
	private LuceneSearcher lucene = null;
	private BruteForceSearcher bfSearch;
	private SearchHistory history;
	private Logger logger;
	private long fileSize;

	public Engine() {
		logger = new Logger();
	}
	
	
	/**
	 * Initializes search components - databases, lucene index, brute force searcher
	 * @param files
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws TransformerConfigurationException
	 * @throws ParseException
	 * @throws SQLException
	 */
	public void start(String files) throws IOException, ParserConfigurationException, SAXException, TransformerConfigurationException, ParseException, SQLException{
		//initialize everything
		try {
			System.out.println("Starting engine...");
			
			// retrieve an xml document
			Combiner combine = new Combiner(this, files);
			Document combinedXML = combine.getCombinedXML();
			fileSize = combine.getFileSize();
			
			if (combinedXML != null) {
				// there is a document - add it to databases, lucene index, and brute force searcher
				DatastoreBuilder dbBuilder = new DatastoreBuilder(this, files, combinedXML);
				setMysql(dbBuilder.getMysql());
				setMongodb(dbBuilder.getMongoDb());
				setLucene(dbBuilder.getLucene());
				setBfSearch(dbBuilder.getBfSearch());
				history = new SearchHistory();  // initialize search history
				System.out.println("Everything is set up!");
				
				
			} else {
				logger.writeToErrorLog("There is no combinedXML file");
			}	

		} catch (TransformerConfigurationException e1) {
			logger.writeToErrorLog(e1.toString());
			e1.printStackTrace();
		} catch (IOException e1) {
			logger.writeToErrorLog(e1.toString());
			e1.printStackTrace();
		} catch (ParserConfigurationException e1) {
			logger.writeToErrorLog(e1.toString());
			e1.printStackTrace();
		} catch (SAXException e1) {
			logger.writeToErrorLog(e1.toString());
			e1.printStackTrace();
		} catch (ParseException e1) {
			logger.writeToErrorLog(e1.toString());
			e1.printStackTrace();
		} catch (SQLException e1) {
			logger.writeToErrorLog(e1.toString());
			e1.printStackTrace();
		}
				
	}


	public MySQLDBInitializer getMysql() {
		return this.mysql;
	}


	private void setMysql(MySQLDBInitializer mysql) {
		this.mysql = mysql;
	}


	public MongoDBInitializer getMongodb() {
		return this.mongodb;
	}


	private void setMongodb(MongoDBInitializer mongodb) {
		this.mongodb = mongodb;
	}
	
	public LuceneSearcher getLucene() {
		return this.lucene;
	}


	private void setLucene(LuceneSearcher lucene) {
		this.lucene = lucene;
	}
	
	public BruteForceSearcher getBfSearch() {
		return this.bfSearch;
	}

	private void setBfSearch(BruteForceSearcher bfSearch) {
		this.bfSearch = bfSearch;
	}
	
	public SearchHistory getHistory() {
		return this.history;
	}

	public Logger getLogger() {
		return logger;
	}
	
	public long getFileSize() {
		return fileSize;
	}

}
