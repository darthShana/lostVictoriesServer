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
		oldCo = new CharacterMessage(UUID.randomUUID(), CharacterType.AVATAR, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null);
		vehicle = new CharacterMessage(UUID.randomUUID(), CharacterType.ANTI_TANK_GUN, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.PRIVATE, oldCo.getId());
		avatar = new CharacterMessage(UUID.randomUUID(), CharacterType.AVATAR, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null);
		oldPassenger = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null);
		myUnit = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.PRIVATE, avatar.getId());

		characterDAO = mock(CharacterDAO.class);
		when(characterDAO.getCharacter(eq(oldCo.id))).thenReturn(oldCo);
		when(characterDAO.getCharacter(eq(avatar.id))).thenReturn(avatar);
		when(characterDAO.getCharacter(eq(oldPassenger.id))).thenReturn(oldPassenger);
		when(characterDAO.getCharacter(eq(myUnit.id))).thenReturn(myUnit);

	}
	
	@Test
	public void testIsAvailableForUpdate(){
		myUnit.setVersion(3);
		CharacterMessage myUnitUpdated = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.PRIVATE, avatar.getId());
		assertFalse(myUnit.isAvailableForUpdate(avatar.getCheckoutClient(), myUnitUpdated));
	}

	@Test
	public void testAvatarBoardVehicle() {
		oldCo.unitsUnderCommand.add(vehicle.id);
		vehicle.passengers.clear();
		
		assertEquals(oldCo.id, vehicle.getCommandingOfficer());
		assertTrue(oldCo.unitsUnderCommand.contains(vehicle.id));
		
		avatar.boardVehicle(vehicle, characterDAO, new HashMap<UUID, CharacterMessage>());
		assertEquals(avatar.id, vehicle.getCommandingOfficer());
		assertEquals(avatar.getCountry(), vehicle.getCountry());
		assertFalse(oldCo.unitsUnderCommand.contains(vehicle.id));
		assertFalse(vehicle.isAbandoned());
	}
	
	@Test
	public void testNonAvatarBoardVehicle(){
		vehicle.passengers.add(oldPassenger.id);
		myUnit.boardVehicle(vehicle, characterDAO, new HashMap<UUID, CharacterMessage>());
		assertTrue(vehicle.passengers.contains(myUnit.id));
		assertFalse(vehicle.passengers.contains(oldPassenger.id));
		assertEquals(vehicle.getCommandingOfficer(), avatar.getId());
		assertFalse(vehicle.isAbandoned());
	}
	
	@Test
	public void testBoardVehicleWithOtherPassengers(){
		vehicle.passengers.add(oldPassenger.id);
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
		assertFalse(vehicle.isAbandoned());
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
		assertFalse(vehicle.isAbandoned());
		
		assertEquals(1, vehicle.passengers.size());
		assertEquals(oldPassenger.id, vehicle.passengers.iterator().next());
	}
	
	@Test
	public void testDisembarkAvatar(){
		vehicle.passengers.add(avatar.id);
		avatar.boardedVehicle = vehicle.id;
		
		vehicle.disembarkPassengers(characterDAO, true);
		assertTrue(vehicle.passengers.isEmpty());
		assertTrue(vehicle.isAbandoned());
	}
	
	@Test
	public void testCantBoardVehicleWithGunnerOFDiffCountry(){
		CharacterMessage vehicle2 = new CharacterMessage(UUID.randomUUID(), CharacterType.ANTI_TANK_GUN, new Vector(0, 0, 0), Country.AMERICAN, Weapon.RIFLE, RankMessage.PRIVATE, oldCo.getId());
		vehicle2.passengers.add(oldPassenger.id);

		avatar.boardVehicle(vehicle2, characterDAO, new HashMap<UUID, CharacterMessage>());
		assertEquals(oldCo.id, vehicle2.getCommandingOfficer());
		assertEquals(Country.AMERICAN, vehicle2.getCountry());
		assertFalse(vehicle2.passengers.contains(avatar.id));
	}
	
	@Test
	public void testCanBoardAbandonedVehicleOFDiffCountry(){
		CharacterMessage vehicle2 = new CharacterMessage(UUID.randomUUID(), CharacterType.ANTI_TANK_GUN, new Vector(0, 0, 0), Country.AMERICAN, Weapon.RIFLE, RankMessage.PRIVATE, oldCo.getId());
		vehicle2.passengers.clear();

		avatar.boardVehicle(vehicle2, characterDAO, new HashMap<UUID, CharacterMessage>());
		assertEquals(avatar.id, vehicle2.getCommandingOfficer());
		assertEquals(Country.GERMAN, vehicle2.getCountry());
		assertTrue(vehicle2.passengers.contains(avatar.id));
	}
	
	@Test
	public void testCanBoarbMyOwnVehicle(){
		vehicle.commandingOfficer = UUID.fromString(avatar.getId().toString());
		avatar.boardVehicle(vehicle, characterDAO, new HashMap<UUID, CharacterMessage>());
		assertTrue(vehicle.passengers.contains(avatar.id));
	}
	
	@Test
	public void testKillGunner(){
		vehicle.passengers.add(oldPassenger.id);
		
		vehicle.killPassenger(characterDAO);
		assertTrue(vehicle.passengers.isEmpty());
		assertTrue(vehicle.isAbandoned());
	}
	
	@Test
	public void testReplaceMeWhileBeingPassengerOnVehicle(){
		UUID vehicleID = UUID.randomUUID();
		CharacterMessage oldCo1 = new CharacterMessage(UUID.randomUUID(), CharacterType.AVATAR, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null);
		CharacterMessage vehicle1 = new CharacterMessage(vehicleID, CharacterType.HALF_TRACK, new Vector(0, 0, 0), Country.AMERICAN, Weapon.RIFLE, RankMessage.PRIVATE, oldCo1.getId());
		CharacterMessage vehicle2 = new CharacterMessage(vehicleID, CharacterType.HALF_TRACK, new Vector(0, 0, 0), Country.AMERICAN, Weapon.RIFLE, RankMessage.PRIVATE, oldCo1.getId());
		CharacterMessage oldPassenger1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, oldCo1.getId());
		oldCo1.unitsUnderCommand.add(vehicleID);
		oldCo1.unitsUnderCommand.add(oldPassenger1.getId());
		
		when(characterDAO.getCharacter(eq(oldCo1.id))).thenReturn(oldCo1);
		when(characterDAO.getCharacter(eq(vehicle2.id))).thenReturn(vehicle2);
		when(characterDAO.getCharacter(eq(oldPassenger1.id))).thenReturn(oldPassenger1);
		HashMap<UUID, CharacterMessage> value = new HashMap<UUID, CharacterMessage>();
		value.put(vehicleID, vehicle2);
		value.put(oldPassenger1.id, oldPassenger1);
		when(characterDAO.getAllCharacters(eq(oldCo1.unitsUnderCommand))).thenReturn(value);
		
		vehicle1.passengers.add(oldPassenger1.getId());
		
		HashMap<UUID, CharacterMessage> toSave = new HashMap<UUID, CharacterMessage>();
		toSave.put(vehicle1.getId(), vehicle1);
		oldCo1.replaceMe(characterDAO, toSave);
		
		assertTrue(toSave.containsKey(vehicle1.getId()));
		assertFalse(toSave.get(vehicle1.getId()).isAbandoned());
		assertEquals(toSave.get(vehicle1.getId()).passengers.size(), 1);
		assertEquals(toSave.get(vehicle1.getId()).passengers.iterator().next(), oldPassenger1.id);
		
	}
	
	@Test
	public void testPromoteToSupreamCommander(){
		avatar.rank = RankMessage.LIEUTENANT;
		avatar.commandingOfficer = oldCo.id;
		oldCo.rank = RankMessage.COLONEL;
		
		avatar.promoteCharacter(oldCo, characterDAO);
		assertEquals(RankMessage.COLONEL, avatar.getRank());
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
