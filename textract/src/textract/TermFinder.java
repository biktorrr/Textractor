package textract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import sun.misc.IOUtils;

public class TermFinder {

	private String baseurl;
	private String basequery;
	
	public TermFinder(){
		baseurl = "http://localhost:9200/";
		basequery = "gtaa/_search?q=preflabel:";
		}
	

	public String getJsonStringFromURL(String urlString) throws IOException{
		URL url = new URL(urlString);
		URLConnection con = url.openConnection();
		InputStream in = con.getInputStream();
		String result  = new Scanner(in,"UTF-8").useDelimiter("\\A").next();
		return result;
	}
	
	public JSONArray findTerm(String searchString) {
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


	}
	
	
		
}


	

	