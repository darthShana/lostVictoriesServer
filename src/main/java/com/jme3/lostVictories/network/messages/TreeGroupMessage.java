package com.jme3.lostVictories.network.messages;

import static com.jme3.lostVictories.network.messages.CharacterMessage.toLatitute;
import static com.jme3.lostVictories.network.messages.CharacterMessage.toLongitude;
import static com.jme3.lostVictories.network.messages.Vector.latLongToVector;
import static lostVictories.dao.CharacterDAO.MAPPER;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import lostVictories.dao.CharacterDAO;



import com.fasterxml.jackson.core.type.TypeReference;
import redis.clients.jedis.GeoCoordinate;

public class TreeGroupMessage implements Serializable{

	private UUID id;
	private Vector location;
	private Set<TreeMessage> trees = new HashSet<>();
	
	private TreeGroupMessage(){}
	
	public TreeGroupMessage(Map<String, String> source, GeoCoordinate geoCoordinate) {
        this.id = UUID.fromString(source.get("id"));
        float altitude = Float.parseFloat(source.get("altitude"));

        this.location = latLongToVector(altitude, (float) geoCoordinate.getLongitude(), (float) geoCoordinate.getLatitude());
		try {
			this.trees = CharacterDAO.MAPPER.readValue((String)source.get("trees"), new TypeReference<Set<TreeMessage>>() {});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Map<String, String> getMapRepresentation() throws IOException {
	    Map<String, String> ret = new HashMap<>();

		ret.put("id", id+"");
		ret.put("altitude", location.y+"");
        ret.put("trees", MAPPER.writerFor(new TypeReference<Set<TreeMessage>>() {}).writeValueAsString(trees));
        return ret;
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
