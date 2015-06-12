package com.jme3.lostVictories.network.messages;

import java.io.Serializable;

public class LostVictoryMessage implements Serializable{
	
	private String clientID;
	
	public LostVictoryMessage(String clientID) {
		this.clientID = clientID;
	}

	public String getClientID(){
		return clientID;
	}
}
