package com.jme3.lostVictories.objectives;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.UUID;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import org.junit.Before;
import org.junit.Test;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.Country;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.Weapon;

public class TransportSquadTest {

	
	private HouseDAO houseDAO;
	private CharacterMessage oldCo;
	private CharacterMessage unit;
	private CharacterDAO characterDAO;

	@Before
	public void setup(){
		oldCo = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null);
		unit = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.PRIVATE, oldCo.getId());
		oldCo.addCharactersUnderCommand(unit);
		oldCo.setLocation(new Vector(100, 1, 1));
		houseDAO = mock(HouseDAO.class);

		characterDAO = mock(CharacterDAO.class);
		when(characterDAO.getCharacter(eq(unit.getId()))).thenReturn(unit);
	}
	
	@Test
	public void testStoreAndRetriveObjective() {
		TransportSquad transportSquad = new TransportSquad(new Vector(100, 1, 1));
		transportSquad.runObjective(oldCo, UUID.randomUUID().toString(), characterDAO, houseDAO, new HashMap<UUID, CharacterMessage>(), new HashMap<>());
		Objective o = transportSquad.issuedOrders.get(oldCo.getId());
		assertNotNull(o);
		assertTrue(o instanceof TravelObjective);
		assertEquals(1, unit.readObjectives().size());

		transportSquad.runObjective(oldCo, UUID.randomUUID().toString(), characterDAO, houseDAO, new HashMap<UUID, CharacterMessage>(), new HashMap<>());
		assertEquals(1, unit.readObjectives().size());
	}

}
