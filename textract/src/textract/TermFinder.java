package textract;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

public class TermFinder {


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
	
	// remove from JSONArray results everything with a es-score below some threshold
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
	
	// find hits in Person Name-axis
	public JSONArray  matchPersonNames (String inputString, ElasticGTAASearcher gtaaES, double minScore){
		JSONArray hitshits = new JSONArray();
		String esJSONString = gtaaES.searchForStringInCS(inputString, "Persoonsnamen");
		
		try {
			JSONObject esJsonObject;
			esJsonObject = (JSONObject) JSONValue.parseWithException(esJSONString);
			JSONObject hits = (JSONObject) esJsonObject.get("hits");
			hitshits = (JSONArray) hits.get("hits");
			
		} catch (ParseException  e) {
			e.printStackTrace();
		}
		return removeLowScores(hitshits, minScore);
	}	
	
	// find hits in Geo-axis
	public JSONArray  matchGeo (String inputString, ElasticGTAASearcher gtaaES, double minScore){
		JSONArray hitshits = new JSONArray();
		String esJSONString = gtaaES.searchForStringInCS(inputString, "GeografischeNamen");
		
		try {
			JSONObject esJsonObject;
			esJsonObject = (JSONObject) JSONValue.parseWithException(esJSONString);
			JSONObject hits = (JSONObject) esJsonObject.get("hits");
			hitshits = (JSONArray) hits.get("hits");
			
		} catch (ParseException  e) {
			e.printStackTrace();
		}
		return removeLowScores(hitshits, minScore);
	}
	
	
	
	
	public static void main(String[] args) throws ParseException {
		TermFinder tf = new TermFinder();
		ElasticGTAASearcher gtaaES = new ElasticGTAASearcher();
		System.out.println(tf.findTermWithThreshold(gtaaES, "personen", 3.0));
	}
		
}


	

	