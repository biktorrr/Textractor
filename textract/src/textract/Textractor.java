package textract;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

import se.kb.oai.OAIException;
import se.kb.oai.pmh.ErrorResponseException;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.QueryBuilder;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;
import se.kb.oai.pmh.ResumptionToken;
import textract.WordFrequencyCounter.Word;


public class Textractor {

	private static String bg_oai_server = "http://integrator.beeldengeluid.nl/search_service_rs/oai/";
	private static ArrayList<String> stopwords;
	
	private static int minFreq = 5; // minimum frequency for a token to be added to the tokenlist
	
	//GTAA elasticsearch params
	private static String host = "127.0.0.1";
	private static int port = 9300;

	// OAI list record parameters
	private static String from = "2014-02-03T12:00:00Z"; 
	private static String until = "2014-02-03T14:00:00Z";
	private static String set = null;
	private static String metadataprefix  = "iMMix"; 
	

	public Textractor(){
		stopwords = new StopWords().getStopwords();
	}

	
	// remove stopwords from frequency Words (return as ArrayList)
	// also remove infrequent words
	public static ArrayList<Word> removeStopwords (Word[] frequencyTokens){
		ArrayList<Word> freqAl = new ArrayList<Word>();
        for(Word w:frequencyTokens){
            if (stopwords.contains(w.word) || w.count<minFreq ){
            }
            else {
            	freqAl.add(w);
            }
        }
        return freqAl;
	}
	
	
	
	// generate terms for a frequency list
	public static ArrayList<TokenMatch> getTermsForFreqList(Client client, ArrayList<Word> wordfreq) throws ParseException{
		ArrayList<TokenMatch> termList =  new ArrayList<TokenMatch>();
		
		for(int i = 0; i<wordfreq.size(); i++){
			TermFinder tf = new TermFinder();
	        JSONArray foundTerm = tf.findTerm(client, wordfreq.get(i).word);
	        
	        TokenMatch tm = new TokenMatch();
	        tm.frequency= wordfreq.get(i).count;
	        tm.gtaaMatches= foundTerm;
	        tm.token= wordfreq.get(i).word;
	        termList.add(tm);
	        if (i%10 == 0){System.out.print(".");}
	        	
	   
	        //System.out.println(tm.toString());
	     }
		if (termList.size() < 1) System.out.println(" no terms found");
		else System.out.println(" done");
		return termList;
	}
	
	

	
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
	        }		
		}
		catch (OAIException e) {e.printStackTrace();}
		
		return metadataString ;
	}
	
	// input: ES client and oai identifier string
	// output: list of tokenmatches for one item
	public static ArrayList<TokenMatch> getTokenMatches(Client client, String oaiIdentifier) throws ParseException{
		System.out.println("Retrieving tokens from record " + oaiIdentifier);
		String x = getMetadataForOAIRecord(oaiIdentifier);
		Word[] frequency = new WordFrequencyCounter().getFrequentTokensFromString(x);
		ArrayList<Word> wordlist = removeStopwords(frequency);
		System.out.print(" Matching to GTAA");
		ArrayList<TokenMatch> result = getTermsForFreqList(client, wordlist);
		return result;
	}
	
	// input: some demarcation of items
	// output: list of items 
	public static RecordsList getOAIItemsForTimePeriod(String from, String until, String set) throws OAIException{
		QueryBuilder builder = new QueryBuilder(bg_oai_server);
		SAXReader reader = new SAXReader();
		try{
			String query = builder.buildListRecordsQuery(metadataprefix, from, until, set);
			Document document = reader.read(query);
			return new RecordsList(document);
			} catch (ErrorResponseException e) {
				throw e;
			} catch (Exception e) {
				throw new OAIException(e);
		}
	
	}
	
	
	
	public static void main(String[] args) throws ParseException {
		try {
			PrintWriter writer;
			writer = new PrintWriter("textractor_output.txt", "UTF-8");
			
			stopwords = new StopWords().getStopwords();
			ArrayList<ArrayList<TokenMatch>> endResult = new ArrayList<ArrayList<TokenMatch>>();
			
			//Connecting to GTAA Elasticsearch server
			System.out.println("Connecting to GTAA Elasticsearch server");
			Settings settings = ImmutableSettings.settingsBuilder().put("cluster_name", "elasticsearch").build();
			Client client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(host, port));
			
			ElasticSearcher gtaaES = new ElasticSearcher();
			
			// get records from OAI
			try {
				System.out.println("Retrieving records from OAI server");
				RecordsList rl = getOAIItemsForTimePeriod(from, until, set);
				List<Record> recordList = rl.asList();
				ResumptionToken rt  = rl.getResumptionToken(); //TODO: go on with new resumption token
				
				// Loop through records
				System.out.println("Looping through records");
				for (int i=0; i<recordList.size();i++){
					String recordid = recordList.get(i).getHeader().getIdentifier();
					writer.println(recordid+ "\n");
					System.out.print(Integer.toString(i) + ": ");

					ArrayList<TokenMatch> result = getTokenMatches(client, recordid);
					for(int j = 0;j<result.size();j++){	
						writer.println(result.get(j).toString());
					}
					writer.println("\n\n");
					endResult.add(result);
				}
			} catch (OAIException e) {
				e.printStackTrace();
			}
	
						
			writer.close();
			client.close();
			System.out.println("I'm done");
			
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	



	}

}
