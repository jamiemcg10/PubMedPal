package edu.bu.metcs622.scandata;

/**
 * Legacy class that holds matches from Brute Force and Lucene searches
 *
 */
public class Result {
	// need this but can probably refactor to avoid
	
	private String articleTitle="";
	private String abstractText="";
	private String authors="";
	private String journalTitle="";
	private String pubDate="";
	private String keywords="";
	
	public Result() {
		
	}
	
//	public String toString() {
//		// string representation of the result
//		return "SEARCH RESULT: " + 
//				"Article Title: " + this.getArticleTitle() + "\n" + 
//				"Abstract: " + this.getAbstractText() + "\n" + 
//				"Authors: " + this.getAuthors() + "\n" +  
//				"Journal Title: " + this.getJournalTitle() + "\n" + 
//				"Date: " + this.getPubDate() + "\n";
//	}
	
	public String toString() {
		// string representation of the result
		return this.getArticleTitle();
	}

	// getters and setters
	public String getArticleTitle() {
		return articleTitle;
	}

	public void setArticleTitle(String articleTitle) {
		this.articleTitle = articleTitle;
	}

	public String getAbstractText() {
		return abstractText;
	}

	public void setAbstractText(String abstractText) {
		this.abstractText = abstractText;
	}

	public String getAuthors() {
		return authors;
	}

	public void setAuthors(String authors) {
		this.authors = authors;
	}

	public String getJournalTitle() {
		return journalTitle;
	}

	public void setJournalTitle(String journalTitle) {
		this.journalTitle = journalTitle;
	}

	public String getPubDate() {
		return pubDate;
	}

	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

}
