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
	private final Map<UUID, Country> avatars;
	
	public AvatarStore(Set<CharacterMessage> allCharacters, Set<CharacterMessage> avatarSet) {
		this.allCharacters = allCharacters.stream().collect(Collectors.toMap(c->c.getId(), Function.identity()));
		this.avatars = avatarSet.stream().collect(Collectors.toMap(a->a.getId(), a->a.getCountry()));
	}
	
	public Optional<UUID> getDeadAvatars(Country country) {
		return avatars.keySet().stream().filter(a->avatars.get(a)==country && (!allCharacters.containsKey(a) || allCharacters.get(a).isDead())).findAny();
	}

	public boolean reincarnateAvatar(UUID uuid, CharacterMessage c, Collection<CharacterMessage> updated) {
		boolean replaceWithAvatar = c.replaceWithAvatar(uuid, updated);
		allCharacters.putAll(updated.stream().collect(Collectors.toMap(u->u.getId(), Function.identity())));
		return replaceWithAvatar;
	}

	public Set<CharacterMessage> getLivingAvatars() {
		return allCharacters.entrySet().stream().filter(e->avatars.containsKey(e.getKey()) && !e.getValue().isDead()).map(e->e.getValue()).collect(Collectors.toSet());
	}
	
	public Country getAvatarCountry(UUID id){
		return avatars.get(id);
	}

}
