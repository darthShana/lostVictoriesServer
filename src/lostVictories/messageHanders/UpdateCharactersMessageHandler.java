package lostVictories.messageHanders;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;
import com.jme3.lostVictories.network.messages.UpdateCharactersRequest;
import com.jme3.lostVictories.network.messages.UpdateCharactersResponse;
import com.jme3.lostVictories.network.messages.Vector;

import lostVictories.CharacterDAO;

public class UpdateCharactersMessageHandler {

	private CharacterDAO characterDAO;
	private static Logger log = Logger.getLogger(UpdateCharactersMessageHandler.class);

	public UpdateCharactersMessageHandler(CharacterDAO characterDAO) {
		this.characterDAO = characterDAO;
	}

	public LostVictoryMessage handle(UpdateCharactersRequest msg) {
		Set<CharacterMessage> allCharacter = ((UpdateCharactersRequest) msg).getCharacters();
		log.debug("client sending "+allCharacter.size()+" characters to update");
		Map<UUID, CharacterMessage> sentFromClient = allCharacter.stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
		Map<UUID, CharacterMessage> existingInServer = characterDAO.getAllCharacters(allCharacter.stream().map(c->c.getId()).collect(Collectors.toSet()));
		Set<UUID> hasChanged = sentFromClient.values().stream().filter(c->c.hasChanged(existingInServer.get(c.getId()))).map(CharacterMessage::getId).collect(Collectors.toSet());
		Collection<CharacterMessage> toSave = existingInServer.values().stream().filter(c->c.isAvailableForUpdate(msg.getClientID())).filter(c->hasChanged.contains(c.getId())).collect(Collectors.toList());
		toSave.stream().forEach(c->c.updateState(sentFromClient.get(c.getId()), msg.getClientID(), System.currentTimeMillis()));
		characterDAO.save(toSave);
		
		Map<UUID, CharacterMessage> toReturn;
		if(msg.getAvatar()!=null){
			Vector v = msg.getAvatar().getLocation();
			Map<UUID, CharacterMessage> inRangeOfAvatar = characterDAO.getAllCharacters(v.x, v.y, v.z, CheckoutScreenMessageHandler.CLIENT_RANGE).stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
			log.debug("found in range on avatar:"+v+" units: "+inRangeOfAvatar.size());
			toReturn = existingInServer.entrySet().stream().filter(entry->inRangeOfAvatar.containsKey(entry.getKey())).collect(Collectors.toMap(p->p.getKey(), p->p.getValue()));
			inRangeOfAvatar.values().stream().filter(c->!existingInServer.containsKey(c.getId())).forEach(c->toReturn.put(c.getId(), c));
		}else{
			toReturn = existingInServer;
			log.debug("client did not send avatar for perspective");
		}
		log.debug("sending back characters:"+toReturn.size());
		return new UpdateCharactersResponse(msg.getClientID(), new HashSet<CharacterMessage>(toReturn.values()));
	}
}
