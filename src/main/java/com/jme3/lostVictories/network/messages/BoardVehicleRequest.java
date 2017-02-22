/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages;

import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class BoardVehicleRequest extends LostVictoryMessage{
    private final UUID vehicleID;
    private final UUID characterID;

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
