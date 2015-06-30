package lostVictories;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.FilterBuilders.geoBoundingBoxFilter;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static com.jme3.lostVictories.network.messages.CharacterMessage.toLatitute;
import static com.jme3.lostVictories.network.messages.CharacterMessage.toLongitude;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.Vector;

public class CharacterDAO {
	private static Logger log = Logger.getLogger(CharacterDAO.class); 

	
	private Client esClient;
	private String indexName;

	public CharacterDAO(Client esClient, String indexName) {
		this.esClient = esClient;
		this.indexName = indexName;
	}
	
	public void putCharacter(UUID uuid, UUID checkedOutBy, CharacterMessage character) {
		try {
			IndexResponse response = esClient.prepareIndex(indexName, "unitStatus", uuid.toString())
			        .setSource(character.getJSONRepresentation())
			        .execute()
			        .actionGet();

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	

	public Set<CharacterMessage> getAllCharacters(float x, float y, float z, float range) {
		
		Vector topLeft = new Vector(x-(range/2), y, z+(range/2));
		Vector bottomRight = new Vector(x+(range/2), y, z-(range/2));
		
		SearchResponse searchResponse = esClient.prepareSearch(indexName)
                .setQuery(filteredQuery(matchAllQuery(), geoBoundingBoxFilter("location").topLeft(toLatitute(topLeft), toLongitude(topLeft)).bottomRight(toLatitute(bottomRight), toLongitude(bottomRight)))).setSize(10000)
                .execute().actionGet();
		
		log.debug("retrived :"+searchResponse.getHits().hits().length+" from elasticsearch");
		Iterator<SearchHit> iterator = searchResponse.getHits().iterator();
		Iterable<SearchHit> iterable = () -> iterator;
		return StreamSupport.stream(iterable.spliterator(), true).map(hit -> fromFields(UUID.fromString(hit.getId()), hit.getSource())).collect(Collectors.toSet());
	}

	private CharacterMessage fromFields(UUID id, Map<String, Object> source) {
		return new CharacterMessage(id, source);
	}

	public void updateCharacterState(UUID uuid, UUID checkedOutBy, CharacterMessage character)  {
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

	public Map<UUID, CharacterMessage> getAllCharacters(Set<UUID> ids) {
		Set<CharacterMessage> ret = new HashSet<CharacterMessage>();
		String[] i = ids.stream().map(UUID::toString).toArray(size->new String[size]);
		QueryBuilder qb = idsQuery().ids(i);
		SearchResponse searchResponse = esClient.prepareSearch(indexName)
                .setQuery(qb).setSize(10000)
                .execute().actionGet();
		
		log.debug("retrived :"+searchResponse.getHits().hits().length+" from elasticsearch");
		Iterator<SearchHit> iterator = searchResponse.getHits().iterator();
		Iterable<SearchHit> iterable = () -> iterator;
		return StreamSupport.stream(iterable.spliterator(), true).map(hit -> fromFields(UUID.fromString(hit.getId()), hit.getSource())).collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
	}

	public void save(Collection<CharacterMessage> values) {
		if(values.isEmpty()){
			log.debug("nothing to save");
			return;
		}
		BulkRequestBuilder bulkRequest = esClient.prepareBulk();
		values.stream().forEach(v->bulkRequest.add(new IndexRequest(indexName, "unitStatus", v.getId().toString()).source(v.getJSONRepresentationUnChecked())));
		bulkRequest.execute().actionGet();
	}

	public CharacterMessage getCharacter(UUID id) {
		GetResponse response = esClient.prepareGet(indexName, "unitStatus", id.toString())
		        .execute()
		        .actionGet();
		return fromFields(UUID.fromString(response.getId()), response.getSource());
	}
}
