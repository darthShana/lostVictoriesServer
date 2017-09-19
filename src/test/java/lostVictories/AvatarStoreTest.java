package lostVictories;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import lostVictories.dao.CharacterDAO;

import org.junit.Before;
import org.junit.Test;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.Country;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.Weapon;

public class AvatarStoreTest {

	private CharacterDAO dao;
	private CharacterMessage s3;
	private CharacterMessage s1;
	private CharacterMessage s2;

	@Before
	public void setUp(){
		s1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null);
		s2 = new CharacterMessage(UUID.randomUUID(), CharacterType.AVATAR, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null);
		s3 = new CharacterMessage(UUID.randomUUID(), CharacterType.AVATAR, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, null);
		s3.kill();
		Set<CharacterMessage> allCharacters = new HashSet<CharacterMessage>();
		allCharacters.add(s1);
		allCharacters.add(s2);
		allCharacters.add(s3);
		
		dao = mock(CharacterDAO.class);
		when(dao.getAllCharacters()).thenReturn(allCharacters);

	}
	
	@Test
	public void testGetDeadAvatars() throws IOException {
		
//		when(mock.getCharacter(eq(s1.getId()))).thenReturn(s1);
//		when(mock.getCharacter(eq(s2.getId()))).thenReturn(s2);
//		when(mock.getCharacter(eq(s3.getId()))).thenReturn(s3);
		
		AvatarStore store = new AvatarStore(dao);
		Optional<CharacterMessage> deadAvatars = store.getDeadAvatars(Country.GERMAN);
		
		assertTrue(deadAvatars.isPresent());
		assertEquals(deadAvatars.get(), s3);

		int[] myList  = {4, 3, 7};
		new Thread();


	}
	
	@Test
	public void testReincarnateAvatar() throws IOException{
		AvatarStore store = new AvatarStore(dao);

		Optional<CharacterMessage> deadAvatars = store.getDeadAvatars(Country.GERMAN);
		
		Set<CharacterMessage> updated = new HashSet<CharacterMessage>();
		CharacterMessage reincarnateAvatar = store.reincarnateAvatar(deadAvatars.get(), s1, updated);
		
		assertTrue(reincarnateAvatar!=null);
		assertEquals(s3.getId(), updated.iterator().next().getId());
		assertFalse(updated.iterator().next().isDead());
	}

}
