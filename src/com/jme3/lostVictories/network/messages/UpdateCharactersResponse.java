package com.jme3.lostVictories.network.messages;

import java.util.Set;
import java.util.UUID;

public class UpdateCharactersResponse extends LostVictoryMessage{

	private Set<CharacterMessage> allCharacters;
	private Set<HouseMessage> allHouses;
	private GameStatistics gameStatistics;

	public UpdateCharactersResponse(UUID clientId, Set<CharacterMessage> allCharacters, Set<HouseMessage> allHouses, GameStatistics statistics) {
		super(clientId);
		this.allCharacters = allCharacters;
		this.allHouses = allHouses;
		this.gameStatistics = statistics;
	}

	public Set<CharacterMessage> getCharacters() {
		return allCharacters;
	}
	


}
