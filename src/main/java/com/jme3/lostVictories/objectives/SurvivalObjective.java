package com.jme3.lostVictories.objectives;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SurvivalObjective extends Objective implements PassiveObjective{

	@JsonIgnore
	private static Logger log = LoggerFactory.getLogger(SurvivalObjective.class);
	
	@Override
	public void runObjective(CharacterMessage c, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills) {
		Vector v = c.getLocation();
		Optional<CharacterMessage> victim = characterDAO.getAllCharacters(v.x, v.y, v.z, 5).stream()
                .filter(other->!other.isDead())
                .filter(other->other.getCountry()!=c.getCountry()).findAny();

//		what is the victim is a vehicle..
//		what if the person is inside a vehicle
		if(victim.isPresent() && Math.random()>.5){
			System.out.println(c.getCountry()+":"+c.getId()+" at loc:"+c.getLocation()+" checkout:"+c.getCheckoutClient()+" killed:"+victim.get().getId()+" at loc:"+victim.get().getLocation());
			kills.put(c.getId(), victim.get().getId());
		}

	}

	@Override
	public boolean clashesWith(Class<? extends Objective> newObjective) {
		return false;
	}

}
