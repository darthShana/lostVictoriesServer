package lostVictories;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.FilterBuilders.geoBoundingBoxFilter;
import static org.elasticsearch.index.query.QueryBuilders.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.log4j.Logger;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import com.jme3.lostVictories.network.messages.Character;

public class CharacterDAO {
	private static Logger log = Logger.getLogger(CharacterDAO.class); 

	
	private Client esClient;
	private String indexName;

	public CharacterDAO(Client esClient, String indexName) {
		this.esClient = esClient;
		this.indexName = indexName;
	}
	
	public void putCharacter(UUID uuid, UUID checkedOutBy, Character character) {
		try {
			IndexResponse response = esClient.prepareIndex(indexName, "unitStatus", uuid.toString())
			        .setSource(character.getJSONRepresentation())
			        .execute()
			        .actionGet();

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	

	public Set<Character> getAllCharacters() {
		Set<Character> ret = new HashSet<Character>();
		
//		SearchResponse searchResponse = esClient.prepareSearch(indexName)
//                .setQuery(matchAllQuery())
//                .execute().actionGet();
		
		SearchResponse searchResponse = esClient.prepareSearch(indexName)
                .setQuery(filteredQuery(matchAllQuery(), geoBoundingBoxFilter("location").topLeft(79, -179).bottomRight(-79, 179))).setSize(10000)
                .execute().actionGet();
		
		log.debug("retrived :"+searchResponse.getHits().hits().length+" from elasticsearch");
		Iterator<SearchHit> iterator = searchResponse.getHits().iterator();
		Iterable<SearchHit> iterable = () -> iterator;
		return StreamSupport.stream(iterable.spliterator(), true).map(hit -> fromFields(UUID.fromString(hit.getId()), hit.getSource())).collect(Collectors.toSet());
	}

	private Character fromFields(UUID id, Map<String, Object> source) {
		return new Character(id, source);
	}

	public void updateCharacterState(UUID uuid, UUID checkedOutBy, Character character)  {
		UpdateRequest updateRequest = new UpdateRequest();
		updateRequest.index(indexName);
		updateRequest.type("unitStatus");
		updateRequest.id(uuid.toString());
		try {
			updateRequest.doc(jsonBuilder()
			        .startObject()
			            .field("location", character.getLocation())
			        .endObject());
			esClient.update(updateRequest).get();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
}
