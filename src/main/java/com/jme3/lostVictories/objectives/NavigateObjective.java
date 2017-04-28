package com.jme3.lostVictories.objectives;

import static com.jme3.lostVictories.network.messages.LostVictoryScene.SCENE_SCALE;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lostVictories.NavMeshStore;
import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;

public class NavigateObjective extends Objective implements CleanupBeforeTransmitting{

	private Vector target;
	List<Vector> path;
    private Vector destination;
	
    @SuppressWarnings("unused")
	private NavigateObjective() {}
    
    public NavigateObjective(Vector destination, Vector target) {
		this.destination = destination;
		this.target = target;
	}
    
	@Override
	public void runObjective(CharacterMessage character, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills) {
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
		final Vector vector = new Vector(newLocation);
		character.setLocation(vector);
		
		Set<CharacterMessage> collect = character.getPassengers().stream().map(id->characterDAO.getCharacter(id)).filter(cc->cc!=null).collect(Collectors.toSet());
		collect.forEach(passenger->passenger.setLocation(vector));
		collect.forEach(moved->toSave.put(moved.getId(), moved));
	}

	@Override
	public boolean clashesWith(Class<? extends Objective> newObjective) {
		return newObjective.isAssignableFrom(FollowCommander.class);
	}

	@Override
	public void cleanupBeforeTransmitting() {
		path = null;
	}
}
