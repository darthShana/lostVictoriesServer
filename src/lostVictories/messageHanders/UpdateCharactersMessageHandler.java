package lostVictories.messageHanders;

import java.io.IOException;
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
import com.jme3.lostVictories.network.messages.LostVictoryMessage;
import com.jme3.lostVictories.network.messages.UpdateCharactersRequest;
import com.jme3.lostVictories.network.messages.UpdateCharactersResponse;
import com.jme3.lostVictories.network.messages.Vector;

import lostVictories.AvatarStore;
import lostVictories.WorldRunner;
import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

public class UpdateCharactersMessageHandler {

	private CharacterDAO characterDAO;
	private static Logger log = Logger.getLogger(UpdateCharactersMessageHandler.class);
	private HouseDAO houseDAO;

	public UpdateCharactersMessageHandler(CharacterDAO characterDAO, HouseDAO houseDAO) {
		this.characterDAO = characterDAO;
		this.houseDAO = houseDAO;
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
			
			GameStatistics statistics = WorldRunner.instance(characterDAO, houseDAO).getStatistics(AvatarStore.getAvatarCountry(msg.getAvatar().getId()));
			AchivementStatus achivementStatus = WorldRunner.instance(characterDAO, houseDAO).getAchivementStatus(storedAvatar);
			return new UpdateCharactersResponse(msg.getClientID(), new HashSet<CharacterMessage>(toReturn.values()), relatedCharacters, houseDAO.getAllHouses(), statistics, achivementStatus);
		}else{
			toReturn = existingInServer;
			log.debug("client did not send avatar for perspective");
		}
		log.debug("sending back characters:"+toReturn.size());
		return new UpdateCharactersResponse(msg.getClientID(), new HashSet<CharacterMessage>(toReturn.values()), new HashSet<CharacterMessage>(), houseDAO.getAllHouses(), null, null);
	}
}
