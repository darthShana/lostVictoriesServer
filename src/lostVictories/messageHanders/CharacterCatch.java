package lostVictories.messageHanders;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.Country;

import lostVictories.dao.CharacterDAO;

public class CharacterCatch {

	private CharacterDAO characterDAO;
	Map<UUID, CharacterMessage> loaded = new HashMap<UUID, CharacterMessage>();


	public CharacterCatch(CharacterDAO characterDAO) {
		this.characterDAO = characterDAO;
	}


	public CharacterMessage getCharacter(UUID id) {
		if(loaded.containsKey(id)){
			return loaded.get(id);
		}
		CharacterMessage character = characterDAO.getCharacter(id);
		loaded.put(character.getId(), character);
		return character;
	}


	public Map<UUID, CharacterMessage> getAllCharacters(Set<UUID> unitsUnderCommand) {
		Map<UUID, CharacterMessage> ret = new HashMap<>();
		for(UUID id:unitsUnderCommand){
			CharacterMessage character = getCharacter(id);
			ret.put(character.getId(), character);
		}
		return ret;
	}



	
}
