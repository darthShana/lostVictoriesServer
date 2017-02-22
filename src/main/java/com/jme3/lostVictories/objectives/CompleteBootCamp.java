package com.jme3.lostVictories.objectives;

import java.util.Map;
import java.util.UUID;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.jme3.lostVictories.network.messages.CharacterMessage;

public class CompleteBootCamp extends Objective implements PassiveObjective{

	@Override
	public void runObjective(CharacterMessage c, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {

	}

	
	@Override
	public boolean clashesWith(Class<? extends Objective> newObjective) {
		return true;
	}

}
