package lostVictories.messageHanders;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.DeathNotificationRequest;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;
import com.jme3.lostVictories.network.messages.UnClaimedEquipmentMessage;
import com.jme3.lostVictories.network.messages.Vector;

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
		CharacterCatch catche = new CharacterCatch(characterDAO);
		CharacterMessage victim = catche.getCharacter(msg.getVictim());
		if(victim==null || victim.isDead()){
			return new LostVictoryMessage(UUID.randomUUID());
		}
		log.info("received death notification:"+victim.getId());
		
		CharacterMessage killer = catche.getCharacter(msg.getKiller());
		victim.kill();
		killer.incrementKills(victim.getId());
		toSave.put(killer.getId(), killer);
		toSave.put(victim.getId(), victim);
		
		victim.replaceMe(catche, toSave);
		
		try {
			characterDAO.saveCommandStructure(toSave);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		if(victim.getWeapon().isReusable() && (victim.getCharacterType()==CharacterType.SOLDIER || victim.getCharacterType()==CharacterType.AVATAR)){
			equipmentDAO.addUnclaiimedEquipment(new UnClaimedEquipmentMessage(UUID.randomUUID(), victim.getWeapon(), victim.getLocation(), new Vector(0, 0, 0)));
		}
		
		return new LostVictoryMessage(UUID.randomUUID());
	}

}
