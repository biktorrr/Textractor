package textract;

import java.util.ArrayList;

// internal datastructure for one immixrecord and the resulting extracted terms
public class ImmixRecord {
	
	private ArrayList<String> manTerms; // the manual terms (if any)
	private ArrayList<TokenMatch> tokenMatches; // matching tokens 
	private String identifier; // OAI identifier of the record

	private int minMatches = 1; // parameter: minimum no matches to be found to be printed

	public ImmixRecord() {
		
	}
	public ImmixRecord(ArrayList<String> mt, ArrayList<TokenMatch> tm, String id) {
		manTerms = mt;
		tokenMatches = tm;
		identifier = id;
	}
	public String toString(){
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
	
		else result += "\n No extracted terms";
		
		result+= "\n\n";
		return result;
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

}
