package lostVictories.dao;

import static com.jme3.lostVictories.network.messages.CharacterMessage.toLatitute;
import static com.jme3.lostVictories.network.messages.CharacterMessage.toLongitude;
import static org.elasticsearch.index.query.FilterBuilders.geoBoundingBoxFilter;
import static org.elasticsearch.index.query.QueryBuilders.filteredQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.log4j.Logger;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.engine.VersionConflictEngineException;
import org.elasticsearch.search.SearchHit;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.UnClaimedEquipmentMessage;
import com.jme3.lostVictories.network.messages.Vector;

public class EquipmentDAO {

	private static Logger log = Logger.getLogger(EquipmentDAO.class); 
	private Client esClient;
	private String indexName;

	public EquipmentDAO(Client esClient, String indexName) {
		this.esClient = esClient;
		this.indexName = indexName;
	}
	
	public void addUnclaiimedEquipment(UnClaimedEquipmentMessage equipment) {
		try {
			esClient.prepareIndex(indexName, "equipmentStatus", equipment.getId().toString())
			        .setSource(equipment.getJSONRepresentation())
			        .setVersion(equipment.getVersion())
			        .execute()
			        .actionGet();

		} catch (VersionConflictEngineException ee){
			log.info("Discarding update to character:"+equipment.getId()+", character has been updated since been loaded");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

	public Set<UnClaimedEquipmentMessage> getUnClaimedEquipment(float x, float y, float z, Long range) {
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
	
	private UnClaimedEquipmentMessage fromFields(UUID id, long version, Map<String, Object> source) {
		return new UnClaimedEquipmentMessage(id, version, source);
	}

	public UnClaimedEquipmentMessage get(UUID equipmentId) {
		GetResponse response = esClient.prepareGet(indexName, "equipmentStatus", equipmentId.toString())
		        .execute()
		        .actionGet();
		if(!response.isExists()){
			return null;
		}
		return fromFields(UUID.fromString(response.getId()), response.getVersion(), response.getSource());
	}

	public boolean delete(UnClaimedEquipmentMessage equipment) {
		DeleteResponse response = esClient.prepareDelete(indexName, "equipmentStatus", equipment.getId().toString())
		        .execute()
		        .actionGet();
		return response.isFound();
		
	}	

}
