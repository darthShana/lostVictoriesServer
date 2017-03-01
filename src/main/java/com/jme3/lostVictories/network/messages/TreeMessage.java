package com.jme3.lostVictories.network.messages;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

public class TreeMessage implements Serializable{
	
	private UUID id;
	private Vector location;
	private String type;
	private boolean standing;

}
