package lostVictories.dao;

import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

import lostVictories.model.GameUsage;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.constantScoreQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

public class PlayerUsageDAO {
	private static Logger log = LoggerFactory.getLogger(PlayerUsageDAO.class);

	private Client esClient;
	private final String indexName = "player_usage";
	private final String gameName;
	
	public PlayerUsageDAO(Client esClient, String gameName) throws IOException {
		this.esClient = esClient;
		this.gameName = gameName;
		IndicesAdminClient adminClient = esClient.admin().indices();
		final IndicesExistsResponse res = adminClient.prepareExists(indexName).execute().actionGet();
		if (!res.isExists()) {
			final CreateIndexRequestBuilder createIndexRequestBuilder = adminClient.prepareCreate(indexName);
			
		    XContentBuilder builder = XContentFactory.jsonBuilder().startObject().startObject("properties");
		    builder.startObject("gameName")
	        	.field("type", "string")
	        	.field("index", "not_analyzed")
	        	.field("store", "yes")
	        	.endObject();		    
		    builder.startObject("userID")
	        	.field("type", "string")
	        	.field("index", "not_analyzed")
	        	.field("store", "yes")
	        	.endObject();
		    builder.startObject("startTime")
	        	.field("type", "long")
	        	.field("index", "not_analyzed")
	        	.field("store", "yes")
	        	.endObject();
		    builder.startObject("endTime")
	        	.field("type", "long")
	        	.field("index", "not_analyzed")
	        	.field("store", "yes")
	        	.endObject();
		    builder.endObject().endObject();
			    
		    createIndexRequestBuilder.addMapping(indexName, builder);
		    createIndexRequestBuilder.execute().actionGet();
        }
	}

	public void registerStartGame(UUID userID, long time) {
		GameUsage gameRequest = new GameUsage(gameName, userID, time, null);
		
		try {
			esClient.prepareIndex(indexName, indexName, UUID.randomUUID().toString())
			        .setSource(gameRequest.getJSONRepresentation())
			        .execute()
			        .actionGet();
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

	public void registerStopGame(UUID userID, long currentTimeMillis) {
		SearchResponse searchResponse = esClient.prepareSearch(indexName)
	    		.setQuery(constantScoreQuery(
	    				boolQuery().must(matchQuery("userID",userID.toString())).must(matchQuery("gameName", gameName))))
	    		.addSort("startTime", SortOrder.DESC)
	            .execute().actionGet();

	    if(searchResponse.getHits().getTotalHits()==0){
	    	log.info("failed to find a matching start record for user:"+userID+" gameName:"+gameName);
	    }else{
	    	SearchHit hit = searchResponse.getHits().iterator().next();
	    	GameUsage usage = new GameUsage(hit.sourceAsMap());
	    	usage.setEndTime(currentTimeMillis);
	    	try {
	    		esClient.prepareUpdate(indexName, indexName, hit.getId()).setDoc(usage.getJSONRepresentation()).execute().actionGet();
	    	} catch (IOException e) {
	    		throw new RuntimeException(e);
	    	}
	    }
	}

	public void endAllGameSessions(long currentTimeMillis) {
		SearchResponse searchResponse = esClient.prepareSearch(indexName)
	    		.setQuery(constantScoreQuery(boolQuery().must(matchQuery("gameName", gameName))))
	    		.execute().actionGet();

	    for(Iterator<SearchHit> it = searchResponse.getHits().iterator();it.hasNext();){
	    	SearchHit hit = it.next();
	    	GameUsage usage = new GameUsage(hit.getSourceAsMap());
	    	usage.setEndTime(currentTimeMillis);
	    	try {
	    		esClient.prepareUpdate(indexName, indexName, hit.getId()).setDoc(usage.getJSONRepresentation()).execute().actionGet();
	    	} catch (IOException e) {
	    		throw new RuntimeException(e);
	    	}
	    }
		
	}

}
