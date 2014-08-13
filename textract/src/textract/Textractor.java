package textract;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

import se.kb.oai.OAIException;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;
import textract.WordFrequencyCounter.Word;


public class Textractor {

	private static String outputFileName = "textractor_test_"; // where to write the results to
	private static String bg_oai_server = "http://integrator.beeldengeluid.nl/search_service_rs/oai/";

	
	private  ArrayList<String> stopwords; // list of stopwords
	private static String from  = "2014-02-11T18:00:00Z";
	private static String until  = "2014-02-13T21:00:00Z";
	private static int minFreq = 2; // minimum frequency for a token to be added to the tokenlist
	private static double minScore= 3.5; // minimum score for a GTAA match 

	private TermFrequency termfreqFinder; // the object used to determine normalized frequencies


	

	
	public Textractor(){
		stopwords = new StopWords().getStopwords();
	}

	
	// remove stopwords from frequency Words (return as ArrayList)
	// also remove infrequent words  (< minScore)
	public ArrayList<Word> removeUnwantedWords (Word[] frequencyTokens){
		ArrayList<Word> freqAl = new ArrayList<Word>();
        for(Word w:frequencyTokens){
            if (this.stopwords.contains(w.word) || w.count<minFreq ){
            }
            else {
            	freqAl.add(w);
            }
        }
        return freqAl;
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
	        tm.normfrequency = (float) tm.frequency / (float) wf; 
	        termList.add(tm);
	        if (i%10 == 0){System.out.print(".");}
	     }
		if (termList.size() < 1) System.out.println(" no terms found");
		else System.out.println(" done");
		return termList;
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
	
	// get the actual wordsequences for a record. question is, do we do the terms for the video as a whole or not?
	// for now, this returns the entire string
	// TODO: make sure we only include TT, not SH or other
	// TODO: this could probably be done within the listrecords request. So this is not particularly efficient...
	public  String getMetadataForOAIRecord(String recordID){
		String metadataString = "";
		OaiPmhServer server = new OaiPmhServer(bg_oai_server);
		Record record;
		try {
			record = server.getRecord(recordID, "iMMix");
			Element metadata = record.getMetadata();
			List list = metadata.selectNodes("//iMMix:sentences" );
			Iterator iter=list.iterator();			
			// Iterate through the word sequences
	        while(iter.hasNext()){
	            Element elt =(Element) iter.next();
	            Element wordseq =  elt.element("speech").element("wordsequence");
	            metadataString += wordseq.getText();
	        }		
		}
		catch (OAIException e) {e.printStackTrace();}	
		return metadataString ;
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
	
	
	
	public static void main(String[] args) throws ParseException {
		try {
			PrintWriter writer;
			writer = new PrintWriter(outputFileName + "from_" + from.replaceAll(":","p") + "_until_" + until.replaceAll(":","p") + ".txt", "UTF-8");			
			Textractor gogo = new Textractor();
			
			gogo.stopwords = new StopWords().getStopwords();
			gogo.termfreqFinder = new TermFrequency();
			
			ArrayList<ImmixRecord> endResult = new ArrayList<ImmixRecord>();
			
			ElasticGTAASearcher gtaaES = new ElasticGTAASearcher();
			
			// get records from OAI
			try {
				System.out.print("Retrieving records from OAI server.");
				
				OAIHarvester myHarvester = new OAIHarvester();
				List<Record> recordList =  myHarvester.getOAIItemsForTimePeriod(from, until, null);
				
				
				System.out.println("done found" + recordList.size() + "records");

				
				// Loop through records
				System.out.println("Looping through records");
				for (int i=0; i<recordList.size();i++){

					//create new immixRecord object with this identifier
					String recordid = recordList.get(i).getHeader().getIdentifier();
					
					// only do expressions?
					if (recordid.contains("Expressie")|| recordid.contains("Selectie")){ //TODO: is this ok? 
						ImmixRecord ir = new ImmixRecord(recordid); 	
				
						// get the tt stuff
						System.out.print(Integer.toString(i) + ": " + recordid + " >> ");
						String ttString = gogo.getMetadataForOAIRecord(recordid);
						ir.setTTString(ttString);	
						
						// get the manual terms
						ArrayList<String> manTerms = gogo.getExistingTerms(recordid);
						ir.setManTerms(manTerms);
							
						if ( ttString.length()>0) {		
							// get the tokenmatches
							ArrayList<TokenMatch> resultTM = gogo.getTokenMatches(gtaaES, ir);
							ir.setTokenMatches(resultTM);
							
							//get the ner results
							NERrer nerrer = new NERrer();
							ArrayList<NamedEntity> nes = nerrer.getGTAANES(gtaaES, ttString);
							ir.setNEList(nes);
						}
						else {
							System.out.println(" no tt found");
						}
						ir.consolidateGTAATerms();
						endResult.add(ir);
					}
				}
			} catch (OAIException | IOException | DocumentException e) {
				e.printStackTrace();
			}
			// output to file
 
			for(int j = 0;j<endResult.size();j++){	
					writer.print(endResult.get(j).toString());
			}
			writer.close();
			gtaaES.closeClient();
			System.out.println("I'm done");
			
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	



	}

}
