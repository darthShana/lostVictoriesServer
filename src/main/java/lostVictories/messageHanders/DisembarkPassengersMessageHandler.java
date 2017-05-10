package lostVictories.messageHanders;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.jme3.lostVictories.network.messages.wrapper.GenericLostVictoryResponse;
import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.wrapper.DisembarkPassengersRequest;
import com.jme3.lostVictories.network.messages.wrapper.LostVictoryMessage;

import lostVictories.dao.CharacterDAO;

public class DisembarkPassengersMessageHandler {

	private static Logger log = Logger.getLogger(DisembarkPassengersMessageHandler.class);
	private CharacterDAO characterDAO;
	

	public DisembarkPassengersMessageHandler(CharacterDAO characterDAO) {
		this.characterDAO = characterDAO;
	}

	public Set<LostVictoryMessage> handle(DisembarkPassengersRequest msg) throws IOException {
		Set<LostVictoryMessage> ret = new HashSet<>();

		log.debug("received disembark request from:"+msg.getVehicleID());
		CharacterMessage vehicle = characterDAO.getCharacter(msg.getVehicleID());
		characterDAO.save(vehicle.disembarkPassengers(characterDAO, true));
		ret.add(new GenericLostVictoryResponse());
		return ret;
	}

}
