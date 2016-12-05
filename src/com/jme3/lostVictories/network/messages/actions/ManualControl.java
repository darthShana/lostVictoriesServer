package com.jme3.lostVictories.network.messages.actions;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

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
