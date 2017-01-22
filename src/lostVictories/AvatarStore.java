package lostVictories;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lostVictories.dao.CharacterDAO;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.Country;

public class AvatarStore {

	private final CharacterDAO characterDAO;
	
	
	public AvatarStore(CharacterDAO characterDAO) {
		this.characterDAO = characterDAO;
	}
	
	public Optional<CharacterMessage> getDeadAvatars(Country country) {
		return characterDAO.getAllCharacters().stream().filter(a->CharacterType.AVATAR==a.getCharacterType() && a.getCountry()==country && a.isDead()).findAny();
	}

	public CharacterMessage reincarnateAvatar(CharacterMessage deadAvatar, CharacterMessage c, Collection<CharacterMessage> updated) throws IOException {
		CharacterMessage replaceWithAvatar = c.replaceWithAvatar(deadAvatar, updated, characterDAO);
		return replaceWithAvatar;
	}

	public Set<CharacterMessage> getLivingAvatars() {
		return characterDAO.getAllCharacters().stream().filter(e->CharacterType.AVATAR==e.getCharacterType() && !e.isDead()).collect(Collectors.toSet());
	}
	

}
