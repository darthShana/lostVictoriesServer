package com.jme3.lostVictories.objectives;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.Vector;

public class SecureSector extends Objective implements CleanupBeforeTransmitting{

	@JsonIgnore
	private static Logger log = Logger.getLogger(SecureSector.class);
	
	Set<UUID> houses = new HashSet<UUID>();
	Vector centre;
	Map<UUID, Objective> issuedOrders = new HashMap<>();
	int deploymentStrength;
    int minimumFightingStrenght;
    SecureSectorState lastState;
    Vector homeBase;
	SecureSectorState state = SecureSectorState.WAIT_FOR_REENFORCEMENTS;
	
	@SuppressWarnings("unused")
	private SecureSector() {}
	
	public SecureSector(Set<HouseMessage> houses, int deploymentStrength, int minimumFightingStrenght, Vector homeBase) {
		this.deploymentStrength = deploymentStrength;
		this.minimumFightingStrenght = minimumFightingStrenght;
		this.homeBase = homeBase;
		this.houses = houses.stream().map(h->h.getId()).collect(Collectors.toSet());
		float totalX = 0, totalY = 0,totalZ = 0;
		for(HouseMessage h:houses){
            totalX+=h.getLocation().x;
            totalY+=h.getLocation().y;
            totalZ+=h.getLocation().z;
        }
        final float x = totalX/houses.size();
        final float y = totalY/houses.size();
        final float z = totalZ/houses.size();
        centre = new Vector(x, y, z);
        log.trace("securing sector:"+centre+" with houses:"+houses.size());
	}

	@Override
	public void runObjective(CharacterMessage c, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills) {
		state.runObjective(c, uuid, this, characterDAO, houseDAO, toSave, kills);
		SecureSectorState newState = state.tansition(c, uuid, this, characterDAO, houseDAO, toSave);
		
		if(newState!=state){
            System.out.println(c.getCountry()+" "+c.getRank()+":"+c.getId()+" new state:"+newState+" houses:"+houses.size()+" centre:"+centre+" loc:"+c.getLocation()+" home:"+homeBase);
			issuedOrders.clear();          
            state = newState;            
        }
		
	}

	@Override
	public boolean clashesWith(Class<? extends Objective> newObjective) {
		return true;
	}

	@Override
	public void cleanupBeforeTransmitting() {
		houses.clear();
		issuedOrders.clear();
	}
}
