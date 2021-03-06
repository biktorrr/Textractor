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
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.FilteredQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermFilterBuilder;


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
				for (String conceptScheme : ed.conceptSchemes.split(" ")){
					try {
						String cstype = "";
						if (conceptScheme.equals("http://data.beeldengeluid.nl/gtaa/Onderwerpen")){
							cstype = "Onderwerpen";
						}
						else if (conceptScheme.equals("http://data.beeldengeluid.nl/gtaa/Persoonsnamen")){
							cstype = "Persoonsnamen";
						}
						else if (conceptScheme.equals("http://data.beeldengeluid.nl/gtaa/Namen")){
							cstype = "Namen";
						}
						else if (conceptScheme.equals("http://data.beeldengeluid.nl/gtaa/GeografischeNamen")){
							cstype = "GeografischeNamen";
						}
						if (cstype != ""){	// only add to index if one of the four axes
							bulkRequest.add(client.prepareIndex("gtaa", cstype, myuri)
							        .setSource(XContentFactory.jsonBuilder()
							                    .startObject()
							                        .field("uri", myuri)
							                        .field("preflabel", ed.preflabel)
							                        .field("altlabel", ed.altlabel) //TODO: add multifields?
							                        .field("conceptScheme", conceptScheme)
							                    .endObject()
							                  )
							        );
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
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
	
	// search for a string in a specific  conceptscheme (one of Onderwerpen, Persoonsnamen, etc) > new version based on es type
	public String searchForStringInCS(String searchString, String conceptScheme) {
		try{
		FilterBuilder filter = FilterBuilders.typeFilter(conceptScheme);
		QueryBuilder qb = QueryBuilders.filteredQuery(QueryBuilders.queryString(searchString), filter);
		
		SearchResponse response = client.prepareSearch(index)
			        .setQuery(qb)   
			        .execute()
			        .actionGet();
		return response.toString();	

		} 
		catch (Exception e){
			System.out.print("e");
			return emptyResult();
		}
	}
	
	/* search for a string in a specific  conceptscheme (one of Onderwerpen, Persoonsnamen, etc)
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
	}*/
	
	/* TODO: fix this here
	//search for a string in a specific  conceptscheme (one of Onderwerpen, Persoonsnamen, etc)
	public String searchForStringInCS(String searchString, String conceptScheme) {
		// remove namespace
		 QueryBuilder qb =  QueryBuilders.filteredQuery(
				 QueryBuilders.termQuery("_all", searchString), FilterBuilders.termFilter("conceptScheme", conceptScheme))			 	
				 ;
				 QueryBuilder qb = QueryBuilders.matchQuery("_all", searchString);
		
				 
		SearchResponse response = client.prepareSearch(index)
			        .setQuery( qb)   
			        .execute()
			        .actionGet();		
		return response.toString();	
	}	*/
	
	private String emptyResult() {
		String returnstring = "{\"hits\":{\"total\":0,\"max_score\":null,\"hits\":[]}}";
		return returnstring;
	}


	public static void main(String[] args) {

		ElasticGTAASearcher es = new ElasticGTAASearcher();
		//System.out.println(es.searchForString("banken"));
		//System.out.println(es.searchForString("iets anders"));
		//System.out.println(es.searchForPrefLabel("personen"));
		//System.out.println(es.searchForStringFuzzy("personen"));
		System.out.println(es.searchForStringInCS("ik:","Onderwerpen"));
		System.out.println(es.searchForStringInCS("derks, pieter","Persoonsnamen"));

	}
}
