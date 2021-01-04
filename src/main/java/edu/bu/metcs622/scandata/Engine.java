package edu.bu.metcs622.scandata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.bu.metcs622.database.DatastoreConnection;
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
		logger = new Logger(true);
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
	public void start(){
		//initialize everything
		try {
			System.out.println("Starting engine...");
			
			// retrieve an xml document
//			File file = new File("\\Users\\Jamie\\eclipse-workspace-2020\\FinalProjectSpring\\data\\pubmeddata.xml");
			File file = new File(System.getenv("DATA_LOCATION")+"pubmeddata.xml");
			fileSize = file.length();
			System.out.format("XML size: %d", fileSize);
			StreamSource source = new StreamSource(new FileInputStream(file));
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			DOMResult result = new DOMResult();
			try {
				transformer.transform(source, result);
			} catch (TransformerException e) {
				e.printStackTrace();
			}
			
			Document combinedXML = (Document) result.getNode();
			
			if (combinedXML != null) {
				// there is a document - add it to databases, lucene index, and brute force searcher
				DatastoreConnection dsConnection = new DatastoreConnection(this, combinedXML);
				setMysql(dsConnection.getMysql());
				System.out.println("MySQL initialized");
				setMongodb(dsConnection.getMongoDb());
				System.out.println("MongoDB initialized");
				setLucene(dsConnection.getLucene());
				System.out.println("Lucene initialized");
				setBfSearch(dsConnection.getBfSearch());
				System.out.println("Brute Force initialized");
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
		} catch (ParseException e1) {
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
