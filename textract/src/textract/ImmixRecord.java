package textract;

import java.util.ArrayList;

import org.json.simple.JSONObject;

// internal datastructure for one immixrecord and the resulting extracted terms
public class ImmixRecord {
	
	private ArrayList<String> manTerms; // the manual terms (if any)
	private String ttString; // all teletekst content
	private ArrayList<TokenMatch> tokenMatches; // tokens and matching gtaa terms TODO: replace by thing below
	private ArrayList<ESDoc> extractedGTAATerms ; // list of final matches based on everything

	private String identifier; // OAI identifier of the record

	private ArrayList<NamedEntity> NEList ; // list of Named Entity objects
	
	private int minMatches = 1; // parameter: minimum no matches to be found to be printed

	public ImmixRecord() {
		
	}
	
	
	// constructor
	public ImmixRecord(String id) {

		identifier = id;
		ttString = "tbd"; //default
		manTerms = new ArrayList<String>();
		tokenMatches = new ArrayList<TokenMatch>();	
		NEList = new ArrayList<NamedEntity>();
	}
	
	// constructor
	public ImmixRecord(ArrayList<String> mt, ArrayList<TokenMatch> tm, String id) {
		manTerms = mt;
		tokenMatches = tm;
		identifier = id;
		ttString = "tbd"; //default
		NEList = new ArrayList<NamedEntity>();

	}
	
	
	// fetch all hits from all JSONArrays and put them as ESDocs in 
	public void consolidateGTAATerms(){
		ArrayList<ESDoc> termsBasedOnKeywords = new ArrayList<ESDoc>();
		for (int i=0;i<tokenMatches.size();i++){
			TokenMatch tm = tokenMatches.get(i);
			for(int j=0;j<tm.gtaaMatches.size();j++){
				ESDoc ed = new ESDoc();
				
				ed.preflabel = (String) ((JSONObject) ((JSONObject) tm.gtaaMatches.get(j)).get("_source")).get("preflabel");
				ed.altlabel = (String) ((JSONObject) ((JSONObject) tm.gtaaMatches.get(j)).get("_source")).get("altlabel");
				ed.uri = (String) ((JSONObject) ((JSONObject) tm.gtaaMatches.get(j)).get("_source")).get("uri");
				ed.conceptSchemes = (String) ((JSONObject) ((JSONObject) tm.gtaaMatches.get(j)).get("_source")).get("conceptSchemes");
				
				if(termsBasedOnKeywords.contains(ed)){//do nothing (does this work?)
				}
				else{
					termsBasedOnKeywords.add(ed);
				}
			}
		}
		
		ArrayList<ESDoc> termsBasedOnPNEs = new ArrayList<ESDoc>();
		if (NEList.size()>0){
			for (int i=0;i<NEList.size();i++){
				NamedEntity tm = NEList.get(i);
				for(int j=0;j<tm.gtaaMatches.size();j++){
					ESDoc ed = new ESDoc();
					
					ed.preflabel = (String) ((JSONObject) ((JSONObject) tm.gtaaMatches.get(j)).get("_source")).get("preflabel");
					ed.altlabel = (String) ((JSONObject) ((JSONObject) tm.gtaaMatches.get(j)).get("_source")).get("altlabel");
					ed.uri = (String) ((JSONObject) ((JSONObject) tm.gtaaMatches.get(j)).get("_source")).get("uri");
					ed.conceptSchemes = (String) ((JSONObject) ((JSONObject) tm.gtaaMatches.get(j)).get("_source")).get("conceptSchemes");
					
					if(termsBasedOnPNEs.contains(ed)){//do nothing (does this work?)
					}
					else{
						termsBasedOnPNEs.add(ed);
					}
	
				}
			}
		}
		termsBasedOnKeywords.addAll(termsBasedOnPNEs);
		extractedGTAATerms = termsBasedOnKeywords;

		
	}
	
	
	public String toStringAll(){
		String result = ""; 
		
		result += "Record " + identifier;
		
		if (manTerms.size() > 0) { 
			result += "\n Manual terms: ";
			for(int j = 0;j<manTerms.size();j++){
				if (j>0) { result+= ", ";}	
				result+= manTerms.get(j);
				
			}
		}
		else result += "\n No manual terms";
	
		if (tokenMatches.size() > 0) { 
			result += " Found terms";
			for(int j = 0;j<tokenMatches.size();j++){	
				// only include when matches are found (minMatches)
				if(tokenMatches.get(j).gtaaMatches.size() >= minMatches){
					result+= "\n  " + tokenMatches.get(j).toString();
				}
			}
		}
	
		else result += "\n No extracted terms\n";
	
		if (NEList.size() > 0) { 
			result += "\n Named Entities: ";
			for(int j = 0;j<NEList.size();j++){
				if (j>0) { result+= ", ";}	
				result+= NEList.get(j).toString() + " ";
				
			}
		}
		else result += "\n No Named Entities";
		
		result+= "\n\n";
		return result;
	}
	
	
	public String toString(){
		return toStringBothMatchesOnly(); // or another stringreturner
	}

	
	// print only if there both manual terms and some teletekst content
		// print only the manual and the matches
		public String toStringBothMatchesOnly(){
			String result = ""; 
			if(manTerms.size()>0 && ttString.length()>0) {
				
				result += "\n\nRecord " + identifier;
				
				if (manTerms.size() > 0) { 
					result += "\n Manual terms: ";
					for(int j = 0;j<manTerms.size();j++){
						if (j>0) { result+= ", ";}	
						result+= manTerms.get(j);
						
					}
				}
				else result += "\n No manual terms";
			
				if (extractedGTAATerms.size() > 0) { 
					result += "\n Found terms: ";
					for(int j = 0;j<extractedGTAATerms.size();j++){	
						if (j>0) { result+= ", ";}	
						result+= extractedGTAATerms.get(j).toString();
					}
				}
			
				else result += "\n No extracted terms\n";
				
			} 
			return result;
		}
		
	
	
