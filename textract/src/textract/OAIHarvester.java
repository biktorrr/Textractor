package textract;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.json.simple.parser.ParseException;

import se.kb.oai.OAIException;
import se.kb.oai.pmh.ErrorResponseException;
import se.kb.oai.pmh.QueryBuilder;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;
import se.kb.oai.pmh.ResumptionToken;

public class OAIHarvester {

	// OAI list record parameters
	private static String bg_oai_server = "http://integrator.beeldengeluid.nl/search_service_rs/oai/";
	private static String defaultfrom = "2014-01-02T08:00:00Z"; 
	private static String defaultuntil = "2014-01-03T23:00:00Z";
	private static String defaultset = null;
	private static String metadataprefix  = "iMMix"; 
	
	public OAIHarvester() {
	}

	

	// input: some demarcation of items
	// output: list of items 
	public RecordsList getOAIItemsForTimePeriodStart(String from, String until, String set) throws OAIException{
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
	// for resumption token
	public RecordsList getOAIItemsForRT(ResumptionToken rt) throws OAIException, DocumentException{
		QueryBuilder builder = new QueryBuilder(bg_oai_server);
		SAXReader reader = new SAXReader();
		String query = builder.buildListRecordsQuery(rt);
		Document document = reader.read(query);
		return new RecordsList(document);
			
			
	
	}
	
	public List<Record> getOAIItemsForTimePeriod(String from, String until, String set) throws OAIException{
			RecordsList recList = getOAIItemsForTimePeriodStart(from, until, set);
			List<Record> returnList = recList.asList();
			ResumptionToken restoken  = recList.getResumptionToken(); 
			System.out.println(recList.getResponseDate() + " " + restoken.getExpirationDate() + " " + restoken.getId());

			boolean more = true;
			// moar records
			while (restoken !=null & more==true){
				
				try{
					recList =  getOAIItemsForRT(restoken);
					returnList.addAll(recList.asList());
					//System.out.print(".");
					restoken  = recList.getResumptionToken();
					System.out.println(recList.getResponseDate() + " " + restoken.getExpirationDate() + " " + restoken.getId());
				}
				catch (Exception e){
					e.printStackTrace();	
					more = false;
				}
			}
			return returnList;
	}
	
	public static void main(String[] args)  {
		
	OAIHarvester main = new OAIHarvester();
	List<Record> result;
	try {
		result = main.getOAIItemsForTimePeriod(defaultfrom, defaultuntil, defaultset);
		System.out.println(result.size());
	} catch (OAIException e) {
		e.printStackTrace();
	}
	}
}
