package lostVictories.messageHanders;

import java.io.IOException;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.DisembarkPassengersRequest;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;

import lostVictories.dao.CharacterDAO;

public class DisembarkPassengersMessageHandler {

	private static Logger log = Logger.getLogger(DisembarkPassengersMessageHandler.class);
	private CharacterDAO characterDAO;
	

	public DisembarkPassengersMessageHandler(CharacterDAO characterDAO) {
		this.characterDAO = characterDAO;
	}

	public LostVictoryMessage handle(DisembarkPassengersRequest msg) throws IOException {
		log.debug("received disembark request from:"+msg.getVehicleID());
		CharacterMessage vehicle = characterDAO.getCharacter(msg.getVehicleID());
		characterDAO.save(vehicle.disembarkPassengers(characterDAO, true));
		return new LostVictoryMessage(UUID.randomUUID());
	}

}
