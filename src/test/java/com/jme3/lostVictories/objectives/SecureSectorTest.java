package com.jme3.lostVictories.objectives;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.awt.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import com.jme3.lostVictories.network.messages.*;
import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class SecureSectorTest {

	private HouseDAO houseDAO;
	private HashSet<HouseMessage> houses;
	private HashSet<BunkerMessage> bunkers;
	private CharacterMessage oldCo;
	private CharacterMessage unit1;
	private CharacterMessage unit2;
	private CharacterDAO characterDAO;
	HashMap<UUID, CharacterMessage> toSave;
	HashMap<UUID, UUID> kills;
	private HouseMessage house1;
	private HouseMessage house2;
	private HouseMessage house3;
	private HouseMessage house4;


	@Before
	public void setup(){
		houseDAO = mock(HouseDAO.class);
		houses = new HashSet<>();
		bunkers = new HashSet<>();
		house1 = new HouseMessage("type2", new Vector(100, 1, 100), new Quaternion(1, 1, 1, 1));
		house2 = new HouseMessage("type2", new Vector(110, 1, 100), new Quaternion(1, 1, 1, 1));
		house3 = new HouseMessage("type2", new Vector(120, 1, 100), new Quaternion(1, 1, 1, 1));
		house4 = new HouseMessage("type2", new Vector(120, 1, 110), new Quaternion(1, 1, 1, 1));
		houses.add(house1);
		houses.add(house2);
		houses.add(house3);
		houses.add(house4);
		when(houseDAO.getHouse(eq(house1.getId()))).thenReturn(house1);
		when(houseDAO.getHouse(eq(house2.getId()))).thenReturn(house2);
		when(houseDAO.getHouse(eq(house3.getId()))).thenReturn(house3);
		when(houseDAO.getHouse(eq(house4.getId()))).thenReturn(house4);

		oldCo = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.LIEUTENANT, null);
		unit1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(111, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, oldCo.getId());
		unit2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(121, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, oldCo.getId());

		oldCo.addCharactersUnderCommand(unit1, unit2);
		
		characterDAO = mock(CharacterDAO.class);
		when(characterDAO.getCharacter(eq(unit1.getId()))).thenReturn(unit1);
		when(characterDAO.getCharacter(eq(unit2.getId()))).thenReturn(unit2);
        HashMap<UUID, CharacterMessage> t = new HashMap<>();
        t.put(unit1.getId(), unit1);
        t.put(unit2.getId(), unit2);
        when(characterDAO.getAllCharacters(eq(new HashSet<>(oldCo.getUnitsUnderCommand())))).thenReturn(t);

		toSave = new HashMap<>();
		kills = new HashMap<>();
	}
	
	@Test
	public void testSaveAndRestoreObjective(){
		SecureSector objective = new SecureSector(UUID.randomUUID(), 3, 1, new Vector(100, 0, 100));
		oldCo.setLocation(new Vector(100, 1, 100));
		objective.state = SecureSectorState.DEPLOY_TO_SECTOR;
		objective.runObjective(oldCo, UUID.randomUUID().toString(), characterDAO, houseDAO, toSave, kills);
		assertEquals(1, unit1.readObjectives().size());
		objective.runObjective(oldCo, UUID.randomUUID().toString(), characterDAO, houseDAO, toSave, kills);
		assertEquals(1, unit1.readObjectives().size());
	}
	
	@Test
	public void testCompleteDeployPhaseAndChangeToCaptureHouse(){
		SecureSector objective = new SecureSector(UUID.randomUUID(),3, 1, new Vector(100, 0, 100));
		oldCo.setLocation(new Vector(110, 1, 100));
		objective.state = SecureSectorState.DEPLOY_TO_SECTOR;
		objective.runObjective(oldCo, UUID.randomUUID().toString(), characterDAO, houseDAO, toSave, kills);
		
		assertEquals(SecureSectorState.CAPTURE_HOUSES, objective.state);
	}
	
	@Test
	public void testCaptureHouses() {
		SecureSector objective = new SecureSector(UUID.randomUUID(), 3, 1, new Vector(100, 0, 100));
		objective.state = SecureSectorState.CAPTURE_HOUSES;
		
		objective.runObjective(oldCo, UUID.randomUUID().toString(), characterDAO, houseDAO, toSave, kills);
		
		CaptureStructure order1 = (CaptureStructure) unit1.getObjectiveSafe(objective.issuedOrders.get(unit1.getId()));
		CaptureStructure order2 = (CaptureStructure) unit2.getObjectiveSafe(objective.issuedOrders.get(unit2.getId()));
		
		assertEquals(order1.structure, house2.getId().toString());
		assertEquals(order2.structure, house3.getId().toString());

		CharacterMessage unit3 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, oldCo.getId());
		oldCo.addCharactersUnderCommand(unit3);
		when(characterDAO.getCharacter(eq(unit3.getId()))).thenReturn(unit3);

        HashMap<UUID, CharacterMessage> t = new HashMap<>();
        t.put(unit1.getId(), unit1);
        t.put(unit2.getId(), unit2);
        t.put(unit3.getId(), unit3);
        when(characterDAO.getAllCharacters(eq(new HashSet<>(oldCo.getUnitsUnderCommand())))).thenReturn(t);
		
		objective.runObjective(oldCo, UUID.randomUUID().toString(), characterDAO, houseDAO, toSave, kills);
		CaptureStructure order3 = (CaptureStructure) unit3.getObjectiveSafe(objective.issuedOrders.get(unit3.getId()));
		assertFalse(order3.structure.equals(order1.structure));
		assertFalse(order3.structure.equals(order2.structure));
	}

	@Test
    public void testCaptureHousesWhenEnterSectorBoundary(){
        SecureSector objective = new SecureSector(UUID.randomUUID(),3, 1, new Vector(100, 0, 100));
        assertEquals(new Rectangle(100, 100, 20, 10), objective.boundary);
        objective.state = SecureSectorState.DEPLOY_TO_SECTOR;
        objective.runObjective(oldCo, UUID.randomUUID().toString(), characterDAO, houseDAO, toSave, kills);

        oldCo.setLocation(new Vector(105, 0, 105));
        objective.runObjective(oldCo, UUID.randomUUID().toString(), characterDAO, houseDAO, toSave, kills);
        assertEquals(SecureSectorState.CAPTURE_HOUSES, objective.state);
    }
	
	@Test
    public void testWaitsForNorminalStrengthBeforeDeployment(){
		SecureSector objective = new SecureSector(UUID.randomUUID(),4, 1, new Vector(100, 0, 100));
		
		objective.runObjective(oldCo, UUID.randomUUID().toString(), characterDAO, houseDAO, toSave, kills);
		assertEquals(SecureSectorState.WAIT_FOR_REENFORCEMENTS, objective.state);
		
		CharacterMessage p1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.PRIVATE, unit1.getId());
		unit1.addCharactersUnderCommand(p1);
		when(characterDAO.getCharacter(eq(p1.getId()))).thenReturn(p1);

		objective.runObjective(oldCo, UUID.randomUUID().toString(), characterDAO, houseDAO, toSave, kills);
		assertEquals(SecureSectorState.DEPLOY_TO_SECTOR, objective.state);
	}
	
	@Test
    public void testRetreatsWhenStrengthFalls(){
		oldCo.setLocation(new Vector(110, 100, 110));
		SecureSector objective = new SecureSector(UUID.randomUUID(),4, 3, new Vector(100, 0, 100));
		
		objective.state = SecureSectorState.DEPLOY_TO_SECTOR;
		objective.runObjective(oldCo, UUID.randomUUID().toString(), characterDAO, houseDAO, toSave, kills);
		assertEquals(SecureSectorState.RETREAT, objective.state);
		
		objective.state = SecureSectorState.CAPTURE_HOUSES;
		objective.runObjective(oldCo, UUID.randomUUID().toString(), characterDAO, houseDAO, toSave, kills);
		assertEquals(SecureSectorState.RETREAT, objective.state);
		
		objective.issuedOrders.clear();
		objective.runObjective(oldCo, UUID.randomUUID().toString(), characterDAO, houseDAO, toSave, kills);
		TravelObjective t1 = (TravelObjective)objective.embededObjective;
		TransportSquad t2 = (TransportSquad) unit1.getObjectiveSafe(objective.issuedOrders.get(unit1.getId()));
		TransportSquad t3 = (TransportSquad) unit2.getObjectiveSafe(objective.issuedOrders.get(unit2.getId()));
		assertEquals(new Vector(100, 0, 100), t1.destination);
		assertEquals(new Vector(100, 0, 100), t2.destination);
		assertEquals(new Vector(100, 0, 100), t3.destination);
	}
	
	@Test
    public void testReteatEndsWhenReachedEnemyBase(){
		oldCo.setLocation(new Vector(110, 100, 110));
		SecureSector objective = new SecureSector(UUID.randomUUID(),4, 3, new Vector(100, 100, 100));
		objective.state = SecureSectorState.RETREAT;
		
		objective.runObjective(oldCo, UUID.randomUUID().toString(), characterDAO, houseDAO, toSave, kills);
		objective.embededObjective.isComplete = true;
		objective.runObjective(oldCo, UUID.randomUUID().toString(), characterDAO, houseDAO, toSave, kills);
		assertEquals(SecureSectorState.WAIT_FOR_REENFORCEMENTS, objective.state);
	}

}
