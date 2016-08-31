package lostVictories.messageHanders;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.EquipmentCollectionRequest;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;
import com.jme3.lostVictories.network.messages.UnClaimedEquipmentMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.Weapon;
import com.jme3.math.Vector3f;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.EquipmentDAO;

public class CollectEquipmentMessageHandler {

	private static Logger log = Logger.getLogger(EquipmentCollectionRequest.class);
	private CharacterDAO characterDAO;
	private EquipmentDAO equipmentDAO;
	private MessageRepository messageRepository;

	public CollectEquipmentMessageHandler(CharacterDAO characterDAO, EquipmentDAO equipmentDAO, MessageRepository messageRepository) {
		this.characterDAO = characterDAO;
		this.equipmentDAO = equipmentDAO;
		this.messageRepository = messageRepository;
	}

	public LostVictoryMessage handle(EquipmentCollectionRequest msg) {
		UnClaimedEquipmentMessage equipment = equipmentDAO.get(msg.getEquipmentId());
		CharacterMessage character = characterDAO.getCharacter(msg.getCharacterID());
		log.info("received equipment pickup:"+msg.getEquipmentId()+" for character "+msg.getCharacterID());
		Vector3f l1 = new Vector3f(equipment.getLocation().x, 0, equipment.getLocation().z);
		Vector3f l2 = new Vector3f(character.getLocation().x, 0, character.getLocation().z);
		if(l1.distance(l2)>2){
			if(CharacterType.AVATAR == character.getCharacterType()){
				messageRepository.addMessage(msg.getClientID(), "Item is too far to collect.");
			}
			return new LostVictoryMessage(UUID.randomUUID());
		}
		
		Weapon drop = character.switchWeapon(equipment);
		if(drop!=null){
			equipmentDAO.addUnclaiimedEquipment(new UnClaimedEquipmentMessage(UUID.randomUUID(), drop, character.getLocation(), new Vector(0, 0, 0)));
		}
		equipmentDAO.delete(equipment);
		characterDAO.putCharacter(character.getId(), character);
		return new LostVictoryMessage(UUID.randomUUID());
	}

}
