package com.jme3.lostVictories.network.messages;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.UUID;

import lostVictories.dao.CharacterDAO;

import org.junit.Test;

public class CharacterMessageTest {

	@Test
	public void testBoardVehicle() {
		CharacterMessage oldCo = new CharacterMessage(UUID.randomUUID(), CharacterType.AVATAR, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null, false);
		UUID avatarID = UUID.randomUUID();
		CharacterMessage avatar  = new CharacterMessage(avatarID, CharacterType.AVATAR, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null, false);
		
		CharacterMessage vehicle  = new CharacterMessage(UUID.randomUUID(), CharacterType.ANTI_TANK_GUN, new Vector(0, 0, 0), Country.AMERICAN, Weapon.RIFLE, RankMessage.PRIVATE, oldCo.getId(), false);
		oldCo.unitsUnderCommand.add(vehicle.id);
		vehicle.gunnerDead = true;
		
		assertEquals(oldCo.id, vehicle.getCommandingOfficer());
		assertTrue(oldCo.unitsUnderCommand.contains(vehicle.id));
		
		CharacterDAO mock = mock(CharacterDAO.class);
		when(mock.getCharacter(eq(oldCo.id))).thenReturn(oldCo);
		
		avatar.boardVehicle(vehicle, mock, new HashMap<UUID, CharacterMessage>());
		assertEquals(avatarID, vehicle.getCommandingOfficer());
		assertEquals(avatar.getCountry(), vehicle.getCountry());
		assertFalse(oldCo.unitsUnderCommand.contains(vehicle.id));
	}
	
	@Test
	public void testBoardVehicleWithOtherPassengers(){
		CharacterMessage oldCo = new CharacterMessage(UUID.randomUUID(), CharacterType.AVATAR, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null, false);
		CharacterMessage oldPassenget = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null, false);

		UUID avatarID = UUID.randomUUID();
		CharacterMessage avatar  = new CharacterMessage(avatarID, CharacterType.AVATAR, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null, false);
		
		CharacterMessage vehicle  = new CharacterMessage(UUID.randomUUID(), CharacterType.ANTI_TANK_GUN, new Vector(0, 0, 0), Country.AMERICAN, Weapon.RIFLE, RankMessage.PRIVATE, oldCo.getId(), false);
		vehicle.passengers.add(oldPassenget.id);
		vehicle.gunnerDead = true;
		oldCo.unitsUnderCommand.add(vehicle.id);
		
		assertEquals(oldCo.id, vehicle.getCommandingOfficer());
		assertTrue(oldCo.unitsUnderCommand.contains(vehicle.id));
		
		CharacterDAO mock = mock(CharacterDAO.class);
		when(mock.getCharacter(eq(oldCo.id))).thenReturn(oldCo);
		when(mock.getCharacter(eq(oldPassenget.id))).thenReturn(oldPassenget);
		
		HashMap<UUID, CharacterMessage> toSave = new HashMap<UUID, CharacterMessage>();
		avatar.boardVehicle(vehicle, mock, toSave);
		assertEquals(avatarID, vehicle.getCommandingOfficer());
		assertEquals(avatar.getCountry(), vehicle.getCountry());
		assertFalse(oldCo.unitsUnderCommand.contains(vehicle.id));
		assertTrue(vehicle.passengers.contains(avatar.id));
		assertFalse(vehicle.passengers.contains(oldPassenget.id));
		assertTrue(toSave.containsKey(oldPassenget.id));
	}
	
	@Test
	public void testCantBoardVehicleWithGunner(){
		CharacterMessage oldCo = new CharacterMessage(UUID.randomUUID(), CharacterType.AVATAR, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null, false);
		CharacterMessage vehicle  = new CharacterMessage(UUID.randomUUID(), CharacterType.ANTI_TANK_GUN, new Vector(0, 0, 0), Country.AMERICAN, Weapon.RIFLE, RankMessage.PRIVATE, oldCo.getId(), false);
		vehicle.gunnerDead = false;
		
		UUID avatarID = UUID.randomUUID();
		CharacterMessage avatar  = new CharacterMessage(avatarID, CharacterType.AVATAR, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null, false);
		
		CharacterDAO mock = mock(CharacterDAO.class);
		when(mock.getCharacter(eq(oldCo.id))).thenReturn(oldCo);
		
		avatar.boardVehicle(vehicle, mock, new HashMap<UUID, CharacterMessage>());
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
	
	@Test
	public void testUpdateState(){
		CharacterMessage oldCo = new CharacterMessage(UUID.randomUUID(), CharacterType.AVATAR, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null, false);
		CharacterMessage vehicle1  = new CharacterMessage(UUID.randomUUID(), CharacterType.ANTI_TANK_GUN, new Vector(0, 0, 0), Country.AMERICAN, Weapon.RIFLE, RankMessage.PRIVATE, oldCo.getId(), false);
		CharacterMessage vehicle2  = new CharacterMessage(UUID.randomUUID(), CharacterType.ANTI_TANK_GUN, new Vector(0, 0, 0), Country.AMERICAN, Weapon.RIFLE, RankMessage.PRIVATE, oldCo.getId(), true);
		
		vehicle1.updateState(vehicle2, UUID.randomUUID(), System.currentTimeMillis());
		assertTrue(vehicle1.gunnerDead);
	}

}
