package com.jme3.lostVictories.objectives;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ObjectNode;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.jme3.lostVictories.network.messages.CharacterMessage;

public class CaptureStructure extends Objective{

	String structure;
	
	public CaptureStructure(String structure){
		this.structure = structure;
	}
	
	private CaptureStructure() {
	}

	@Override
	public void runObjective(CharacterMessage c, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
		
	}
	
	public String asJSON() throws JsonGenerationException, JsonMappingException, IOException{
		ObjectNode node = MAPPER.createObjectNode();
        node.put("structure", structure);
        node.put("classType", getClass().getName());

        return MAPPER.writeValueAsString(node);
	}
}
