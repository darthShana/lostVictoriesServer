package lostVictories.messageHanders;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.DeathNotificationRequest;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;
import com.jme3.lostVictories.network.messages.UnClaimedEquipmentMessage;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.EquipmentDAO;

public class DeathNotificationMessageHandler {

	private static Logger log = Logger.getLogger(DeathNotificationMessageHandler.class);
	private CharacterDAO characterDAO;
	private EquipmentDAO equipmentDAO;

	public DeathNotificationMessageHandler(CharacterDAO characterDAO, EquipmentDAO equipmentDAO) {
		this.characterDAO = characterDAO;
		this.equipmentDAO = equipmentDAO;
	}

	public LostVictoryMessage handle(DeathNotificationRequest msg) {
		Map<UUID, CharacterMessage> toSave = new HashMap<UUID, CharacterMessage>();
		
		CharacterMessage victim = characterDAO.getCharacter(msg.getVictim());
		if(victim==null || victim.isDead()){
			return new LostVictoryMessage(UUID.randomUUID());
		}
		log.info("received death notification:"+victim.getId());
		
		CharacterMessage killer = characterDAO.getCharacter(msg.getKiller());
		victim.kill();
		killer.incrementKills(victim.getId());
		toSave.put(victim.getId(), victim);
		toSave.put(killer.getId(), killer);
		
		victim.replaceMe(characterDAO, toSave);
		
		if(victim.getWeapon().isReusable()){
			equipmentDAO.addUnclaiimedEquipment(new UnClaimedEquipmentMessage(UUID.randomUUID(), victim.getWeapon(), victim.getLocation(), victim.getOrientation()));
		}
		
		try {
			characterDAO.saveCommandStructure(toSave);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return new LostVictoryMessage(UUID.randomUUID());
	}

}
