package com.jme3.lostVictories.network.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class Quaternion implements Serializable{

	float x;
	float y;
	float z;
	float w;

	public Quaternion(@JsonProperty("x")float x, @JsonProperty("y")float y, @JsonProperty("z")float z, @JsonProperty("w")float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public com.lostVictories.api.Quaternion toMessage() {
		return com.lostVictories.api.Quaternion.newBuilder()
				.setX(x).setY(y).setZ(z).setW(w)
				.build();
	}
}
