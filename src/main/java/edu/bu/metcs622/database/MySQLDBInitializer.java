package edu.bu.metcs622.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

////import edu.bu.metcs622.main.Constants;
import edu.bu.metcs622.scandata.Engine;


/**
 * Initializes connection with MySQL and provides methods for searching database by keyword and date
*/
public class MySQLDBInitializer {
	private Connection con = null;
	private Statement stmt;
	private String tableName = "";
	private Engine engine;

	
	/**
	 * Constructor initializes connection with database
	 */
	public MySQLDBInitializer(Engine engine) {
		this.engine = engine;
		this.tableName = "pubmeddata";
		try {
			// connect to database with password user supplied
//			con = DriverManager.getConnection(Constants.MYSQL_ADDRESS);			
			con = DriverManager.getConnection(System.getenv("MYSQL_ADDRESS"));

			// create database if doesn't exist
			stmt = con.createStatement();
			
		} catch (SQLException e1) {
			// password or database location is invalid
			engine.getLogger().writeToErrorLog(e1.toString());
			System.out.println("Cannot connect to MYSQL. Will proceed with only MongoDB.");		
		}
	}
	
	/**
	 * Constructor initializes connection with database to provision data
	 */
	public MySQLDBInitializer(Engine engine, String files) {
		this.engine = engine;
		this.tableName = files;
		try {
			// connect to database with password user supplied
//			con = DriverManager.getConnection(Constants.MYSQL_ADDRESS);
			con = DriverManager.getConnection(System.getenv("MYSQL_ADDRESS"));
			
			// create database if doesn't exist
			stmt = con.createStatement();
			try {
				// create table
				stmt.execute("CREATE TABLE IF NOT EXISTS " + files +" (articleTitle VARCHAR(500), abstractText VARCHAR(5000), keywords VARCHAR(1000), "
						+ "pubDateString DATE)");
				//stmt.execute("DELETE FROM " + files);  // clear database	
			} catch (SQLException e) {
				engine.getLogger().writeToErrorLog(e.toString());
				e.printStackTrace();
			}
			
		} catch (SQLException e1) {
			// password or database location is invalid
			e1.printStackTrace();
			System.out.println("Cannot connect to MYSQL. Will proceed with only MongoDB.");
			
		}
	}
	
	/**
	 * Checks for whether a table exists in the MySQL database
	 * @param tableName
	 * @return whether table exists in MySQL database
	 */
	public boolean checkTableExists(String tableName) {
		if (con != null) {  // if there is a database connection
			
			// search for number of records that match entered keyword
			ResultSet sqlResults = sendMYSQLQuery("SELECT COUNT(*) FROM " + tableName);
			
				try { 
					if (sqlResults.next()) {
						if (!sqlResults.getString(1).equals("0")) {
							return true;
						}
					}
				} catch (SQLException e) {
					engine.getLogger().writeToErrorLog(e.toString());
					System.out.println("Sorry, there was a problem with this operation.");
					return false;
				}
		}
		
		return false;
	}
	
	public Connection getCon() {
		return this.con;
	}
	
	/**
	 * Inserts row into MySQL database with given values
	 * @param articleTitle
	 * @param abstractText
	 * @param keywords
	 * @param pubDateString - if blank, will not insert
	 */
	public void insertRow(String articleTitle, String abstractText, String keywords, String pubDateString) {
		try {
			if (pubDateString.equals("")) {  // if there's no date, don't insert a date
				stmt.executeUpdate("INSERT INTO " + tableName + "(articleTitle, abstractText, keywords) "
						+ "VALUES ('" 
						+ articleTitle 
						+ "', '" 
						+ abstractText 
						+ "', '" 
						+ keywords 
						+ "')");
			} else { 
				// there's a date
				stmt.executeUpdate("INSERT INTO " + tableName + "(articleTitle, abstractText, keywords, pubDateString) "
						+ "VALUES ('" 
						+ articleTitle 
						+ "', '" 
						+ abstractText 
						+ "', '" 
						+ keywords 
						+ "', '" 
						+ pubDateString
						+ "')");
			}

		} catch (SQLException e) {
			// likely here because a value was truncated
			// nothing needs to be done
			//e.printStackTrace();

		}
	}
	
