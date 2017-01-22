package lostVictories.dao;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;

public class GameStatusDAO {
	
	private static Logger log = Logger.getLogger(GameStatusDAO.class); 
	private Client esClient;
	private String indexName;

	public GameStatusDAO(Client esClient, String indexName) {
		this.esClient = esClient;
		this.indexName = indexName;
	}

	public void createGameStatus(String gameID, String gameName, int gamePort, String... indexes) throws IOException {
		XContentBuilder gameDetails = jsonBuilder()
		    .startObject()
		    	.field("name", gameName)
		    	.field("host", "connect.lostvictories.com")
		        .field("port", gamePort)
		        .field("gameID", gameID)
		        .field("indexes", indexes)		        
		        .field("gameVersion", "pre_alpha")
		        .field("gameStatus", "inProgress")
		        .field("startDate", new Date().getTime())
		    .endObject();
		esClient.prepareIndex(indexName, "gameStatus", "gameStatus")
		    .setSource(gameDetails)
		    .execute()
		    .actionGet();
	}

	public void recordAmericanVictory() throws ElasticsearchException, IOException {
		esClient.prepareUpdate(indexName, "gameStatus", "gameStatus")
        .setDoc(jsonBuilder()               
            .startObject()
                .field("gameStatus", "finished")
                .field("victor", "AMERICAN")
                .field("endDate", new Date().getTime())
            .endObject())
        .get();
		
	}
	
	public void recordGermanVictory() throws ElasticsearchException, IOException {
		esClient.prepareUpdate(indexName, "gameStatus", "gameStatus")
		.setDoc(jsonBuilder()               
				.startObject()
				.field("gameStatus", "finished")
				.field("victor", "GERMAN")
				.field("endDate", new Date().getTime())
				.endObject())
				.get();
		
	}
}
