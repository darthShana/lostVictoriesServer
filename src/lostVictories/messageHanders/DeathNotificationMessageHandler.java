package lostVictories.messageHanders;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.DeathNotificationRequest;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;

import lostVictories.CharacterDAO;

public class DeathNotificationMessageHandler {

	private static Logger log = Logger.getLogger(DeathNotificationMessageHandler.class);
	private CharacterDAO characterDAO;

	public DeathNotificationMessageHandler(CharacterDAO characterDAO) {
		this.characterDAO = characterDAO;
	}

	public LostVictoryMessage handle(DeathNotificationRequest msg) {
		Set<CharacterMessage> toSave = new HashSet<CharacterMessage>();
		
		CharacterMessage character = characterDAO.getCharacter(msg.getVictim());
		log.debug("received death notification:"+character.getId());
		character.kill();
		toSave.add(character);
		
		Set<CharacterMessage> replacement = character.replaceMe(characterDAO);
		toSave.addAll(replacement);
		
		characterDAO.save(toSave);
		return new LostVictoryMessage(UUID.randomUUID());
	}

}
