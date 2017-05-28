package com.jme3.lostVictories.network.messages;

import static com.jme3.lostVictories.network.messages.CharacterMessage.toLatitute;
import static com.jme3.lostVictories.network.messages.CharacterMessage.toLongitude;
import static com.jme3.lostVictories.network.messages.Vector.latLongToVector;
import static lostVictories.dao.CharacterDAO.MAPPER;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import lostVictories.dao.CharacterDAO;

import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.xcontent.XContentBuilder;

import com.fasterxml.jackson.core.type.TypeReference;

public class TreeGroupMessage implements Serializable{

	private UUID id;
	private Vector location;
	private Set<TreeMessage> trees = new HashSet<>();
	
	private TreeGroupMessage(){}
	
	public TreeGroupMessage(UUID id, Map<String, Object> source) {
		this.id = id;
		HashMap<String, Double> loc =  (HashMap<String, Double>) source.get("location");
		float altitude = ((Double)source.get("altitude")).floatValue();
		this.location = latLongToVector(altitude, loc.get("lon").floatValue(), loc.get("lat").floatValue());
		try {
			this.trees = CharacterDAO.MAPPER.readValue((String)source.get("trees"), new TypeReference<Set<TreeMessage>>() {});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public XContentBuilder getJSONRepresentation() throws IOException {
		return jsonBuilder()
	            .startObject()
	                .field("location", new GeoPoint(toLatitute(location), toLongitude(location)))
	                .field("altitude", location.y)
	                .field("trees", MAPPER.writerFor(new TypeReference<Set<TreeMessage>>() {}).writeValueAsString(trees))
	            .endObject();
	}

	public UUID getId() {
		return id;
	}
	
	public void setId(UUID id){
		this.id = id;
	}

	public Vector getLocation() {
		return location;
	}

	public Set<TreeMessage> getTrees() {
		return trees;
	}
}
