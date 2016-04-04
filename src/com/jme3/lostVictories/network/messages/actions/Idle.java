package com.jme3.lostVictories.network.messages.actions;

public class Idle extends Action {

	private String type = "idle"; 
	
	@Override
	public boolean equals(Object obj) {
		return obj !=null && obj instanceof Idle;
	}
}
