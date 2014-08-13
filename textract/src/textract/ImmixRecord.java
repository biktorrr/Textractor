package textract;

import java.util.ArrayList;

// internal datastructure for one immixrecord and the resulting extracted terms
public class ImmixRecord {
	
	private ArrayList<String> manTerms; // the manual terms (if any)
	private String ttString; // all teletekst content
	private ArrayList<TokenMatch> tokenMatches; // tokens and matching gtaa terms TODO: replace by thing below
	 public ArrayList<ESDoc> allTokenMatches ; // list of final matches based on tokens
	 public ArrayList<ESDoc> allPNMatches ; // list of final matches based on Named Entities

	
	
	
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
		
	}
	
	// constructor
	public ImmixRecord(ArrayList<String> mt, ArrayList<TokenMatch> tm, String id) {
		manTerms = mt;
		tokenMatches = tm;
		identifier = id;
		ttString = "tbd"; //default
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
	
	// print only if there both manual terms and some teletekst content
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
