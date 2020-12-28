package edu.bu.metcs622.lucene;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import edu.bu.metcs622.main.Constants;
import edu.bu.metcs622.scandata.Engine;


/**
 * Class to initialize Lucene index for searches
 *
 */
public class LuceneSearcher {
	IndexWriter indexWriter;
	IndexWriter testIndexWriter;
	StandardAnalyzer analyzer;
	FSDirectory index;
	String indexName = "";
	Engine engine;
	
	
	/**
	 * Initialize lucene index writer for app
	 */
	public LuceneSearcher(Engine engine) throws ParseException {
		this.indexName = "pubmeddata";
		this.engine = engine;
		try {
//			index = FSDirectory.open(Paths.get(Constants.LUCENE_LOCATION+indexName));
			index = FSDirectory.open(Paths.get(System.getenv("LUCENE_LOCATION")+indexName));
		} catch (IOException e) {
			engine.getLogger().writeToErrorLog(e.toString());
			e.printStackTrace();
		}
	} // end constructor
	
	
	/**
	 * Initialize lucene index writer
	 */
	public LuceneSearcher(Engine engine, String fileName) throws ParseException {
		this.indexName = fileName;
		this.engine = engine;
		try {
			indexWriter = initializeLucene();	
		} catch (IOException e) {
			engine.getLogger().writeToErrorLog(e.toString());
			e.printStackTrace();
		}
	} // end constructor
	

	
	// open index and delete all entries so it can be recreated
	private IndexWriter initializeLucene() throws IOException, ParseException {
		analyzer = new StandardAnalyzer();
//		index = FSDirectory.open(Paths.get(Constants.LUCENE_LOCATION+indexName));
		index = FSDirectory.open(Paths.get(System.getenv("LUCENE_LOCATION")+indexName));
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		IndexWriter w = new IndexWriter(index, config);
		
		return w;
	}
	
	
	public boolean checkIndexExists(String indexName) {
		try {
			IndexReader reader = DirectoryReader.open(index);
			if (reader.numDocs() > 0) {
				return true;
			}
		} catch (IOException e) {
			engine.getLogger().writeToErrorLog(e.toString());
			return false;
		}

		return false;
	}

	/**
	 * Add Lucene Document to index
	 * @param articleTitle
	 * @param abstractText
	 * @param keywords
	 * @param pubDateString
	 * @throws IOException
	 */
	public void addDoc(String articleTitle, String abstractText, String keywords, String pubDateString) throws IOException {
		org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();  // create new doc to add to index
		
		// add fields to doc
		doc.add(new TextField("articleTitle", articleTitle, Field.Store.YES));
		doc.add(new TextField("abstractText", abstractText, Field.Store.YES));
		doc.add(new TextField("keywords", keywords, Field.Store.YES));
		doc.add(new StringField("pubDateString", pubDateString, Field.Store.YES));
		
		try {
			// add doc to indexWriter
			indexWriter.addDocument(doc);
		} catch (Exception e) {
			engine.getLogger().writeToErrorLog(e.toString());
			e.printStackTrace();
		}
	}
	

	/**
	 * Search for specified term in articleTitle, abstractText, and keywords fields of index
	 * @param word
	 * @return array with html string of results as <li>s and the number of results found
	 * @throws ParseException
	 */
	public String[] luceneSearch(String word) throws ParseException {
		HashSet<String> resultsSet = new HashSet<String>();  // create set to hold results
		String returnResultsString = "";
		String searchTerm = ".*" + makeRegexString(word) + ".*";  // create regex for search term
		
		try {
			
			// search these fields for the term
			RegexpQuery q1 = new RegexpQuery(new Term("articleTitle", searchTerm));
			RegexpQuery q2 = new RegexpQuery(new Term("abstractText", searchTerm));
			RegexpQuery q3 = new RegexpQuery(new Term("keywords", searchTerm));
			
			IndexReader reader = DirectoryReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			
			// find results from query - up to number of articles
			TopDocs docs1 = searcher.search(q1, 100000);
			TopDocs docs2 = searcher.search(q2, 100000);
			TopDocs docs3 = searcher.search(q3, 100000);
			
			
			ScoreDoc[] hits1 = docs1.scoreDocs;
			ScoreDoc[] hits2 = docs2.scoreDocs;
			ScoreDoc[] hits3 = docs3.scoreDocs;
			
			
			// loop through results from each query and add title to results set to remove duplicates
			for (int i=0; i<hits1.length; i++) {
				org.apache.lucene.document.Document d = searcher.doc(hits1[i].doc);
				resultsSet.add(d.get("articleTitle"));
			}
			
			for (int i=0; i<hits2.length; i++) {
				org.apache.lucene.document.Document d = searcher.doc(hits2[i].doc);
				resultsSet.add(d.get("articleTitle"));
			}
			
			for (int i=0; i<hits3.length; i++) {
				org.apache.lucene.document.Document d = searcher.doc(hits3[i].doc);
				resultsSet.add(d.get("articleTitle"));
			}

		} catch (IOException e) {
			engine.getLogger().writeToErrorLog(e.toString());
			e.printStackTrace();
		}
		
		
		for (String article : resultsSet) {
			// for each unique title found, add it to the results string as an <li>
			returnResultsString += "<li>" + article + "</li>";	
		}
		
		String[] arrayToReturn = {returnResultsString, String.valueOf(resultsSet.size())};
		return arrayToReturn;
	}
	
