package lostVictories.dao;


import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.jme3.lostVictories.network.messages.BunkerMessage;
import com.jme3.lostVictories.network.messages.Vector;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;

import com.jme3.lostVictories.network.messages.HouseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

public class HouseDAO {
	private static Logger log = LoggerFactory.getLogger(HouseDAO.class);

	private Client esClient;
	private String indexName;

	private List<BunkerMessage> bunkers = new ArrayList<>();


	public HouseDAO(Client esClient, String indexName) {
		this.esClient = esClient;
		this.indexName = indexName;
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(-250.0f, 96.289536f, -225.11862f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(-342.9169f, 96.32557f, -144.11838f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(57.210407f, 100.232506f, -270.88477f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(99.83392f, 96.43404f, 95.658516f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(336.1358f, 95.88243f, 66.05069f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(-96.58569f, 97.62429f, 231.24171f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(204.1758f, 102.116066f, -24.266691f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(137.88399f, 100.28509f, -270.88477f)));
	}
	
	public void putHouse(UUID uuid, HouseMessage house) {
		try {
			esClient.prepareIndex(indexName, "houseStatus", uuid.toString())
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

	public HouseMessage getHouse(UUID id) {
		GetResponse response = esClient.prepareGet(indexName, "houseStatus", id.toString())
		        .execute()
		        .actionGet();
		if(!response.isExists()){
			return null;
		}
		return fromFields(UUID.fromString(response.getId()), response.getSource());
	}

    public List<BunkerMessage> getAllBunkers() {
        return bunkers;
    }

    public List<BunkerMessage> getBunkers(Set<UUID> ids) {
	    return bunkers.stream().filter(b->ids.contains(b.getId())).collect(Collectors.toList());
    }
}
