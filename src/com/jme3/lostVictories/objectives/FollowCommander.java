package com.jme3.lostVictories.objectives;

import static com.jme3.lostVictories.network.messages.LostVictoryScene.SCENE_SCALE;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;

public class FollowCommander extends Objective implements PassiveObjective{
	
    Vector direction;
    int maxDistance;
    
    private FollowCommander() {}
    
    public FollowCommander(Vector direction, int maxDistance){
		this.direction = direction;
		this.maxDistance = maxDistance;
    }
    
	@Override
	public void runObjective(CharacterMessage character, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
		Vector c = character.getLocation();
		Vector3f currentLocation = new Vector3f(c.x, c.y, c.z);
		
		CharacterMessage toFollow = characterDAO.getCharacter(character.getCommandingOfficer());
		if(toFollow==null){
			isComplete = true;
			return;
		}
		
		Vector3f dest = toFollow.getLocation().toVector();
		Vector3f newLocation = currentLocation.add(dest.subtract(currentLocation).normalize().mult(10*SCENE_SCALE));
		
		Vector vector = new Vector(newLocation.x, newLocation.y, newLocation.z);
		character.setLocation(vector);
	}
	
	@Override
	public boolean clashesWith(Class<? extends Objective> newObjective) {
		return newObjective.isAssignableFrom(TravelObjective.class);
	}
    

}