	// print only if there both manual terms and some teletekst content
	// print details
	public String toStringBoth(){
		if(manTerms.size()>0 && ttString.length()>0) {
			String result = ""; 
			
			result += "Record " + identifier;
			
			if (manTerms.size() > 0) { 
				result += "\n Manual terms:";
				for(int j = 0;j<manTerms.size();j++){
					if (j>0) { result+= ", ";}	
					result+= manTerms.get(j);
					
				}
			}
			else result += "\n No manual terms";
		
			if (tokenMatches.size() > 0) { 
				result += " Found terms";
				for(int j = 0;j<tokenMatches.size();j++){	
					// only include when matches are found (minMatches)
					if(tokenMatches.get(j).gtaaMatches.size() >= minMatches){
						result+= "\n  " + tokenMatches.get(j).toString();
					}
				}
			}
		
			else result += "\n No extracted terms\n";
			
			if (NEList.size() > 0) { 
				result += "\n Named Entities: ";
				for(int j = 0;j<NEList.size();j++){
					if (j>0) { result+= ", ";}	
					result+= NEList.get(j).toString() + " ";
					
				}
			}
			else result += "\n No Named Entities";
			
			result+= "\n\n";
			return result;}
		else 
			return "";
	}
	
	
	//getters n setters
	public ArrayList<String> getManTerms() {
		return manTerms;
	}
	public void setManTerms(ArrayList<String> manTerms) {
		this.manTerms = manTerms;
	}
	public ArrayList<TokenMatch> getTokenMatches() {
		return tokenMatches;
	}
	public void setTokenMatches(ArrayList<TokenMatch> tokenMatches) {
		this.tokenMatches = tokenMatches;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getTTString() {
		return ttString;
	}

	public void setTTString(String ttString) {
		this.ttString = ttString;
	}

	public ArrayList<NamedEntity> getNEList() {
		return NEList;
	}

	public void setNEList(ArrayList<NamedEntity> nEList) {
		NEList = nEList;
	}

}
