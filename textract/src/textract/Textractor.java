package textract;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

import se.kb.oai.OAIException;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;
import textract.WordFrequencyCounter.Word;


public class Textractor {

	private String outputFileName = "textractor_test_"; // where to write the results to
	private String bg_oai_server = "http://integrator.beeldengeluid.nl/search_service_rs/oai/";

	public static boolean debug = false;
	
	private ArrayList<String> stopwords; // list of stopwords
	
	private String from  = "2014-02-01T04:00:00Z";
	private String until  = "2014-02-03T23:59:00Z";
	
	private int minAbsFreq = 2; // minimum frequency for a token to be added to the tokenlist
	private double minNormFreq	= 5.00E-5;	 // minimum normalized frequency for a token to be added to the tokenlist
	private double minScore= 3.3; // minimum score for a GTAA match 

	private TermFrequency termfreqFinder; // the object used to determine normalized frequencies

	private  ArrayList<ImmixRecord> theResults;

	
	public Textractor(){
		stopwords = new StopWords().getStopwords();
	}

	
	// remove stopwords from frequency Words (return as ArrayList)
	// also remove infrequent words  (< minScore)
	public ArrayList<Word> removeUnwantedWords (Word[] frequencyTokens){
		ArrayList<Word> freqAl = new ArrayList<Word>();
        for(Word w:frequencyTokens){
            if (this.stopwords.contains(w.word) || w.count<minAbsFreq ){
            	// do nothing
            }
            else {
            	freqAl.add(w);
            }
        }
        return freqAl;
	}
	
	// remove words with lower than threshold normalized word freq from 
	private ArrayList<TokenMatch> removeCommonWords(ArrayList<TokenMatch> termList, double threshold) {
		ArrayList<TokenMatch> resultList = new ArrayList<TokenMatch>();

        for(int i=0;i<termList.size();i++){
        	
            if (termList.get(i).normfrequency >= threshold){
            	resultList.add(termList.get(i));
            }
        }
        return resultList;
	}
	
	
	
	// generate terms for a frequency list
	public  ArrayList<TokenMatch> getTermsForFreqList(ElasticGTAASearcher es, ArrayList<Word> wordfreq) throws ParseException{
		ArrayList<TokenMatch> termList =  new ArrayList<TokenMatch>();
		
		for(int i = 0; i<wordfreq.size(); i++){
			TermFinder tf = new TermFinder();
			
	        JSONArray foundTerm = tf.findTermWithThreshold(es, wordfreq.get(i).word, minScore);
	        
	        TokenMatch tm = new TokenMatch();
	        tm.frequency= wordfreq.get(i).count;
	        tm.gtaaMatches= foundTerm;
	        tm.token= wordfreq.get(i).word;
	        int wf =  termfreqFinder.getWordFrequency(tm.token);
	        tm.normfrequency =  (double) tm.frequency / (double) wf; 
	        termList.add(tm);
	        if (i%10 == 0){System.out.print(".");}
	     }
		
		ArrayList<TokenMatch> uncommonTerms = removeCommonWords(termList, minNormFreq);
		
		if (uncommonTerms.size() < 1) System.out.println(" no uncommon terms found");
		else System.out.println(" done");
		return uncommonTerms;
	}
	
	



	// this function gets the manually added terms (used to train or evaluate the automated system)
	public  ArrayList<String> getExistingTerms(String recordID){
		ArrayList<String> result = new ArrayList<String>();
		
		OaiPmhServer server = new OaiPmhServer(bg_oai_server);
		Record record;
		try {
			record = server.getRecord(recordID, "iMMix");
			Element metadata = record.getMetadata();
			List list = metadata.selectNodes("//iMMix:trefwoord" );
			Iterator iter=list.iterator();			
			// Iterate through the word sequences
	        while(iter.hasNext()){
	            Element elt =(Element) iter.next();
	            result.add(elt.getText());
	        }		
		}
		catch (OAIException e) {e.printStackTrace();}	
		return result ;
	}
	
