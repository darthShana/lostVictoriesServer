package com.jme3.lostVictories.network.messages;

import java.io.Serializable;

import com.jme3.math.Vector3f;

public class Vector implements Serializable{
	
	public float x;
	public float y;
	public float z;
	
	public Vector(double x, double y, double z) {
		this.x = (float) x;
		this.y = (float) y;
		this.z = (float) z;
	}
	
	public Vector(float x, float y, float z) {
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
		return x == o.x && y == o.y && z == o.z;
	}
	
	@Override
	public int hashCode() {
		return (x+""+y+""+z).hashCode();
	}
}
