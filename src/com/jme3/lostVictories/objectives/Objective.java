package com.jme3.lostVictories.objectives;

import static com.jme3.lostVictories.objectives.Objective.MAPPER;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
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
		return unit.isDead() || unit.getObjectives().values().stream()
				.map(s->toJsonNodeSafe(s))
				.map(json->toObjectiveSafe(json))
				.filter(o->o!=null)
			.anyMatch(o->!(o instanceof PassiveObjective));
	}
	
	

	private Objective toObjectiveSafe(JsonNode json) {
		
		try {
			Class objectiveClass;
			objectiveClass = Class.forName(json.get("classType").asText());
			Objective objective = (Objective) MAPPER.treeToValue(json, objectiveClass);
			return objective;
		} catch (ClassNotFoundException e) {
			//not all objectives are present lets assume these are passive
			return null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
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
