package com.jme3.lostVictories.network.messages;

import java.io.Serializable;
import java.util.UUID;

public class LostVictoryMessage implements Serializable{
	protected static final long serialVersionUID = -2422514305655908187L;

	private UUID clientID;

	
	public LostVictoryMessage(UUID clientID) {
		this.clientID = clientID;
	}

	public UUID getClientID(){
		return clientID;
	}
}
