package com.jme3.lostVictories.network.messages;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.UUID;

import lostVictories.LostVictoryScene;
import lostVictories.dao.CharacterDAO;

import org.elasticsearch.common.collect.ImmutableSet;
import org.junit.Test;

public class HouseMessageTest {

	@Test
	public void testCaptureHouse() {
		HouseMessage house = new HouseMessage("type2", new Vector(100, 1, 100), new Quaternion(1, 1, 1, 1));
		assertNull(house.getOwner());
		assertEquals(CaptureStatus.NONE, house.getStatus());
		
		CharacterDAO characterDAO = mock(CharacterDAO.class);
		
		when(characterDAO.getAllCharacters(100f, 1f, 100f, HouseMessage.CAPTURE_RANGE*LostVictoryScene.SCENE_SCALE)).thenReturn(ImmutableSet.of(getCharacter(Country.AMERICAN)));
		house.chechOwnership(characterDAO);
		assertEquals(CaptureStatus.CAPTURING, house.getStatus());
		assertNull(house.getOwner());
		
		house.chechOwnership(characterDAO);
		assertEquals(CaptureStatus.CAPTURING, house.getStatus());
		assertNull("capture is not instant", house.getOwner());
		
		house.statusChangeTime = 100l;
		house.chechOwnership(characterDAO);
		assertEquals(CaptureStatus.CAPTURED, house.getStatus());
		assertEquals(Country.AMERICAN, house.getOwner());
	}
	
	@Test
	public void testCantCaptureCapturedHouse(){
		HouseMessage house = new HouseMessage("type2", new Vector(100, 1, 100), new Quaternion(1, 1, 1, 1));
		house.captureStatus = CaptureStatus.CAPTURED;
		house.owner = Country.AMERICAN;
		
		CharacterDAO characterDAO = mock(CharacterDAO.class);
		
		when(characterDAO.getAllCharacters(100f, 1f, 100f, HouseMessage.CAPTURE_RANGE)).thenReturn(ImmutableSet.of(getCharacter(Country.AMERICAN), getCharacter(Country.AMERICAN)));
		house.chechOwnership(characterDAO);
		assertEquals(CaptureStatus.CAPTURED, house.getStatus());
		assertEquals(Country.AMERICAN, house.getOwner());
	}
	
	@Test
	public void testCantHijachHouseCapture(){
		CharacterDAO characterDAO = mock(CharacterDAO.class);
		HouseMessage house = new HouseMessage("type2", new Vector(100, 1, 100), new Quaternion(1, 1, 1, 1));
		house.statusChangeTime = 100l;
		house.captureStatus = CaptureStatus.CAPTURING;
		house.contestingOwner = Country.AMERICAN;
		when(characterDAO.getAllCharacters(100f, 1f, 100f, HouseMessage.CAPTURE_RANGE)).thenReturn(ImmutableSet.of(getCharacter(Country.GERMAN)));
		house.chechOwnership(characterDAO);
		assertEquals(CaptureStatus.NONE, house.getStatus());
		assertNull(house.getOwner());
	}
	
	@Test
	public void testCaptureInterupted(){
		HouseMessage house = new HouseMessage("type2", new Vector(100, 1, 100), new Quaternion(1, 1, 1, 1));
		house.owner = null;
		house.captureStatus = CaptureStatus.CAPTURING;
		house.contestingOwner = Country.AMERICAN;
		
		CharacterDAO characterDAO = mock(CharacterDAO.class);
		
		when(characterDAO.getAllCharacters(100f, 1f, 100f, HouseMessage.CAPTURE_RANGE)).thenReturn(ImmutableSet.of(getCharacter(Country.GERMAN), getCharacter(Country.AMERICAN)));
		house.chechOwnership(characterDAO);
		assertEquals(CaptureStatus.NONE, house.getStatus());
		assertNull(house.getOwner());
		
		house.owner = null;
		house.captureStatus = CaptureStatus.CAPTURING;
		house.contestingOwner = Country.AMERICAN;
		when(characterDAO.getAllCharacters(100f, 1f, 100f, HouseMessage.CAPTURE_RANGE)).thenReturn(new HashSet<CharacterMessage>());
		house.chechOwnership(characterDAO);
		assertEquals(CaptureStatus.NONE, house.getStatus());
		assertNull(house.getOwner());
	}
	
