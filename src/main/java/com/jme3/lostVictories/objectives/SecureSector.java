package com.jme3.lostVictories.objectives;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.jme3.lostVictories.network.messages.*;
import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecureSector extends Objective implements CleanupBeforeTransmitting{

	@JsonIgnore
	private static Logger log = LoggerFactory.getLogger(SecureSector.class);
	
	UUID sectorId;
	Vector centre;
	Map<UUID, UUID> issuedOrders = new HashMap<>();
	int deploymentStrength;
    int minimumFightingStrength;
    Vector homeBase;
	SecureSectorState state = SecureSectorState.WAIT_FOR_REENFORCEMENTS;
	Objective embededObjective;
    public Long securedHouseCount;

    @JsonIgnore
    Rectangle2D.Float boundary;

    @SuppressWarnings("unused")
	private SecureSector() {}
	
	public SecureSector(UUID sectorId, int deploymentStrength, int minimumFightingStrength, Vector homeBase) {
		this.deploymentStrength = deploymentStrength;
		this.minimumFightingStrength = minimumFightingStrength;
		this.homeBase = homeBase;
		this.sectorId = sectorId;
        log.trace("securing sector:"+sectorId);
	}

    private Rectangle2D.Float calculateBoundry(Set<HouseMessage> houses) {
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
        return new Rectangle.Float(minX, minZ, (maxX-minX), (maxZ-minZ));
    }

    @Override
	public void runObjective(CharacterMessage c, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills) {

        GameSector gameSector = houseDAO.getGameSector(sectorId);
        Set<HouseMessage> houses = gameSector.getHouses(houseDAO);

        this.boundary = calculateBoundry(houses);

        HashMap<UUID, CharacterMessage> toSave1 = new HashMap<>();
//        if(Country.AMERICAN == c.getCountry() && houses.size()>25){
//            System.out.println(c.getCountry()+" lieu:"+c.getId()+" at:"+c.getLocation()+" strength:"+c.getCurrentStrength(characterDAO)+" state:"+state);
//        }
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
		issuedOrders.clear();
	}

}
