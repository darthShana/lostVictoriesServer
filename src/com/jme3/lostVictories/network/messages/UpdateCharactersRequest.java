package com.jme3.lostVictories.network.messages;

import java.util.Set;


public class UpdateCharactersRequest extends LostVictoryMessage {

	private Set<CharacterMessage> characters;
	
	public UpdateCharactersRequest(String clientID) {
		super(clientID);
	}
	
	public Set<CharacterMessage> getCharacters(){
		return characters;
	}

}
