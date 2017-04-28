package com.jme3.lostVictories.network.messages.wrapper;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.wrapper.LostVictoryMessage;

import java.util.Set;
import java.util.UUID;


public class UpdateCharactersRequest extends LostVictoryMessage {


	private CharacterMessage character;
	private UUID avatar;
	private long clientStartTime;

	private UpdateCharactersRequest(){}

	public UpdateCharactersRequest(UUID clientID, CharacterMessage character, UUID avatar, long clientStartTime) {
		super(clientID);
		this.clientStartTime = clientStartTime;
		this.character = character;
		this.avatar = avatar;
	}

	public CharacterMessage getCharacter(){
		return character;
	}
	
	public UUID getAvatar(){
		return avatar;
	}

	public long getClientStartTime() {
		return clientStartTime;
	}

}
