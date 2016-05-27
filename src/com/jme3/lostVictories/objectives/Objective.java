package com.jme3.lostVictories.objectives;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.DeserializationConfig;

import com.jme3.lostVictories.network.messages.CharacterMessage;

public abstract class Objective {
	
	public static ObjectMapper MAPPER;
	
    static{
            MAPPER = new ObjectMapper();
            MAPPER.setVisibility(JsonMethod.FIELD, Visibility.ANY);
            MAPPER.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

	public boolean isComplete = false;

	public abstract void runObjective(CharacterMessage c, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave);
    
	public abstract String asJSON() throws JsonGenerationException, JsonMappingException, IOException;
	
	public boolean isBusy(CharacterMessage unit) {
		return unit.isDead() || unit.getObjectives().values().stream().map(s->toJsonNodeSafe(s)).anyMatch(n->!isPassiveObjective(n));
	}
	
	private boolean isPassiveObjective(JsonNode n) {
		String s = n.get("classType").asText();
		return "com.jme3.lostVictories.objectives.SurvivalObjective".equals(s) || "com.jme3.lostVictories.objectives.RemanVehicle".equals(s) || "com.jme3.lostVictories.objectives.FollowUnit".equals(s);
	}

	private JsonNode toJsonNodeSafe(String s) {
		try {
			return MAPPER.readTree(s);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public abstract boolean clashesWith(Class<? extends Objective> newObjective);

}
