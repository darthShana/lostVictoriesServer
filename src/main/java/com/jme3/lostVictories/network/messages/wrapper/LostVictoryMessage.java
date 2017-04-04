package com.jme3.lostVictories.network.messages.wrapper;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.UUID;

@JsonTypeInfo(use= JsonTypeInfo.Id.MINIMAL_CLASS, include= JsonTypeInfo.As.PROPERTY, property="class")
public abstract class LostVictoryMessage implements Serializable{

	private UUID clientID;

	public  LostVictoryMessage(){}

	public LostVictoryMessage(UUID clientID) {
		this.clientID = clientID;
	}

	public UUID getClientID(){
		return clientID;
	}

}
