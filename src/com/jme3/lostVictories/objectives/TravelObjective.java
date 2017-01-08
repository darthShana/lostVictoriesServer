package com.jme3.lostVictories.objectives;

import static com.jme3.lostVictories.network.messages.LostVictoryScene.SCENE_SCALE;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ObjectNode;

import lostVictories.NavMeshStore;
import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;

public class TravelObjective extends Objective{
	
	private static Logger log = Logger.getLogger(TravelObjective.class);
	
	private Vector facePoint;
    List<Vector> path;
    private Vector destination;
    
    private TravelObjective(){}
    
    public TravelObjective(Vector destination, Vector facePoint) {
		this.facePoint = facePoint;
    	this.destination = destination;
	}
    
	@Override
	public void runObjective(CharacterMessage character, String objectiveId, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
		Vector c = character.getLocation();
		Vector3f currentLocation = new Vector3f(c.x, c.y, c.z);
		
		if(path == null){
	    	path = NavMeshStore.intstace().findPath(character.getLocation(), destination);
		}
		if(path == null){
			isComplete = true;
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
		character.setLocation(new Vector(newLocation));
		toSave.put(character.getId(), character);
		
	}
	
	public String asJSON() throws JsonGenerationException, JsonMappingException, IOException {
        ObjectNode node = MAPPER.createObjectNode();
        JsonNode d = MAPPER.valueToTree(destination);
        node.put("destination", d);
        if(facePoint!=null){
        	JsonNode f = MAPPER.valueToTree(facePoint);
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
