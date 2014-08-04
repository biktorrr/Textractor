package textract;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import se.kb.oai.OAIException;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;
import se.kb.oai.pmh.ResumptionToken;

// This class is used to fill an ES instance with GTAA, retrieved from the GTAA OpenSKOS endpoint
public class GTAAIndexer {


	
	
	// get documents to be indexed in elasticsearch
	public  ArrayList<ESDoc> getOAIRecords() {
		ArrayList<ESDoc> result = new ArrayList<ESDoc>();
		
		String gtaa_oai_server = "http://openskos.org/oai-pmh";	
		OaiPmhServer server = new OaiPmhServer(gtaa_oai_server);
		
		boolean more = true;
		int counter = 0;
		int maxCounter = 9999999;
		ResumptionToken rt = null;
		
		while(more  && counter < maxCounter){
			try {
				System.out.print(".."+counter);
				RecordsList records = null;
				if (counter==0){records = server.listRecords("oai_rdf", "0001-01-01T12:00:00Z", "9999-09-01T12:00:00Z", "4");}
				else{
					records = server.listRecords(rt); // with resumptiontoken
					} 
				
				List list = records.asList();
				Iterator iter=list.iterator();			
				// Iterate through the records 
		        while(iter.hasNext()){
		            Record elt =(Record) iter.next();
		            ESDoc esd = elementToESDoc(elt);
		            if(esd !=null){
		            	result.add(esd);
		            	}
		        }		
				rt = records.getResumptionToken();
				counter ++;
			}
			catch (Exception e) {
				e.printStackTrace(); 
				counter ++;
				System.out.println("used identifiers");  // still an error here
				}		
			}
		return result;
		
		
	}

	// just do them all
	public  void indexInterativelyOAIRecords(ElasticGTAASearcher es){
		
		String gtaa_oai_server = "http://openskos.org/oai-pmh";	
		OaiPmhServer server = new OaiPmhServer(gtaa_oai_server);
		
		boolean more = true;
		int counter = 0;
		int maxCounter = 999999;
		ResumptionToken rt = null;
		
		System.out.println("------ start -------");
		while(more  && counter < maxCounter){
			ArrayList<ESDoc> onePage = new ArrayList<ESDoc>();
			System.out.print(counter + "..retrieving..");

			try {
				
				RecordsList records = null;
				if (counter==0){records = server.listRecords("oai_rdf");}
				else{records = server.listRecords(rt);} // with resumptiontoken
				
				List list = records.asList();
				Iterator iter=list.iterator();			
				// Iterate through the records 
		        while(iter.hasNext()){
		            Record elt =(Record) iter.next();
		            ESDoc esd = elementToESDoc(elt);
		            if(esd !=null){
		            	onePage.add(esd);
		            	}
		        }		
				rt = records.getResumptionToken();
				counter ++;
				//System.out.println(counter);
			}
			catch (OAIException e) {
				e.printStackTrace(); 
				more = false;
				}
			System.out.print("indexing..");
			es.indexESDocs(onePage);		
			System.out.println("done");
			}
		System.out.println("------ done -------");
	}
		
		
	// this variant respods with the esdoc list without indexing it
	private ESDoc elementToESDoc(Record elt) {
        if (elt.getMetadata() != null){
            Element data= (Element) elt.getMetadata().elements().get(0);
            
            String uri = data.attribute("about").getText();
            
            List<Element> altLabelList = data.elements("altLabel");
            String altLabel = "";
            for (int i=0; i<altLabelList.size();i++) {
            	if (i>0){altLabel +=" ";}
            	altLabel += altLabelList.get(i).getText();
            }
            
            List<Element> prefLabelList = data.elements("prefLabel");
            String prefLabel = "";
            for (int i=0; i<prefLabelList.size();i++) {
            	if (i>0){prefLabel +=" ";}
            	prefLabel += prefLabelList.get(i).getText();
            	
            }            
            List<Element> csList = data.elements("inScheme");
            String cs = "";
            for (int j=0; j<csList.size();j++) {		            	
            	if (j>0){cs +=" ";}
            	cs += csList.get(j).attribute("resource").getValue();
            }
            
           // System.out.println("found uri: " + uri + " pl "+ prefLabel + " al: " + altLabel + " cs: "+ cs);
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
		GTAAIndexer moi = new GTAAIndexer();
		ElasticGTAASearcher es = new ElasticGTAASearcher();
		moi.indexInterativelyOAIRecords(es);
		//System.out.println(moi.getOAIRecords().size());
		
	}

}
