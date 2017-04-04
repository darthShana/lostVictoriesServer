package lostVictories.messageHanders;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.jme3.lostVictories.network.messages.wrapper.GenericLostVictoryResponse;
import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.wrapper.AddObjectiveRequest;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.wrapper.LostVictoryMessage;

import lostVictories.dao.CharacterDAO;

public class AddObjectiveMessageHandler {
	
	private static Logger log = Logger.getLogger(AddObjectiveMessageHandler.class);

	private CharacterDAO characterDAO;

	public AddObjectiveMessageHandler(CharacterDAO characterDAO) {
		this.characterDAO = characterDAO;
	}

	public Set<LostVictoryMessage> handle(AddObjectiveRequest msg) {
		Set<LostVictoryMessage> ret = new HashSet<>();

		CharacterMessage character = characterDAO.getCharacter(msg.getCharacter());
		character.addObjective(msg.getIdentity(), msg.getObjective());
		characterDAO.putCharacter(character.getId(), character);
		ret.add(new GenericLostVictoryResponse());
		return ret;
	}



}
