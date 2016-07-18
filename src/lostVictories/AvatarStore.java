package lostVictories;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.Country;

public class AvatarStore {

	private final Map<UUID, CharacterMessage> allCharacters;
	
	
	public AvatarStore(Set<CharacterMessage> allCharacters) {
		this.allCharacters = allCharacters.stream().collect(Collectors.toMap(c->c.getId(), Function.identity()));
	}
	
	public Optional<CharacterMessage> getDeadAvatars(Country country) {
		return allCharacters.values().stream().filter(a->CharacterType.AVATAR==a.getCharacterType() && a.getCountry()==country && a.isDead()).findAny();
	}

	public CharacterMessage reincarnateAvatar(CharacterMessage deadAvatar, CharacterMessage c, Collection<CharacterMessage> updated) {
		CharacterMessage replaceWithAvatar = c.replaceWithAvatar(deadAvatar, updated, allCharacters);
		allCharacters.putAll(updated.stream().collect(Collectors.toMap(u->u.getId(), Function.identity())));
		if(replaceWithAvatar!=null){
			allCharacters.remove(c.getId());
		}
		return replaceWithAvatar;
	}

	public Set<CharacterMessage> getLivingAvatars() {
		return allCharacters.values().stream().filter(e->CharacterType.AVATAR==e.getCharacterType() && !e.isDead()).collect(Collectors.toSet());
	}
	

}
