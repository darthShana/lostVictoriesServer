package com.jme3.lostVictories.objectives;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.Country;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.Quaternion;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.Weapon;

public class SecureSectorTest {

	private HouseDAO houseDAO;
	private HashSet<HouseMessage> houses;
	private CharacterMessage oldCo;
	private CharacterMessage unit1;
	private CharacterMessage unit2;
	private CharacterDAO characterDAO;

	@Before
	public void setup(){
		houseDAO = mock(HouseDAO.class);
		houses = new HashSet<HouseMessage>();
		HouseMessage house1 = new HouseMessage("type2", new Vector(100, 1, 100), new Quaternion(1, 1, 1, 1));
		HouseMessage house2 = new HouseMessage("type2", new Vector(110, 1, 100), new Quaternion(1, 1, 1, 1));
		HouseMessage house3 = new HouseMessage("type2", new Vector(120, 1, 100), new Quaternion(1, 1, 1, 1));
		houses.add(house1);
		houses.add(house2);
		houses.add(house3);
		when(houseDAO.getHouse(eq(house1.getId()))).thenReturn(house1);
		when(houseDAO.getHouse(eq(house2.getId()))).thenReturn(house2);
		when(houseDAO.getHouse(eq(house3.getId()))).thenReturn(house3);
		
		oldCo = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.LIEUTENANT, null);
		unit1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, oldCo.getId());
		unit2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, oldCo.getId());

		oldCo.addCharactersUnderCommand(unit1, unit2);
		
		characterDAO = mock(CharacterDAO.class);
		when(characterDAO.getCharacter(eq(unit1.getId()))).thenReturn(unit1);
		when(characterDAO.getCharacter(eq(unit2.getId()))).thenReturn(unit2);
	}
	
	@Test
	public void testSaveAndRestoreObjective(){
		SecureSector objective = new SecureSector(houses);
		HashMap<UUID, CharacterMessage> toSave = new HashMap<UUID, CharacterMessage>();
		oldCo.setLocation(new Vector(100, 1, 100));

		objective.runObjective(oldCo, UUID.randomUUID().toString(), characterDAO, houseDAO, toSave);
		assertEquals(1, unit1.getObjectives().size());
		objective.runObjective(oldCo, UUID.randomUUID().toString(), characterDAO, houseDAO, toSave);
		assertEquals(1, unit1.getObjectives().size());
	}
	
	@Test
	public void testCompleteDeployPhaseAndChangeToCaptureHouse(){
		SecureSector objective = new SecureSector(houses);
		oldCo.setLocation(new Vector(110, 1, 100));
		HashMap<UUID, CharacterMessage> toSave = new HashMap<UUID, CharacterMessage>();
		objective.runObjective(oldCo, UUID.randomUUID().toString(), characterDAO, houseDAO, toSave);
		
		assertEquals(SecureSectorState.CAPTURE_HOUSES, objective.state);
	}
	
	@Test
	public void testCaptureHouses() {
		SecureSector objective = new SecureSector(houses);
		objective.state = SecureSectorState.CAPTURE_HOUSES;
		HashMap<UUID, CharacterMessage> toSave = new HashMap<UUID, CharacterMessage>();
		
		objective.runObjective(oldCo, UUID.randomUUID().toString(), characterDAO, houseDAO, toSave);
		
		assertTrue(toSave.containsKey(unit1.getId()));
		CaptureStructure order1 = (CaptureStructure) objective.issuedOrders.get(unit1.getId());
		CaptureStructure order2 = (CaptureStructure) objective.issuedOrders.get(unit2.getId());
		
		assertFalse(order1.structure.equals(order2.structure));
		
		CharacterMessage unit3 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, oldCo.getId());
		oldCo.addCharactersUnderCommand(unit3);
		when(characterDAO.getCharacter(eq(unit3.getId()))).thenReturn(unit3);
		
		objective.runObjective(oldCo, UUID.randomUUID().toString(), characterDAO, houseDAO, toSave);
		CaptureStructure order3 = (CaptureStructure) objective.issuedOrders.get(unit3.getId());
		assertFalse(order3.structure.equals(order1.structure));
		assertFalse(order3.structure.equals(order2.structure));
	}

}
