package lostVictories;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.Country;

public class AvatarStore {

	private final Map<UUID, CharacterMessage> allCharacters;
	private final Map<UUID, CharacterMessage> avatars;
	
	public AvatarStore(Set<CharacterMessage> allCharacters, Set<CharacterMessage> avatarSet) {
		this.allCharacters = allCharacters.stream().collect(Collectors.toMap(c->c.getId(), Function.identity()));
		this.avatars = avatarSet.stream().collect(Collectors.toMap(a->a.getId(), Function.identity()));
	}
	
	public Optional<CharacterMessage> getDeadAvatars(Country country) {
		return avatars.values().stream().filter(a->a.getCountry()==country && (!allCharacters.containsKey(a) || allCharacters.get(a.getId()).isDead())).findAny();
	}

	public boolean reincarnateAvatar(CharacterMessage deadAvatar, CharacterMessage c, Collection<CharacterMessage> updated) {
		boolean replaceWithAvatar = c.replaceWithAvatar(deadAvatar, updated);
		allCharacters.putAll(updated.stream().collect(Collectors.toMap(u->u.getId(), Function.identity())));
		return replaceWithAvatar;
	}

	public Set<CharacterMessage> getLivingAvatars() {
		return allCharacters.entrySet().stream().filter(e->avatars.containsKey(e.getKey()) && !e.getValue().isDead()).map(e->e.getValue()).collect(Collectors.toSet());
	}
	

}
