package com.jme3.lostVictories.network.messages;

import java.util.Set;
import java.util.UUID;

public class UpdateCharactersResponse extends LostVictoryMessage{

	private Set<CharacterMessage> allCharacters;

	public UpdateCharactersResponse(UUID clientId, Set<CharacterMessage> allCharacters) {
		super(clientId);
		this.allCharacters = allCharacters;
		
	}

	public Set<CharacterMessage> getCharacters() {
		return allCharacters;
	}
	


}