	// Build Immix Record based on OAI getRecord 
	// get the actual wordsequences for a record. question is, do we do the terms for the video as a whole or not?
	// for now, this returns the entire string
	// TODO: make sure we only include TT, not SH or other
	public ImmixRecord getMetadataForOAIRecord(String recordID){
		ImmixRecord irnew = new ImmixRecord(recordID);
		
		String metadataString = "";
		OaiPmhServer server = new OaiPmhServer(bg_oai_server);
		Record record;
		try {
			record = server.getRecord(recordID, "iMMix");
			Element metadata = record.getMetadata();
			 //get teletekst
			List list = metadata.selectNodes("//iMMix:sentences" );
			Iterator iter=list.iterator();			
			// Iterate through the word sequences
	        while(iter.hasNext()){
	            Element elt =(Element) iter.next();
	            Element wordseq =  elt.element("speech").element("wordsequence");
	            metadataString += wordseq.getText();
	        }
			irnew.setTTString(metadataString);
			
			 //get doc id
			Element docidElt = (Element) metadata.selectNodes("//iMMix:doc" ).get(0);
			String docID = docidElt.attributeValue("id");
			irnew.setDocID(docID);
		}
		catch (OAIException e) {e.printStackTrace();}	
		
		return irnew;
	}
	
	
	
	// input: ES client and oai identifier string
	// output: list of tokenmatches for one item
	public  ArrayList<TokenMatch> getTokenMatches(ElasticGTAASearcher es, ImmixRecord oneRecord) throws ParseException{
		System.out.println("Retrieving tokens from record " + oneRecord.getIdentifier());
		Word[] frequency = new WordFrequencyCounter().getFrequentTokensFromString(oneRecord.getTTString());
		ArrayList<Word> wordlist = removeUnwantedWords(frequency); // remove stopwords and such
		System.out.print(" Matching to GTAA");
		ArrayList<TokenMatch> result = getTermsForFreqList(es, wordlist);
		return result;
	}
	
	// input: ES client and oai identifier string
	// output: list of NGRAM matches for one item
	public  ArrayList<TokenMatch> getNGramMatches(ElasticGTAASearcher es, ImmixRecord oneRecord, int n) throws ParseException{
		System.out.println("Retrieving ngrams from record " + oneRecord.getIdentifier());
		Word[] frequency = new WordFrequencyCounter().getFrequentNGramsFromString(oneRecord.getTTString(),	n);
		ArrayList<Word> wordlist = removeUnwantedWords(frequency); // remove stopwords and such
		System.out.print(" Matching Ngrams to GTAA");
		ArrayList<TokenMatch> result = getTermsForFreqList(es, wordlist);
		return result;
	}	
	
	// transform a list of immixrecords (a result) to XML document
	public Document resultToXML(ArrayList<ImmixRecord> endResult){
		Document document =  DocumentHelper.createDocument();
		Element root = document.addElement("root");
		root.addElement("from")
			.addText(this.from.replaceAll(":","p"));
		root.addElement("to")
			.addText(this.until.replaceAll(":","p"));
		root.addElement("minNormFreq")
			.addText(Double.toString(this.minNormFreq));
		root.addElement("minScore")
		.	addText(Double.toString(minScore));
		root.addElement("minAbsFreq")
			.addText(Integer.toString(minAbsFreq));
		
		Element results = root.addElement("results");
		for(ImmixRecord e : endResult){
			results.add(e.toXML());
		}
			
		return document;
		
	}
	
