package lostVictories;

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
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;

import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.Vector;

public class HouseDAO {
	private static Logger log = Logger.getLogger(HouseDAO.class); 

	private Client esClient;
	private String indexName;

	public HouseDAO(Client esClient, String indexName) {
		this.esClient = esClient;
		this.indexName = indexName;
	}
	
	public void putHouse(UUID uuid, HouseMessage house) {
		try {
			IndexResponse response = esClient.prepareIndex(indexName, "houseStatus", uuid.toString())
			        .setSource(house.getJSONRepresentation())
			        .execute()
			        .actionGet();

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Set<HouseMessage> getAllHouses() {
		SearchResponse searchResponse = esClient.prepareSearch(indexName)
                .setQuery(matchAllQuery()).setSize(10000)
                .execute().actionGet();
		
		log.trace("retrived :"+searchResponse.getHits().hits().length+" houses from elasticsearch");
		Iterator<SearchHit> iterator = searchResponse.getHits().iterator();
		Iterable<SearchHit> iterable = () -> iterator;
		return StreamSupport.stream(iterable.spliterator(), true).map(hit -> fromFields(UUID.fromString(hit.getId()), hit.getSource())).collect(Collectors.toSet());
		
	}

	private HouseMessage fromFields(UUID id, Map<String, Object> source) {
		return new HouseMessage(id, source);
	}

	public void save(Set<HouseMessage> values) {
		if(values.isEmpty()){
			log.trace("nothing to save");
			return;
		}
		BulkRequestBuilder bulkRequest = esClient.prepareBulk();
		values.stream().forEach(v->bulkRequest.add(new IndexRequest(indexName, "houseStatus", v.getId().toString()).source(v.getJSONRepresentationUnChecked())));
		bulkRequest.execute().actionGet();
		
	}

}
