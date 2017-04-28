package lostVictories.messageHanders;

import java.io.IOException;
import java.util.*;

import com.jme3.lostVictories.network.messages.wrapper.GenericLostVictoryResponse;
import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.wrapper.BoardVehicleRequest;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.wrapper.LostVictoryMessage;

import lostVictories.dao.CharacterDAO;

public class BoardingVehicleMessageHandler {

	private static Logger log = Logger.getLogger(BoardingVehicleMessageHandler.class); 
	private CharacterDAO characterDAO;
	private MessageRepository messageRepository;

	public BoardingVehicleMessageHandler(CharacterDAO characterDAO, MessageRepository messageRepository) {
		this.characterDAO = characterDAO;
		this.messageRepository = messageRepository;
	}

	public Set<LostVictoryMessage> handle(BoardVehicleRequest msg) throws IOException {
		Set<LostVictoryMessage> ret = new HashSet<>();

		CharacterMessage vehicle = characterDAO.getCharacter(msg.getVehicleID());
		CharacterMessage passenger = characterDAO.getCharacter(msg.getCharacterID());
		log.debug("recived boarding request for passenger:"+passenger.getId()+" vehicle:"+msg.getVehicleID());
		if(vehicle.getLocation().distance(passenger.getLocation())>7.5f){
			if(CharacterType.AVATAR == passenger.getCharacterType() && passenger.getCheckoutClient().equals(passenger.getId())){
				messageRepository.addMessage(msg.getClientID(), "Vehicle is too far to get in.");
				
			}
			log.debug("passenger:"+passenger.getId()+" is too far to get in");
			return ret;
		}
		if(vehicle.getPassengers().contains(msg.getCharacterID())){
			if(CharacterType.AVATAR == passenger.getCharacterType() && passenger.getCheckoutClient().equals(passenger.getId())){
				
				log.debug("passenger:"+passenger.getId()+" is is already onboard vehicle");
			}
			messageRepository.addMessage(msg.getClientID(), "Avatar already onboard vehicle.");
			return ret;
		}
		
		log.debug("passenger:"+passenger.getId()+" checkes passed about to board vehicle:"+msg.getVehicleID());
		Map<UUID, CharacterMessage> toSave = new HashMap<UUID, CharacterMessage>();
		passenger.boardVehicle(vehicle, characterDAO, toSave);
		log.debug("passenger:"+passenger.getId()+" boarding complete");
		
		if(msg.getClientID().equals(passenger.getId()) && passenger.getCharacterType()==CharacterType.AVATAR){
			log.debug("checkout boarded vehicle:"+msg.getVehicleID()+" by client:"+msg.getClientID());
			CharacterMessage savedVehicle = toSave.get(msg.getVehicleID());
			savedVehicle.setCheckoutClient(msg.getClientID());
			savedVehicle.setCheckoutTime(System.currentTimeMillis());
		}
		
		log.debug("vehicle new passegers:"+toSave.get(msg.getVehicleID()).getPassengers());
		characterDAO.save(toSave.values());
		characterDAO.refresh();
		ret.add(new GenericLostVictoryResponse());
		return ret;
	}

}
