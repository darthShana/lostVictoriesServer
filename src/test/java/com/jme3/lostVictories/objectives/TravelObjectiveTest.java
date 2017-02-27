package com.jme3.lostVictories.objectives;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import org.junit.Test;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.Country;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.Weapon;
import com.jme3.math.Vector3f;

public class TravelObjectiveTest {

	@Test
	public void testTravelTowardsWaypoint() {
		Vector3f destination = new Vector3f(100, 100, 100);
		CharacterMessage myUnit = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.PRIVATE, null);
		TravelObjective objective = new TravelObjective(myUnit, new Vector(destination), null);
		Vector3f start = new Vector3f(0, 100, 0);
		CharacterMessage character = new CharacterMessage(UUID.randomUUID(), CharacterType.AVATAR, new Vector(start), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null);
		ArrayList<Vector> path = new ArrayList<Vector>();
		path.add(new Vector(destination));
		objective.path = path;
		
		objective.runObjective(character, "", mock(CharacterDAO.class), mock(HouseDAO.class), new HashMap<UUID, CharacterMessage>(), new HashMap<>());
		assertTrue(character.getLocation().toVector().distance(destination)<start.distance(destination));
	}
	
	@Test
	public void testDontGoPastWaypoint(){
		Vector3f destination = new Vector3f(100, 100, 100);
		CharacterMessage myUnit = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.PRIVATE, null);
		TravelObjective objective = new TravelObjective(myUnit, new Vector(destination), null);
		CharacterMessage character = new CharacterMessage(UUID.randomUUID(), CharacterType.AVATAR, new Vector(48.5f, 100f, 48.5f), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null);
		
		ArrayList<Vector> path = new ArrayList<Vector>();
		path.add(new Vector(50, 100, 50));
		path.add(new Vector(100, 100, 100));
		objective.path = path;
		
		objective.runObjective(character, "", mock(CharacterDAO.class), mock(HouseDAO.class), new HashMap<UUID, CharacterMessage>(), new HashMap<>());
		assertEquals(new Vector(50, 100, 50), character.getLocation());
		
		objective.runObjective(character, "", mock(CharacterDAO.class), mock(HouseDAO.class), new HashMap<UUID, CharacterMessage>(), new HashMap<>());
		assertEquals(1, objective.path.size());
		assertEquals(destination, objective.path.get(0).toVector());
	}

}
