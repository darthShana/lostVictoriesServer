package com.jme3.lostVictories.network.messages.wrapper;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.jme3.lostVictories.network.messages.wrapper.LostVictoryMessage;

import java.util.UUID;

public class EquipmentCollectionRequest extends LostVictoryMessage {

    private UUID equipmentID;
    private UUID characterID;

    private EquipmentCollectionRequest(){}

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
