package com.jme3.lostVictories.network.messages;

import java.util.Set;

public class CheckoutScreenResponse extends LostVictoryMessage{

	private Set<CharacterMessage> allCharacters;

	public CheckoutScreenResponse(Set<CharacterMessage> allCharacters) {
		super("CheckoutScreenResponse");
		this.allCharacters = allCharacters;
		
	}
	


}
