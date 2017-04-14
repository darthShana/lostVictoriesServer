package com.jme3.lostVictories.objectives;

import static com.jme3.lostVictories.network.messages.LostVictoryScene.SCENE_SCALE;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import lostVictories.NavMeshStore;
import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;

public class TravelObjective extends Objective implements CleanupBeforeTransmitting{
	@JsonIgnore
	private static Logger log = Logger.getLogger(TravelObjective.class);
	
	private Vector facePoint;
    List<Vector> path;
    Vector destination;
    
    @SuppressWarnings("unused")
	private TravelObjective(){}
    
    public TravelObjective(CharacterMessage character, Vector destination, Vector facePoint) {
    	if(character.getCharacterType()!=CharacterType.SOLDIER && character.getCharacterType()!=CharacterType.AVATAR){
    		new RuntimeException().printStackTrace();
    	}
		this.facePoint = facePoint;
    	this.destination = destination;
	}
    
	@Override
	public void runObjective(CharacterMessage character, String objectiveId, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills) {
		Vector c = character.getLocation();
		Vector3f currentLocation = new Vector3f(c.x, c.y, c.z);
		if(currentLocation.distance(destination.toVector())<1){
			isComplete = true;
			return;
		}
		
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
	}
	
	@Override
	public boolean clashesWith(Class<? extends Objective> newObjective) {
		if(newObjective.isAssignableFrom(CaptureStructure.class)){
			return true;
		}
		if(newObjective.isAssignableFrom(TransportSquad.class)){
			return true;
		}
		if(newObjective.isAssignableFrom(FollowCommander.class)){
			return true;
		}

		return false;
	}

	@Override
	public void cleanupBeforeTransmitting() {
		path = null;
	}
}