	@Test
	public void testNoCaptureOfHouse(){
		HouseMessage house = new HouseMessage("type2", new Vector(100, 1, 100), new Quaternion(1, 1, 1, 1));
		assertNull(house.getOwner());
		assertEquals(CaptureStatus.NONE, house.getStatus());
		
		CharacterDAO characterDAO = mock(CharacterDAO.class);
		
		when(characterDAO.getAllCharacters(100f, 1f, 100f, HouseMessage.CAPTURE_RANGE)).thenReturn(new HashSet<CharacterMessage>());
		house.chechOwnership(characterDAO);
		assertEquals(CaptureStatus.NONE, house.getStatus());
		assertNull(house.getOwner());
		
		when(characterDAO.getAllCharacters(100f, 1f, 100f, HouseMessage.CAPTURE_RANGE)).thenReturn(ImmutableSet.of(getCharacter(Country.GERMAN), getCharacter(Country.AMERICAN)));
		house.chechOwnership(characterDAO);
		assertEquals(CaptureStatus.NONE, house.getStatus());
		assertNull(house.getOwner());
	}
	
	@Test 
	public void testDeCaptureHouse(){
		HouseMessage house = new HouseMessage("type2", new Vector(100, 1, 100), new Quaternion(1, 1, 1, 1));
		house.owner = Country.AMERICAN;
		house.captureStatus = CaptureStatus.CAPTURED;
		
		CharacterDAO characterDAO = mock(CharacterDAO.class);
		
		when(characterDAO.getAllCharacters(100f, 1f, 100f, HouseMessage.CAPTURE_RANGE*LostVictoryScene.SCENE_SCALE)).thenReturn(ImmutableSet.of(getCharacter(Country.GERMAN), getCharacter(Country.GERMAN)));
		house.chechOwnership(characterDAO);
		assertEquals(CaptureStatus.DECAPTURING, house.getStatus());
		assertEquals(Country.AMERICAN, house.getOwner());
		
		house.chechOwnership(characterDAO);
		assertEquals(CaptureStatus.DECAPTURING, house.getStatus());
		assertEquals("decapture is not instant", Country.AMERICAN, house.getOwner());
		
		house.statusChangeTime = 100l;
		when(characterDAO.getAllCharacters(100f, 1f, 100f, HouseMessage.CAPTURE_RANGE*LostVictoryScene.SCENE_SCALE)).thenReturn(ImmutableSet.of(getCharacter(Country.GERMAN), getCharacter(Country.GERMAN)));
		house.chechOwnership(characterDAO);
		assertEquals(CaptureStatus.NONE, house.getStatus());
		assertNull(house.getOwner());
	} 
	
	@Test
	public void tesDeCaptureInterupted(){
		HouseMessage house = new HouseMessage("type2", new Vector(100, 1, 100), new Quaternion(1, 1, 1, 1));
		house.owner = Country.AMERICAN;
		house.captureStatus = CaptureStatus.DECAPTURING;
		
		CharacterDAO characterDAO = mock(CharacterDAO.class);
		
		when(characterDAO.getAllCharacters(100f, 1f, 100f, HouseMessage.CAPTURE_RANGE*LostVictoryScene.SCENE_SCALE)).thenReturn(ImmutableSet.of(getCharacter(Country.GERMAN), getCharacter(Country.AMERICAN)));
		house.chechOwnership(characterDAO);
		assertEquals(CaptureStatus.CAPTURED, house.getStatus());
		assertEquals(Country.AMERICAN, house.getOwner());
		
		house.owner = Country.AMERICAN;
		house.captureStatus = CaptureStatus.DECAPTURING;
		when(characterDAO.getAllCharacters(100f, 1f, 100f, HouseMessage.CAPTURE_RANGE)).thenReturn(new HashSet<CharacterMessage>());
		house.chechOwnership(characterDAO);
		assertEquals(CaptureStatus.CAPTURED, house.getStatus());
		assertEquals(Country.AMERICAN, house.getOwner());
	}
	
	@Test
	public void testUnmannedVehicleCAntStopCapture(){
		HouseMessage house = new HouseMessage("type2", new Vector(100, 1, 100), new Quaternion(1, 1, 1, 1));
		assertNull(house.getOwner());
		assertEquals(CaptureStatus.NONE, house.getStatus());
		
		CharacterDAO characterDAO = mock(CharacterDAO.class);
		CharacterMessage v = new CharacterMessage(UUID.randomUUID(), CharacterType.ARMORED_CAR, new Vector(0, 0, 0), Country.GERMAN, Weapon.MG42, RankMessage.PRIVATE, null);
		v.passengers.clear();
		
		when(characterDAO.getAllCharacters(100f, 1f, 100f, HouseMessage.CAPTURE_RANGE*LostVictoryScene.SCENE_SCALE)).thenReturn(ImmutableSet.of(getCharacter(Country.AMERICAN), v));
		house.chechOwnership(characterDAO);
		assertEquals(CaptureStatus.CAPTURING, house.getStatus());
		assertNull(house.getOwner());
				
	}

	private CharacterMessage getCharacter(Country country) {
		return new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), country, Weapon.RIFLE, RankMessage.PRIVATE, null);
	}
}
