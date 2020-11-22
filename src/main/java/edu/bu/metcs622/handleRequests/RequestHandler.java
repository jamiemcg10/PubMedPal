package edu.bu.metcs622.handleRequests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.bu.metcs622.scandata.Engine;

/**
 * Class to handle and direct user searches
 *
 */
public class RequestHandler {
	
	/**
	 * Function that takes initial request from user and determines if it's valid
	 * Also determines if it's a request to see the search history
	 * @param engine
	 * @param request
	 * @return JSON-formatted string with term, search dates, whether the user wants a count
	 * of articles and search history (if that's what user requested)
	 */
	public static String parseRequest(Engine engine, String request) {
		
		String lowerRequest = request.toLowerCase();
		try {
			lowerRequest = URLDecoder.decode(lowerRequest, "UTF-8");  // decode url (translate special characters)
		} catch (UnsupportedEncodingException e) {
			engine.getLogger().writeToErrorLog(e.toString());
			e.printStackTrace();
		}
		
        lowerRequest = lowerRequest.replaceAll("[?.!/]$","");  // remove punctuation from end of request
        lowerRequest = lowerRequest.replaceAll("again$",""); // remove unnecessary words from end of request
        lowerRequest = lowerRequest.replaceAll("please$",""); 
        lowerRequest = lowerRequest.trim();
        
		String searchHistory = "\"\""; // initialize string of empty string for search history
		String requestType = "undefined";
		
		// initialize indices of terms and dates in request to not found value (-1)
		int termFinderIndex = -1;
		int termStartIndex = -1;
		int sDateFinderIndex = -1;
		int sDateStartIndex = -1;
		int eDateFinderIndex = -1;
		int eDateStartIndex = -1;
		int eDateEndIndex = -1;
		boolean returnCount = false;
		
		
		String term = "";
		String startDate = "";
		String endDate = "";
		
		if (lowerRequest.contains("search history")) {
			// return search history
			requestType = "get search history";
			searchHistory = "{\"frequencies\": \""+ engine.getHistory().getSearchFrequency() + "\", \"descriptions\": \"" + engine.getHistory().getSearchHistory() + "\"}";
		} else {
			// not a request for the search history - find the search term

			// determine whether count of articles should be returned
			if (lowerRequest.indexOf("how many") >= 0 || lowerRequest.indexOf("count ") >= 0) {
				returnCount = true;
			}
		
			
			// look for key phrases to identify the start of the search term
			if ((termFinderIndex = lowerRequest.indexOf("papers on")) >= 0) {
				termStartIndex = termFinderIndex + 9;
			} else if ((termFinderIndex = lowerRequest.indexOf("papers about")) >= 0) {
				termStartIndex = termFinderIndex + 12;
			} else if ((termFinderIndex = lowerRequest.indexOf("articles on")) >= 0) {
				termStartIndex = termFinderIndex + 11;
			} else if ((termFinderIndex = lowerRequest.indexOf("articles for")) >= 0) {
				termStartIndex = termFinderIndex + 12;
			} else if ((termFinderIndex = lowerRequest.indexOf("articles about")) >= 0) {
				termStartIndex = termFinderIndex + 14;
			} else if ((termFinderIndex = lowerRequest.indexOf("stuff on")) >= 0) {
				termStartIndex = termFinderIndex + 8;
			} else if ((termFinderIndex = lowerRequest.indexOf("stuff about")) >= 0) {
				termStartIndex = termFinderIndex + 11;
			} else if ((termFinderIndex = lowerRequest.indexOf("things on")) >= 0) {
				termStartIndex = termFinderIndex + 9;
			} else if ((termFinderIndex = lowerRequest.indexOf("things about")) >= 0) {
				termStartIndex = termFinderIndex + 12;
			} else if ((termFinderIndex = lowerRequest.indexOf("include the term")) >= 0) {
				termStartIndex = termFinderIndex + 16;
			} else if ((termFinderIndex = lowerRequest.indexOf("search for")) >= 0) {
				termStartIndex = termFinderIndex + 10;
			} else if ((termFinderIndex = lowerRequest.indexOf("look for")) >= 0) {
				termStartIndex = termFinderIndex + 8;
			} else if ((termFinderIndex = lowerRequest.indexOf("show me")) >= 0) {
				termStartIndex = termFinderIndex + 7;
			} else if ((termFinderIndex = lowerRequest.indexOf("are there about")) >= 0) {
				termStartIndex = termFinderIndex + 15;
			} else if ((termFinderIndex = lowerRequest.indexOf("are there for")) >= 0) {
				termStartIndex = termFinderIndex + 13;
			} else if ((termFinderIndex = lowerRequest.indexOf("are there on")) >= 0) {
				termStartIndex = termFinderIndex + 12;
			} else if ((termFinderIndex = lowerRequest.indexOf("are on")) >= 0) {
				termStartIndex = termFinderIndex + 6;
			} else if ((termFinderIndex = lowerRequest.indexOf("are about")) >= 0) {
				termStartIndex = termFinderIndex + 9;
			}
			
			// if a key phrase was found, find the end of the search term
			if (termStartIndex >= 0) { 
				int termEndIndex = lowerRequest.indexOf(" ", termStartIndex+1); // look for a space after the term
				if (termEndIndex == -1) { // term is last word of request
					term = lowerRequest.substring(termStartIndex).trim();
				} else {
					term = lowerRequest.substring(termStartIndex, termEndIndex).trim();
				}
	
				
				// there is a term - look for search range
				if (termEndIndex != -1) {  
					if ((sDateFinderIndex = lowerRequest.indexOf("between")) >= 0) { // removed termEndIndex from indexOf
						sDateStartIndex = sDateFinderIndex + 7;
						int[] indexes = getEDateStartIndex(lowerRequest, sDateStartIndex, new String[]{"and"});
						eDateStartIndex = indexes[1];  // beginning of end date
						eDateFinderIndex = indexes[0]; // beginning of end date key phrase
					} else if ((sDateFinderIndex = lowerRequest.indexOf("from")) >= 0) {
						sDateStartIndex = sDateFinderIndex + 4;
						int[] indexes = getEDateStartIndex(lowerRequest, sDateStartIndex, new String[]{"to", "through"});
						eDateStartIndex = indexes[1];
						eDateFinderIndex = indexes[0];
					} else if ((sDateFinderIndex = lowerRequest.indexOf("starting in")) >= 0) {
						sDateStartIndex = sDateFinderIndex + 11;
						int[] indexes = getEDateStartIndex(lowerRequest, sDateStartIndex, new String[]{"through", "until", "ending in"});
						eDateStartIndex = indexes[1];
						eDateFinderIndex = indexes[0];
					} else if ((sDateFinderIndex = lowerRequest.indexOf("starting from")) >= 0) {
						sDateStartIndex = sDateFinderIndex + 13;
						int[] indexes = getEDateStartIndex(lowerRequest, sDateStartIndex, new String[]{"through", "until", "ending in"});
						eDateStartIndex = indexes[1];
						eDateFinderIndex = indexes[0];
					} 
					
						
						if (eDateStartIndex > -1) { // there is a start date and end date - set the start and end date
							startDate = lowerRequest.substring(sDateStartIndex, eDateFinderIndex).replaceAll("[^0-9]", "");  // set start date and remove non-numeric chars
							if ((eDateEndIndex = lowerRequest.indexOf(" ", eDateStartIndex+1)) >= 0) { // there is a space after the end date
								endDate = lowerRequest.substring(eDateStartIndex, eDateEndIndex).replaceAll("[^0-9]", "");  // set end date and remove non-numeric chars
							} else {
								endDate = lowerRequest.substring(eDateStartIndex).replaceAll("[^0-9]", ""); //  set end date and remove non-numeric chars
							}
						}

					
					if (sDateStartIndex == -1) { // there is no start date yet
						if ((sDateFinderIndex = lowerRequest.indexOf("after")) >= 0) {
							sDateStartIndex = sDateFinderIndex + 5;
							startDate = startDate.replaceAll("[^0-9]", "");  // remove non-numeric characters
							// start from next year
							try {
								int numDate = Integer.parseInt(startDate);
								numDate++;
								startDate = new Integer(numDate).toString();
							} catch (Exception e){
								e.printStackTrace();
								engine.getLogger().writeToErrorLog(e.toString());
							}
						} if ((eDateFinderIndex = lowerRequest.indexOf("before")) >= 0) {
							eDateStartIndex = eDateFinderIndex + 6;
							endDate = endDate.replaceAll("[^0-9]", "");  // remove non-numeric characters
							// end year before
							try {
								int numDate = Integer.parseInt(endDate);
								numDate--;
								endDate = new Integer(numDate).toString();
							} catch (Exception e){
								e.printStackTrace();
								engine.getLogger().writeToErrorLog(e.toString());
							}

						}
					}

				}
								
			} // end if termStartIndex >=0 
			
			// have entire date now
		}
		
		if (term != "") {
			// there is a term so there will be a search
			requestType = "search";
		}
		
		// return json for botui
		return "{\"term\": \"" + term + "\", \"requestType\": \"" + requestType + "\", \"searchHistory\": " + searchHistory + 
				", \"startDate\": \"" + parseDate(startDate) + "\", \"endDate\": \"" + parseDate(endDate)+ "\", \"getCount\": \"" + returnCount + "\"}";
		 
	} // end parseRequest()
	
