package com.jme3.lostVictories.objectives;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ObjectNode;

import com.jme3.lostVictories.network.messages.CharacterMessage;

public class CompleteBootCamp extends Objective {

	@Override
	public void runObjective(CharacterMessage c, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {

	}

	public String asJSON() throws JsonGenerationException, JsonMappingException, IOException{
		ObjectNode node = MAPPER.createObjectNode();
        node.put("classType", getClass().getName());
        return MAPPER.writeValueAsString(node);
	}
	
	@Override
	public boolean clashesWith(Class<? extends Objective> newObjective) {
		return true;
	}

}
