package lostVictories;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.Country;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.network.messages.Weapon;

public class AvatarStore {

	private static Map<UUID, Country> avatars = new HashMap<UUID, Country>();
	private Map<UUID, CharacterMessage> allCharacters;
	
	static{
		avatars.put(UUID.fromString("2fbe421f-f701-49c9-a0d4-abb0fa904204"), Country.GERMAN);
	}

	public AvatarStore(Set<CharacterMessage> allCharacters) {
		this.allCharacters = allCharacters.stream().collect(Collectors.toMap(c->c.getId(), Function.identity()));
	}
	
	public Optional<UUID> getDeadAvatars(Country country) {
		return avatars.keySet().stream().filter(a->avatars.get(a)==country && (!allCharacters.containsKey(a) || allCharacters.get(a).isDead())).findAny();
	}

	public CharacterMessage reincarnateAvatar(UUID uuid, CharacterMessage c) {
		return c.replaceWithAvatar(uuid);
	}

}
