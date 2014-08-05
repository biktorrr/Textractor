package textract;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class TokenMatch{
	 public String token; 			//the token itself
	 public int frequency;			//the frequency in the subtitles
	 public float normfrequency ; 	//the frequency in Dutch language
	 public JSONArray gtaaMatches;  //the matched gtaa entries
	 
	 
	 public TokenMatch(){

	 }
	 
	 public TokenMatch(String t, int f, String g){
		 token = t;
		 frequency = f;
	 }
	 
	 // print the whole Jsonarray
	 public String toStringFull(){
		 return token + " " + Integer.toString(frequency) + " matches: " + gtaaMatches.toString();
		 
	 }
	 
	 // only preflabels matches
	 public String toStringPL(){
		 String returnString =  token + " " + Integer.toString(frequency) + " " + Float.toString(normfrequency);
		 for (int i=0;i<gtaaMatches.size();i++){
			 
			 if (i>0){returnString += ", ";}
			 else {returnString += " matches: ";}
			 String pl =  (String) ((JSONObject) ((JSONObject) gtaaMatches.get(i)).get("_source")).get("preflabel");
			 returnString += pl;
		 }			 
		 return  returnString;		 
	 }
	 
	 public String toString(){
		 return toStringPL();
	 }
}