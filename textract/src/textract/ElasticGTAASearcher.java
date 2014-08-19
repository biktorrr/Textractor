package textract;
import java.io.IOException;
import java.util.ArrayList;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;


// Class for searching for terms in GTAA. In this case it is done using Elasticsearch

public class ElasticGTAASearcher {
	private static Client client; 
	
	//GTAA elasticsearch params
	private static String host = "localhost";
	private static int port = 9300;
	private static String index = "gtaa";
	
	public ElasticGTAASearcher(){
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


	// ---------------------  SECRET INDEXING METHOD -------------------------------
	
	// this method indexes ESDocs, as retrieved from the GTAA OAI interface
	public void indexESDocs(ArrayList<ESDoc> esdocs){
		BulkRequestBuilder bulkRequest = client.prepareBulk();
		for( int i = 0 ; i< esdocs.size();i++){
			ESDoc ed = esdocs.get(i);
			String myuri = ed.uri.replaceAll("/", "_");
			// only gtaa concepts
			if (ed.conceptSchemes.contains("data.beeldengeluid.nl/gtaa")){
				try {
					
					bulkRequest.add(client.prepareIndex("gtaa", "concept", myuri)
					        .setSource(XContentFactory.jsonBuilder()
					                    .startObject()
					                        .field("uri", myuri)
					                        .field("preflabel", ed.preflabel)
					                        .field("altlabel", ed.altlabel)
					                        .field("conceptScheme", ed.conceptSchemes)
					                    .endObject()
					                  )
					        );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		BulkResponse bulkResponse = bulkRequest.execute().actionGet();
		
		if (bulkResponse.hasFailures()) {
		    // process failures by iterating through each bulk response item
			}
	}
	
	
	
	// ---------------------- SEARCHING METHODS ----------------------------	
	
	// preflabel only
	public String searchForPrefLabel(String searchString) {
		SearchResponse response = client.prepareSearch(index)
			      //  .setTypes("type1", "type2")
			       // .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			        .setQuery(QueryBuilders.termQuery("preflabel", searchString))             // Query
			        .execute()
			        .actionGet();		
		return response.toString();	
	}
	
	// Search for a string in any ES document
	public String searchForString(String searchString) {
		try{
			SearchResponse response = client.prepareSearch(index)	
			        .setQuery(QueryBuilders.queryString(searchString))             // Query
			        .execute()
			        .actionGet();		
		return response.toString();
		}
		catch (Exception e){
			//e.printStackTrace();
			return "{[]}";
		}
	}	
	
	
	//fuzzy
	public String searchForStringFuzzy(String searchString) {
		SearchResponse response = client.prepareSearch(index)
			        .setQuery(QueryBuilders.fuzzyQuery("preflabel", searchString))             // Query
			        .execute()
			        .actionGet();		
		return response.toString();	
	}	
	

	// search for a string in a specific  conceptscheme (one of Onderwerpen, Persoonsnamen, etc)
	public String searchForStringInCS(String searchString, String conceptScheme) {
		// remove namespace
		BoolQueryBuilder qb = QueryBuilders
                .boolQuery()
				 .must(QueryBuilders.matchQuery("_all", searchString))
				 .must(QueryBuilders.matchQuery("conceptScheme",conceptScheme))
				 ;	
		SearchResponse response = client.prepareSearch(index)
			        .setQuery(qb)   
			        .execute()
			        .actionGet();		
		return response.toString();	
	}	
	
	
	
	public static void main(String[] args) {

		ElasticGTAASearcher es = new ElasticGTAASearcher();
		//System.out.println(es.searchForString("banken"));
		//System.out.println(es.searchForString("iets anders"));
		//System.out.println(es.searchForPrefLabel("personen"));
		//System.out.println(es.searchForStringFuzzy("personen"));
		System.out.println(es.searchForStringInCS("banken","Onderwerpen"));
		System.out.println(es.searchForStringInCS("Jan Peter Balkenende","Persoonsnamen"));

	}
}
