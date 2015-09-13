package com.jme3.lostVictories.network.messages;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

import lostVictories.LostVictoryScene;

import com.jme3.math.Vector3f;

public class Vector implements Serializable{
	
	public float x;
	public float y;
	public float z;
	
	public Vector(@JsonProperty("x")float x, @JsonProperty("y")float y, @JsonProperty("z")float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	
	@Override
	public String toString() {
		return x+","+y+","+z;
	}

	public Vector add(long i, long j, long k) {
		Vector3f v = new Vector3f(x, y, z);
		v.add(i, j, k);
		return new Vector(v.x, v.y, v.z);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Vector)){
			return false;
		}
		Vector o = (Vector) obj;
		return equals(x, o.x) && equals(y, o.y) && equals(z, o.z);
	}
	
	private boolean equals(final float a, final float b) {
	    return (Math.abs(a - b) < 0.1);
	}
	
	@Override
	public int hashCode() {
		return (x+""+y+""+z).hashCode();
	}

	public Map<String, Float> toMap() {
		HashMap<String, Float> hashMap = new HashMap<String, Float>();
		hashMap.put("x", x);
		hashMap.put("y", y);
		hashMap.put("z", z);
		return hashMap;
	}
	
	public static Vector latLongToVector(HashMap<String, Double> location, float altitude){
		return new Vector(location.get("lon").floatValue()/180*LostVictoryScene.SCENE_WIDTH, altitude, location.get("lat").floatValue()/80*LostVictoryScene.SCENE_HEIGHT);
	}
}
