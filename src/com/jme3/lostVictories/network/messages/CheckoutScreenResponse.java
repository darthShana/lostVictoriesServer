package com.jme3.lostVictories.network.messages;

import java.util.Set;

public class CheckoutScreenResponse extends LostVictoryMessage{

	private Set<Character> allCharacters;

	public CheckoutScreenResponse(Set<Character> allCharacters) {
		super("CheckoutScreenResponse");
		this.allCharacters = allCharacters;
		
	}
	


}
