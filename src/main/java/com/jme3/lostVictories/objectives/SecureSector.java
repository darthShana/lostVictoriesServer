package com.jme3.lostVictories.objectives;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.jme3.lostVictories.network.messages.BunkerMessage;
import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecureSector extends Objective implements CleanupBeforeTransmitting{

	@JsonIgnore
	private static Logger log = LoggerFactory.getLogger(SecureSector.class);
	
	Set<UUID> houses = new HashSet<>();
	Set<UUID> bunkers = new HashSet<>();
	Vector centre;
    @JsonIgnore
	Rectangle.Float boundary;
	Map<UUID, UUID> issuedOrders = new HashMap<>();
	int deploymentStrength;
    int minimumFightingStrength;
    Vector homeBase;
	SecureSectorState state = SecureSectorState.WAIT_FOR_REENFORCEMENTS;
	Objective embededObjective;
    public Long securedHouseCount;

    @SuppressWarnings("unused")
	private SecureSector() {}
	
	public SecureSector(Set<HouseMessage> houses, Set<BunkerMessage> bunkers, int deploymentStrength, int minimumFightingStrength, Vector homeBase) {
		this.deploymentStrength = deploymentStrength;
		this.minimumFightingStrength = minimumFightingStrength;
		this.homeBase = homeBase;
		this.houses = houses.stream().map(h->h.getId()).collect(Collectors.toSet());
		this.bunkers = bunkers.stream().map(b->b.getId()).collect(Collectors.toSet());
        calculateBoundry(houses);
        log.trace("securing sector:"+centre+" with houses:"+houses.size());
	}

    private void calculateBoundry(Set<HouseMessage> houses) {
	    float totalX = 0, totalY = 0,totalZ = 0;
        Float minX = null, minY = null ,minZ = null;
        Float maxX = null , maxY = null ,maxZ = null;
        for(HouseMessage h:houses){
            totalX+=h.getLocation().x;
            totalY+=h.getLocation().y;
            totalZ+=h.getLocation().z;
            minX = (minX==null || h.getLocation().x<minX)?h.getLocation().x:minX;
            minY = (minY==null || h.getLocation().y<minY)?h.getLocation().y:minY;
            minZ = (minZ==null || h.getLocation().z<minZ)?h.getLocation().z:minZ;
            maxX = (maxX==null || h.getLocation().x>maxX)?h.getLocation().x:maxX;
            maxY = (maxY==null || h.getLocation().y>maxY)?h.getLocation().y:maxY;
            maxZ = (maxZ==null || h.getLocation().z>maxZ)?h.getLocation().z:maxZ;
        }
        final float x = totalX/houses.size();
        final float y = totalY/houses.size();
        final float z = totalZ/houses.size();
        centre = new Vector(x, y, z);
        boundary = new Rectangle.Float(minX, minZ, (maxX-minX), (maxZ-minZ));
    }

    @Override
	public void runObjective(CharacterMessage c, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills) {

		if(boundary == null){
	        calculateBoundry(houses.stream().map(h->houseDAO.getHouse(h)).collect(Collectors.toSet()));
        }
        HashMap<UUID, CharacterMessage> toSave1 = new HashMap<>();
        state.runObjective(c, uuid, this, characterDAO, houseDAO, toSave1, kills);
        try {
            characterDAO.save(toSave1.values());
        } catch (IOException e) {
            new RuntimeException(e);
        }
        SecureSectorState newState = state.transition(c, uuid, this, characterDAO, houseDAO, toSave);
		
		if(newState!=state){
            System.out.println(c.getCountry()+" "+c.getRank()+":"+c.getId()+" new state:"+newState+" houses:"+houses.size()+" centre:"+centre+" loc:"+c.getLocation()+" home:"+homeBase);
			issuedOrders.clear();
            embededObjective = null;
            state = newState;
            securedHouseCount = null;
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