	//wriet an XML document
	public void write(Document document, String fileString) throws IOException {
		
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter writer = new XMLWriter(
            new FileWriter(fileString ), format
        );
        writer.write( document );
        writer.close();      
    }
	
	
	// For one record ID, build an ImmixRecord Object and enrich it with all the GTAA terms we can find
	public ImmixRecord retrieveAll(String recordid, ElasticGTAASearcher gtaaES) throws ParseException, IOException, DocumentException{
		
		ImmixRecord ir =  this.getMetadataForOAIRecord(recordid);	// initialize ImmixRecord doc
		
		// get the manual terms
		ArrayList<String> manTerms = this.getExistingTerms(recordid);
		ir.setManTerms(manTerms);
			
		if ( ir.getTTString().length()>0) {		
			// get the tokenmatches
			ArrayList<TokenMatch> resultTM = this.getTokenMatches(gtaaES, ir);
			if (debug){System.out.println("N=1 Matches: " + resultTM);}
			
			
			ArrayList<TokenMatch> resultTM2 = this.getNGramMatches(gtaaES, ir, 2);
			if (resultTM2.size()>0) {
				System.out.println(" Found bigram matches: ");
				//for (int j=0; j<resultTM2.size();j++){System.out.println(" " + resultTM2.get(j).toString());}
			}
			if (debug){System.out.println("N=1 Matches: " + resultTM);}

			ArrayList<TokenMatch> resultTM3 = this.getNGramMatches(gtaaES, ir, 3 );
			if (resultTM3.size()>0) {
				System.out.println(" Found trigram matches: ");
				//for (int j=0; j<resultTM3.size();j++){System.out.println(" " + resultTM3.get(j).toString());}
			}
			resultTM.addAll(resultTM2);
			resultTM.addAll(resultTM3);
	
			ir.setTokenMatches(resultTM);
			
			//get the ner results
			NERrer nerrer = new NERrer();
			ArrayList<NamedEntity> nes = nerrer.getGTAANES(gtaaES, ir.getTTString());
			ir.setNEList(nes);
		}
		else {
			System.out.println(" no tt found");
		}
		
		ir.consolidateGTAATerms();
		return ir;
		
		
	}
	
	// new run, lean and mean
	public ArrayList<ImmixRecord> run() throws  DocumentException, OAIException, ParseException, IOException{
		System.out.println("\n---- Initializing ----\n");
		this.stopwords = new StopWords().getStopwords();
		this.termfreqFinder = new TermFrequency();
		ArrayList<ImmixRecord> endResult = new ArrayList<ImmixRecord>();
		ElasticGTAASearcher gtaaES = new ElasticGTAASearcher();
		
		// get records from OAI
		System.out.println("\n---- Retrieving records from OAI server ----\n");	
		OAIHarvester myHarvester = new OAIHarvester();
		List<Record> recordList =  myHarvester.getOAIItemsForTimePeriod(this.from, this.until, null);
		System.out.println("Found " + recordList.size() + " records.");

		
		// Loop through records
		System.out.println("\n---- Looping through records -----\n");
		for (int i=0; i<recordList.size();i++){

			//create new immixRecord object with this identifier
			String recordid = recordList.get(i).getHeader().getIdentifier();
			System.out.print(Integer.toString(i) + ": " + recordid + " >> ");

			
			// only do expressions?
			if (recordid.contains("Expressie")|| recordid.contains("Selectie")){ //TODO: is this ok? 
				ImmixRecord ir = retrieveAll(recordid, gtaaES); 	
				endResult.add(ir);
			}
			else {
				System.out.println("not expressie or selectie");
			}
		}
		System.out.println("\n---- I'm done ----\n");
		return endResult;
	}
	
	
	
	
	public static void main(String[] args) {
		Textractor gogo = new Textractor();
		
		try {
			gogo.theResults = gogo.run();
			System.out.println("\n---- Writing results ----\n");
			Document doc = gogo.resultToXML(gogo.theResults);
			String filename = "output" + Long.toString(new Date().getTime()) + ".xml";
			gogo.write(doc, filename);		
			System.out.println("\n---- Also done ----\n");
		} catch (ParseException | IOException | DocumentException | OAIException e) {
			e.printStackTrace();
		}
	}


	// getters n setters
	public double getMinNormFreq() {
		return minNormFreq;
	}


	public void setMinNormFreq(double minNormFreq) {
		this.minNormFreq = minNormFreq;
	}
	public ArrayList<String> getStopwords() {
		return stopwords;
	}

	public void setStopwords(ArrayList<String> stopwords) {
		this.stopwords = stopwords;
	}

	public TermFrequency getTermfreqFinder() {
		return termfreqFinder;
	}

	public void setTermfreqFinder(TermFrequency termfreqFinder) {
		this.termfreqFinder = termfreqFinder;
	}


	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getUntil() {
		return until;
	}

	public void setUntil(String until) {
		this.until = until;
	}

	public int getMinAbsFreq() {
		return minAbsFreq;
	}

	public void setMinAbsFreq(int minAbsFreq) {
		this.minAbsFreq = minAbsFreq;
	}

	public double getMinScore() {
		return minScore;
	}

	public void setMinScore(double minScore) {
		this.minScore = minScore;
	}

}
