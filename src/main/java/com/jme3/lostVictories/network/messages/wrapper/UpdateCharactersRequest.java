package com.jme3.lostVictories.network.messages.wrapper;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.wrapper.LostVictoryMessage;

import java.util.Set;
import java.util.UUID;


public class UpdateCharactersRequest extends LostVictoryMessage {

	private static final long serialVersionUID = 1L;
	
	private Set<CharacterMessage> characters;
	private CharacterMessage avatar;

	private UpdateCharactersRequest(){}

	public UpdateCharactersRequest(UUID clientID) {
		super(clientID);
	}
	
	public UpdateCharactersRequest(UUID clientID, Set<CharacterMessage> characters, CharacterMessage avatar) {
		super(clientID);
		this.characters = characters;
		this.avatar = avatar;
	}

	public Set<CharacterMessage> getCharacters(){
		return characters;
	}
	
	public CharacterMessage getAvatar(){
		return avatar;
	}

}
