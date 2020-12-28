package edu.bu.metcs622.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import edu.bu.metcs622.bucketaccess.GetS3Object;
import edu.bu.metcs622.bucketaccess.PutS3Object;

/**
 * Class to log search results and errors to file
 *
 */
public class Logger {

	private File searchLog;
	private BufferedWriter searchLogWriter;
	private File errorLog;
	private BufferedWriter errorLogWriter;
	
	/**
	 * Constructor to initialize log files
	 */
	public Logger(){
	  	System.setProperty("aws.accessKeyId", Constants.ACCESS_KEY_ID); 
	  	System.setProperty("aws.secretAccessKey", Constants.SECRET_ACCESS_KEY);
	  	
//		searchLog = new File(Constants.SEARCH_LOG_LOCATION);
//		errorLog = new File(Constants.ERROR_LOG_LOCATION);
	  	
	  	searchLog = GetS3Object.getFile("search_log.csv");
	  	errorLog = GetS3Object.getFile("error_log.txt");

		
		try {
//			errorLog.createNewFile();
		  	if (errorLog.length() == 0) {
		  		// file doesn't exist on AWS
		  		System.out.println("Error log doesn't exist on AWS");
				try {
					errorLog = new File(".//error_log.txt");
					errorLog.createNewFile();
					PutS3Object.writeFile(errorLog, "error_log.txt");
					
				} catch (IOException e) {
					e.printStackTrace();
				}
		  	}
			errorLogWriter = new BufferedWriter(new FileWriter(errorLog, true));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
//			if (searchLog.createNewFile()) {
//				System.out.println("Search log doesn't exist");
//				searchLogWriter = new BufferedWriter(new FileWriter(searchLog));
//				searchLogWriter.write("Date,File size (KB),Type,Method,Term,Time (ms.)\n");
//				searchLogWriter.flush();
//			} else {
		  	if (searchLog.length() == 0) {
		  		// file doesn't exist on AWS
		  		System.out.println("Search log doesn't exist on AWS");
				try {
					searchLog = new File(".//search_log.csv");
					searchLog.createNewFile();
					searchLogWriter = new BufferedWriter(new FileWriter(searchLog));
					searchLogWriter.write("Date,File size (KB),Type,Method,Term,Time (ms.)\n");
					searchLogWriter.flush();
					PutS3Object.writeFile(searchLog, "search_log.csv");
					
				} catch (IOException e) {
					e.printStackTrace();
				}
		  	}
			searchLogWriter = new BufferedWriter(new FileWriter(searchLog, true));
//			}
		} catch (IOException e) {
			writeToErrorLog(e.toString());
			e.printStackTrace();
		}
		

		System.out.println("Logs initialized");
	}
	
	/**
	 * Appends search data to search log
	 * @param type
	 * @param fileSize
	 * @param method
	 * @param term
	 * @param time
	 * @return boolean of whether write was successful
	 */
	public boolean writeToSearchLog(String type, long fileSize, String method, String term, long time) {
		boolean successful = false;
		
		try {
			searchLog = GetS3Object.getFile("search_log.csv");
			searchLogWriter = new BufferedWriter(new FileWriter(searchLog, true));
			searchLogWriter.write(new Date().toString() + "," + fileSize/1000 + "," + type + "," + method + "," + term + ","+ time + "\n");
			searchLogWriter.flush();
			PutS3Object.writeFile(searchLog, "search_log.csv");
			
			successful = true;
		} catch (IOException e) {
			writeToErrorLog(e.toString());
			e.printStackTrace();
		}
		
		
		return successful;
	}
	
	/**
	 * Appends error with date to error log
	 * @param error
	 * @return boolean of whether write was successful
	 */
	public boolean writeToErrorLog(String error) {
		boolean successful = false;
		

		try {
			errorLog = GetS3Object.getFile("error_log.txt");
			errorLogWriter = new BufferedWriter(new FileWriter(errorLog, true));
			errorLogWriter.write(new Date().toString()+"\n"+error+"\n");
			errorLogWriter.flush();
			PutS3Object.writeFile(errorLog, "error_log.txt");
			
			successful = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return successful;
	}
	
	/**
	 * Attempts to close search log
	 * @return boolean of whether log was successfully closed
	 */
	public boolean closeSearchLog() {
		boolean successful = false;
		try {
			searchLogWriter.flush();
			searchLogWriter.close();
			successful = true;
		} catch (IOException e) {
			writeToErrorLog(e.toString());
			e.printStackTrace();
		} catch (Exception e) {
			writeToErrorLog(e.toString());
			e.printStackTrace();
		}
		
		return successful;
	}
	
	/**
	 * Attempts to close error log
	 * @return boolean of whether log was successfully closed
	 */
	public boolean closeErrorLog() {
		boolean successful = false;
		try {
			errorLogWriter.flush();
			errorLogWriter.close();
			successful = true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return successful;
	}
	
}
