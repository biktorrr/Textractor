package textract;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class TermFinder {


	private String index;

	public TermFinder(){

		index = "gtaa";
		}
	
/*
	public String getJsonStringFromURL(String urlString) throws IOException{
		URL url = new URL(urlString);
		URLConnection con = url.openConnection();
		InputStream in = con.getInputStream();
		String result  = new Scanner(in,"UTF-8").useDelimiter("\\A").next();
		return result;
	}
	
	public String getJsonStringFromPOST(String urlString, String postString) throws IOException{
		URL url;
	    HttpURLConnection connection = null;  
	    try {
	      //Create connection
	      url = new URL(urlString);
	      connection = (HttpURLConnection)url.openConnection();
	      connection.setRequestMethod("POST");
	      connection.setRequestProperty("Content-Type", 
	           "application/x-www-form-urlencoded");
				
	      connection.setRequestProperty("Content-Length", "" + 
	               Integer.toString(postString.getBytes().length));
	      connection.setRequestProperty("Content-Language", "en-US");  
				
	      connection.setUseCaches (false);
	      connection.setDoInput(true);
	      connection.setDoOutput(true);

	      //Send request
	      DataOutputStream wr = new DataOutputStream (
	                  connection.getOutputStream ());
	      wr.writeBytes (postString);
	      wr.flush ();
	      wr.close ();

	      //Get Response	
	      InputStream is = connection.getInputStream();
	      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	      String line;
	      StringBuffer response = new StringBuffer(); 
	      while((line = rd.readLine()) != null) {
	        response.append(line);
	        response.append('\r');
	      }
	      rd.close();
	      return response.toString();

	    } catch (Exception e) {

	      e.printStackTrace();
	      return null;

	    } finally {

	      if(connection != null) {
	        connection.disconnect(); 
	      }
	    }
	}
	*/

	public JSONArray findTerm(Client client, String searchString)  {
		JSONArray hitshits = new JSONArray();
		
		SearchResponse response = client.prepareSearch(index)
			      //  .setTypes("type1", "type2")
			       // .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			        .setQuery(QueryBuilders.termQuery("preflabel", searchString))             // Query
			        .execute()
			        .actionGet();
		

			try {
				String esJSONString =response.toString();	
				JSONObject esJsonObject;
				esJsonObject = (JSONObject) JSONValue.parseWithException(esJSONString);
				JSONObject hits = (JSONObject) esJsonObject.get("hits");
				hitshits = (JSONArray) hits.get("hits");
				
			} catch (ParseException  e) {
				e.printStackTrace();
			}

			return hitshits;

	}
	/*
	public JSONArray findTerm(Client client, String searchString) {
		JSONArray hitshits = new JSONArray();
		String urlString = baseurl + basequery + searchString;
		try {
			String esJSONString = getJsonStringFromURL(urlString);	
			JSONObject esJsonObject = (JSONObject) JSONValue.parseWithException(esJSONString);
			JSONObject hits = (JSONObject) esJsonObject.get("hits");
			 hitshits = (JSONArray) hits.get("hits");
			return hitshits;
			//returnString = hitshits.toString();
		} catch (ParseException | IOException e) {
			//JSONArray hitshits = new JSONArray();
			//returnString = "error";
			//e.printStackTrace();
	}
		return hitshits;


	}*/
	public static void main(String[] args) throws ParseException {
		JSONObject j = new JSONObject(); 
		j.put("query", "{"); 
		j.put("age", "20"); 

	}
		
}


	

	