package com.jme3.lostVictories.objectives;

import static com.jme3.lostVictories.network.messages.LostVictoryScene.SCENE_SCALE;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lostVictories.NavMeshStore;
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
	List<Vector> path;
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
		
		if(path == null){
	    	path = NavMeshStore.intstace().findPath(character.getLocation(), destination);
		}
		if(path == null){
			return;
		}
		
		if(!path.isEmpty() && currentLocation.distance(path.get(0).toVector())<1){
			path.remove(0);
		}
		if(path.isEmpty()){
			isComplete = true;
			return;
		}
				
		Vector3f newLocation = currentLocation.add(path.get(0).toVector().subtract(currentLocation).normalize().mult(10*SCENE_SCALE));
		if(currentLocation.distance(newLocation)>currentLocation.distance(path.get(0).toVector())){
			newLocation = path.get(0).toVector();
		}
		final Vector v = new Vector(newLocation);
		character.setLocation(v);
		
		Set<CharacterMessage> collect = character.getPassengers().stream().map(id->characterDAO.getCharacter(id)).collect(Collectors.toSet());
		collect.forEach(passenger->passenger.setLocation(v));
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
        if(path!=null){
        	JsonNode f = MAPPER.valueToTree(path);
        	node.put("path", f);
        }
        node.put("classType", getClass().getName());

        return MAPPER.writeValueAsString(node);
	}

	@Override
	public boolean clashesWith(Class<? extends Objective> newObjective) {
		return newObjective.isAssignableFrom(FollowCommander.class);
	}

}
