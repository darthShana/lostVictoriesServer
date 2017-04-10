package com.jme3.lostVictories.network.messages;

import com.jme3.lostVictories.network.messages.wrapper.LostVictoryMessage;

import java.util.UUID;

public class EquipmentCollectionRequest extends LostVictoryMessage {

    private final UUID equipmentID;
    private final UUID characterID;

    public EquipmentCollectionRequest(UUID clientID, UUID equipmentID, UUID characterID) {
        super(clientID);
        this.equipmentID = equipmentID;
        this.characterID = characterID;
        
    }

	public UUID getEquipmentId() {
		return equipmentID;
	}

	public UUID getCharacterID() {
		return characterID;
	}
}
