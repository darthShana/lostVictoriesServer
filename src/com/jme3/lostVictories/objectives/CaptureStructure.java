package com.jme3.lostVictories.objectives;

import static lostVictories.LostVictoryScene.SCENE_SCALE;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ObjectNode;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;

public class CaptureStructure extends Objective{

	private static Logger log = Logger.getLogger(CaptureStructure.class);
	String structure;
	
	public CaptureStructure(String structure){
		this.structure = structure;
	}
	
	private CaptureStructure() {}

	@Override
	public void runObjective(CharacterMessage c, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
		HouseMessage house = houseDAO.getHouse(UUID.fromString(structure));
		if(house.getOwner()==c.getCountry()){
			log.debug("completed stucture capture:"+structure);
			isComplete = true;
			return;
		}
			
		TravelObjective tt = new TravelObjective(new Vector(house.getLocation().toVector()), null);
		tt.runObjective(c, uuid, characterDAO, houseDAO, toSave);
		if(tt.isComplete){
			isComplete = true;
		}
		
	}
	
	public String asJSON() throws JsonGenerationException, JsonMappingException, IOException{
		ObjectNode node = MAPPER.createObjectNode();
        node.put("structure", structure);
        node.put("classType", getClass().getName());

        return MAPPER.writeValueAsString(node);
	}

	@Override
	public boolean clashesWith(Class<? extends Objective> newObjective) {
		if(newObjective.isAssignableFrom(TravelObjective.class)){
			return true;
		}
		return false;
	}
}
