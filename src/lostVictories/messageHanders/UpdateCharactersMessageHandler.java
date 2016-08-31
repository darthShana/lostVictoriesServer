package lostVictories.messageHanders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.AchivementStatus;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.GameStatistics;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;
import com.jme3.lostVictories.network.messages.UnClaimedEquipmentMessage;
import com.jme3.lostVictories.network.messages.UpdateCharactersRequest;
import com.jme3.lostVictories.network.messages.UpdateCharactersResponse;
import com.jme3.lostVictories.network.messages.Vector;

import lostVictories.WorldRunner;
import lostVictories.dao.CharacterDAO;
import lostVictories.dao.EquipmentDAO;
import lostVictories.dao.HouseDAO;

public class UpdateCharactersMessageHandler {

	private CharacterDAO characterDAO;
	private static Logger log = Logger.getLogger(UpdateCharactersMessageHandler.class);
	private HouseDAO houseDAO;
	private EquipmentDAO equipmentDAO;
	private WorldRunner worldRunner;
	private MessageRepository messageRepository;

	public UpdateCharactersMessageHandler(CharacterDAO characterDAO, HouseDAO houseDAO, EquipmentDAO equipmentDAO, WorldRunner worldRunner, MessageRepository messageRepository) {
		this.characterDAO = characterDAO;
		this.houseDAO = houseDAO;
		this.equipmentDAO = equipmentDAO;
		this.worldRunner = worldRunner;
		this.messageRepository = messageRepository;
	}

	public LostVictoryMessage handle(UpdateCharactersRequest msg) throws IOException {
		Set<CharacterMessage> allCharacter = ((UpdateCharactersRequest) msg).getCharacters();
		log.trace("client sending "+allCharacter.size()+" characters to update");
		Map<UUID, CharacterMessage> sentFromClient = allCharacter.stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
		Map<UUID, CharacterMessage> existingInServer = characterDAO.getAllCharacters(allCharacter.stream().map(c->c.getId()).collect(Collectors.toSet()));
		
		Set<UUID> hasChanged = sentFromClient.values().stream()
				.filter(c->c.hasChanged(existingInServer.get(c.getId())))
			.map(CharacterMessage::getId).collect(Collectors.toSet());
		
		Map<UUID, CharacterMessage> toSave = existingInServer.values().stream()
				.filter(c->c.isAvailableForUpdate(msg.getClientID()))
				.filter(c->hasChanged.contains(c.getId()))
			.collect(Collectors.toMap(c->c.getId(), Function.identity()));
		
		toSave.values().stream().forEach(c->c.updateState(sentFromClient.get(c.getId()), msg.getClientID(), System.currentTimeMillis()));
		characterDAO.updateCharacterState(toSave);
		
		Map<UUID, CharacterMessage> toReturn;
		Set<HouseMessage> allHouses = houseDAO.getAllHouses();
		if(msg.getAvatar()!=null){
			CharacterMessage storedAvatar = characterDAO.getCharacter(msg.getAvatar().getId());
			Vector v = (storedAvatar!=null)?storedAvatar.getLocation():msg.getAvatar().getLocation();
			Map<UUID, CharacterMessage> inRangeOfAvatar = characterDAO.getAllCharacters(v.x, v.y, v.z, CheckoutScreenMessageHandler.CLIENT_RANGE).stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
			log.trace("found in range on avatar:"+v+" units: "+inRangeOfAvatar.size());
			
			toReturn = existingInServer.entrySet().stream()
					.filter(entry->inRangeOfAvatar.containsKey(entry.getKey()))
					.collect(Collectors.toMap(p->p.getKey(), p->p.getValue()));
			inRangeOfAvatar.values().stream().filter(c->!existingInServer.containsKey(c.getId())).forEach(c->toReturn.put(c.getId(), c));
			
			Set<CharacterMessage> relatedCharacters = toReturn.values().stream()
				.map(c->c.getUnitsUnderCommand()).filter(u->!toReturn.containsKey(u))
				.map(u->characterDAO.getAllCharacters(u).values()).flatMap(l->l.stream()).collect(Collectors.toSet());
			
			
			GameStatistics statistics = worldRunner.getStatistics(storedAvatar.getCountry());
			AchivementStatus achivementStatus = worldRunner.getAchivementStatus(storedAvatar);
			
			Set<UnClaimedEquipmentMessage> unClaimedEquipment = equipmentDAO.getUnClaimedEquipment(v.x, v.y, v.z, CheckoutScreenMessageHandler.CLIENT_RANGE);
			return new UpdateCharactersResponse(msg.getClientID(), new HashSet<CharacterMessage>(toReturn.values()), relatedCharacters, unClaimedEquipment, allHouses, statistics, achivementStatus, messageRepository.popMessages(msg.getClientID()));
		}else{
			toReturn = existingInServer;
			log.debug("client did not send avatar for perspective");
		}
		
		log.debug("sending back characters:"+toReturn.size());
		return new UpdateCharactersResponse(msg.getClientID(), new HashSet<CharacterMessage>(toReturn.values()), new HashSet<CharacterMessage>(), new HashSet<UnClaimedEquipmentMessage>(), allHouses, null, null, new ArrayList<String>());
	}
}
