package lostVictories.messageHanders;


import java.util.UUID;

import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.AddObjectiveRequest;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.Country;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;
import com.jme3.lostVictories.network.messages.RankMessage;

import lostVictories.dao.CharacterDAO;

public class AddObjectiveMessageHandler {
	
	private static Logger log = Logger.getLogger(AddObjectiveMessageHandler.class);

	private CharacterDAO characterDAO;

	public AddObjectiveMessageHandler(CharacterDAO characterDAO) {
		this.characterDAO = characterDAO;
	}

	public LostVictoryMessage handle(AddObjectiveRequest msg) {
		CharacterMessage character = characterDAO.getCharacter(msg.getCharacter());
		character.addObjective(msg.getIdentity(), msg.getObjective());
		characterDAO.putCharacter(character.getId(), character);
		return new LostVictoryMessage(UUID.randomUUID());
	}



}