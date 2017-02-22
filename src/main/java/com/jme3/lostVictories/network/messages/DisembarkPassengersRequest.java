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
public class DisembarkPassengersRequest extends LostVictoryMessage{
    private final UUID vehicleID;


    public DisembarkPassengersRequest(UUID clientID, UUID vehicleID) {
        super(clientID);
        this.vehicleID = vehicleID;
    }


	public UUID getVehicleID() {
		return vehicleID;
	}
    
}