	/**
	 * Search for specified term in articleTitle, abstractText, and keywords fields of index within a specified date range
	 * @param word
	 * @param startDate
	 * @param endDate
	 * @return array with html string of results as <li>s and the number of results found
	 * @throws ParseException
	 */
	public String[] luceneSearch(String word, String startDate, String endDate) throws ParseException {
		HashSet<String> resultsSet = new HashSet<String>();  // create set to hold results
		String returnResultsString = "";
		String searchTerm = ".*" + makeRegexString(word) + ".*";  // create regex for search term
		startDate += "-01-01";  // add month and day to start year
		endDate += "-12-31";   // add month and day to end year
		
		try {
			
			// search these fields for the term
			RegexpQuery q1 = new RegexpQuery(new Term("articleTitle", searchTerm));
			RegexpQuery q2 = new RegexpQuery(new Term("abstractText", searchTerm));
			RegexpQuery q3 = new RegexpQuery(new Term("keywords", searchTerm));
			
			IndexReader reader = DirectoryReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			
			// find results from query - up to number of articles
			TopDocs docs1 = searcher.search(q1, 100000);
			TopDocs docs2 = searcher.search(q2, 100000);
			TopDocs docs3 = searcher.search(q3, 100000);
			
			
			ScoreDoc[] hits1 = docs1.scoreDocs;
			ScoreDoc[] hits2 = docs2.scoreDocs;
			ScoreDoc[] hits3 = docs3.scoreDocs;
			
			
			// loop through results from each query and add to results set
			for (int i=0; i<hits1.length; i++) {
				org.apache.lucene.document.Document d = searcher.doc(hits1[i].doc);
				String articleDate = d.get("pubDateString");
				if ((articleDate.compareTo(startDate) >= 0 && articleDate.compareTo(endDate) <= 0)) {
					// add to set if date is within range
					resultsSet.add(d.get("articleTitle"));
				}

			}
			
			for (int i=0; i<hits2.length; i++) {
				org.apache.lucene.document.Document d = searcher.doc(hits2[i].doc);
				String articleDate = d.get("pubDateString");
				if ((articleDate.compareTo(startDate) >= 0 && articleDate.compareTo(endDate) <= 0)) {
					// add to set if date is within range
					resultsSet.add(d.get("articleTitle"));
				}
			}
			
			for (int i=0; i<hits3.length; i++) {
				org.apache.lucene.document.Document d = searcher.doc(hits3[i].doc);
				String articleDate = d.get("pubDateString");
				if ((articleDate.compareTo(startDate) >= 0 && articleDate.compareTo(endDate) <= 0)) {
					// add to set if date is within range
					resultsSet.add(d.get("articleTitle"));
				}
			}
			
		} catch (IOException e) {
			engine.getLogger().writeToErrorLog(e.toString());
			e.printStackTrace();
		}
		
		for (String article : resultsSet) {
			// for each unique articlt title, add it to the results string
			returnResultsString += "<li>" + article + "</li>";	
		}
		
		String[] arrayToReturn = {returnResultsString, String.valueOf(resultsSet.size())};
		return arrayToReturn;
	}
	
	// make keyword into a regex string to find word in any case
	private String makeRegexString(String initialString) {
		String transformedString = "";
		
		for(int i=0; i<initialString.length(); i++) {  // match either lowercase or uppercase 			
			transformedString += "[" + initialString.substring(i,i+1).toLowerCase() + "|" + initialString.substring(i,i+1).toUpperCase() + "]";
		}
		
		return transformedString;
		
	};
	
	
	public void closeIndexWriter() throws IOException {
		if (indexWriter.isOpen()) {
			indexWriter.close();
		}
	}
	
} // end LuceneSearcher class


