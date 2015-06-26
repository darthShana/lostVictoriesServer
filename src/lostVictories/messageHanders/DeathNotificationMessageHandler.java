package lostVictories.messageHanders;

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
		CharacterMessage character = characterDAO.getCharacter(msg.getVictim());
		character.kill();
		characterDAO.putCharacter(character.getId(), character.getCheckoutClient(), character);
		return new LostVictoryMessage(UUID.randomUUID());
	}

}
