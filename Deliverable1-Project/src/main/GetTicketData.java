package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
// used for log in console
import java.util.logging.Level;
import java.util.logging.Logger;

// add in build path java-json.jar
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;



public class GetTicketData {

	
	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		
		return sb.toString();	   
	}
	
	public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
		
		try (
			InputStream is = new URL(url).openStream();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		) {
	        String jsonText = readAll(rd);
	        return new JSONArray(jsonText);
		}
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		
		try (
			InputStream is = new URL(url).openStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		) {
	        String jsonText = readAll(rd);
	        return new JSONObject(jsonText);
		}
	}
	
	public static void main(String[] args) throws IOException, JSONException {
		
		GenerateTicketCsv csv = new GenerateTicketCsv();
        Logger logger = Logger.getLogger(GetTicketData.class.getName());   	        
        String log;
        
    	/* Explanation of the parameter in the query:
    	 * 
    	 * status = resolved
		 * 		A resolution has been taken, and it is awaiting verification by reporter. From here, issues are either reopened, or are closed.
		 * status = closed
		 *		The issue is considered finished. The resolution is correct. Issues which are closed can be reopened.
    	 *
    	 * resolution = fixed
    	 * 		A fix for this issue is checked into the tree and tested.
    	 */
        
		String project = "FALCON";
		String issueType = "(standardIssueTypes(), subTaskIssueTypes())";
		String status = "(Resolved, Closed)";
		String resolution = "fixed";
		
		String jql = ""
				+ "project=" + project + " AND "
				+ "issuetype in " + issueType + "  AND "
				+ "status in " + status + " AND "
				+ "resolution=" + resolution; 

		// replace the white spaces with %20 to fit the format of an url
		jql = jql.replace(" ", "%20");

		
	    /* 
	     * Using JSON API, get the tickets of the project	
	     * For each ticket get the date
	     * 
	     */
		
		Integer from = 0; 
		Integer to = 0; 
		Integer total = 1;
		Integer ticketPerIteration = 1000;
		
		// For each iteration, ask for ticketPerIteration tickets
	    do {
	    	
	        to = from + ticketPerIteration;
	        
	        String url = "https://issues.apache.org/jira/rest/api/2/search?"
	        		+ "jql=" + jql + "&"
	                + "fields=key,resolutiondate,versions,created&"
	                + "startAt=" + from.toString() + "&"
	                + "maxResults=" + to.toString();
	        	        
	        log = "\n\t" + String.format("Query at: %s", url) + "\n\t";
	        log = String.format("%sGet the tickets from %d to %d",log, from, to);
	        logger.log(Level.INFO, log);

	        
	        JSONObject json = readJsonFromUrl(url);
	        
	        // get the total number of issues. "total" can be > of "to"
	        total = json.getInt("total");
	        
	        log = "\n\tQuery succedded\n\t";
	        log = String.format("%sNumber of total issues: %d", log, total);
	        logger.log(Level.INFO, log);
	        
	        // get the array of issues
	        JSONArray issues = json.getJSONArray("issues");

        	// Iterate through each issue
	        for(; from < total && from < to; from++) {
	        	
	        	// get the single issue
	        	JSONObject issue = issues.getJSONObject(from % ticketPerIteration);
	        		        	
	        	// get the issue's fields
	        	JSONObject issueField = issue.getJSONObject("fields");
	        	
	        	// Put the information in a data structure to parse them
	        	csv.addDate(issueField.get("resolutiondate").toString());
	        }  
	        
	    } while(from < total);
	    
	    csv.generateCsv(); 
        logger.log(Level.INFO, "File created");
	}

}