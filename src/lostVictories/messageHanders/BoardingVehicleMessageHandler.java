package lostVictories.messageHanders;

import java.util.UUID;

import com.jme3.lostVictories.network.messages.BoardVehicleRequest;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;

import lostVictories.dao.CharacterDAO;

public class BoardingVehicleMessageHandler {

	private CharacterDAO characterDAO;

	public BoardingVehicleMessageHandler(CharacterDAO characterDAO) {
		this.characterDAO = characterDAO;
	}

	public LostVictoryMessage handle(BoardVehicleRequest msg) {
		CharacterMessage vehicle = characterDAO.getCharacter(msg.getVehicleID());
		CharacterMessage passenger = characterDAO.getCharacter(msg.getCharacterID());
		
		passenger.boardVehicle(vehicle);
		
		characterDAO.putCharacter(vehicle.getId(), vehicle);
		characterDAO.putCharacter(passenger.getId(), passenger);
		
		return new LostVictoryMessage(UUID.randomUUID());
	}

}
