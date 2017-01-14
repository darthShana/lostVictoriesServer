package com.jme3.lostVictories.network.messages.actions;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use=Id.CLASS, include=As.PROPERTY, property="class")
public class Move extends Action {

	public Move() {
		super("move");
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj !=null && obj instanceof Move;
	}
}
