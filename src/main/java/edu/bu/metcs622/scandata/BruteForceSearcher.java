package edu.bu.metcs622.scandata;

//import java.io.BufferedWriter;
//import java.io.FileWriter;
import java.io.IOException;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Scanner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BruteForceSearcher {
	private Document doc;
		
	public BruteForceSearcher(Document doc) {
		this.doc = doc;
	}
	
	/**
	 * Search a Document for a given term - searches article title, keywords, and abstract
	 * @param searchTerm - term to search for
	 * @return - array with results and number of results
	 * @throws IOException
	 */
	public String[] searchFile(String searchTerm) throws IOException {
		String returnResultsString = "";
		int returnResultCount = 0;
		searchTerm = searchTerm.toLowerCase();  // change search term to lower case to increase match accuracy
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
			
			
			if (articleTitle.toLowerCase().indexOf(searchTerm) >= 0 || abstractText.toLowerCase().indexOf(searchTerm) >= 0 ||
					keywords.toLowerCase().indexOf(searchTerm) >= 0) {  // if the keyword is in the article title, abstract, or keywords, a result was found
																		// process it
				Result searchResult = new Result();  // create new result
				
				// add Article Title to result
				searchResult.setArticleTitle(articleTitle);
				
				// add Journal Title to result
//				searchResult.setJournalTitle(( (Element) articleElement.getElementsByTagName("Journal").item(0) )
//						.getElementsByTagName("Title").item(0).getTextContent());
				
				//	add published date to result - only add individial fields if they are included
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
				
				searchResult.setPubDate(pubDateString);

				// Add authors names to result
				String authorString = "";
//				try {
//					NodeList authors = ((Element) articleElement.getElementsByTagName("AuthorList").item(0)).getElementsByTagName("Author");
//					for (int a=0; a<authors.getLength(); a++) {
//						if (((Element) authors.item(a)).getElementsByTagName("LastName").item(0) == null) { // if the result doesn't include a name, break from the loop
//							break;
//						}
//						//	AuthorList - Author - ForeName
//						authorString += ((Element) authors.item(a)).getElementsByTagName("ForeName").item(0).getTextContent() + " ";
//						authorString +=  ((Element) authors.item(a)).getElementsByTagName("LastName").item(0).getTextContent() + ", ";
//	
//					}
//				} catch(NullPointerException e) {
//					;
//				}
				searchResult.setAuthors(authorString);

				//	add abstract to result
				searchResult.setAbstractText(abstractText);
			
				
				// add result to results string
				returnResultsString += "<li>" + searchResult + "</li>";
				returnResultCount++;
						
				
			} // end match 	

		} // end loop through articles
		

		String[] results = {returnResultsString, String.valueOf(returnResultCount)};
		return results;
	}
	
	/**
	 * Search a Document for a given term - searches article title, keywords, and abstract
	 * @param searchTerm
	 * @param startDate
	 * @param endDate
	 * @return array with results and number of results
	 * @throws IOException
	 */
	public String[] searchFile(String searchTerm, String startDate, String endDate) throws IOException {
		// search a Document for a given term - searches article title, keywords, and abstract
		String returnResultsString = "";
		int returnResultCount = 0;
		searchTerm = searchTerm.toLowerCase();  // change search term to lower case to increase match accuracy
		startDate += "-01-01";
		endDate += "12-31";
		
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
			if (journalIssue.getElementsByTagName("Year").item(0) == null) {
				continue;
			} else {
				pubDateString += journalIssue.getElementsByTagName("Year").item(0).getTextContent() + " ";
			}

			
			// check for match
			if ((pubDateString.compareTo(startDate) >= 0 && pubDateString.compareTo(endDate) <= 0) && (articleTitle.toLowerCase().indexOf(searchTerm) >= 0 || abstractText.toLowerCase().indexOf(searchTerm) >= 0 ||
					keywords.toLowerCase().indexOf(searchTerm) >= 0)) {  // if the keyword is in the article title, abstract, or keywords, a result was found
																		// process it
				

				Result searchResult = new Result();  // create new result
						
				
				// add Article Title to result
				searchResult.setArticleTitle(articleTitle);
				
				// add Journal Title to result
//				searchResult.setJournalTitle(( (Element) articleElement.getElementsByTagName("Journal").item(0) )
//						.getElementsByTagName("Title").item(0).getTextContent());
				
				
				// finish pub date
				if (journalIssue.getElementsByTagName("Month").item(0) != null) {
					pubDateString += journalIssue.getElementsByTagName("Month").item(0).getTextContent() + " ";
				}
				if (journalIssue.getElementsByTagName("Day").item(0) != null) {
					pubDateString += journalIssue.getElementsByTagName("Day").item(0).getTextContent() + " ";
				}
				searchResult.setPubDate(pubDateString);

				// Add authors names to result
				String authorString = "";
//				try {
//					NodeList authors = ((Element) articleElement.getElementsByTagName("AuthorList").item(0)).getElementsByTagName("Author");
//					for (int a=0; a<authors.getLength(); a++) {
//						if (((Element) authors.item(a)).getElementsByTagName("LastName").item(0) == null) { // if the result doesn't include a name, break from the loop
//							break;
//						}
//						//	AuthorList - Author - ForeName
//						authorString += ((Element) authors.item(a)).getElementsByTagName("ForeName").item(0).getTextContent() + " ";
//						authorString +=  ((Element) authors.item(a)).getElementsByTagName("LastName").item(0).getTextContent() + ", ";
//	
//					}
//				} catch(NullPointerException e) {
//					;
//				}
				searchResult.setAuthors(authorString);

				//	add abstract to result
				searchResult.setAbstractText(abstractText);
			
				
				// add result to results string
				returnResultsString += "<li>" + searchResult + "</li>";
				returnResultCount++;
				
				
			} // end match 	

		} // end loop through articles
		

		System.out.println(returnResultsString);
		String[] results = {returnResultsString, String.valueOf(returnResultCount)};
		return results;
	}



}
