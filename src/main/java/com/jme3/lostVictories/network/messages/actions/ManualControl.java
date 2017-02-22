package com.jme3.lostVictories.network.messages.actions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use=Id.CLASS, include=As.PROPERTY, property="class")
public class ManualControl extends Action{

	private String gear;
    private String steering;
    
    @JsonCreator
	public ManualControl(@JsonProperty("steering")String steering, @JsonProperty("gear")String gear, @JsonProperty("type")String type) {
		super("manualControl");
		this.steering = steering;
		this.gear = gear;		
	}
	
}
