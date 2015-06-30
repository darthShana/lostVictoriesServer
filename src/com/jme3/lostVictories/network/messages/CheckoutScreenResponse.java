package com.jme3.lostVictories.network.messages;

import java.util.Set;

public class CheckoutScreenResponse extends LostVictoryMessage{

	private static final long serialVersionUID = 1L;
	private Set<CharacterMessage> allCharacters;
	private Set<HouseMessage> allHouses;

	public CheckoutScreenResponse(Set<CharacterMessage> allCharacters, Set<HouseMessage> allHouses) {
		super(null);
		this.allCharacters = allCharacters;
		this.allHouses = allHouses;
		
	}

	public Set<CharacterMessage> getCharacters() {
		return allCharacters;
	}
	


}
