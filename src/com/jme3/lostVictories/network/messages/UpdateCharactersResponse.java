package com.jme3.lostVictories.network.messages;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class UpdateCharactersResponse extends LostVictoryMessage{

	private Set<CharacterMessage> allCharacters;
	private Set<CharacterMessage> relatedCharacters;
	private Set<HouseMessage> allHouses;
	private GameStatistics gameStatistics;
	private AchivementStatus achivementStatus;
	private Set<UnClaimedEquipmentMessage> unClaimedEquipment;
	private List<String> messages;

	public UpdateCharactersResponse(UUID clientId, Set<CharacterMessage> allCharacters, Set<CharacterMessage> relatedCharacters, Set<UnClaimedEquipmentMessage> unClaimedEquipment, Set<HouseMessage> allHouses, GameStatistics statistics, AchivementStatus achivementStatus, List<String> messages) {
		super(clientId);
		this.allCharacters = allCharacters;
		this.relatedCharacters = relatedCharacters;
		this.unClaimedEquipment = unClaimedEquipment;
		this.allHouses = allHouses;
		this.gameStatistics = statistics;
		this.achivementStatus = achivementStatus;
		this.messages = messages;
	}

	public Set<CharacterMessage> getCharacters() {
		return allCharacters;
	}
	


}
