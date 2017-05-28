package com.jme3.lostVictories.network.messages;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jme3.math.Vector2f;
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

	
	public Vector(Vector3f v) {
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
	}

	public Vector(com.lostVictories.api.Vector v) {
		this.x = v.getX();
		this.y = v.getY();
		this.z = v.getZ();
	}


    @Override
	public String toString() {
		return x+","+y+","+z;
	}

	public Vector add(long i, long j, long k) {
		Vector3f v = new Vector3f(x, y, z);
		v.addLocal(i, j, k);
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
	
	public static Vector latLongToVector(float altitude, float lon, float lat){
		return new Vector(lon /180*LostVictoryScene.SCENE_WIDTH, altitude, lat /80*LostVictoryScene.SCENE_HEIGHT);
	}


	public Vector3f toVector() {
		return new Vector3f(x, y, z);
	}


	public float distance(Vector other) {
		Vector2f me = new Vector2f(x, z);
		return me.distance(new Vector2f(other.x, other.z));
	}

	public com.lostVictories.api.Vector toMessage() {
		return com.lostVictories.api.Vector.newBuilder().setX(x).setY(y).setZ(z).build();
	}
}