	/**
	 * Finds and returns the number of records (article title, abstract, or keywords) a given keyword is in if
	 * there is a connection to the database
	 * @param keyword
	 * @return String with the number of occurrences found
	 */
	public String countKeywordsMYSQL(String keyword) {
		String numKeywords = "";
		if (con != null) {  // if there is a database connection
			// search for number of records that match entered keyword
			ResultSet sqlResults = sendMYSQLQuery("SELECT COUNT(articleTitle) FROM " + tableName + " WHERE articleTitle LIKE '%" + keyword + "%' OR abstractText LIKE '%" + keyword + "%' OR keywords LIKE '%" + keyword + "%'");
			
				try { 
					while(sqlResults.next()) {
						// print number of matches
						numKeywords = sqlResults.getString(1);
					}
				} catch (SQLException e) {
					engine.getLogger().writeToErrorLog(e.toString());
					System.out.println("Sorry, there was a problem with this operation.");
				}
		}
	
		return numKeywords;
	}
	
	/**
	 * Finds and returns the number of records (article title, abstract, or keywords) a given keyword is in within a given
	 * date range if there is a connection to the database
	 * @param keyword
	 * @param beginYear
	 * @param endYear
	 * @return String with the number of records found
	 */
	public String countKeywordsMYSQL(String keyword, String beginYear, String endYear) {
		String numKeywords = "";
		if (con != null) {  // if there is a database connection
			
			// search for number of records that match entered keyword
			ResultSet sqlResults = sendMYSQLQuery("SELECT COUNT(articleTitle) FROM " + tableName + " WHERE (articleTitle LIKE '%" + keyword + "%' OR abstractText LIKE '%" + keyword + "%' OR keywords LIKE '%" + keyword + "%')" 
					+ "AND (pubDateString BETWEEN '" + beginYear + "-01" + "-01' AND '" + endYear + "-12" + "-31')");
			
				try { 
					while(sqlResults.next()) { // if there is a match
						numKeywords = sqlResults.getString(1);
					}
				} catch (SQLException e) {
					engine.getLogger().writeToErrorLog(e.toString());
					System.out.println("Sorry, there was a problem with this operation.");
				}
		}
		
		return numKeywords;
	}
	
	/**
	 * Finds the article titles of articles matching the given keyword if there is a connection to the database
	 * @param keyword
	 * @return an HTML string containing the results as <li>s
	 */
	public String keywordSearchMYSQL(String keyword) {
		String results = "";
		if (con != null) {  // if there is a database connection
			// query database for date range and keywords
			ResultSet sqlResults = sendMYSQLQuery("SELECT articleTitle, abstractText, keywords, pubDateString FROM " + tableName + " WHERE (articleTitle LIKE '%" + keyword + "%' OR abstractText LIKE '%" + keyword + "%' OR keywords LIKE '%" + keyword + "%')");
					
			
			try {
				// add results to result string
				while(sqlResults.next()) {
					results += "<li>" + sqlResults.getString(1) +"</li>";
				}
			} catch (SQLException e) {
				engine.getLogger().writeToErrorLog(e.toString());
				e.printStackTrace();
			}
		}
		
		return results;
		
	}
	
	/**
	 * Finds the article titles of articles matching the given keyword within the given date range if there is a connection to the database
	 * @param keyword
	 * @param beginYear starting year
	 * @param endYear ending year
	 * @return an HTML string containing the results as <li>s
	 */
	public String keywordSearchMYSQL(String keyword, String beginYear, String endYear) {
		String results = "";
		if (con != null) {  // if there is a database connection
			
			// query database for date range and keywords
			ResultSet sqlResults = sendMYSQLQuery("SELECT articleTitle, abstractText, keywords, pubDateString FROM " + tableName + " WHERE (articleTitle LIKE '%" + keyword + "%' OR abstractText LIKE '%" + keyword + "%' OR keywords LIKE '%" + keyword + "%') "
					+ "AND pubDateString BETWEEN '" + beginYear + "-01" + "-01' AND '" + endYear + "-12" + "-31'");
			
			try {
				// add results to result string
				while(sqlResults.next()) {
					results += "<li>" + sqlResults.getString(1) + "</li>";
				}
			} catch (SQLException e) {
				engine.getLogger().writeToErrorLog(e.toString());
				e.printStackTrace();
			}
			
		}
		
		return results;
	}
	

	
	private ResultSet sendMYSQLQuery(String queryStr) {
		// helper function to query database
		ResultSet rs = null;
		try {
			rs = stmt.executeQuery(queryStr);			
		} catch (SQLException e) {
			engine.getLogger().writeToErrorLog(e.toString());
			e.printStackTrace();
		}  
		
		return rs;
	}
	
	public void closeSQLDatabase() throws SQLException {
		// close database if it's open
		if (con != null) {
			con.close();
		}
	}
}
