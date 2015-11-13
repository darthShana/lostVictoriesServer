package com.jme3.lostVictories.objectives;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ObjectNode;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;

public class FollowUnit extends Objective{
	
	UUID unitToFollow;
    Vector direction;
    int maxDistance;
    
	@Override
	public void runObjective(CharacterMessage character, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
		Vector c = character.getLocation();
		Vector3f currentLocation = new Vector3f(c.x, c.y, c.z);
		
		CharacterMessage toFollow = characterDAO.getCharacter(unitToFollow);
		if(toFollow==null){
			isComplete = true;
			return;
		}
		
		Vector3f dest = toFollow.getLocation().toVector();
		Vector3f newLocation = currentLocation.add(dest.subtract(currentLocation).normalize().mult(5));
		
		Vector vector = new Vector(newLocation.x, newLocation.y, newLocation.z);
		character.setLocation(vector);
		toSave.put(character.getId(), character);
	}
	@Override
	public String asJSON() throws JsonGenerationException, JsonMappingException, IOException {
		ObjectNode node = MAPPER.createObjectNode();
		JsonNode d = MAPPER.valueToTree(direction);
		node.put("unitToFollow", unitToFollow.toString());
        node.put("direction", d);
        node.put("maxDistance", maxDistance);
        node.put("classType", getClass().getName());
        return MAPPER.writeValueAsString(node);
	}
	
	@Override
	public boolean clashesWith(Class<? extends Objective> newObjective) {
		return newObjective.isAssignableFrom(TravelObjective.class);
	}
    

}
