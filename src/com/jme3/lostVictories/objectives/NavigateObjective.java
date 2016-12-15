package com.jme3.lostVictories.objectives;

import static com.jme3.lostVictories.network.messages.LostVictoryScene.SCENE_SCALE;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ObjectNode;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;

public class NavigateObjective extends Objective{

	private Vector target;
    private Vector destination;
	
    private NavigateObjective() {}
    
    public NavigateObjective(Vector destination, Vector target) {
		this.destination = destination;
		this.target = target;
	}
    
	@Override
	public void runObjective(CharacterMessage character, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
		Vector c = character.getLocation();
		Vector3f currentLocation = new Vector3f(c.x, c.y, c.z);
		Vector3f dest = new Vector3f(destination.x, destination.y, destination.z);
		Vector3f newLocation = currentLocation.add(dest.subtract(currentLocation).normalize().mult(10*SCENE_SCALE));
		
		if(currentLocation.distance(newLocation)>currentLocation.distance(dest)){
			newLocation = dest;
			isComplete = true;
		}
		
		Vector vector = new Vector(newLocation.x, newLocation.y, newLocation.z);
		character.setLocation(vector);
		
		Set<CharacterMessage> collect = character.getPassengers().stream().map(id->characterDAO.getCharacter(id)).collect(Collectors.toSet());
		collect.forEach(passenger->passenger.setLocation(vector));
		collect.add(character);
		collect.forEach(moved->toSave.put(moved.getId(), moved));
	}

	@Override
	public String asJSON() throws JsonGenerationException, JsonMappingException, IOException {
		ObjectNode node = MAPPER.createObjectNode();
        JsonNode d = MAPPER.valueToTree(destination);
        node.put("destination", d);
        if(target!=null){
        	JsonNode f = MAPPER.valueToTree(target);
        	node.put("facePoint", f);
        }
        node.put("classType", getClass().getName());

        return MAPPER.writeValueAsString(node);
	}

	@Override
	public boolean clashesWith(Class<? extends Objective> newObjective) {
		return newObjective.isAssignableFrom(FollowUnit.class);
	}

}
