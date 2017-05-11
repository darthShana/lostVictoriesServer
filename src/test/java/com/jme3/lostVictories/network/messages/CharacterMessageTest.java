package com.jme3.lostVictories.network.messages;

import static lostVictories.dao.CharacterDAO.MAPPER;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.jme3.lostVictories.network.messages.actions.Idle;
import lostVictories.VehicleFactory;
import lostVictories.WeaponsFactory;
import lostVictories.dao.CharacterDAO;
import lostVictories.messageHanders.CharacterCatch;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jme3.lostVictories.objectives.TravelObjective;

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
		assertFalse(myUnit.isAvailableForUpdate(avatar.getCheckoutClient(), myUnitUpdated, 2000));
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
		
		vehicle.killPassenger(new CharacterCatch(characterDAO));
		assertTrue(vehicle.passengers.isEmpty());
		assertTrue(vehicle.isAbandoned());
	}
	
	@Test
	public void testReplaceMeWhileBeingPassengerOnVehicle(){
		UUID vehicleID = UUID.randomUUID();
		CharacterMessage oldCo1 = new CharacterMessage(UUID.randomUUID(), CharacterType.AVATAR, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null);
		CharacterMessage vehicle1 = new CharacterMessage(vehicleID, CharacterType.HALF_TRACK, new Vector(0, 0, 0), Country.AMERICAN, Weapon.RIFLE, RankMessage.PRIVATE, oldCo1.getId());
		CharacterMessage oldPassenger1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, oldCo1.getId());
		oldCo1.unitsUnderCommand.add(vehicleID);
		oldCo1.unitsUnderCommand.add(oldPassenger1.getId());
		
		when(characterDAO.getCharacter(eq(oldCo1.id))).thenReturn(oldCo1);
		when(characterDAO.getCharacter(eq(vehicle1.id))).thenReturn(vehicle1);
		when(characterDAO.getCharacter(eq(oldPassenger1.id))).thenReturn(oldPassenger1);
		HashMap<UUID, CharacterMessage> value = new HashMap<UUID, CharacterMessage>();
		value.put(vehicleID, vehicle1);
		value.put(oldPassenger1.id, oldPassenger1);
		when(characterDAO.getAllCharacters(eq(oldCo1.unitsUnderCommand))).thenReturn(value);
		
		vehicle1.passengers.add(oldPassenger1.getId());
		
		HashMap<UUID, CharacterMessage> toSave = new HashMap<UUID, CharacterMessage>();
		toSave.put(vehicle1.getId(), vehicle1);
		oldCo1.replaceMe(new CharacterCatch(characterDAO), toSave);
		
		assertTrue(toSave.containsKey(vehicle1.getId()));
		assertFalse(toSave.get(vehicle1.getId()).isAbandoned());
		assertEquals(toSave.get(vehicle1.getId()).passengers.size(), 1);
		assertEquals(toSave.get(vehicle1.getId()).passengers.iterator().next(), oldPassenger1.id);
		
	}
	
	@Test
	public void testRepleaceLiutenant(){
		CharacterMessage oldCo1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.LIEUTENANT, null);
		CharacterMessage cp1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, oldCo1.getId());
		CharacterMessage cp2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, oldCo1.getId());
		oldCo1.addCharactersUnderCommand(cp1, cp2);
		
		CharacterMessage p1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, cp1.getId());
		CharacterMessage p2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, cp1.getId());
		cp1.addCharactersUnderCommand(p1, p2);
		CharacterMessage p3 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, cp2.getId());
		CharacterMessage p4 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, cp2.getId());
		cp2.addCharactersUnderCommand(p3, p4);
		
		when(characterDAO.getCharacter(cp1.getId())).thenReturn(cp1);
		when(characterDAO.getCharacter(cp2.getId())).thenReturn(cp2);
		when(characterDAO.getCharacter(p1.getId())).thenReturn(p1);
		when(characterDAO.getCharacter(p2.getId())).thenReturn(p2);
		when(characterDAO.getCharacter(p3.getId())).thenReturn(p3);
		when(characterDAO.getCharacter(p4.getId())).thenReturn(p4);

		
		HashMap<UUID, CharacterMessage> toSave = new HashMap<UUID, CharacterMessage>();
		oldCo1.replaceMe(new CharacterCatch(characterDAO), toSave);
		
		Set<CharacterMessage> collect = toSave.values().stream().filter(c->c.rank==RankMessage.LIEUTENANT).collect(Collectors.toSet());
		assertEquals(1, collect.size());
		
		CharacterMessage newLiutenant = collect.iterator().next();
		assertEquals(2, newLiutenant.getUnitsUnderCommand().size());
		Iterator<UUID> iterator = newLiutenant.getUnitsUnderCommand().iterator();
		CharacterMessage newCorporal1 = toSave.get(iterator.next());
		CharacterMessage newCorporal2 = toSave.get(iterator.next());
		assertEquals(RankMessage.CADET_CORPORAL, newCorporal1.rank);
		assertEquals(RankMessage.CADET_CORPORAL, newCorporal2.rank);
		assertEquals(newLiutenant.id, newCorporal1.commandingOfficer);
		assertEquals(newLiutenant.id, newCorporal2.commandingOfficer);
		
		
	}
	
	@Test
	public void testRepleaceVehicleKillsPassengers(){
		UUID vehicleID = UUID.randomUUID();
		CharacterMessage oldCo1 = new CharacterMessage(UUID.randomUUID(), CharacterType.AVATAR, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null);
		CharacterMessage vehicle1 = new CharacterMessage(vehicleID, CharacterType.HALF_TRACK, new Vector(0, 0, 0), Country.AMERICAN, Weapon.RIFLE, RankMessage.PRIVATE, oldCo1.getId());
		CharacterMessage oldPassenger1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, oldCo1.getId());
		oldCo1.unitsUnderCommand.add(vehicleID);
		oldCo1.unitsUnderCommand.add(oldPassenger1.getId());
		
		when(characterDAO.getCharacter(eq(oldCo1.id))).thenReturn(oldCo1);
		when(characterDAO.getCharacter(eq(vehicle1.id))).thenReturn(vehicle1);
		when(characterDAO.getCharacter(eq(oldPassenger1.id))).thenReturn(oldPassenger1);
		HashMap<UUID, CharacterMessage> value = new HashMap<UUID, CharacterMessage>();
		value.put(vehicleID, vehicle1);
		value.put(oldPassenger1.id, oldPassenger1);
		when(characterDAO.getAllCharacters(eq(oldCo1.unitsUnderCommand))).thenReturn(value);
		
		vehicle1.passengers.add(oldPassenger1.getId());
		
		HashMap<UUID, CharacterMessage> toSave = new HashMap<UUID, CharacterMessage>();
		vehicle1.replaceMe(new CharacterCatch(characterDAO), toSave);
		assertTrue(toSave.get(oldPassenger1.getId()).dead);
		assertFalse(toSave.get(oldCo1.getId()).unitsUnderCommand.contains(vehicle1.getId()));
		assertFalse(toSave.get(oldCo1.getId()).unitsUnderCommand.contains(oldPassenger1.getId()));

	}
	
	@Test
	public void testRepleaceLiutenantNoOrphanUnits(){
		CharacterMessage supreamLeader = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.COLONEL, null);
		CharacterMessage oldCo1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.LIEUTENANT, supreamLeader.getId());
		CharacterMessage cp1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, oldCo1.getId());
		oldCo1.addCharactersUnderCommand(cp1);
		supreamLeader.addCharactersUnderCommand(oldCo1);
		
		CharacterMessage p1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, cp1.getId());
		CharacterMessage p2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, cp1.getId());
		cp1.addCharactersUnderCommand(p1, p2);

		when(characterDAO.getCharacter(supreamLeader.getId())).thenReturn(supreamLeader);
		when(characterDAO.getCharacter(cp1.getId())).thenReturn(cp1);
		when(characterDAO.getCharacter(p1.getId())).thenReturn(p1);
		when(characterDAO.getCharacter(p2.getId())).thenReturn(p2);
		
		HashMap<UUID, CharacterMessage> toSave = new HashMap<UUID, CharacterMessage>();
		oldCo1.replaceMe(new CharacterCatch(characterDAO), toSave);
		
		Set<CharacterMessage> collect = toSave.values().stream().filter(c->c.rank==RankMessage.LIEUTENANT).collect(Collectors.toSet());
		assertEquals(1, collect.size());
		
		CharacterMessage newLiutenant = collect.iterator().next();
		assertEquals(1, newLiutenant.getUnitsUnderCommand().size());
		Iterator<UUID> iterator = newLiutenant.getUnitsUnderCommand().iterator();
		CharacterMessage newCorporal1 = toSave.get(iterator.next());
		assertEquals(newCorporal1.getId(), newLiutenant.getUnitsUnderCommand().iterator().next());
		assertEquals(RankMessage.CADET_CORPORAL, newCorporal1.rank);
		
		if(newCorporal1.getId().equals(p1.id)){
			assertEquals(toSave.get(p2.id).commandingOfficer, newCorporal1.id);
		}else{
			assertEquals(toSave.get(p1.id).commandingOfficer, newCorporal1.id);
		}
		CharacterMessage cm3 = toSave.get(supreamLeader.getId());
		assertEquals(1, cm3.unitsUnderCommand.size());
 		assertEquals(newLiutenant.getId(), cm3.unitsUnderCommand.iterator().next());
 		
	}
	
	@Test
	public void testRepleaceLiutenantWithOrphanUnits(){
		CharacterMessage oldCo1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.LIEUTENANT, null);
		CharacterMessage cp1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, oldCo1.getId());
		oldCo1.addCharactersUnderCommand(cp1);
		
		CharacterMessage p1 = new CharacterMessage(UUID.randomUUID(), CharacterType.ANTI_TANK_GUN, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, cp1.getId());
		cp1.addCharactersUnderCommand(p1);
		
		when(characterDAO.getCharacter(cp1.getId())).thenReturn(cp1);
		when(characterDAO.getCharacter(p1.getId())).thenReturn(p1);
		
		HashMap<UUID, CharacterMessage> toSave = new HashMap<UUID, CharacterMessage>();
		oldCo1.replaceMe(new CharacterCatch(characterDAO), toSave);
		
		Set<CharacterMessage> collect = toSave.values().stream().filter(c->c.rank==RankMessage.LIEUTENANT).collect(Collectors.toSet());
		assertEquals(1, collect.size());
		
		CharacterMessage newLiutenant = collect.iterator().next();

		assertTrue(newLiutenant.getUnitsUnderCommand().isEmpty());
		assertNull(toSave.get(p1.getId()).commandingOfficer);
		
	}
	
	@Test
	public void testPromoteToSupreamCommander() throws IOException{
		avatar.rank = RankMessage.LIEUTENANT;
		avatar.commandingOfficer = oldCo.id;
		oldCo.rank = RankMessage.COLONEL;
		
		avatar.promoteAvatar(oldCo, characterDAO);
		assertEquals(RankMessage.COLONEL, avatar.getRank());
	}
	
	@Test
	public void testPromoteAvatarToLieutenant() throws IOException{
		oldCo.rank = RankMessage.LIEUTENANT;
		CharacterMessage unit1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.PRIVATE, avatar.getId());
		CharacterMessage unit2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.PRIVATE, avatar.getId());
		when(characterDAO.getCharacter(unit1.getId())).thenReturn(unit1);
		when(characterDAO.getCharacter(unit2.getId())).thenReturn(unit2);
		
		avatar.addCharactersUnderCommand(unit1, unit2);
		avatar.commandingOfficer = oldCo.id;

		avatar.promoteAvatar(oldCo, characterDAO);
		
		assertEquals(RankMessage.LIEUTENANT, avatar.getRank());
		assertEquals(1, avatar.getUnitsUnderCommand().size());		
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
		
		Vector latLongToVector = Vector.latLongToVector(97.52623f, latlong.get("lon").floatValue(), latlong.get("lat").floatValue());
		
		assertEquals(new Vector(177.0471f, 97.52623f, -14.900481f), latLongToVector);
		
	}
	
	@Test
	public void testLogConv2(){
		HashMap<String, Double> latlong = new HashMap<String, Double>();
		latlong.put("lat", -24.09786033630371);
		latlong.put("lon", 44.689910888671875);
		Vector other = Vector.latLongToVector(100.49753f, latlong.get("lon").floatValue(), latlong.get("lat").floatValue());
		System.out.println("other:"+other);
		
		latlong.put("lat", -57.450252532958984);
		latlong.put("lon", 29.49251937866211);
		Vector ava = Vector.latLongToVector(100.72956f, latlong.get("lon").floatValue(), latlong.get("lat").floatValue());
		System.out.println("avatar:"+ava);
		System.out.println("distance:"+ava.distance(other));
	}
	
	@Test
	public void testReenforceCharacter() throws IOException{
		CharacterMessage cp1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null);
		
		CharacterMessage p1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, cp1.getId());
		CharacterMessage p2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, cp1.getId());
		cp1.addCharactersUnderCommand(p1, p2);
		
		WeaponsFactory weaponsFactory = new WeaponsFactory(Country.GERMAN);
		weaponsFactory.updateSenses(new HashSet<>());
		VehicleFactory vehicleFactory = new VehicleFactory(Country.GERMAN);
		vehicleFactory.updateSenses(new HashSet<>());
		
		Collection<CharacterMessage> reenforceCharacter = cp1.reenforceCharacter(new Vector(100, 0, 100), weaponsFactory, vehicleFactory, characterDAO);
		Set<UUID> unitsUnderCommand = cp1.getUnitsUnderCommand();
		assertEquals(4, unitsUnderCommand.size());
		unitsUnderCommand.remove(p1.getId());
		unitsUnderCommand.remove(p2.getId());
		
		assertEquals(1, reenforceCharacter.size());
		assertEquals(cp1.getId(), reenforceCharacter.iterator().next().getId());
		ArgumentCaptor<UUID> valuesArgument = ArgumentCaptor.forClass(UUID.class);
		verify(characterDAO, times(2)).putCharacter(valuesArgument.capture(), isA(CharacterMessage.class));
		assertTrue(unitsUnderCommand.contains(valuesArgument.getAllValues().get(0)));
		assertTrue(unitsUnderCommand.contains(valuesArgument.getAllValues().get(1)));		
		
	}
	
	@Test
	public void testReplaceWithAvatar() throws IOException{
		CharacterMessage oldCo1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.LIEUTENANT, null);
		CharacterMessage cp1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, oldCo1.getId());
		oldCo1.addCharactersUnderCommand(cp1);
		
		CharacterMessage p1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, cp1.getId());
		CharacterMessage p2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, cp1.getId());
		cp1.addCharactersUnderCommand(p1, p2);
		
		CharacterMessage cp2 = new CharacterMessage(UUID.randomUUID(), CharacterType.AVATAR, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, oldCo1.getId());
		
		HashSet<CharacterMessage> toUpdate = new HashSet<CharacterMessage>();
		CharacterDAO allCharacters = mock(CharacterDAO.class);
		when(allCharacters.getCharacter(eq(oldCo1.getId()))).thenReturn(oldCo1);
		when(allCharacters.getCharacter(eq(cp1.getId()))).thenReturn(cp1);
		when(allCharacters.getCharacter(eq(p1.getId()))).thenReturn(p1);
		when(allCharacters.getCharacter(eq(p2.getId()))).thenReturn(p2);
		when(allCharacters.getCharacter(eq(cp2.getId()))).thenReturn(cp2);
		
		cp1.replaceWithAvatar(cp2, toUpdate, new CharacterCatch(allCharacters));
		
		assertTrue(toUpdate.contains(oldCo1));
		assertTrue(oldCo1.getUnitsUnderCommand().contains(cp2.getId()));
		assertFalse(oldCo1.getUnitsUnderCommand().contains(cp1.getId()));

	}
	
	@Test
	public void testReplaceWithAvatarWhileBeingPassenger() throws IOException{
		UUID cp2id = UUID.randomUUID();
		UUID cp1id = UUID.randomUUID();
		UUID vehicleID = UUID.randomUUID();
		
		CharacterMessage cp2 = new CharacterMessage(cp2id, CharacterType.AVATAR, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null);
		CharacterMessage cp1 = new CharacterMessage(cp1id, CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null);
		cp1.unitsUnderCommand.add(vehicleID);
		cp1.boardedVehicle = vehicleID;
		
		HashSet<CharacterMessage> toUpdate = new HashSet<CharacterMessage>();
		CharacterDAO allCharacters = mock(CharacterDAO.class);
		when(allCharacters.getCharacter(eq(cp1.getId()))).then(new Answer<CharacterMessage>() {
			@Override
			public CharacterMessage answer(InvocationOnMock invocation) throws Throwable {
				return new CharacterMessage(cp1id, CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null);
			}
		});
		when(allCharacters.getCharacter(eq(vehicleID))).then(new Answer<CharacterMessage>() {
			@Override
			public CharacterMessage answer(InvocationOnMock invocation) throws Throwable {
				CharacterMessage characterMessage = new CharacterMessage(vehicleID, CharacterType.HALF_TRACK, new Vector(0, 0, 0), Country.AMERICAN, Weapon.RIFLE, RankMessage.PRIVATE, cp1.getId());
				characterMessage.passengers.add(cp1.id);
				return characterMessage;
			}
		});
		
		cp1.replaceWithAvatar(cp2, toUpdate, new CharacterCatch(allCharacters));
		CharacterMessage newAvator = toUpdate.stream().filter(a->a.id.equals(cp2.id)).findFirst().get();
		CharacterMessage vehicle1 = toUpdate.stream().filter(a->a.id.equals(vehicleID)).findFirst().get();
		
		assertTrue(newAvator.unitsUnderCommand.contains(vehicle1.id));
		assertTrue(vehicle1.passengers.contains(newAvator.getId()));
		assertFalse(vehicle1.passengers.contains(cp1.getId()));
		assertTrue(toUpdate.contains(vehicle1));
	}
	
	@Test 
	public void testUpdateState() throws JsonProcessingException{
		CharacterMessage oldCo1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.LIEUTENANT, null);
		CharacterMessage other = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.LIEUTENANT, null);
		String objectiveID = UUID.randomUUID().toString();
		other.objectives.put(objectiveID, MAPPER.writeValueAsString(new TravelObjective(other, new Vector(0, 0, 0), null)));
		other.completedObjectives = new HashSet<>();
		oldCo1.updateState(other, UUID.randomUUID(), System.currentTimeMillis());
		
		assertTrue(oldCo1.objectives.containsKey(objectiveID));

	}

	public static CharacterMessage createCharacter(UUID identity, UUID commandingOfficer, Vector location, RankMessage rank, boolean isDead) throws JsonProcessingException {
		CharacterMessage s1 = new CharacterMessage(identity, CharacterType.SOLDIER, location, Country.GERMAN, Weapon.RIFLE, rank, commandingOfficer);
		s1.userID = UUID.randomUUID();
		s1.boardedVehicle = UUID.randomUUID();
		s1.unitsUnderCommand.add(UUID.randomUUID());
		s1.passengers.add(UUID.randomUUID());
		s1.checkoutClient = UUID.randomUUID();
		s1.checkoutTime = 123l;
		s1.orientation = new Vector(1, 2, 3);
		s1.actions.add(new Idle());
		s1.objectives.put(UUID.randomUUID().toString(), MAPPER.writeValueAsString(new TravelObjective(s1, new Vector(0, 0, 0), null)));
		s1.dead = isDead;
		s1.engineDamaged = true;
		s1.timeOfDeath = 123l;
		s1.version = 456l;
		s1.kills.add(UUID.randomUUID());
		s1.squadType = SquadType.ANTI_TANK_GUN;
		return s1;
	}
	

}
