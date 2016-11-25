package com.jme3.lostVictories.network.messages.actions;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;


public class Drive extends Action {

	private float steering;
	private String gear;
	private float speed;

	@JsonCreator
	public Drive(@JsonProperty("steering")float steering, @JsonProperty("gear")String gear, @JsonProperty("speed")float speed, @JsonProperty("type")String type) {
		super("drive");
		this.steering = steering;
		this.gear = gear;
		this.speed = speed;
	}

}
