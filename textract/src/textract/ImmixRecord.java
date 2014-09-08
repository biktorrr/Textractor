package textract;

import java.util.ArrayList;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.simple.JSONObject;

// internal datastructure for one immixrecord and the resulting extracted terms
public class ImmixRecord {
	
	private ArrayList<String> manTerms; // the manual terms (if any)
	private String ttString; // all teletekst content
	private ArrayList<TokenMatch> tokenMatches; // tokens and matching gtaa terms TODO: replace by thing below
	
	private ArrayList<ESDoc> extractedGTAATerms ; // list of final matches based on everything

	private ArrayList<ESDoc> extractedGTAATopics ; // list of final matches based on everything Topics
	private ArrayList<ESDoc> extractedGTAAPersons ; // list of final matches based on everything Persons
	private ArrayList<ESDoc> extractedGTAALocations ; // list of final matches based on everything Locations

	
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
	
	
	
	public void consolidateGTAATerms(){
		consolidateTopics();
		consolidateNE();
		
		ArrayList<ESDoc> temp = new ArrayList<ESDoc>();
		temp.addAll(extractedGTAATopics);
		temp.addAll(extractedGTAAPersons);
		temp.addAll(extractedGTAALocations);
		setExtractedGTAATerms(temp);
		
	}
	
	public void consolidateTopics(){
		ArrayList<ESDoc> termsBasedOnKeywords = new ArrayList<ESDoc>();
		for (int i=0;i<tokenMatches.size();i++){
			TokenMatch tm = tokenMatches.get(i);
			for(int j=0;j<tm.gtaaMatches.size();j++){
				ESDoc ed = new ESDoc();
				
				ed.preflabel = (String) ((JSONObject) ((JSONObject) tm.gtaaMatches.get(j)).get("_source")).get("preflabel");
				ed.altlabel = (String) ((JSONObject) ((JSONObject) tm.gtaaMatches.get(j)).get("_source")).get("altlabel");
				ed.uri = (String) ((JSONObject) ((JSONObject) tm.gtaaMatches.get(j)).get("_source")).get("uri");
				ed.conceptSchemes = (String) ((JSONObject) ((JSONObject) tm.gtaaMatches.get(j)).get("_source")).get("conceptSchemes");
				
			
				termsBasedOnKeywords.add(ed); //TODO: remove doubles?
				
			}
		}
		setExtractedGTAATopics(termsBasedOnKeywords);
	}
	
	public void consolidateNE(){
		ArrayList<ESDoc> persons = new ArrayList<ESDoc>();
		ArrayList<ESDoc> locations = new ArrayList<ESDoc>();

		if (NEList.size()>0){
			for (int i=0;i<NEList.size();i++){
				NamedEntity tm = NEList.get(i);
				for(int j=0;j<tm.gtaaMatches.size();j++){
					ESDoc ed = new ESDoc();
					
					ed.preflabel = (String) ((JSONObject) ((JSONObject) tm.gtaaMatches.get(j)).get("_source")).get("preflabel");
					ed.altlabel = (String) ((JSONObject) ((JSONObject) tm.gtaaMatches.get(j)).get("_source")).get("altlabel");
					ed.uri = (String) ((JSONObject) ((JSONObject) tm.gtaaMatches.get(j)).get("_source")).get("uri");
					ed.conceptSchemes = (String) ((JSONObject) ((JSONObject) tm.gtaaMatches.get(j)).get("_source")).get("conceptSchemes");
					
					if(tm.neClass.contains("PERSON")){
						if (persons.contains(ed)){
							// do nothing TODO: do something with the frequencies?
						}
						else
							persons.add(ed);
					}
					else if(tm.neClass.contains("LOCATION")){
						if (locations.contains(ed)){
							// do nothing TODO: do something with the frequencies?
						}
						else 
							locations.add(ed);
					}
				}
			}
		}
		setExtractedGTAAPersons(persons);
		setExtractedGTAALocations(locations);
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
			result += "\n Found terms";
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
		
	
	public Element toXML(){
		Element record = DocumentHelper.createElement("record");
		
		record.addAttribute("identifier",identifier);
		Element terms = record.addElement("terms");
		
		record.addAttribute("ttlength",Integer.toString(ttString.length()));
		
		for(int j = 0;j<extractedGTAATopics.size();j++){	
			terms.addElement("term")
				.addAttribute("axis", "topic")
				.addAttribute("uri", extractedGTAATopics.get(j).uri)
				.addText(extractedGTAATopics.get(j).preflabel);
		}
		
		for(int j = 0;j<extractedGTAAPersons.size();j++){	
			terms.addElement("term")
			.addAttribute("axis", "person")
			.addAttribute("uri", extractedGTAAPersons.get(j).uri)
			.addText(extractedGTAAPersons.get(j).preflabel);	
		}
		
		for(int j = 0;j<extractedGTAALocations.size();j++){	
			terms.addElement("term")
			.addAttribute("axis", "locations")
			.addAttribute("uri", extractedGTAALocations.get(j).uri)
			.addText(extractedGTAALocations.get(j).preflabel);	
		}
		
		return record;
		
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

	public String getTtString() {
		return ttString;
	}


	public void setTtString(String ttString) {
		this.ttString = ttString;
	}


	public ArrayList<ESDoc> getExtractedGTAATerms() {
		return extractedGTAATerms;
	}


	public void setExtractedGTAATerms(ArrayList<ESDoc> extractedGTAATerms) {
		this.extractedGTAATerms = extractedGTAATerms;
	}


	public ArrayList<ESDoc> getExtractedGTAATopics() {
		return extractedGTAATopics;
	}


	public void setExtractedGTAATopics(ArrayList<ESDoc> extractedGTAATopics) {
		this.extractedGTAATopics = extractedGTAATopics;
	}


	public ArrayList<ESDoc> getExtractedGTAAPersons() {
		return extractedGTAAPersons;
	}


	public void setExtractedGTAAPersons(ArrayList<ESDoc> extractedGTAAPersons) {
		this.extractedGTAAPersons = extractedGTAAPersons;
	}


	public ArrayList<ESDoc> getExtractedGTAALocations() {
		return extractedGTAALocations;
	}


	public void setExtractedGTAALocations(ArrayList<ESDoc> extractedGTAALocations) {
		this.extractedGTAALocations = extractedGTAALocations;
	}


	public int getMinMatches() {
		return minMatches;
	}


	public void setMinMatches(int minMatches) {
		this.minMatches = minMatches;
}
}