	private static int[] getEDateStartIndex(String lowerRequest, int sDateStartIndex, String[] keyPhrases) {
		int eDateStartIndex = -1;
		int eDateFinderIndex = -1;
		
		for (String phrase : keyPhrases) { // loop through key phrases and look for them in request
			if ((eDateFinderIndex = lowerRequest.indexOf(phrase, sDateStartIndex)) >= 0) {
				eDateStartIndex = eDateFinderIndex + phrase.length();
				return new int[]{eDateFinderIndex, eDateStartIndex};
			}
		}
		
		return new int[]{eDateFinderIndex, eDateStartIndex};  // returns if key phrases weren't found
		
	}  // end getEDateStartIndex()
	
	
	private static String parseDate(String rawDate) {
		String cleanDate = "";
		rawDate.replaceAll("'", ""); // remove apostrophe (if any) from date
		// try to match to different regex strings
		Pattern yearPattern4 = Pattern.compile("((19|20)[0-9][0-9])");
		Matcher m4 = yearPattern4.matcher(rawDate);
		if (m4.find()) { // 4 digit year found
			cleanDate = m4.group();
		} else { // try to match to 2 digit year
			Pattern yearPattern2 = Pattern.compile("([0-9][0-9])");
			Matcher m2 = yearPattern2.matcher(rawDate);
			if (m2.find()) {  // 2 digit year foudn
				cleanDate = m2.group();
				if (Integer.parseInt(cleanDate) >= 30) {  // add millenium to year
					cleanDate = "19"+cleanDate;
				} else {
					cleanDate = "20"+cleanDate;
				}
			}
		}
		
		return cleanDate;
		
	}
	
