package com.jme3.lostVictories.network.messages;

import java.io.Serializable;

public class LostVictoryMessage implements Serializable{
	
	private String message;
	
	public LostVictoryMessage(String message) {
		this.message = message;
	}

	public String getMessage(){
		return message;
	}
}
