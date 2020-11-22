package edu.bu.metcs622.main;



import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.bu.metcs622.handleRequests.RequestHandler;
import edu.bu.metcs622.handleRequests.SearchParams;
import edu.bu.metcs622.scandata.Engine;


// RUN USING mvn spring-boot:run ON COMMAND LINE
@SpringBootApplication
@Controller
public class PubMedPal {
	Engine engine = null;
	GsonBuilder builder = new GsonBuilder();  // initialize and create gson builder for forming and parsing json
	Gson gson = builder.create();

	public static void main(String[] args) {
		SpringApplication.run(PubMedPal.class, args);
	}
	
	// server running check route
	@GetMapping("/ping")
	public String hello() {
		return String.format("%s!", "pong");
		
	}
	
	// displays main page
	@GetMapping({"/", "/index.html"})
	public String index() {
		System.out.println("PubMed Pal launched!");
		return "index";
	}
	
	
	/**
	 * Determines whether user request is valid / is for search history
	 * @param userRequest
	 * @return json response to send to bot
	 */
	@PostMapping("/api/parse")
	@ResponseBody
		public String parseMessage(@RequestBody String userRequest) {
			String response = RequestHandler.parseRequest(engine, userRequest);  // parse term and dates from request
			return response;
		}
	
	/**
	 * Directs search using specifed method and parameters and returns result if found
	 * @param userRequest
	 * @return json response with search results for bot
	 */
	@PostMapping("/api/search")
	@ResponseBody
		public String performSearch(@RequestBody String userRequest) {
			SearchParams searchParams = gson.fromJson(userRequest, SearchParams.class);  // pull params generated from parseRequest and method 
			String response = "";
			// perform search
			try {
				response = RequestHandler.getRequestResult(
					engine, searchParams.getMethod(),searchParams.getTerm(),searchParams.getStartDate(), searchParams.getEndDate(), searchParams.getGetCount());
			} catch (Exception e) {
				// most likely here because a file was saved while the program was running
				response = "{\"shortSearchDescription\": \"\", \"results\": \"Sorry, something went wrong. Please refresh the page.\", \"getCount\": \"0\"}";

			}
			//System.out.println(response);
			return response;
		}
	
	/**
	 * Takes files user specified and builds and runs the engine
	 * @param files
	 * @return "response"
	 */
	@PostMapping("/api/initialize")
	@ResponseBody
		public String buildDataStores(@RequestBody String files) {
		System.out.println("FILES SELECTED: " + files);
			engine = new Engine();
			
			try {
				engine.start(files);
			} catch (Exception e) {
				engine.getLogger().writeToErrorLog(e.toString());
				e.printStackTrace();
			} 
			return "response";
	}
	
	/**
	 * Closes connection to SQL database, lucene index, and log files
	 */
	@GetMapping("/api/close")
	@ResponseBody
		public void closeResources() {
			if (engine != null) {
				System.out.println("Closing resources...");
				try {
					if (engine.getMysql() != null) {
						engine.getMysql().closeSQLDatabase();
					}
				} catch (Exception e){
					engine.getLogger().writeToErrorLog(e.toString());
				}
				try {
					if (engine.getLucene() != null) {
						engine.getLucene().closeIndexWriter();
					}
				} catch (Exception e){
					engine.getLogger().writeToErrorLog(e.toString());
				}
				try {
					engine.getLogger().closeSearchLog();
				} catch (Exception e){
					engine.getLogger().writeToErrorLog(e.toString());
				}
				try {
					engine.getLogger().closeErrorLog();
				} catch (Exception e){
					;
				}
				
				System.out.println("Resources closed.");
			}
			
	} // end closeResources()
	
	

}
