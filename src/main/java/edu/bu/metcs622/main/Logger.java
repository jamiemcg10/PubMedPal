package edu.bu.metcs622.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

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
		searchLog = new File(Constants.SEARCH_LOG_LOCATION);
		errorLog = new File(Constants.ERROR_LOG_LOCATION);
				
		
		try {
			errorLog.createNewFile();
			errorLogWriter = new BufferedWriter(new FileWriter(errorLog, true));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			
			if (searchLog.createNewFile()) {
				System.out.println("Search log doesn't exist");
				searchLogWriter = new BufferedWriter(new FileWriter(searchLog));
				searchLogWriter.write("Date,File size (KB),Type,Method,Term,Time (ms.)\n");
				searchLogWriter.flush();
			} else {
				searchLogWriter = new BufferedWriter(new FileWriter(searchLog, true));
			}
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
			searchLogWriter.write(new Date().toString() + "," + fileSize/1000 + "," + type + "," + method + "," + term + ","+ time + "\n");
			searchLogWriter.flush();
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
			errorLogWriter.write(new Date().toString()+"\n"+error+"\n");
			errorLogWriter.flush();
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
		}
		
		return successful;
	}
	
}
