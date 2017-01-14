package com.jme3.lostVictories.objectives;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.UUID;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.Country;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.Weapon;

public class ObjectiveTest {

	@Test
	public void testIsBusy() throws IOException, JsonMappingException, IOException {
		CharacterMessage character = new CharacterMessage(UUID.randomUUID(), CharacterType.AVATAR, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null);

		TravelObjective objective = new TravelObjective(new Vector(100, 0, 100), null);
		assertFalse(character.isBusy());

		FollowCommander obj2 = new FollowCommander(new Vector(0, 0, 0), 3);
		character.addObjective(UUID.randomUUID(), obj2);
		assertFalse(character.isBusy());
		
		character.addObjective(UUID.randomUUID(), objective);
		assertTrue(character.isBusy());
	}

}
