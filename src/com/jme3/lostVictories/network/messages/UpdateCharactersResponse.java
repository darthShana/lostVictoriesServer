package com.jme3.lostVictories.network.messages;

import java.util.Set;
import java.util.UUID;

public class UpdateCharactersResponse extends LostVictoryMessage{

	private Set<CharacterMessage> allCharacters;
	private Set<CharacterMessage> relatedCharacters;
	private Set<HouseMessage> allHouses;
	private GameStatistics gameStatistics;

	public UpdateCharactersResponse(UUID clientId, Set<CharacterMessage> allCharacters, Set<CharacterMessage> relatedCharacters, Set<HouseMessage> allHouses, GameStatistics statistics) {
		super(clientId);
		this.allCharacters = allCharacters;
		this.relatedCharacters = relatedCharacters;
		this.allHouses = allHouses;
		this.gameStatistics = statistics;
	}

	public Set<CharacterMessage> getCharacters() {
		return allCharacters;
	}
	


}
