package lostVictories.dao;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jme3.lostVictories.network.messages.TreeGroupMessage;

public class TreeDAO {

	private Client esClient;
	private String indexName;

	public TreeDAO(Client esClient, String indexName) {
		this.esClient = esClient;
		this.indexName = indexName;
	}
	
	public Set<TreeGroupMessage> getAllTrees() {
		SearchResponse searchResponse = esClient.prepareSearch(indexName)
                .setQuery(matchAllQuery()).setSize(10000)
                .execute().actionGet();
		
		Iterator<SearchHit> iterator = searchResponse.getHits().iterator();
		Iterable<SearchHit> iterable = () -> iterator;
		return StreamSupport.stream(iterable.spliterator(), true).map(hit -> fromFields(UUID.fromString(hit.getId()), hit.getSource())).collect(Collectors.toSet());
		
	}
	
	private TreeGroupMessage fromFields(UUID id, Map<String, Object> source) {
		return new TreeGroupMessage(id, source);
	}

	public void putTree(UUID uuid, TreeGroupMessage t) {
		try {
			esClient.prepareIndex(indexName, "treeStatus", uuid.toString())
			        .setSource(CharacterDAO.MAPPER.writeValueAsString(t))
			        .execute()
			        .actionGet();
		} catch (ElasticsearchException | JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
