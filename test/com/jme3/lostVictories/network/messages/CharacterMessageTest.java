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
		
		assertEquals(oldCo.id, vehicle.getCommandingOfficer());
		assertTrue(oldCo.unitsUnderCommand.contains(vehicle.id));
		
		CharacterDAO mock = mock(CharacterDAO.class);
		when(mock.getCharacter(eq(oldCo.id))).thenReturn(oldCo);
		
		avatar.boardVehicle(vehicle, mock, new HashMap<UUID, CharacterMessage>());
		assertEquals(avatarID, vehicle.getCommandingOfficer());
		assertEquals(avatar.getCountry(), vehicle.getCountry());
		assertFalse(oldCo.unitsUnderCommand.contains(vehicle.id));
	}

}
