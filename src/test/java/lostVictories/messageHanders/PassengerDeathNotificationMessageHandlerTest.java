package lostVictories.messageHanders;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import lostVictories.dao.CharacterDAO;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.Country;
import com.jme3.lostVictories.network.messages.PassengerDeathNotificationRequest;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.Weapon;

public class PassengerDeathNotificationMessageHandlerTest {

	@Test
	public void testPassengerKilledByCommandingOfficer() throws IOException {
		CharacterDAO characterDAO = mock(CharacterDAO.class);
		PassengerDeathNotificationMessageHandler handler = new PassengerDeathNotificationMessageHandler(characterDAO);
		UUID coID = UUID.randomUUID();
		UUID victimID = UUID.randomUUID();
		CharacterMessage victim = new CharacterMessage(victimID, CharacterType.SOLDIER, new Vector(0, 0, 0), Country.AMERICAN, Weapon.RIFLE, RankMessage.PRIVATE, coID);
		UUID vehicleID = UUID.randomUUID();
		CharacterMessage vehicle = new CharacterMessage(vehicleID, CharacterType.ARMORED_CAR, new Vector(0, 0, 0), Country.AMERICAN, Weapon.RIFLE, RankMessage.PRIVATE, coID);
		vehicle.addPassengers(victimID);
		
		when(characterDAO.getCharacter(coID)).thenAnswer(new Answer<CharacterMessage>() {

			@Override
			public CharacterMessage answer(InvocationOnMock invocation) throws Throwable {
				CharacterMessage characterMessage = new CharacterMessage(coID, CharacterType.SOLDIER, new Vector(0, 0, 0), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, UUID.randomUUID());
				characterMessage.getUnitsUnderCommand().add(victimID);
				characterMessage.getUnitsUnderCommand().add(vehicleID);
				return characterMessage;
			}
		});
		when(characterDAO.getCharacter(victimID)).thenReturn(victim);
		when(characterDAO.getCharacter(vehicleID)).thenReturn(vehicle);
		
		handler.handle(new PassengerDeathNotificationRequest(UUID.randomUUID(), coID, vehicleID));
		ArgumentCaptor<HashMap> toSave = ArgumentCaptor.forClass(HashMap.class);
		verify(characterDAO, times(1)).saveCommandStructure(toSave.capture());
		
		HashMap<UUID, CharacterMessage> saved = toSave.getValue();
		assertTrue(saved.containsKey(coID));
		assertFalse(saved.get(coID).getUnitsUnderCommand().contains(victimID));

	}

}
