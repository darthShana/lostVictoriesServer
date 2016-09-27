package lostVictories.messageHanders;

import java.util.UUID;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.DisembarkPassengersRequest;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;

import lostVictories.dao.CharacterDAO;

public class DisembarkPassengersMessageHandler {

	private CharacterDAO characterDAO;

	public DisembarkPassengersMessageHandler(CharacterDAO characterDAO) {
		this.characterDAO = characterDAO;
	}

	public LostVictoryMessage handle(DisembarkPassengersRequest msg) {
		CharacterMessage vehicle = characterDAO.getCharacter(msg.getVehicleID());
		characterDAO.save(vehicle.disembarkPassengers(characterDAO, true));
		return new LostVictoryMessage(UUID.randomUUID());
	}

}
