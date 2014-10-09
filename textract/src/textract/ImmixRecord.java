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
	private ArrayList<ESDoc> extractedGTAANames ; // list of final matches based on everything Locations

	
	private String identifier; 		// OAI identifier of the record
	private String docID ;			// Identifier of the video
	
	private ArrayList<NamedEntity> NEList ; // list of Named Entity objects
	private int minMatches = 1; // parameter: minimum no matches to be found to be printed

	private int minFreq = 2; // minimum nr of times a concept appears in the record
	
	private boolean complete = true; // print all information in xml?
	
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
	
	
	//for all terms (not in use?)
	public void consolidateGTAATerms(){
		consolidateTopics();
		consolidateNE();
		
		
		ArrayList<ESDoc> temp = new ArrayList<ESDoc>();
		temp.addAll(extractedGTAATopics);
		temp.addAll(extractedGTAAPersons);
		temp.addAll(extractedGTAALocations);
		temp.addAll(extractedGTAANames);

		
		ArrayList<ESDoc> results = removeInfrequentTerms(temp);
		setExtractedGTAATerms(results);
				
	}
	
	private ArrayList<ESDoc> removeInfrequentTerms(ArrayList<ESDoc> temp) {
		ArrayList<ESDoc> results = new ArrayList<ESDoc>();
		for(ESDoc ed : temp){
			if(ed.freq>=minFreq){	// remove infrequent things
				results.add(ed);
			}		
		}
		return results;
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
				ed.conceptSchemes = (String) ((JSONObject) ((JSONObject) tm.gtaaMatches.get(j)).get("_source")).get("conceptScheme");
				ed.freq = tm.frequency;
			
				termsBasedOnKeywords.add(ed); 
				
			}
		}
		setExtractedGTAATopics(termsBasedOnKeywords);
	}
	
	public void consolidateNE(){
		ArrayList<ESDoc> persons = new ArrayList<ESDoc>();
		ArrayList<ESDoc> locations = new ArrayList<ESDoc>();
		ArrayList<ESDoc> onderwerpen = new ArrayList<ESDoc>() ; //getExtractedGTAATopics(); //add to already extracted stuff
		ArrayList<ESDoc> names = new ArrayList<ESDoc>();
		
		if (NEList.size()>0){
			for (int i=0;i<NEList.size();i++){
				NamedEntity tm = NEList.get(i);
				for(int j=0;j<tm.gtaaMatches.size();j++){
					ESDoc ed = new ESDoc();
					
					ed.preflabel = (String) ((JSONObject) ((JSONObject) tm.gtaaMatches.get(j)).get("_source")).get("preflabel");
					ed.altlabel = (String) ((JSONObject) ((JSONObject) tm.gtaaMatches.get(j)).get("_source")).get("altlabel");
					ed.uri = (String) ((JSONObject) ((JSONObject) tm.gtaaMatches.get(j)).get("_source")).get("uri");
					ed.conceptSchemes = (String) ((JSONObject) ((JSONObject) tm.gtaaMatches.get(j)).get("_source")).get("conceptScheme");
					
					
					if(ed.conceptSchemes.contains("Persoonsnamen")){
						boolean found = false;
						for (ESDoc alreadyin : persons){
							if(alreadyin.equals(ed)){
								alreadyin.freq +=1;
								found = true;
							}
						}
						if (found==false){
							ed.freq = 1;
							persons.add(ed);
						}
					}
					
					else if(ed.conceptSchemes.contains("Onderwerpen")){
						boolean found = false;
						for (ESDoc alreadyin : onderwerpen){
							if(alreadyin.equals(ed)){
								alreadyin.freq +=1;
								found = true;
							}
						}
						if (found==false){
							ed.freq = 1;
							onderwerpen.add(ed);
						}
					}
					
					else if(ed.conceptSchemes.contains("GeografischeNamen")){
						boolean found = false;
						for (ESDoc alreadyin : locations){
							if(alreadyin.equals(ed)){
								alreadyin.freq +=1;
								found = true;
							}
						}
						if (found==false){
							ed.freq = 1;
							locations.add(ed);
						}
					}
					
					else if(ed.conceptSchemes.contains("Namen")){
						boolean found = false;
						for (ESDoc alreadyin : names){
							if(alreadyin.equals(ed)){
								alreadyin.freq +=1;
								found = true;
							}
						}
						if (found==false){
							ed.freq = 1;
							names.add(ed);
						}
					}
				}
			}
		}
		setExtractedGTAANames(removeInfrequentTerms(names));
		setExtractedGTAAPersons(removeInfrequentTerms(persons));
		setExtractedGTAALocations(removeInfrequentTerms(locations));
		addExtractedGTAATopics(onderwerpen);
		}
	


	// add topics from both sides
	private void addExtractedGTAATopics(ArrayList<ESDoc> onderwerpen) {
		ArrayList<ESDoc> templist = new ArrayList<ESDoc>();
		templist.addAll(onderwerpen);
		templist.addAll(this.extractedGTAATopics);
		this.extractedGTAATopics = removeInfrequentTerms(templist);
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
		
		public String toCSV(){
			String returnString  ="" ;
			
						
			for(int j = 0;j<extractedGTAATopics.size();j++){	
				returnString += identifier+ ";";
				returnString += docID+ ";";
				returnString += "Onderwerpen" + ";";  
				returnString += extractedGTAATopics.get(j).uri.replace("http:__","http://").replace("_gtaa_", "/gtaa/") + ";";  
				returnString += extractedGTAATopics.get(j).preflabel + ";";
				returnString += Integer.toString(extractedGTAATopics.get(j).freq) + "\n";

			}
			
			for(int j = 0;j<extractedGTAAPersons.size();j++){	
				returnString += identifier+ ";";
				returnString += docID+ ";";
				returnString += "PersoonsNamen" + ";";  
				returnString += extractedGTAAPersons.get(j).uri.replace("http:__","http://").replace("_gtaa_", "/gtaa/") + ";";  
				returnString += extractedGTAAPersons.get(j).preflabel + ";";
				returnString += Integer.toString(extractedGTAAPersons.get(j).freq) + "\n";
			}
			
			for(int j = 0;j<extractedGTAALocations.size();j++){	
				returnString += identifier+ ";";
				returnString += docID+ ";";
				returnString += "GeografischeNamen" + ";";  
				returnString += extractedGTAALocations.get(j).uri.replace("http:__","http://").replace("_gtaa_", "/gtaa/") + ";";  
				returnString += extractedGTAALocations.get(j).preflabel + ";";
				returnString += Integer.toString(extractedGTAALocations.get(j).freq) + "\n";
			}
			
			for(int j = 0;j<extractedGTAANames.size();j++){	
				returnString += identifier+ ";";
				returnString += docID+ ";";
				returnString += "Namen" + ";";  
				returnString += extractedGTAANames.get(j).uri.replace("http:__","http://").replace("_gtaa_", "/gtaa/") + ";";  
				returnString += extractedGTAANames.get(j).preflabel + ";";
				returnString += Integer.toString(extractedGTAANames.get(j).freq) + "\n";
			}
			return returnString;
			
		}	
		
	public Element toXML(){
		Element record = DocumentHelper.createElement("record");
		
		record.addAttribute("identifier",identifier);
		record.addAttribute("docid",docID);
		Element terms = record.addElement("terms");
		
		record.addAttribute("ttlength",Integer.toString(ttString.length()));
		
		
		for(int j = 0;j<extractedGTAATopics.size();j++){	
			terms.addElement("term")
				.addAttribute("axis", "Onderwerpen")
				.addAttribute("uri", extractedGTAATopics.get(j).uri.replace("http:__","http://").replace("_gtaa_", "/gtaa/"))
				.addAttribute("score", Integer.toString(extractedGTAATopics.get(j).freq))
				.addText(extractedGTAATopics.get(j).preflabel);
		}
		
		for(int j = 0;j<extractedGTAAPersons.size();j++){	
			terms.addElement("term")
			.addAttribute("axis", "PersoonsNamen")
			.addAttribute("uri", extractedGTAAPersons.get(j).uri.replace("http:__","http://").replace("_gtaa_", "/gtaa/"))
			.addAttribute("score", Integer.toString(extractedGTAAPersons.get(j).freq))
			.addText(extractedGTAAPersons.get(j).preflabel);	
		}
		
		for(int j = 0;j<extractedGTAALocations.size();j++){	
			terms.addElement("term")
			.addAttribute("axis", "GeografischeNamen")
			.addAttribute("uri", extractedGTAALocations.get(j).uri.replace("http:__","http://").replace("_gtaa_", "/gtaa/"))
			.addAttribute("score", Integer.toString(extractedGTAALocations.get(j).freq))
			.addText(extractedGTAALocations.get(j).preflabel);	
		}
		
		for(int j = 0;j<extractedGTAANames.size();j++){	
			terms.addElement("term")
			.addAttribute("axis", "Namen")
			.addAttribute("uri", extractedGTAANames.get(j).uri.replace("http:__","http://").replace("_gtaa_", "/gtaa/"))
			.addAttribute("score", Integer.toString(extractedGTAANames.get(j).freq))
		.addText(extractedGTAANames.get(j).preflabel);	
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



	private void setExtractedGTAANames(ArrayList<ESDoc> names) {
		this.extractedGTAANames = names;
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


	public String getDocID() {
		return docID;
	}


	public void setDocID(String docID) {
		this.docID = docID;
	}


	public int getMinFreq() {
		return minFreq;
	}


	public void setMinFreq(int minFreq) {
		this.minFreq = minFreq;
	}
}