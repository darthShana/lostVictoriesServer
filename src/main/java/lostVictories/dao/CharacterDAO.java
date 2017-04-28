package lostVictories.dao;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.FilterBuilders.geoBoundingBoxFilter;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static com.jme3.lostVictories.network.messages.CharacterMessage.toLatitute;
import static com.jme3.lostVictories.network.messages.CharacterMessage.toLongitude;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.log4j.Logger;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.engine.VersionConflictEngineException;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.network.messages.Vector;

public class CharacterDAO {
	private static Logger log = Logger.getLogger(CharacterDAO.class); 
	public static ObjectMapper MAPPER;
	
    static{
            MAPPER = new ObjectMapper();
            MAPPER.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
            MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            MAPPER.setSerializationInclusion(Include.NON_NULL);

    }
	
	private Client esClient;
	private String indexName;

	public CharacterDAO(Client esClient, String indexName) {
		this.esClient = esClient;
		this.indexName = indexName;
	}
	  
	public void putCharacter(UUID uuid, CharacterMessage character) {
		try {
			esClient.prepareIndex(indexName, "unitStatus", uuid.toString())
			        .setSource(character.getJSONRepresentation())
			        .setVersion(character.getVersion())
			        .execute()
			        .actionGet();

		} catch (VersionConflictEngineException ee){
			String cot = (character.getCheckoutTime()!=null)?(System.currentTimeMillis()-character.getCheckoutTime())+"":"";
			log.info("Discarding put to character:"+uuid+", character has been updated since been loaded version:"+character.getVersion()+" checkout:"+cot);
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
		
		log.trace("retrived :"+searchResponse.getHits().hits().length+" from elasticsearch");
		Iterator<SearchHit> iterator = searchResponse.getHits().iterator();
		Iterable<SearchHit> iterable = () -> iterator;
		return StreamSupport.stream(iterable.spliterator(), true).map(hit -> fromFields(UUID.fromString(hit.getId()), hit.getVersion(), hit.getSource())).collect(Collectors.toSet());
	}
	
	public CharacterMessage findClosestCharacter(CharacterMessage c, RankMessage rank) {
		double tl_latitute = toLatitute(c.getLocation());
		double tl_longitude = toLongitude(c.getLocation());
		
		GeoDistanceSortBuilder geoDistanceSort = SortBuilders.geoDistanceSort("location").point(tl_latitute, tl_longitude)
	            .unit(DistanceUnit.METERS)
	            .geoDistance(GeoDistance.PLANE);

	    SearchResponse searchResponse = esClient.prepareSearch(indexName)
	    		.setQuery(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders.termFilter("rank",rank.name()))).setSize(100)
	    		.addSort(geoDistanceSort)
	            .setVersion(true)
                .execute().actionGet();

	    if(searchResponse.getHits().getTotalHits()==0){
	    	return null;
	    }
	    SearchHit closest = searchResponse.getHits().iterator().next();
		return fromFields(UUID.fromString(closest.getId()), closest.getVersion(), closest.getSource());
	}

	private CharacterMessage fromFields(UUID id, long version, Map<String, Object> source) {
		return new CharacterMessage(id, version, source);
	}	

	public Map<UUID, CharacterMessage> getAllCharacters(Set<UUID> ids) {
		String[] i = ids.stream().map(UUID::toString).toArray(size->new String[size]);
		QueryBuilder qb = idsQuery().ids(i);
		SearchResponse searchResponse = esClient.prepareSearch(indexName)
                .setQuery(qb).setSize(10000)
                .setVersion(true)
                .execute().actionGet();
		
		log.trace("retrived :"+searchResponse.getHits().hits().length+" from elasticsearch");
		Iterator<SearchHit> iterator = searchResponse.getHits().iterator();
		Iterable<SearchHit> iterable = () -> iterator;
		return StreamSupport.stream(iterable.spliterator(), true).map(hit -> fromFields(UUID.fromString(hit.getId()), hit.getVersion(), hit.getSource())).collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
	}
	
	public Set<CharacterMessage> getAllCharacters() {
		SearchResponse searchResponse = esClient.prepareSearch(indexName)
				.setTypes("unitStatus")
				.setQuery(matchAllQuery()).setSize(10000)
				.setVersion(true)
				.execute().actionGet();
		
		Iterator<SearchHit> iterator = searchResponse.getHits().iterator();
		Iterable<SearchHit> iterable = () -> iterator;
		return StreamSupport.stream(iterable.spliterator(), true).map(hit -> fromFields(UUID.fromString(hit.getId()), hit.version(), hit.getSource())).collect(Collectors.toSet());
	}
	
	public void save(Collection<CharacterMessage> values) throws IOException {		
		BulkRequestBuilder bulkRequest = esClient.prepareBulk();
		for(CharacterMessage v: values){
			bulkRequest.add(
					new UpdateRequest(indexName, "unitStatus", v.getId().toString()).doc(v.getJSONRepresentation())
					);
		}
		
		bulkRequest.execute().actionGet();
	}
	
	public CharacterMessage updateCharacterState(CharacterMessage msg) {

		try {
            UpdateRequest updateRequest = new UpdateRequest(indexName, "unitStatus", msg.getId().toString());
            updateRequest.doc(msg.getStateUpdate()).version(msg.getVersion());
            ActionFuture<UpdateResponse> update = esClient.update(updateRequest);
//            if ("2fbe421f-f701-49c9-a0d4-abb0fa904204".equals(msg.getId().toString())) {
//                System.out.println("in here updating avatar version:" + msg.getVersion() + "to location:" + msg.getLocation() + " version:" + update.get().getVersion());
//            }
            msg.setVersion(update.get().getVersion());
            return msg;
        } catch (IOException e){
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
            log.info("Discarding update to character:"+msg.getId()+", character has been updated since been loaded version:"+msg.getVersion());
		}
		return null;
	}

	public void updateCharacterStateNoCheckout(Map<UUID, CharacterMessage> map) throws IOException{
		if(map.isEmpty()){
			log.trace("nothing to save");
			return;
		}
		
		BulkRequestBuilder bulkRequest = esClient.prepareBulk();
		for(CharacterMessage v: map.values()){
			bulkRequest.add(
					new UpdateRequest(indexName, "unitStatus", v.getId().toString()).doc(v.getStateUpdateNoCheckout()).version(v.getVersion())
					);
		}
		
		bulkRequest.execute().actionGet();
	}
	
	public void saveCommandStructure(Map<UUID, CharacterMessage> map) throws IOException {
		if(map.isEmpty()){
			log.trace("nothing to save");
			return;
		}
		
		BulkRequestBuilder bulkRequest = esClient.prepareBulk();
		for(CharacterMessage v: map.values()){
			bulkRequest.add(
				new UpdateRequest(indexName, "unitStatus", v.getId().toString()).doc(v.getCommandStructureUpdate())
			);
		}
				
		bulkRequest.execute().actionGet();
		
	}
	
	public void updateCharactersUnderCommand(CharacterMessage c) throws IOException {
		esClient.prepareUpdate(indexName, "unitStatus", c.getId().toString())
        .setDoc(jsonBuilder()               
            .startObject()
                .field("unitsUnderCommand", c.getUnitsUnderCommand())
            .endObject())
        .get();
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

	public void refresh() {
		esClient.admin().indices().refresh(new RefreshRequest(indexName)).actionGet();
	}



}
