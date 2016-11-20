package lostVictories;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.Country;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.Weapon;

public class AvatarStoreTest {

	@Test
	public void testGetDeadAvatars() throws IOException {
		CharacterMessage s1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null, false);
		CharacterMessage s2 = new CharacterMessage(UUID.randomUUID(), CharacterType.AVATAR, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null, false);
		CharacterMessage s3 = new CharacterMessage(UUID.randomUUID(), CharacterType.AVATAR, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null, false);
		s3.kill();
		Set<CharacterMessage> allCharacters = new HashSet<CharacterMessage>();
		allCharacters.add(s1);
		allCharacters.add(s2);
		allCharacters.add(s3);
		
		AvatarStore store = new AvatarStore(allCharacters);
		Optional<CharacterMessage> deadAvatars = store.getDeadAvatars(Country.GERMAN);
		
		assertTrue(deadAvatars.isPresent());
		assertEquals(deadAvatars.get(), s3);
		
		Set<CharacterMessage> updated = new HashSet<CharacterMessage>();
		CharacterMessage reincarnateAvatar = store.reincarnateAvatar(deadAvatars.get(), s1, updated);
		assertTrue(reincarnateAvatar!=null);
		assertEquals(s3.getId(), updated.iterator().next().getId());
		assertFalse(updated.iterator().next().isDead());
		assertFalse(store.getDeadAvatars(Country.GERMAN).isPresent());
	}

}
