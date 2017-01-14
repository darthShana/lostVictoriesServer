package com.jme3.lostVictories.network.messages.actions;



import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.jme3.lostVictories.network.messages.Vector;

@JsonTypeInfo(use=Id.CLASS, include=As.PROPERTY, property="class")
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
