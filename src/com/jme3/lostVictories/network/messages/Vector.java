package com.jme3.lostVictories.network.messages;

import java.io.Serializable;

import com.jme3.math.Vector3f;

public class Vector implements Serializable{
	
	float x;
	float y;
	float z;
	
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
}
