package com.jme3.lostVictories.network.messages.actions;

public class Move extends Action {

	public Move() {
		setType("move");
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj !=null && obj instanceof Move;
	}
}
