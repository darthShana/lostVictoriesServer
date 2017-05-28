package com.jme3.lostVictories.network.messages;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import static com.lostVictories.service.LostVictoriesService.bytes;

public class TreeMessage implements Serializable{
	
	private UUID id;
	private Vector location;
	private String type;
	private boolean standing;

    public com.lostVictories.api.TreeMessage toMessage() {
		return com.lostVictories.api.TreeMessage.newBuilder()
				.setId(bytes(id)).setLocation(location.toMessage()).setType(type).setStanding(standing)
				.build();
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
