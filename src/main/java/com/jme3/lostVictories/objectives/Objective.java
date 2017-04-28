package com.jme3.lostVictories.objectives;

import static lostVictories.dao.CharacterDAO.MAPPER;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.jme3.lostVictories.network.messages.CharacterMessage;


@JsonTypeInfo(use= Id.CLASS, include= JsonTypeInfo.As.PROPERTY, property="class")
public abstract class Objective {

	public boolean isComplete = false;

	public abstract void runObjective(CharacterMessage c, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills);	
	
	public static Objective toObjectiveSafe(JsonNode json) {
		
		try {
			Class objectiveClass;
			objectiveClass = Class.forName(json.get("class").asText());
			Objective objective = (Objective) MAPPER.treeToValue(json, objectiveClass);
			return objective;
		} catch (ClassNotFoundException e) {
			//not all objectives are present lets assume these are passive
			return null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

	public static JsonNode toJsonNodeSafe(String s) {
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
