package lostVictories.messageHanders;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.jme3.lostVictories.network.messages.BoardVehicleRequest;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;

import lostVictories.dao.CharacterDAO;

public class BoardingVehicleMessageHandler {

	private CharacterDAO characterDAO;
	private MessageRepository messageRepository;

	public BoardingVehicleMessageHandler(CharacterDAO characterDAO, MessageRepository messageRepository) {
		this.characterDAO = characterDAO;
		this.messageRepository = messageRepository;
	}

	public LostVictoryMessage handle(BoardVehicleRequest msg) {
		CharacterMessage vehicle = characterDAO.getCharacter(msg.getVehicleID());
		CharacterMessage passenger = characterDAO.getCharacter(msg.getCharacterID());
		
		if(vehicle.getLocation().distance(passenger.getLocation())>5){
			if(CharacterType.AVATAR == passenger.getCharacterType()){
				messageRepository.addMessage(msg.getClientID(), "Vehicle is too far to get in.");
			}
			return new LostVictoryMessage(UUID.randomUUID());
		}
		Map<UUID, CharacterMessage> toSave = new HashMap<UUID, CharacterMessage>();
		passenger.boardVehicle(vehicle, characterDAO, toSave);
		
		characterDAO.save(toSave.values());
		return new LostVictoryMessage(UUID.randomUUID());
	}

}
