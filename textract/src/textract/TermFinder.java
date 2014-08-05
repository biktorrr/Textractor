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

import org.apache.lucene.index.SegmentInfos.FindSegmentsFile;
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
	private String searchType = "searchForStringInCSOnderwerpen"; 	// parameter: set the type of ES searcher (one of: searchForString, searchForPrefLabel, 
													// searchForStringFuzzy, searchForStringInCSOnderwerpen etc

	public TermFinder(){

		
		}
	


	public JSONArray findTerm(ElasticGTAASearcher es, String searchString)  {
		JSONArray hitshits = new JSONArray();
		String esJSONString = "";
		if(searchType=="searchForString"){ esJSONString = es.searchForString(searchString);}
		else if(searchType=="searchForPrefLabel"){ esJSONString = es.searchForPrefLabel(searchString);}
		else if(searchType=="searchForStringFuzzy"){ esJSONString = es.searchForStringFuzzy(searchString);}
		else if(searchType=="searchForStringInCSOnderwerpen"){ esJSONString = es.searchForStringInCS(searchString, "Onderwerpen");}
		else { esJSONString = es.searchForString(searchString);} //default = search for string
		
			try {
				JSONObject esJsonObject;
				esJsonObject = (JSONObject) JSONValue.parseWithException(esJSONString);
				JSONObject hits = (JSONObject) esJsonObject.get("hits");
				hitshits = (JSONArray) hits.get("hits");
				
			} catch (ParseException  e) {
				e.printStackTrace();
			}

		return hitshits;
	}
	
	// remove from results everything below threshold
	public JSONArray removeLowScores(JSONArray ja, double minScore){
		
		JSONArray result = new JSONArray();
		for(int i = 0; i< ja.size();i++ ){
			JSONObject jo = (JSONObject) ja.get(i);
			double score = (double) jo.get("_score");
			if (score >= minScore) {
				result.add(jo);
			}
		}
		return result;
	}
	
	// remove from results everything below threshold
	public JSONArray findTermWithThreshold(ElasticGTAASearcher es, String searchString, double minScore) {
		JSONArray result =  findTerm(es, searchString);
		return removeLowScores(result, minScore);		
	}
	
	
	public static void main(String[] args) throws ParseException {
		TermFinder tf = new TermFinder();
		ElasticGTAASearcher gtaaES = new ElasticGTAASearcher();
		System.out.println(tf.findTermWithThreshold(gtaaES, "personen", 3.0));
	}
		
}


	

	