/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages.wrapper;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.jme3.lostVictories.network.messages.wrapper.LostVictoryMessage;

import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class BoardVehicleRequest extends LostVictoryMessage {
    private UUID vehicleID;
    private UUID characterID;

    private  BoardVehicleRequest(){}

    public BoardVehicleRequest(UUID clientID, UUID vehicleID, UUID characterID) {
        super(clientID);
        this.vehicleID = vehicleID;
        this.characterID = characterID;
        
    }

	public UUID getVehicleID() {
		return vehicleID;
	}

	public UUID getCharacterID() {
		return characterID;
	}
    
}
