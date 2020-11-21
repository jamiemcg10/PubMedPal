package edu.bu.metcs622.main;

/**
 * Class for holding constant values
 * Update file paths and MySQL credentials for your system
 * Rename ConstantsTemplate to Constants
 * Rename ConstantsTemplate.java to Constants.java
 */
public class ConstantsTemplate {
	// Location with .xml.gz files
	public static String DATA_LOCATION = "\\Users\\Jamie\\eclipse-workspace-2020\\FinalProjectSpring\\data\\";
	// Location for Lucene index
	public static String LUCENE_LOCATION = "/Users/Jamie/eclipse-workspace-2020/FinalProjectSpring/data/LuceneData/";
	
	// Where the search log will be saved - folder must already exist 
	public static String SEARCH_LOG_LOCATION = "C:\\Users\\Jamie\\eclipse-workspace-2020\\FinalProjectSpring\\logs\\search_log.csv";
	// Where the error log will be saved - folder must already exist 
	public static String ERROR_LOG_LOCATION = "C:\\Users\\Jamie\\eclipse-workspace-2020\\FinalProjectSpring\\logs\\error_log.txt";
	
	// Address and credentials for MySQL
	public static String MYSQL_ADDRESS = "jdbc:mysql://localhost:3306/PubMed?useTimezone=true&serverTimezone=UTC";
	public static String MYSQL_USERNAME = "root";
	public static String MYSQL_PWD = "PASSWORD";
}
