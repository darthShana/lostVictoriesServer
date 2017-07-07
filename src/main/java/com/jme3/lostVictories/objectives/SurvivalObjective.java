package com.jme3.lostVictories.objectives;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.log4j.Logger;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.Vector;

public class SurvivalObjective extends Objective implements PassiveObjective{

	@JsonIgnore
	private static Logger log = Logger.getLogger(SurvivalObjective.class);
	
	@Override
	public void runObjective(CharacterMessage c, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills) {
		Vector v = c.getLocation();
		Optional<CharacterMessage> victim = characterDAO.getAllCharacters(v.x, v.y, v.z, 15).stream().filter(other->other.getCountry()!=c.getCountry()).findAny();

		if(victim.isPresent()){
			kills.put(c.getId(), victim.get().getId());
		}

	}

	@Override
	public boolean clashesWith(Class<? extends Objective> newObjective) {
		return false;
	}

}
