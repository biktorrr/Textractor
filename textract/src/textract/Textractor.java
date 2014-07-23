package textract;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.dom4j.Element;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

import se.kb.oai.OAIException;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;
import textract.WordFrequencyCounter.Word;


public class Textractor {

	private static String bg_oai_server = "http://integrator.beeldengeluid.nl/search_service_rs/oai/";
	private static ArrayList<String> stopwords;
	
	public Textractor(){
		stopwords = new StopWords().getStopwords();
	}

	
	// remove stopwords from frequency Words (return as ArrayList)
	public static ArrayList<Word> removeStopwords (Word[] frequencyTokens){
		ArrayList<Word> freqAl = new ArrayList<Word>();
        for(Word w:frequencyTokens){
            if (stopwords.contains(w.word)){
            	//System.out.println("STOP " + w.word+"="+w.count);            	
            }
            else {
            	freqAl.add(w);
            	// System.out.println(w.word+"="+w.count);
            }
        }
        return freqAl;
	}
	
	
	
	// generate terms for a frequency list
	public static ArrayList<TokenMatch> getTermsForFreqList(ArrayList<Word> wordfreq) throws ParseException{
		ArrayList<TokenMatch> termList =  new ArrayList<TokenMatch>();
		
		for(int i = 0; i<wordfreq.size(); i++){
			TermFinder tf = new TermFinder();
	        JSONArray foundTerm = tf.findTerm(wordfreq.get(i).word);
	        
	        TokenMatch tm = new TokenMatch();
	        tm.frequency= wordfreq.get(i).count;
	        tm.gtaaMatches= foundTerm;
	        tm.token= wordfreq.get(i).word;
	        termList.add(tm);
	        //System.out.println(tm.toString());
	     }
		return termList;
	}
	
	
	/* older generate terms 
	public static ArrayList<String> getTerms(String inputString) throws ParseException{
		ArrayList<String> termList =  new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(inputString);

	     while (st.hasMoreTokens()) {
	    	String curToken = st.nextToken();
	    	System.out.println(curToken);
	        TermFinder tf = new TermFinder();
	        String foundTerm = tf.findTerm(curToken);
	        System.out.println(foundTerm);
	     }
		return termList;
	}
	*/
	
	// get the actual wordsequences for a record. question is, do we do the terms for the video as a whole or not?
	// for now, this returns the entire string
	// TODO: make sure we only include TT, not SH or other
	public static String getMetadataForOAIRecord(String recordID){
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
	            // System.out.println(wordseq.getText());
	            // get terms, 
	            // put them back in the XML?
	            // with: version, something like that
	        }
			
			//metadataString = record.getMetadataAsString();

		}
		catch (OAIException e) {e.printStackTrace();}
		
		return metadataString ;
	}
	
	
	public static ArrayList<TokenMatch> getTokenMatches(String oaiIdentifier) throws ParseException{
		String x = getMetadataForOAIRecord(oaiIdentifier);
		Word[] frequency = new WordFrequencyCounter().getFrequentTokensFromString(x);
		ArrayList<Word> test = removeStopwords(frequency);
		ArrayList<TokenMatch> result = getTermsForFreqList(test);
		return result;
	}
	
	
	
	
	
	public static void main(String[] args) throws ParseException {
		stopwords = new StopWords().getStopwords();
		ArrayList<TokenMatch> result = getTokenMatches("oai:beeldengeluid.nl:Expressie:4269665");
		
		for(int i = 0;i<result.size();i++){	
			System.out.println(result.get(i).toString());
		}

		


			
		



	}

}
