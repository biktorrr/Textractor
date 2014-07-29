package textract;
import static org.elasticsearch.node.NodeBuilder.*;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;

public class ElasticSearcher {
	private static Node node;
	private static Client client; 
	

	public static void startNode(){
		Node node = nodeBuilder().node();
		client = node.client();

	}
	
	public static void closeNode(){
		node.close();

	}
	
	public static void main(String[] args) {

		Settings settings = ImmutableSettings.settingsBuilder()
			    .put("cluster_name", "elasticsearch").build();
		
		Client client = new TransportClient(settings)
        .addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300));

		

		SearchResponse response = client.prepareSearch("gtaa")
		      //  .setTypes("type1", "type2")
		       // .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
		        .setQuery(QueryBuilders.termQuery("preflabel", "mensen"))             // Query
		        .execute()
		        .actionGet();
		
		System.out.println(response);
		
		client.close();
		
		
		

		/*startNode();
		
		String json = "{" +
		        "\"user\":\"kimchy\"," +
		        "\"postDate\":\"2013-01-30\"," +
		        "\"message\":\"trying out Elasticsearch\"" +
		    "}";
		
		IndexResponse response = client.prepareIndex("twitter", "tweet")
		        .setSource(json)
		        .execute()
		        .actionGet();

		// on shutdown
		 */
		
	}

}
