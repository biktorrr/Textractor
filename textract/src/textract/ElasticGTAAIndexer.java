package textract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import se.kb.oai.OAIException;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;
import se.kb.oai.pmh.ResumptionToken;
import static org.elasticsearch.common.xcontent.XContentFactory.*;

// This class is used to fill an ES instance with GTAA, retrieved from the GTAA OpenSKOS endpoint
public class ElasticGTAAIndexer {

	private class ESDoc {
		public String preflabel;
		public String altlabel;
		public String conceptSchemes;
		public String uri;

	}
	
	private static Client client; 

	//GTAA elasticsearch params
	private static String host = "localhost";
	private static int port = 9300;
	private static String index = "gtaa";
	
	public ElasticGTAAIndexer(){
		System.out.println("Connecting to GTAA Elasticsearch server");
		startClient();
			}

	
	public void closeClient(){
		client.close();
	}
	public static void startClient(){
		Settings settings = ImmutableSettings.settingsBuilder().put("cluster_name", "elasticsearch").build();
		client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(host, port));
	}
	
	public Client getClient() {
		return client;
	}


	public void indexOneDoc(String id, String uri, String preflabel, String altlabels, String conceptSchemes) throws ElasticsearchException, IOException{
		IndexResponse response = client.prepareIndex("gtaa", "concept", id)
		        .setSource(jsonBuilder()
		                    .startObject()
		                        .field("uri", uri)
		                        .field("preflabel", preflabel)
		                        .field("altlabels", altlabels)
		                        .field("conceptschemes", conceptSchemes)
		                    .endObject()
		                  )
		        .execute()
		        .actionGet();
	}
	
	
	// get documents to be indexed in elasticsearch
	public  ArrayList<ESDoc> getOAIRecords(){
		ArrayList<ESDoc> result = new ArrayList<ESDoc>();
		
		String gtaa_oai_server = "http://openskos.org/oai-pmh";	
		OaiPmhServer server = new OaiPmhServer(gtaa_oai_server);
		
		boolean more = true;
		int counter = 0;
		int maxCounter = 5;
		ResumptionToken rt = null;
		
		while(more  && counter < maxCounter){
			
			try {
				
				RecordsList records = null;
				if (counter==0){records = server.listRecords("oai_rdf");}
				else{records = server.listRecords(rt);} // with resumptiontoken
				
				List list = records.asList();
				Iterator iter=list.iterator();			
				// Iterate through the records 
		        while(iter.hasNext()){
		            Record elt =(Record) iter.next();            
		            result.add(elementToESDoc(elt));
		        }		
				rt = records.getResumptionToken();
				counter ++;
				System.out.println(counter);
			}
			catch (OAIException e) {
				e.printStackTrace(); 
				more = false;
				}
		
			}
		return result;
		
		
	}

	
	private ESDoc elementToESDoc(Record elt) {
        if (elt.getMetadata() != null){
            Element data= (Element) elt.getMetadata().elements().get(0);
            String uri = data.attribute("about").getText();
            String prefLabel = data.element("prefLabel").getText();
            
            List<Element> altLabelList = data.elements("altLabel");
            String altLabel = "";
            for (int i=0; i<altLabelList.size();i++) {
            	altLabel += altLabelList.get(i).getText();
            }
            
            List<Element> csList = data.elements("inScheme");
            String cs = "";
            for (int j=0; j<csList.size();j++) {		            	
            	cs += csList.get(j).attribute("resource").getValue();
            }
            
            System.out.println("found uri: " + uri + " pl "+ prefLabel + " al: " + altLabel + " cs: "+ cs);
            ESDoc esd = new ESDoc();
            
            esd.uri = uri;
            esd.preflabel = prefLabel;
            esd.altlabel = altLabel;
            esd.conceptSchemes = cs;
            return(esd);
        	}
        else return null;
        }


	public static void main(String[] args) {
		ElasticGTAAIndexer indexer = new ElasticGTAAIndexer();
		ArrayList<ESDoc> bla = indexer.getOAIRecords();
		
		/*
		ElasticGTAAIndexer indexer = new ElasticGTAAIndexer();
		try {
			indexer.indexOneDoc("24997", "test", "test", "test", "test");
		} catch (ElasticsearchException | IOException e) {
			e.printStackTrace();
		}
		indexer.closeClient();
*/
	}

}
