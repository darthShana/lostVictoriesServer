package com.jme3.lostVictories.network.messages;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class Quaternion implements Serializable{

	float x;
	float y;
	float z;
	float w;

	public Quaternion(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public Map<String, Float> toMap() {
		Map<String, Float> ret = new HashMap<String, Float>();
		ret.put("x", x);
		ret.put("y", y);
		ret.put("z", z);
		ret.put("w", w);
		return ret;
	}
	
	public static Quaternion toQuaternion(Map<String, Double> q){
		return new Quaternion(q.get("x").floatValue(), q.get("y").floatValue(), q.get("z").floatValue(), q.get("w").floatValue());
	}

	public com.lostVictories.api.Quaternion toMessage() {
		return com.lostVictories.api.Quaternion.newBuilder()
				.setX(x).setY(y).setZ(z).setW(w)
				.build();
	}
}
