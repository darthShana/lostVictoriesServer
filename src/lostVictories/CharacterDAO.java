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
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.engine.VersionConflictEngineException;
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
			        .setVersion(character.getVersion())
			        .execute()
			        .actionGet();

		} catch (VersionConflictEngineException ee){
			log.info("Discarding update to character:"+uuid+", character has been updated since been loaded");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	

	public Set<CharacterMessage> getAllCharacters(float x, float y, float z, float range) {
		
		Vector topLeft = new Vector(x-range, y, z+range);
		Vector bottomRight = new Vector(x+range, y, z-range);
		
		double tl_latitute = toLatitute(topLeft);
		double tl_longitude = toLongitude(topLeft);
		double br_latitute = toLatitute(bottomRight);
		double br_longitude = toLongitude(bottomRight);
		
		tl_latitute = tl_latitute > 80 ? 80 : tl_latitute;
		tl_longitude = tl_longitude < -180 ? -180 : tl_longitude;
		br_latitute = br_latitute < -80 ? -80 : br_latitute;
		br_longitude = br_longitude > 180 ? 180 : br_longitude;
		
		SearchResponse searchResponse = esClient.prepareSearch(indexName)
                .setQuery(filteredQuery(matchAllQuery(), geoBoundingBoxFilter("location").topLeft(tl_latitute, tl_longitude).bottomRight(br_latitute, br_longitude))).setSize(10000)
                .setVersion(true)
                .execute().actionGet();
		
		log.debug("retrived :"+searchResponse.getHits().hits().length+" from elasticsearch");
		Iterator<SearchHit> iterator = searchResponse.getHits().iterator();
		Iterable<SearchHit> iterable = () -> iterator;
		return StreamSupport.stream(iterable.spliterator(), true).map(hit -> fromFields(UUID.fromString(hit.getId()), hit.getVersion(), hit.getSource())).collect(Collectors.toSet());
	}

	private CharacterMessage fromFields(UUID id, long version, Map<String, Object> source) {
		return new CharacterMessage(id, version, source);
	}

	public void updateCharacterState(UUID uuid, UUID checkedOutBy, CharacterMessage character)  {
		UpdateRequest updateRequest = new UpdateRequest();
		updateRequest.index(indexName);
		updateRequest.type("unitStatus");
		updateRequest.id(uuid.toString());
		updateRequest.version(character.getVersion());
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
		String[] i = ids.stream().map(UUID::toString).toArray(size->new String[size]);
		QueryBuilder qb = idsQuery().ids(i);
		SearchResponse searchResponse = esClient.prepareSearch(indexName)
                .setQuery(qb).setSize(10000)
                .setVersion(true)
                .execute().actionGet();
		
		log.debug("retrived :"+searchResponse.getHits().hits().length+" from elasticsearch");
		Iterator<SearchHit> iterator = searchResponse.getHits().iterator();
		Iterable<SearchHit> iterable = () -> iterator;
		return StreamSupport.stream(iterable.spliterator(), true).map(hit -> fromFields(UUID.fromString(hit.getId()), hit.getVersion(), hit.getSource())).collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
	}
	
	public Set<CharacterMessage> getAllCharacters() {
		SearchResponse searchResponse = esClient.prepareSearch(indexName)
				.setQuery(matchAllQuery()).setSize(10000)
				.setVersion(true)
				.execute().actionGet();
		
		log.debug("retrived :"+searchResponse.getHits().hits().length+" from elasticsearch");
		Iterator<SearchHit> iterator = searchResponse.getHits().iterator();
		Iterable<SearchHit> iterable = () -> iterator;
		return StreamSupport.stream(iterable.spliterator(), true).map(hit -> fromFields(UUID.fromString(hit.getId()), hit.version(), hit.getSource())).collect(Collectors.toSet());
	}
	
	public void save(Collection<CharacterMessage> values) {
		values.stream().forEach(c->putCharacter(c.getId(), c.getCheckoutClient(), c));
	}
	
	public void updateLocation(Collection<CharacterMessage> values) throws IOException{
		if(values.isEmpty()){
			log.debug("nothing to save");
			return;
		}
		
		BulkRequestBuilder bulkRequest = esClient.prepareBulk();
		for(CharacterMessage v: values){
			bulkRequest.add(
				new UpdateRequest(indexName, "unitStatus", v.getId().toString()).doc(jsonBuilder()
					.startObject()
						.field("location", new GeoPoint(toLatitute(v.getLocation()), toLongitude(v.getLocation())))
						.field("altitude", v.getLocation().y)
						.field("orientation", v.getOrientation().toMap())
					.endObject())
				);
		}
				
		bulkRequest.execute().actionGet();
		
	}

	public CharacterMessage getCharacter(UUID id) {
		GetResponse response = esClient.prepareGet(indexName, "unitStatus", id.toString())
		        .execute()
		        .actionGet();
		if(!response.isExists()){
			return null;
		}
		return fromFields(UUID.fromString(response.getId()), response.getVersion(), response.getSource());
	}

	public boolean delete(CharacterMessage c) {
		DeleteResponse response = esClient.prepareDelete(indexName, "unitStatus", c.getId().toString())
		        .execute()
		        .actionGet();
		return response.isFound();
	}

	public void saveAndRefresh(CharacterMessage character) {
		esClient.prepareIndex(indexName, "unitStatus", character.getId().toString()).setSource(character.getJSONRepresentationUnChecked()).setVersion(character.getVersion()).execute().actionGet();
		esClient.admin().indices().refresh(new RefreshRequest(indexName)).actionGet();
	}
}
