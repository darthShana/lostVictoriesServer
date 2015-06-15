package com.jme3.lostVictories.network.messages;

import java.util.Set;

public class CheckoutScreenResponse extends LostVictoryMessage{

	private static final long serialVersionUID = 1L;
	private Set<CharacterMessage> allCharacters;

	public CheckoutScreenResponse(Set<CharacterMessage> allCharacters) {
		super(null);
		this.allCharacters = allCharacters;
		
	}

	public Set<CharacterMessage> getCharacters() {
		return allCharacters;
	}
	


}