	/**
	 * Directs the search to the correct search type with the appropriate parameters
	 * @param engine - the Engine instance
	 * @param method - an int string representing the type of search
	 * @param term - the term to search for
	 * @param startDate - the starting year for the search range
	 * @param endDate - the ending year for the search range
	 * @param getCount - boolean string for whether the search should count the number of articles the search returns
	 * @return JSON string with results, a description of the search, and whether the result is a count
	 */
	public static String getRequestResult(Engine engine, String method, String term, String startDate, String endDate, String getCount) {
		String results = "";
		String searchDescription = "";
		String shortSearchDescription = "";
		String searchType = "basic";
		
		if (getCount.equals("true")) {
			searchType = "count";
		} else {
			searchType = "find";
		}
		
		// for timing search
		long searchStartTime = 0;
		long searchEndTime = 0;
		
		method = SearchParams.getMethodString(method);  // turn method int String into a word String
		
		// route search request
		if (startDate.equals("") && endDate.equals("")) {  // search just for term
			shortSearchDescription = term;
			if (method.equals("Brute force")) { 
				try {
					if (getCount.equals("true")) { // search for number of articles
						searchStartTime = new Date().getTime();
						results = engine.getBfSearch().searchFile(term)[1];
						searchEndTime = new Date().getTime();
						searchDescription = "Do a brute force search for " + term;
					} else { // search for article titles
						searchStartTime = new Date().getTime();
						results = engine.getBfSearch().searchFile(term)[0];
						searchEndTime = new Date().getTime();
						searchDescription = "Do a brute force search for " + term;
					}
				} catch (IOException e) {
					engine.getLogger().writeToErrorLog(e.toString());
					e.printStackTrace();
				}
			} else if (method.equals("Lucene")) {
				try {
					if (getCount.equals("true")) { // search for number of articles
						searchStartTime = new Date().getTime();
						results = engine.getLucene().luceneSearch(term)[1];
						searchEndTime = new Date().getTime();
						searchDescription = "Do a lucene search for " + term;
					} else { // search for article titles
						searchStartTime = new Date().getTime();
						results = engine.getLucene().luceneSearch(term)[0];
						searchEndTime = new Date().getTime();
						searchDescription = "Do a lucene search for " + term;
					}
				} catch (org.apache.lucene.queryparser.classic.ParseException e) {
					engine.getLogger().writeToErrorLog(e.toString());
					e.printStackTrace();
				}
			}  else if (method.equals("MongoDB")) { 
				try {
					if (getCount.equals("true")) { // search for number of articles
						searchStartTime = new Date().getTime();
						results = engine.getMongodb().countKeywordsMongoDB(term);
						searchEndTime = new Date().getTime();
						searchDescription = "Get the number of results for " + term + "from MongoDB ";
					} else { // search for article titles
						searchStartTime = new Date().getTime();
						results = engine.getMongodb().keywordSearchMongoDB(term);
						searchEndTime = new Date().getTime();
						searchDescription = "Do a MongoDB search for " + term;
					}
				} catch (ParseException e) {
					engine.getLogger().writeToErrorLog(e.toString());
					e.printStackTrace();
				}
			}  else if (method.equals("MySQL")) { 
				try {
					if (getCount.equals("true")) { // search for number of articles
						searchStartTime = new Date().getTime();
						results = engine.getMysql().countKeywordsMYSQL(term);
						searchEndTime = new Date().getTime();
						searchDescription = "Get the number of results for " + term + "from MySQL ";
					} else { // search for article titles
						searchStartTime = new Date().getTime();
						results = engine.getMysql().keywordSearchMYSQL(term);
						searchEndTime = new Date().getTime();
						searchDescription = "Do a MySQL database search for " + term;
					}
				} catch (Exception e) {
					engine.getLogger().writeToErrorLog(e.toString());
					e.printStackTrace();
				}
			} 
		} else { // at least one date is present in search
			shortSearchDescription = term + (startDate.equals("") ? "" : (" from " + startDate)) + (endDate.equals("") ? " through now" : (" through " + endDate));
			if (getCount.equals("true")) {
				searchType = "count within date range";
			} else {
				searchType = "find within date range";
			}
			
			if (startDate.equals("")){ // set start date if none provided
				startDate = "1970";
			} else if (endDate.equals("")) {  // set end date if none provided
				endDate = "2021";
			}
			
			
			
			if (method.equals("Brute force")) {
				try {
					if (getCount.equals("true")) { // search for number of articles
						searchStartTime = new Date().getTime();
						results = engine.getBfSearch().searchFile(term, startDate, endDate)[1];
						searchEndTime = new Date().getTime();
						searchDescription = "Do a brute force search for " + term + " from " + startDate + " through " + endDate;
					} else { // search for article titles
						searchStartTime = new Date().getTime();
						results = engine.getBfSearch().searchFile(term, startDate, endDate)[0];
						searchEndTime = new Date().getTime();
						searchDescription = "Do a brute force search for " + term + " from " + startDate + " through " + endDate;
					}
				} catch (IOException e) {
					engine.getLogger().writeToErrorLog(e.toString());
					e.printStackTrace();
				}
			} else if (method.equals("Lucene")) { 
				try {
					if (getCount.equals("true")) { // search for number of articles
						searchStartTime = new Date().getTime();
						results = engine.getLucene().luceneSearch(term, startDate, endDate)[1];
						searchEndTime = new Date().getTime();
						searchDescription = "Do a lucene search for " + term + " from " + startDate + " through " + endDate;
					} else { // search for article titles
						searchStartTime = new Date().getTime();
						results = engine.getLucene().luceneSearch(term, startDate, endDate)[0];
						searchEndTime = new Date().getTime();
						searchDescription = "Do a lucene search for " + term + " from " + startDate + " through " + endDate;
					}
				} catch (org.apache.lucene.queryparser.classic.ParseException e) {
					engine.getLogger().writeToErrorLog(e.toString());
					e.printStackTrace();
				}
			}  else if (method.equals("MongoDB")) { 
				try {
					if (getCount.equals("true")) { // search for number of articles
						searchStartTime = new Date().getTime();
						results = engine.getMongodb().countKeywordsMongoDB(term, startDate, endDate);
						searchEndTime = new Date().getTime();
						searchDescription = "Count the number of results for " + term + " from MongoDB from " + startDate + " through " + endDate;
					} else { // search for article titles
						searchStartTime = new Date().getTime();
						results = engine.getMongodb().keywordSearchMongoDB(term, startDate, endDate);
						searchEndTime = new Date().getTime();
						searchDescription = "Do a MongoDB search for " + term + " from " + startDate + " through " + endDate;
					}
				} catch (ParseException e) {
					engine.getLogger().writeToErrorLog(e.toString());
					e.printStackTrace();
				}
			}  else if (method.equals("MySQL")) {
				try {
					if (getCount.equals("true")) { // search for number of articles
						searchStartTime = new Date().getTime();
						results = engine.getMysql().countKeywordsMYSQL(term, startDate, endDate);
						searchEndTime = new Date().getTime();
						searchDescription = "Count the number of results for " + term + " from MySQL from " + startDate + " through " + endDate;
					} else { // search for article titles
						searchStartTime = new Date().getTime();
						results = engine.getMysql().keywordSearchMYSQL(term, startDate, endDate);
						searchEndTime = new Date().getTime();
						searchDescription = "Do a MySQL database search for " + term + " from " + startDate + " through " + endDate;
					}
				} catch (Exception e) {
					engine.getLogger().writeToErrorLog(e.toString());
					e.printStackTrace();
				}
				
			} 
			
		}
		
		
		// add to search history
		engine.getHistory().addToSearchHistory(method, searchDescription);
		
		
		// handle search time
		System.out.println("Search ("+ searchType +") took " + (searchEndTime - searchStartTime) + " ms.");
		engine.getLogger().writeToSearchLog(searchType, engine.getFileSize(), method, term, (searchEndTime - searchStartTime));
		
		// have the result of the function returned as a string
		results = results.replace("\"", "\\\"");

		return "{\"shortSearchDescription\": \"" + shortSearchDescription + "\", \"results\": \"" + results + "\", \"getCount\": \"" + getCount + "\"}";
		
	}
	
	
}
