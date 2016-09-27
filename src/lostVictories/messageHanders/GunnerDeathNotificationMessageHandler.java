package lostVictories.messageHanders;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.GunnerDeathNotificationRequest;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;

import lostVictories.dao.CharacterDAO;

public class GunnerDeathNotificationMessageHandler {

	private static Logger log = Logger.getLogger(GunnerDeathNotificationMessageHandler.class);
	private CharacterDAO characterDAO;

	public GunnerDeathNotificationMessageHandler(CharacterDAO characterDAO) {
		this.characterDAO = characterDAO;
	}

	public LostVictoryMessage handle(GunnerDeathNotificationRequest msg) throws IOException {
		Map<UUID, CharacterMessage> toSave = new HashMap<UUID, CharacterMessage>();
		
		CharacterMessage vehicle = characterDAO.getCharacter(msg.getVictim());
		if(vehicle==null || vehicle.isDead()){
			return new LostVictoryMessage(UUID.randomUUID());
		}
		log.info("received gunner death notification:"+vehicle.getId());
		
		CharacterMessage killer = characterDAO.getCharacter(msg.getKiller());
		CharacterMessage victim = vehicle.killGunner(characterDAO);
		toSave.put(vehicle.getId(), vehicle);
		if(victim!=null){
			victim.kill();
			killer.incrementKills(victim.getId());
			victim.replaceMe(characterDAO, toSave);
			toSave.put(victim.getId(), victim);
			toSave.put(killer.getId(), killer);
		}
		
		characterDAO.saveCommandStructure(toSave);
		return new LostVictoryMessage(UUID.randomUUID());
	}

}
