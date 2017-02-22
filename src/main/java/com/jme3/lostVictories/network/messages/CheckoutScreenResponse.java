package com.jme3.lostVictories.network.messages;

import java.util.Set;

public class CheckoutScreenResponse extends LostVictoryMessage{

	private static final long serialVersionUID = 1L;
	private Set<CharacterMessage> allCharacters;
	private Set<HouseMessage> allHouses;
	private Set<UnClaimedEquipmentMessage> allEquipment;

	public CheckoutScreenResponse(Set<CharacterMessage> allCharacters, Set<HouseMessage> allHouses, Set<UnClaimedEquipmentMessage> allEquipment) {
		super(null);
		this.allCharacters = allCharacters;
		this.allHouses = allHouses;
		this.allEquipment = allEquipment;
		
	}

	public Set<CharacterMessage> getCharacters() {
		return allCharacters;
	}
	


}
