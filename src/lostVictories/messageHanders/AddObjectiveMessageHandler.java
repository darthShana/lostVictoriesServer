package lostVictories.messageHanders;

import java.util.UUID;

import com.jme3.lostVictories.network.messages.AddObjectiveRequest;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;

import lostVictories.CharacterDAO;

public class AddObjectiveMessageHandler {

	private CharacterDAO characterDAO;

	public AddObjectiveMessageHandler(CharacterDAO characterDAO) {
		this.characterDAO = characterDAO;
		
	}

	public LostVictoryMessage handle(AddObjectiveRequest msg) {
		CharacterMessage character = characterDAO.getCharacter(msg.getCharacter());
		character.addObjective(msg.getIdentity(), msg.getObjective());
		characterDAO.putCharacter(character.getId(), msg.getClientID(), character);
		return new LostVictoryMessage(UUID.randomUUID());
	}

}
