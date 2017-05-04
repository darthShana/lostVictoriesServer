package lostVictories.messageHanders;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import lostVictories.dao.CharacterDAO;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.jme3.lostVictories.network.messages.wrapper.BoardVehicleRequest;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.Country;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.Weapon;

public class BoardingVehicleMessageHandlerTest {

	@Test
	public void testHandle() throws IOException {
		UUID client1 = UUID.randomUUID();
		UUID client2 = UUID.randomUUID();
		
		CharacterDAO characterDAO = mock(CharacterDAO.class);
		UUID passengerID = client2;
		CharacterMessage passenger = new CharacterMessage(passengerID, CharacterType.AVATAR, new Vector(0, 0, 0), Country.AMERICAN, Weapon.RIFLE, RankMessage.PRIVATE, null);
		passenger.setCheckoutClient(client2);
		UUID vehicleID = UUID.randomUUID();
		CharacterMessage vehicle = new CharacterMessage(vehicleID, CharacterType.ARMORED_CAR, new Vector(0, 0, 0), Country.AMERICAN, Weapon.RIFLE, RankMessage.PRIVATE, null);
		vehicle.setCheckoutClient(client1);
		
		when(characterDAO.getCharacter(passengerID)).thenReturn(passenger);
		when(characterDAO.getCharacter(vehicleID)).thenReturn(vehicle);
		
		BoardingVehicleMessageHandler handler = new BoardingVehicleMessageHandler(characterDAO, mock(MessageRepository.class));
		handler.handle(new BoardVehicleRequest(client2, vehicleID, passengerID));
		
		ArgumentCaptor<Collection> toSave = ArgumentCaptor.forClass(Collection.class);
		verify(characterDAO, times(1)).save(toSave.capture());
		
		CharacterMessage vehicleToSave = (CharacterMessage) toSave.getValue().stream().filter(c->((CharacterMessage)c).getId().equals(vehicleID)).findFirst().get();
		assertEquals(vehicle, vehicleToSave);
		assertTrue(vehicleToSave.getPassengers().contains(passengerID));
		assertEquals(vehicleToSave.getCheckoutClient(), client2);
	}

}
