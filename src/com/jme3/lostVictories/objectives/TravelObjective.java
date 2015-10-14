package com.jme3.lostVictories.objectives;

import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;

public class TravelObjective extends Objective{
	
	private static Logger log = Logger.getLogger(TravelObjective.class);
	
	private Vector facePoint;
    private Vector source;
    private Vector destination;
	@Override
	public void runObjective(CharacterMessage character, String objectiveId, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
		Vector c = character.getLocation();
		Vector3f currentLocation = new Vector3f(c.x, c.y, c.z);
		Vector3f dest = new Vector3f(destination.x, destination.y, destination.z);
		Vector3f newLocation = currentLocation.add(dest.subtract(currentLocation).normalize().mult(10));
		
		if(currentLocation.distance(newLocation)>currentLocation.distance(dest)){
			newLocation = dest;
			character.getObjectives().remove(objectiveId);
			log.debug("complted travel objective");
		}
		
		Vector vector = new Vector(newLocation.x, newLocation.y, newLocation.z);
		character.setLocation(vector);
		toSave.put(character.getId(), character);
	}

}
