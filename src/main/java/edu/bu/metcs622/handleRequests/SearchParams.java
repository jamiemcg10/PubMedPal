package edu.bu.metcs622.handleRequests;

/**
 * Class to define structure of searches for gson parser
 *
 */
public class SearchParams {
	private String method;
	private String term;
	private String startDate;
	private String endDate;
	private String getCount;
	
	public SearchParams() {}
	
	public String toString() {
		return "{\"method\": \"" + method + "\", " +
				"\"term\": \"" + term + "\", " +
				"\"startDate\": \"" + startDate  + "\", " +
				"\"endDate\": \"" + endDate +  "\", " +
				"\"getCount\": \"" + getCount +"\""
				+ "}";
	}
	
	
	/**
	 * Static method to translate number into search method string
	 * @param num
	 * @return String with method
	 */
	public static String getMethodString(String num) {
		String searchMethodWord = "";
		if (num.equals("1")) {
			searchMethodWord = "Brute force";
		} else if (num.equals("2")) {
			searchMethodWord = "Lucene";
		}  else if (num.equals("3")) {
			searchMethodWord = "MongoDB";
		}  else if (num.equals("4")) {
			searchMethodWord = "MySQL";
		} 	
		
		return searchMethodWord;
	}
	
	public String getMethod() {
		return this.method;
	}

	public void setMethod(String method) {
		this.method = method;
	}
	
	public String getTerm() {
		return this.term;
	}

	public void setTerm(String term) {
		this.term = term;
	}
	
	public String getStartDate() {
		return this.startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	
	public String getEndDate() {
		return this.endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	
	public String getGetCount() {
		return this.getCount;
	}

	public void setGetCount(String getCount) {
		this.getCount = getCount;
	}
	
	
}
