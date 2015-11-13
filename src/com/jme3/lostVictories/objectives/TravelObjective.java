package com.jme3.lostVictories.objectives;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ObjectNode;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;

public class TravelObjective extends Objective{
	
	private static Logger log = Logger.getLogger(TravelObjective.class);
	
	private Vector facePoint;
    private Vector destination;
    
    private TravelObjective(){}
    
    public TravelObjective(Vector destination, Vector facePoint) {
    	this.destination = destination;
		this.facePoint = facePoint;
	}
    
	@Override
	public void runObjective(CharacterMessage character, String objectiveId, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
		Vector c = character.getLocation();
		Vector3f currentLocation = new Vector3f(c.x, c.y, c.z);
		Vector3f dest = new Vector3f(destination.x, destination.y, destination.z);
		Vector3f newLocation = currentLocation.add(dest.subtract(currentLocation).normalize().mult(5));
		
		if(currentLocation.distance(newLocation)>currentLocation.distance(dest)){
			newLocation = dest;
			isComplete = true;
		}
		
		Vector vector = new Vector(newLocation.x, newLocation.y, newLocation.z);
		character.setLocation(vector);
		toSave.put(character.getId(), character);
	}
	
	public String asJSON() throws JsonGenerationException, JsonMappingException, IOException {
        ObjectNode node = MAPPER.createObjectNode();
        JsonNode d = MAPPER.valueToTree(destination);
        JsonNode f = MAPPER.valueToTree(facePoint);
        node.put("destination", d);
        node.put("facePoint", f);
        node.put("classType", getClass().getName());

        return MAPPER.writeValueAsString(node);
    }
	
	@Override
	public boolean clashesWith(Class<? extends Objective> newObjective) {
		return newObjective.isAssignableFrom(FollowUnit.class);
	}

}
