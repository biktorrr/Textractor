package textract;

import org.json.simple.JSONArray;

public class TokenMatch{
	 public String token;
	 public int frequency;
	 public JSONArray gtaaMatches; //TODO: make arraylist
	 
	 
	 public TokenMatch(){

	 }
	 
	 public TokenMatch(String t, int f, String g){
		 token = t;
		 frequency = f;
	 }
	 
	 public String toString(){
		 return token + " " + Integer.toString(frequency) + " --> " + gtaaMatches.toString();
		 
	 }
}