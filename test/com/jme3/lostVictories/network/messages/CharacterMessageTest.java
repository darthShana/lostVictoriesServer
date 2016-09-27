package com.jme3.lostVictories.network.messages;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.UUID;

import lostVictories.dao.CharacterDAO;

import org.junit.Before;
import org.junit.Test;

public class CharacterMessageTest {
	
	private CharacterMessage oldCo;
	private CharacterMessage vehicle;
	private CharacterMessage avatar;
	private CharacterDAO characterDAO;
	private CharacterMessage oldPassenger;
	private CharacterMessage myUnit;

	@Before
	public void setup(){
		oldCo = new CharacterMessage(UUID.randomUUID(), CharacterType.AVATAR, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null, false);
		vehicle = new CharacterMessage(UUID.randomUUID(), CharacterType.ANTI_TANK_GUN, new Vector(0, 0, 0), Country.AMERICAN, Weapon.RIFLE, RankMessage.PRIVATE, oldCo.getId(), false);
		avatar = new CharacterMessage(UUID.randomUUID(), CharacterType.AVATAR, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null, false);
		oldPassenger = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null, false);
		myUnit = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.PRIVATE, avatar.getId(), false);

		characterDAO = mock(CharacterDAO.class);
		when(characterDAO.getCharacter(eq(oldCo.id))).thenReturn(oldCo);
		when(characterDAO.getCharacter(eq(avatar.id))).thenReturn(avatar);
		when(characterDAO.getCharacter(eq(oldPassenger.id))).thenReturn(oldPassenger);
		when(characterDAO.getCharacter(eq(myUnit.id))).thenReturn(myUnit);

	}

	@Test
	public void testAvatarBoardVehicle() {
		oldCo.unitsUnderCommand.add(vehicle.id);
		vehicle.gunnerDead = true;
		
		assertEquals(oldCo.id, vehicle.getCommandingOfficer());
		assertTrue(oldCo.unitsUnderCommand.contains(vehicle.id));
		
		avatar.boardVehicle(vehicle, characterDAO, new HashMap<UUID, CharacterMessage>());
		assertEquals(avatar.id, vehicle.getCommandingOfficer());
		assertEquals(avatar.getCountry(), vehicle.getCountry());
		assertFalse(oldCo.unitsUnderCommand.contains(vehicle.id));
		assertFalse(vehicle.gunnerDead);
	}
	
	@Test
	public void testNonAvatarBoardVehicle(){
		vehicle.passengers.add(oldPassenger.id);
		vehicle.gunnerDead = true;
		myUnit.boardVehicle(vehicle, characterDAO, new HashMap<UUID, CharacterMessage>());
		assertTrue(vehicle.passengers.contains(myUnit.id));
		assertFalse(vehicle.passengers.contains(oldPassenger.id));
		assertEquals(vehicle.getCommandingOfficer(), avatar.getId());
		assertFalse(vehicle.gunnerDead);
	}
	
	@Test
	public void testBoardVehicleWithOtherPassengers(){
		vehicle.passengers.add(oldPassenger.id);
		vehicle.gunnerDead = true;
		oldCo.unitsUnderCommand.add(vehicle.id);
		
		assertEquals(oldCo.id, vehicle.getCommandingOfficer());
		assertTrue(oldCo.unitsUnderCommand.contains(vehicle.id));
		
		HashMap<UUID, CharacterMessage> toSave = new HashMap<UUID, CharacterMessage>();
		avatar.boardVehicle(vehicle, characterDAO, toSave);
		assertEquals(avatar.id, vehicle.getCommandingOfficer());
		assertEquals(avatar.getCountry(), vehicle.getCountry());
		assertFalse(oldCo.unitsUnderCommand.contains(vehicle.id));
		assertTrue(vehicle.passengers.contains(avatar.id));
		assertFalse(vehicle.passengers.contains(oldPassenger.id));
		assertFalse(vehicle.gunnerDead);
		assertTrue(toSave.containsKey(oldPassenger.id));
	}
	
	@Test
	public void testDisembarkPassengers(){
		vehicle.passengers.add(avatar.id);
		vehicle.passengers.add(oldPassenger.id);
		avatar.boardedVehicle = vehicle.id;
		oldPassenger.boardedVehicle = vehicle.id;
		
		vehicle.disembarkPassengers(characterDAO, true);
		assertFalse(vehicle.passengers.isEmpty());
		assertFalse(vehicle.gunnerDead);
		
		assertEquals(1, vehicle.passengers.size());
		assertEquals(oldPassenger.id, vehicle.passengers.iterator().next());
	}
	
	@Test
	public void testDisembarkAvatar(){
		vehicle.passengers.add(avatar.id);
		avatar.boardedVehicle = vehicle.id;
		
		vehicle.disembarkPassengers(characterDAO, true);
		assertTrue(vehicle.passengers.isEmpty());
		assertTrue(vehicle.gunnerDead);
	}
	
	@Test
	public void testCantBoardVehicleWithGunner(){
		vehicle.gunnerDead = false;
		
		avatar.boardVehicle(vehicle, characterDAO, new HashMap<UUID, CharacterMessage>());
		assertEquals(oldCo.id, vehicle.getCommandingOfficer());
		assertEquals(Country.AMERICAN, vehicle.getCountry());
		assertFalse(vehicle.passengers.contains(avatar.id));
	}
	
	@Test
	public void testLatitudeLongitueCOnversion(){
		Vector location = new Vector(177.0471f, 97.52623f, -14.900481f);
		double latitute = CharacterMessage.toLatitute(location);
		double longitude = CharacterMessage.toLongitude(location);
		
		assertEquals("lat", "-2.328200101852417", latitute+"");
		assertEquals("lon", "62.24312210083008", longitude+"");
		
		HashMap<String, Double> latlong = new HashMap<String, Double>();
		latlong.put("lat", latitute);
		latlong.put("lon", longitude);
		
		Vector latLongToVector = Vector.latLongToVector(latlong, 97.52623f);
		
		assertEquals(new Vector(177.0471f, 97.52623f, -14.900481f), latLongToVector);
		
	}

}
