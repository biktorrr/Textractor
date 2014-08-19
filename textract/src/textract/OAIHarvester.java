package textract;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import se.kb.oai.OAIException;
import se.kb.oai.pmh.ErrorResponseException;
import se.kb.oai.pmh.QueryBuilder;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;
import se.kb.oai.pmh.ResumptionToken;

public class OAIHarvester {

	// OAI list record parameters
	private static String bg_oai_server = "http://integrator.beeldengeluid.nl/search_service_rs/oai/";
	private static String defaultfrom = "2014-01-20T10:00:00Z"; 
	private static String defaultuntil = "2014-01-30T23:00:00Z";
	private static String defaultset = null;
	private static String metadataprefix  = "iMMix"; 
	
	public OAIHarvester() {
	}

	

	// input: some demarcation of items
	// output: list of OAI records
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
	
	// This is the main function, it retrives records for a given start and end period. It first calls the initialization 
	// method and then calls the one with the resumptiontoken
	public List<Record> getOAIItemsForTimePeriod(String from, String until, String set) throws OAIException{
			RecordsList recList = getOAIItemsForTimePeriodStart(from, until, set);
			List<Record> returnList = recList.asList();
			ResumptionToken restoken  = recList.getResumptionToken(); 
			
			// ResumptionToken needs to be fixed to deal with encoding issues
			ResumptionToken fixedrestoken = fixresumptionToken(restoken);
			
			System.out.println(recList.getResponseDate() + " " + fixedrestoken.getExpirationDate() + " " + fixedrestoken.getId());

			boolean more = true;
			// moar records
			while (fixedrestoken !=null & more==true){
				
				try{
					recList =  getOAIItemsForRT(fixedrestoken);
					returnList.addAll(recList.asList());
					//System.out.print(".");
					//newrestoken  = recList.getResumptionToken();
					fixedrestoken = fixresumptionToken(recList.getResumptionToken());
					System.out.println(recList.getResponseDate() + " " + fixedrestoken.getExpirationDate() + " " + fixedrestoken.getId());
				}
				catch (Exception e){
					e.printStackTrace();	
					more = false;
				}
			}
			return returnList;
	}
	
	// build up a new resumptiontoken, with the properly encoded content. This to avoid incorrect tokens
	private ResumptionToken fixresumptionToken(ResumptionToken restoken) {
		String newExpDate = restoken.getExpirationDate();
		String newContent;

		Element elt = DocumentHelper.createElement("resumptionToken");
		elt.addAttribute("expirationDate", newExpDate);
		try {
			newContent = URLEncoder.encode(restoken.getId(),"UTF-8");
			elt.addText(newContent);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		ResumptionToken newrt = new ResumptionToken(elt);
		return newrt;
	}



	public static void main(String[] args)  {
		
	OAIHarvester main = new OAIHarvester();
	List<Record> result;
	try {
		result = main.getOAIItemsForTimePeriod(defaultfrom, defaultuntil, defaultset);
		System.out.println(result.size() +  " records found");
	} catch (OAIException e) {
		e.printStackTrace();
	}
	}
}
