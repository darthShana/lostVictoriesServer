package com.jme3.lostVictories.network.messages.actions;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import com.jme3.lostVictories.network.messages.Vector;

public class Shoot extends Action {

	private long shootTime;
	private Vector[] targets;

	@JsonCreator
	public Shoot(@JsonProperty("shootTime")long shootTime, @JsonProperty("targets")Vector[] targets, @JsonProperty("type")String type) {
		super("shoot");
		this.shootTime = shootTime;
		this.targets = targets;
	}

}
