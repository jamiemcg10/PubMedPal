package edu.bu.metcs622.scandata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

////import edu.bu.metcs622.main.Constants;

/**
 * Given a list of files to combine, unzips them and combines them into a single Document
 *
 */
public class Combiner {
	Document combinedXML = null;
	Engine engine;
	
	/**
	 * Constructor that initializes Combiner with files
	 * @param listOfFiles
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws TransformerConfigurationException
	 * @throws ParseException
	 * @throws SQLException
	 */
	public Combiner(Engine engine, String listOfFiles) throws IOException, ParserConfigurationException, SAXException, TransformerConfigurationException, ParseException, SQLException{
		this.engine = engine;
		
		String[] fileList = listOfFiles.split(",");
		File[] files = new File[fileList.length];
		
		for (int i=0; i<fileList.length; i++) {
			files[i] = new File(System.getProperty("user.dir") + "/data/" +  fileList[i] +  ".xml.gz");
		}
		
		
		if (files.length > 0) {
			combinedXML = combineFiles(files); // create Document of combined xml file		
		}
			
	}

	/**
	 * Unzips files and calls for them to be merged 
	 * @param inputFiles
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws TransformerConfigurationException
	 */
	public Document combineFiles (File[] inputFiles) throws IOException, SAXException, TransformerConfigurationException{
		
		// UNZIPS .GZ FILES AND RETURNS A DOCUMENT OF XML FILES MERGED TOGETHER
		Document combinedFiles = null;
		
		long startUnzipTime = new Date().getTime();
		
		File[] unzippedFiles = new File[inputFiles.length];  // Empty array to hold the unzipped files
		
		byte[] buffer = new byte[1024];
		
		for (int i=0; i<inputFiles.length; i++) { // loop through .gz files
////			unzippedFiles[i] = new File(Constants.DATA_LOCATION + inputFiles[i].getName().replace(".gz",""));
			FileOutputStream individualFile = new FileOutputStream(unzippedFiles[i]);  // create new file for unzipped file
			GZIPInputStream zipFileInput = new GZIPInputStream(new FileInputStream(inputFiles[i]));  // open inputstream for zipped file
			int bytesRead;
			zipFileInput.read();
			individualFile.write(Byte.parseByte("060"));
			while ((bytesRead = zipFileInput.read(buffer)) > 0) {
				// while there are still bytes to be read, write them to the unzipped file
				individualFile.write(buffer,0,bytesRead);
			}
			
			// close resources
			zipFileInput.close();
			individualFile.close();
			
			
		}
		
		long endUnzipTime = new Date().getTime();
		
		long startCombineTime = new Date().getTime();
		try {
			// call method to combine unzipped files into one file
			
			combinedFiles = mergeXMLs(unzippedFiles);
		} catch (ParserConfigurationException e) {
			engine.getLogger().writeToErrorLog(e.toString());
			e.printStackTrace();
		}

		long endCombineTime = new Date().getTime();

		System.out.println("Time to unzip file(s): " + (endUnzipTime - startUnzipTime)/1000 + "s.");
		System.out.println("Time to combine file(s): " + (endCombineTime - startCombineTime)/1000 + "s.");
		System.out.println("Time to unzip and combine file(s): " + (endCombineTime - startUnzipTime)/1000 + "s.");
		
		return combinedFiles;
	}
	
	/**
	 * Merges xml files
	 * @param unzippedFiles
	 * @return
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @throws TransformerConfigurationException
	 */
	public Document mergeXMLs(File[] unzippedFiles) throws ParserConfigurationException, IOException, SAXException, TransformerConfigurationException {
		// COMBINE MULTIPLE XML FILES
		
		// CREATE FACTORY AND BUILDER TO CREATE DOCUMENT FROM  XML FILE
		DocumentBuilderFactory combinationFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder combinationBuilder = combinationFactory.newDocumentBuilder();
		Document combinedDoc = null;
		
		
		try {
			// PARSE XML FILE INTO DOCUMENT
			combinedDoc = combinationBuilder.parse(unzippedFiles[0]);
			combinedDoc.getDocumentElement().normalize();

		} catch (Exception e) {
			engine.getLogger().writeToErrorLog(e.toString());
			e.printStackTrace();
		} 
		
		int unzippedFileInd = 1;
		while (unzippedFileInd < unzippedFiles.length) { // loop through all but first unzipped file in array and add them to the first file
			Document mergeDoc = combinationBuilder.parse(unzippedFiles[unzippedFileInd]);
			NodeList docToMerge = mergeDoc.getElementsByTagName("PubmedArticle");  // get PubmedArticles of XML file being merged in
			for (int i=0; i<docToMerge.getLength(); i++) {  // loop through Pubmed Articles being added and add them to first file 
				Node importedNode = combinedDoc.importNode(docToMerge.item(i), true);
				combinedDoc.getDocumentElement().appendChild(importedNode);
			}
						
			unzippedFileInd++;
		}
		
		
		saveXMLFile(combinedDoc);
		
		return combinedDoc;
		
				
	}
	
	/**
	 * Saves xml files
	 * @param Document combinedDoc - document to save
	 * @return boolean of whether file saved successfully
	 * @throws TransformerConfigurationException 
	 * @throws IOException
	 * @throws TransformerException
	 */
	private boolean saveXMLFile(Document combinedDoc) throws TransformerConfigurationException {
		// SAVE XML FILE
		// Create File and FileWriter for new, combined file
		boolean saveSuccessful = false;
		FileWriter writer;
		try {
			writer = new FileWriter(new File("\\Users\\Jamie\\eclipse-workspace-2020\\FinalProjectSpring\\data\\pubmeddata.xml"));
			StreamResult result = new StreamResult(writer);
		
			// INSTANTIATE TRANSFORMER TO CONVERT DOCUMENT WITH COMBINED XML INTO A NEW XML FILE
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");  // for formatting
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); // for formatting
			
			DOMSource source = new DOMSource(combinedDoc);
			
			try {
				transformer.transform(source, result);
				saveSuccessful = true;
			} catch (TransformerException e) {
				engine.getLogger().writeToErrorLog(e.toString());
				e.printStackTrace();
			}
		
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		

		
		return saveSuccessful;
	}
	
	
	public Document getCombinedXML() {
		return this.combinedXML;
	}

	
}
