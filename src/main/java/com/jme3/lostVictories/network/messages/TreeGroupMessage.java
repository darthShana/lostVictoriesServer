package com.jme3.lostVictories.network.messages;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TreeGroupMessage {

	private UUID id;
	private Vector location;
	private Set<TreeMessage> trees = new HashSet<>();
	
	private TreeGroupMessage(){}
	
	public TreeGroupMessage(UUID id, Map<String, Object> source) {
		this.id = id;
		this.location = (Vector) source.get("location");
		this.trees = (Set<TreeMessage>) source.get("trees");
	}

	public UUID getId() {
		return id;
	}
	
	public void setId(UUID id){
		this.id = id;
	}

}
