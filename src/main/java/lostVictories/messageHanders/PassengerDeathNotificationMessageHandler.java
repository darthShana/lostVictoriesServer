package lostVictories.messageHanders;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.PassengerDeathNotificationRequest;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;

import lostVictories.dao.CharacterDAO;

public class PassengerDeathNotificationMessageHandler {

	private static Logger log = Logger.getLogger(PassengerDeathNotificationMessageHandler.class);
	private CharacterDAO characterDAO;

	public PassengerDeathNotificationMessageHandler(CharacterDAO characterDAO) {
		this.characterDAO = characterDAO;
	}

	public LostVictoryMessage handle(PassengerDeathNotificationRequest msg) throws IOException {
		CharacterCatch catche = new CharacterCatch(characterDAO);
		Map<UUID, CharacterMessage> toSave = new HashMap<UUID, CharacterMessage>();
		
		CharacterMessage vehicle = catche.getCharacter(msg.getVictim());
		if(vehicle==null || vehicle.isDead()){
			return new LostVictoryMessage(UUID.randomUUID());
		}
		log.info("received gunner death notification:"+vehicle.getId());
		
		CharacterMessage killer = catche.getCharacter(msg.getKiller());
		CharacterMessage victim = vehicle.killPassenger(catche);
		toSave.put(vehicle.getId(), vehicle);
		if(victim!=null){
			victim.kill();
			killer.incrementKills(victim.getId());
			victim.replaceMe(catche, toSave);
			toSave.put(victim.getId(), victim);
			toSave.put(killer.getId(), killer);
		}
		
		characterDAO.saveCommandStructure(toSave);
		return new LostVictoryMessage(UUID.randomUUID());
	}

}
