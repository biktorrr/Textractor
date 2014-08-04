package textract;
import static org.elasticsearch.node.NodeBuilder.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.lucene.queryparser.xml.FilterBuilderFactory;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;


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

	public String searchForPrefLabel(String searchString) {
		SearchResponse response = client.prepareSearch(index)
			      //  .setTypes("type1", "type2")
			       // .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			        .setQuery(QueryBuilders.termQuery("preflabel", searchString))             // Query
			        .execute()
			        .actionGet();		
		return response.toString();	
	}
	
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
	
	//search in one Concept Scheme
	// doesnt work
	public String searchForStringInCS(String searchString, String conceptScheme) {
		// remove namespace
		SearchResponse response = client.prepareSearch(index)
			        .setQuery(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), 
			        		FilterBuilders.andFilter(
			        				FilterBuilders.termFilter("preflabel", searchString),
			        				FilterBuilders.termFilter("altlabel", "")
			        				)
			        				))     
			        .execute()
			        .actionGet();		
		return response.toString();	
	}	
	
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
	
	
	public static void main(String[] args) {

		ElasticGTAASearcher es = new ElasticGTAASearcher();
		System.out.println(es.searchForString("-"));
		System.out.println(es.searchForString("iets anders"));
		//System.out.println(es.searchForPrefLabel("personen"));
		//System.out.println(es.searchForStringFuzzy("personen"));
		//System.out.println(es.searchForStringInCS("personen","vermissingen"));
		
	}
}
