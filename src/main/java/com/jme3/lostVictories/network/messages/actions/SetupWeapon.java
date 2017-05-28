package com.jme3.lostVictories.network.messages.actions;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use=Id.CLASS, include=As.PROPERTY, property="class")
public class SetupWeapon extends Action {

	public SetupWeapon() {
		super("setupWeapon");
	}

	@Override
	public com.lostVictories.api.Action toMessage() {
		com.lostVictories.api.Action.Builder builder = com.lostVictories.api.Action.newBuilder();
		builder.setActionType(com.lostVictories.api.Action.ActionType.SETUP_WEAPON);
		return builder.build();
	}

}
