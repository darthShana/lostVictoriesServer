package lostVictories.messageHanders;

import java.io.IOException;
import java.util.*;

import com.jme3.lostVictories.network.messages.wrapper.GenericLostVictoryResponse;
import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.wrapper.PassengerDeathNotificationRequest;
import com.jme3.lostVictories.network.messages.wrapper.LostVictoryMessage;

import lostVictories.dao.CharacterDAO;

public class PassengerDeathNotificationMessageHandler {

	private static Logger log = Logger.getLogger(PassengerDeathNotificationMessageHandler.class);
	private CharacterDAO characterDAO;

	public PassengerDeathNotificationMessageHandler(CharacterDAO characterDAO) {
		this.characterDAO = characterDAO;
	}

	public Set<LostVictoryMessage> handle(PassengerDeathNotificationRequest msg) throws IOException {
		Set<LostVictoryMessage> ret = new HashSet<>();
		CharacterCatch catche = new CharacterCatch(characterDAO);
		Map<UUID, CharacterMessage> toSave = new HashMap<UUID, CharacterMessage>();
		
		CharacterMessage vehicle = catche.getCharacter(msg.getVictim());
		if(vehicle==null || vehicle.isDead()){
			return ret;
		}
		log.info("received gunner death notification:"+vehicle.getId());
		
		CharacterMessage killer = catche.getCharacter(msg.getKiller());
		CharacterMessage victim = vehicle.killPassenger(catche);
		log.info("killed passenger:"+victim.getId());

		toSave.put(vehicle.getId(), vehicle);
		if(victim!=null){
			victim.kill();
			killer.incrementKills(victim.getId());
			victim.replaceMe(catche, toSave);
			toSave.put(victim.getId(), victim);
			toSave.put(killer.getId(), killer);
		}
		
		characterDAO.saveCommandStructure(toSave);
		characterDAO.refresh();
		ret.add(new GenericLostVictoryResponse());
		return ret;
	}

}
