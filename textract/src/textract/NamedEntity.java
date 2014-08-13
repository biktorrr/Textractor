package textract;

import org.json.simple.JSONArray;

// class for Named Entity objects
public class NamedEntity {
	public String neString ;
	public String neClass ; 
	public int frequencyInDoc ;  // not used
	public JSONArray gtaaMatches ;  //  the matching concepts in GTAA (come from elasticsearch matcher)
	
	
	public String toString(){
		return "[ "+ neString + " | "+ neClass + " | " + gtaaMatches.toString() + "]";
		
	}
}
