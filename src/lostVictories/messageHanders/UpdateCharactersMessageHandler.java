package lostVictories.messageHanders;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CheckoutScreenResponse;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;
import com.jme3.lostVictories.network.messages.UpdateCharactersRequest;
import com.jme3.lostVictories.network.messages.Vector;

import lostVictories.CharacterDAO;

public class UpdateCharactersMessageHandler {

	private CharacterDAO characterDAO;

	public UpdateCharactersMessageHandler(CharacterDAO characterDAO) {
		this.characterDAO = characterDAO;
	}

	public LostVictoryMessage handle(UpdateCharactersRequest msg) {
		Set<CharacterMessage> allCharacters = ((UpdateCharactersRequest) msg).getCharacters();
		Map<UUID, CharacterMessage> existing = characterDAO.getAllCharacters(allCharacters.stream().map(c->c.getId()).collect(Collectors.toSet()));
		allCharacters = allCharacters.stream().filter(c->c.hasChanged(existing.get(c.getId()))).collect(Collectors.toSet());
		Map<UUID, CharacterMessage> sent = allCharacters.stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
		existing.values().stream().filter(c->c.isAvailableForUpdate(msg.getClientID())).forEach(c->c.updateState(sent.get(c.getId()), msg.getClientID(), System.currentTimeMillis()));
		characterDAO.save(existing.values());
		
		Vector v = msg.getAvatar().getLocation();
		Set<CharacterMessage> inRangeOfAvatar = characterDAO.getAllCharacters(v.x, v.y, v.z, CheckoutScreenMessageHandler.CLIENT_RANGE);
		inRangeOfAvatar.stream().filter(c->!existing.containsKey(c.getId())).forEach(c->existing.put(c.getId(), c));
		
		return new CheckoutScreenResponse(new HashSet<CharacterMessage>(existing.values()));
	}
}
