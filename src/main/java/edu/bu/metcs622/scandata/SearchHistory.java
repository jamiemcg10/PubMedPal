package edu.bu.metcs622.scandata;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.bu.metcs622.handleRequests.SearchParams;

/**
 * SearchHistory holds information about previous searches
 *
 */
public class SearchHistory {

	private List<Object> searchHistory; 
	private Map<String, Integer> searchFrequency;
	
	/**
	 * Constuctor initializes map for search frequencies and list for searches
	 */
	public SearchHistory() {
		searchHistory = new ArrayList<Object>();
		
		searchFrequency = new HashMap<String, Integer>();
		searchFrequency.put("Brute force", 0);
		searchFrequency.put("Lucene", 0);
		searchFrequency.put("MongoDB", 0);
		searchFrequency.put("MySQL", 0);
	}

	/**
	 * Adds search to search history
	 * @param searchMethod
	 * @param searchDescription
	 */
	public void addToSearchHistory(String searchMethod, String searchDescription) {
		
		//String searchMethodWord = SearchParams.getMethodString(searchMethod);
		
		// add to map tracking frequency of search types
		searchFrequency.replace(searchMethod, searchFrequency.get(searchMethod) + 1);		
		
		
		// add search term and search time to List
		Object[] newSearchHistoryAddition = {searchDescription, new Date().getTime()};
		searchHistory.add(newSearchHistoryAddition);
		
		
	}
	
	/**
	 * Gets how many times each type of search was performed
	 * @return String with how many times searches were performed
	 */
	public String getSearchFrequency() { 
		String searchFrequencyString = "";
		for (Map.Entry<String, Integer> entry : searchFrequency.entrySet()) {
			searchFrequencyString += "<li>" + entry.getKey() + ": " + entry.getValue() + "</li>";
		}
		
		return searchFrequencyString;
		
	}
	
	/**
	 * Gets history of user's searches
	 * @return String containing searches
	 */
	public String getSearchHistory() {  
		String searchHistoryString = "";
		
		for(Object listItem : this.searchHistory) {
			Object[] searchHistoryItem = (Object[]) listItem;
			searchHistoryString += "<li>" + new Date((long) searchHistoryItem[1]) + " --> " + searchHistoryItem[0] + "</li>";
		}
		
		return searchHistoryString;
		
	}

}
