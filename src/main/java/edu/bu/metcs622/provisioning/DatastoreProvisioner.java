package edu.bu.metcs622.provisioning;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.bu.metcs622.database.MongoDBInitializer;
import edu.bu.metcs622.database.MySQLDBInitializer;
import edu.bu.metcs622.lucene.LuceneSearcher;
import edu.bu.metcs622.scandata.BruteForceSearcher;
import edu.bu.metcs622.scandata.Combiner;
import edu.bu.metcs622.scandata.Engine;

/**
 * Builds and holds references to 4 search methods:
 *   1) Brute force searcher
 *   2) Lucene index
 *   3) MongoDB
 *   4) MySQL
 */
public class DatastoreProvisioner {
	private MongoDBInitializer mongoDb;
	private MySQLDBInitializer mysql;
	private LuceneSearcher lucene;
	private BruteForceSearcher bfSearch;
	
	public static void main(String[] args) {
		Engine lightEngine = new Engine();
		Combiner combine;
		try {
			combine = new Combiner(lightEngine, "pubmed20n1151,pubmed20n1169");
			Document combinedXML = combine.getCombinedXML();
			DatastoreProvisioner datastoreProvisioner = new DatastoreProvisioner(combinedXML);
			
		} catch (TransformerConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
	}
	
	/**
	 * Constructor
	 * @param doc Document for building databases and index
	 * @throws IOException
	 * @throws ParseException
	 */
	public DatastoreProvisioner (Document doc) throws IOException, ParseException {  // will be a w3c Document
		Engine engine = new Engine();
		
		// initialize databases
		String files = "pubmeddata";
				
		mysql = new MySQLDBInitializer(engine, files);
		mongoDb = new MongoDBInitializer(engine, files);
		
		
		System.out.println("GATHERING DATA...");
		try {
			// initialize lucene index
			lucene = new LuceneSearcher(engine, files);
		} catch (org.apache.lucene.queryparser.classic.ParseException e) {
			engine.getLogger().writeToErrorLog(e.toString());
			e.printStackTrace();
		}

		// check to see if table is in MySQL
		boolean mySQLTableExists = mysql.checkTableExists(files);
		boolean mongoDBTableExists = mongoDb.checkTableExists(files);
		boolean luceneIndexExists = lucene.checkIndexExists(files);
			

		// build database and lucene index
		if (!mySQLTableExists || !mongoDBTableExists || !luceneIndexExists) {
			buildDatabase(doc, mySQLTableExists, mongoDBTableExists, luceneIndexExists); 
		}
		
		// initialize brute force searcher with data
		System.out.println("GATHERING DATA FINISHED!");
		
		lucene.closeIndexWriter();
	} // end constructor
	

	
	/**
	 * Loops through articles in document and adds each record to MySQL (if successfully connected), MongoDB, and lucene index
	 * @param doc a reference to the combined document to be imported
	 */
	private void buildDatabase(Document doc, boolean mySQLTableExists, boolean mongoDBTableExists, boolean luceneIndexExists) throws IOException, ParseException {
		NodeList allPubmedArticles = doc.getElementsByTagName("PubmedArticle");  // get list of all PubMed Articles
		
		
		for (int i=0; i<allPubmedArticles.getLength(); i++) {  // loop through articles
			Node pubmedArticle = allPubmedArticles.item(i);
			
			Element pubmedArticleElement = (Element) pubmedArticle;
			
			// get article within Pubmed Article
			Node article = ((Element) pubmedArticle).getElementsByTagName("Article").item(0);
			Element articleElement = (Element) article;
			

			String articleTitle = articleElement.getElementsByTagName("ArticleTitle").item(0).getTextContent();
			String abstractText = "";
			String keywords = "";
			
			// if there are any keywords listed for the article, store them in the keywords variable
			if (pubmedArticleElement.getElementsByTagName("KeywordList").item(0) != null) {
				keywords = pubmedArticleElement.getElementsByTagName("KeywordList").item(0).getTextContent();
			} 
			
			// if there is an abstract for the article, store it in the abstract variable
			if ((Element) articleElement.getElementsByTagName("Abstract").item(0) != null) {
				 abstractText = ( (Element) articleElement.getElementsByTagName("Abstract").item(0))
							.getElementsByTagName("AbstractText").item(0).getTextContent();
			} 
						

			//	add published date to result - only add individual fields if they are included
			Element journalIssue = (Element) articleElement.getElementsByTagName("Journal").item(0);
			String pubDateString = "";
			journalIssue = (Element) journalIssue.getElementsByTagName("JournalIssue").item(0);
			if (journalIssue.getElementsByTagName("Year").item(0) != null) {
				pubDateString += journalIssue.getElementsByTagName("Year").item(0).getTextContent() + " ";
			}
			if (journalIssue.getElementsByTagName("Month").item(0) != null) {
				pubDateString += journalIssue.getElementsByTagName("Month").item(0).getTextContent() + " ";
			}
			if (journalIssue.getElementsByTagName("Day").item(0) != null) {
				pubDateString += journalIssue.getElementsByTagName("Day").item(0).getTextContent() + " ";
			}
			

			// add doc with info to MongoDB database
			if (!mongoDBTableExists) {
				org.bson.Document mongoDoc = mongoDb.createRecord();
				mongoDb.addToRecord(mongoDoc, "articleTitle", articleTitle);
				mongoDb.addToRecord(mongoDoc, "abstractText", abstractText);
				mongoDb.addToRecord(mongoDoc, "keywords", keywords);
				mongoDb.addDateToRecord(mongoDoc, "pubDateString", convertToDatetime(pubDateString));
				mongoDb.insertRecord(mongoDoc);
			}
			
			// add record to MySQL database
			if (!mySQLTableExists) {				
				if (mysql.getCon() != null) {
					// replace single apostrophes so they don't interfere with queries
					articleTitle = articleTitle.replaceAll("'", "''");
					abstractText = abstractText.replaceAll("'", "''");
					keywords = keywords.replaceAll("'", "''");
					
					mysql.insertRow(articleTitle, abstractText, keywords, convertToDatetime(pubDateString));
		
				}  // end if (mysql.getCon() != null) 
			} // end of add to MYSQL DB
			
			// add doc with info to Lucene index
			if (!luceneIndexExists) {
				lucene.addDoc(articleTitle, abstractText, keywords, convertToDatetime(pubDateString));
			}
				
		} // end loop through articles

	} // end buildDatabase()
	
	

	//Private helper method to normalize dates for comparison
	private String convertToDatetime(String dateString) {
		String convertedDateString = "";
		String[] dateArray = dateString.split(" ");
		
		for (int i=0; i<dateArray.length; i++) {
			if (i==0) {
				convertedDateString += dateArray[i];  // add year
			} else if (i == 1) {
				convertedDateString += "-";
				if (dateArray[i].equals("Jan")) {  // change abbreviated month name to number
					convertedDateString += "01";
				} else if (dateArray[i].equals("Feb")) {
					convertedDateString += "02";
				} else if (dateArray[i].equals("Mar")) {
					convertedDateString += "03";
				} else if (dateArray[i].equals("Apr")) {
					convertedDateString += "04";
				} else if (dateArray[i].equals("May")) {
					convertedDateString += "05";
				} else if (dateArray[i].equals("Jun")) {
					convertedDateString += "06";
				} else if (dateArray[i].equals("Jul")) {
					convertedDateString += "07";
				} else if (dateArray[i].equals("Aug")) {
					convertedDateString += "08";
				} else if (dateArray[i].equals("Sep")) {
					convertedDateString += "09";
				} else if (dateArray[i].equals("Oct")) {
					convertedDateString += "10";
				} else if (dateArray[i].equals("Nov")) {
					convertedDateString += "11";
				} else if (dateArray[i].equals("Dec")) {
					convertedDateString += "12";
				} else {  // month is already a number - pad 1-9 with a leading 0 and add
					if (dateArray[1].length() == 1) {
						convertedDateString += "0" + dateArray[i];
					} else {
						convertedDateString += dateArray[i];
					}
				}
				
			} else if (i == 2) { 
				convertedDateString += "-" + dateArray[i]; // add date
			}
		}  // end for loop
		
		if (dateArray.length == 1 && !dateArray[0].replaceAll(" ", "").equals("")) {  // date contains year only - add month and day
			convertedDateString += "-01-01";
		} else if (dateArray.length == 2) {  // date contains month only - add day
			convertedDateString += "-01";
		}
		return convertedDateString;
		
	}
	
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


