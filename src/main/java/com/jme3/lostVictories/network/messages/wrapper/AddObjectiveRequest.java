package com.jme3.lostVictories.network.messages.wrapper;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.jme3.lostVictories.network.messages.wrapper.LostVictoryMessage;

import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class AddObjectiveRequest extends LostVictoryMessage {
    
    UUID characterId;
    UUID identity;
    String objectives;

    private  AddObjectiveRequest(){}

    public AddObjectiveRequest(UUID clientID, UUID characterId, UUID identity, String toJson) {
    	super(clientID);
    	this.identity = identity;
        this.characterId = characterId;
        objectives = toJson;
    }

	public UUID getCharacter() {
		return characterId;
	}

	public String getObjective() {
		return objectives;
	}

	public UUID getIdentity() {
		return identity;
	}
    
}

