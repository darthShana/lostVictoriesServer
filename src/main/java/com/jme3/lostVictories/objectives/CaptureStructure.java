package com.jme3.lostVictories.objectives;


import java.util.Map;
import java.util.UUID;


import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaptureStructure extends Objective{
	@JsonIgnore
	private static Logger log = LoggerFactory.getLogger(CaptureStructure.class);
	String structure;
	Objective travelObjective;
	
	public CaptureStructure(String structure){
		this.structure = structure;
	}
	
	@SuppressWarnings("unused")
	private CaptureStructure() {}

	@Override
	public void runObjective(CharacterMessage c, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills) {
		HouseMessage house = houseDAO.getHouse(UUID.fromString(structure));
		if(house.getOwner()==c.getCountry()){
			log.debug("completed stucture capture:"+structure);
			isComplete = true;
			return;
		}

		if(travelObjective==null){
			if(CharacterType.AVATAR == c.getCharacterType() || CharacterType.SOLDIER == c.getCharacterType()){
				travelObjective = new TravelObjective(c, new Vector(house.getLocation().toVector()), null);
			}else{
				travelObjective = new NavigateObjective(new Vector(house.getLocation().toVector()), null);
			}
		}
		travelObjective.runObjective(c, uuid, characterDAO, houseDAO, toSave, kills);
		if(travelObjective.isComplete){
			isComplete = true;
		}
		
	}
	


	@Override
	public boolean clashesWith(Class<? extends Objective> newObjective) {
		if(newObjective.isAssignableFrom(TravelObjective.class)){
			return true;
		}
		if(newObjective.isAssignableFrom(TransportSquad.class)){
			return true;
		}
		return false;
	}
}
