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

	@Override
	public com.lostVictories.api.Action toMessage() {
		com.lostVictories.api.Action.Builder builder = com.lostVictories.api.Action.newBuilder();
		builder.setActionType(com.lostVictories.api.Action.ActionType.MANUAL_CONTROL);
		builder.setGear(gear);
		builder.setSteering(steering);
		return builder.build();
	}
	
}
