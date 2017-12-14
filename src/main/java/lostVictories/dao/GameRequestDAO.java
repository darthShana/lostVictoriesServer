package lostVictories.dao;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.net.Inet4Address;
import java.util.Date;
import java.util.UUID;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameRequestDAO {

	private static Logger log = LoggerFactory.getLogger(GameRequestDAO.class);
	
	private Client esClient;
	private String indexName = "game_request";

	public GameRequestDAO(Client esClient) {
		this.esClient = esClient;
	}
	
	public UUID getGameRequest(String gameName){
		SearchResponse response = esClient.prepareSearch(indexName)
		        .setTypes(indexName)
		        .setQuery(QueryBuilders.termQuery("gameName", gameName))
		        .setFrom(0).setSize(60).setExplain(true)
		        .execute()
		        .actionGet();
		
		SearchHits hits = response.getHits();
		if(hits.getTotalHits()>0){
			return UUID.fromString(hits.iterator().next().getId());
		}
		return null;
	}

    public void updateGameStatus(UUID requestID, String gameID, String gameName, int gamePort, String nameSpace) throws IOException {

        esClient.prepareUpdate(indexName, indexName, requestID.toString())
                .setDoc(jsonBuilder()
						.startObject()
						.field("name", gameName)
						.field("host", "connect.lostvictories.com")
                        .field("localIP", Inet4Address.getLocalHost())
						.field("port", gamePort)
						.field("gameID", gameID)
						.field("nameSpace", nameSpace)
						.field("gameVersion", "pre_alpha")
						.field("status", "inProgress")
						.field("startDate", new Date().getTime())
						.endObject()
				)
                .get();
    }

    public void recordAmericanVictory(UUID requestID) throws ElasticsearchException, IOException {
        log.info("recordAmericanVictory game request:"+requestID);
        esClient.prepareUpdate(indexName, indexName, requestID.toString())
                .setDoc(jsonBuilder()
                        .startObject()
                        .field("victor", "AMERICAN")
                        .field("endDate", new Date().getTime())
                        .field("status", "COMPLETED")
                        .endObject())
                .get();
    }

    public void recordGermanVictory(UUID requestID) throws ElasticsearchException, IOException {
        log.info("recordGermanVictory game request:"+requestID);
        esClient.prepareUpdate(indexName, indexName, requestID.toString())
                .setDoc(jsonBuilder()
                        .startObject()
                        .field("victor", "GERMAN")
                        .field("endDate", new Date().getTime())
                        .field("status", "COMPLETED")
                        .endObject())
                .get();
    }
}
