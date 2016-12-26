package lostVictories.messageHanders;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.BoardVehicleRequest;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;

import lostVictories.dao.CharacterDAO;

public class BoardingVehicleMessageHandler {

	private static Logger log = Logger.getLogger(BoardingVehicleMessageHandler.class); 
	private CharacterDAO characterDAO;
	private MessageRepository messageRepository;

	public BoardingVehicleMessageHandler(CharacterDAO characterDAO, MessageRepository messageRepository) {
		this.characterDAO = characterDAO;
		this.messageRepository = messageRepository;
	}

	public LostVictoryMessage handle(BoardVehicleRequest msg) throws IOException {
		CharacterMessage vehicle = characterDAO.getCharacter(msg.getVehicleID());
		CharacterMessage passenger = characterDAO.getCharacter(msg.getCharacterID());
		log.debug("recived boarding request for:"+passenger.getId());
		if(vehicle.getLocation().distance(passenger.getLocation())>7.5f){
			if(CharacterType.AVATAR == passenger.getCharacterType() && passenger.getCheckoutClient().equals(passenger.getId())){
				messageRepository.addMessage(msg.getClientID(), "Vehicle is too far to get in.");
			}
			return new LostVictoryMessage(UUID.randomUUID());
		}
		if(vehicle.getPassengers().contains(msg.getCharacterID())){
			if(CharacterType.AVATAR == passenger.getCharacterType() && passenger.getCheckoutClient().equals(passenger.getId())){
				messageRepository.addMessage(msg.getClientID(), "Avatar already onboard vehicle.");
			}
			return new LostVictoryMessage(UUID.randomUUID());
		}
		
		Map<UUID, CharacterMessage> toSave = new HashMap<UUID, CharacterMessage>();
		passenger.boardVehicle(vehicle, characterDAO, toSave);
		
		characterDAO.save(toSave.values());
		return new LostVictoryMessage(UUID.randomUUID());
	}

}
