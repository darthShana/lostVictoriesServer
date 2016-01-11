package lostVictories.messageHanders;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.EquipmentCollectionRequest;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;
import com.jme3.lostVictories.network.messages.UnClaimedEquipmentMessage;
import com.jme3.lostVictories.network.messages.Weapon;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.EquipmentDAO;

public class CollectEquipmentMessageHandler {

	private static Logger log = Logger.getLogger(EquipmentCollectionRequest.class);
	private CharacterDAO characterDAO;
	private EquipmentDAO equipmentDAO;

	public CollectEquipmentMessageHandler(CharacterDAO characterDAO, EquipmentDAO equipmentDAO) {
		this.characterDAO = characterDAO;
		this.equipmentDAO = equipmentDAO;
	}

	public LostVictoryMessage handle(EquipmentCollectionRequest msg) {
		UnClaimedEquipmentMessage equipment = equipmentDAO.get(msg.getEquipmentId());
		CharacterMessage character = characterDAO.getCharacter(msg.getCharacterID());
		log.info("received equipment pickup:"+msg.getEquipmentId()+" for character "+msg.getCharacterID());
		
		if(equipment.getLocation().toVector().distance(character.getLocation().toVector())>1){
			return new LostVictoryMessage(UUID.randomUUID());
		}
		
		Weapon drop = character.switchWeapon(equipment);
		if(drop!=null){
			equipmentDAO.addUnclaiimedEquipment(new UnClaimedEquipmentMessage(UUID.randomUUID(), drop, character.getLocation(), character.getOrientation()));
		}
		equipmentDAO.delete(equipment);
		characterDAO.putCharacter(character.getId(), character);
		return new LostVictoryMessage(UUID.randomUUID());
	}

}
