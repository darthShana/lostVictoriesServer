package lostVictories.dao;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;

public class GameRequestDAO {

	private static Logger log = Logger.getLogger(GameRequestDAO.class); 
	
	private Client esClient;
	private String indexName = "game_request";

	public GameRequestDAO(Client esClient) {
		this.esClient = esClient;
	}
	
	public UUID getGameRequest(String gameName){
		SearchResponse response = esClient.prepareSearch(indexName)
		        .setTypes(indexName)
		        .setQuery(QueryBuilders.termQuery("gameName", gameName))             // Query
		        .setFrom(0).setSize(60).setExplain(true)
		        .execute()
		        .actionGet();
		
		SearchHits hits = response.getHits();
		if(hits.getTotalHits()>0){
			return UUID.fromString(hits.iterator().next().getId());
		}
		return null;
	}
	
	public void updateGameeRequest(UUID requestID) throws ElasticsearchException, IOException{
		log.debug("updateing game request:"+requestID);
		esClient.prepareUpdate(indexName, "game_request", requestID.toString())
        .setDoc(jsonBuilder()               
            .startObject()
                .field("status", "STARTED")
            .endObject())
        .get();
	}
}
