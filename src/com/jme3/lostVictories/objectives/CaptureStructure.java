package com.jme3.lostVictories.objectives;


import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ObjectNode;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.Vector;

public class CaptureStructure extends Objective{

	private static Logger log = Logger.getLogger(CaptureStructure.class);
	String structure;
	TravelObjective travelObjective;
	
	public CaptureStructure(String structure){
		this.structure = structure;
	}
	
	@SuppressWarnings("unused")
	private CaptureStructure() {}

	@Override
	public void runObjective(CharacterMessage c, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
		HouseMessage house = houseDAO.getHouse(UUID.fromString(structure));
		if(house.getOwner()==c.getCountry()){
			log.debug("completed stucture capture:"+structure);
			isComplete = true;
			return;
		}
			
		if(travelObjective==null){
			travelObjective = new TravelObjective(new Vector(house.getLocation().toVector()), null);
		}
		travelObjective.runObjective(c, uuid, characterDAO, houseDAO, toSave);
		if(travelObjective.isComplete){
			isComplete = true;
		}
		
	}
	
	public String asJSON() throws JsonGenerationException, JsonMappingException, IOException{
		ObjectNode node = MAPPER.createObjectNode();
        node.put("structure", structure);
        node.put("classType", getClass().getName());
        if(travelObjective!=null){
        	JsonNode _travelObjective = MAPPER.valueToTree(travelObjective);
        	node.put("travelObjective", _travelObjective);
        }
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
