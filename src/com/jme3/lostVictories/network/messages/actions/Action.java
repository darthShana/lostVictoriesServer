package com.jme3.lostVictories.network.messages.actions;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.jme3.lostVictories.network.messages.Vector;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use=Id.CLASS, include=As.PROPERTY, property="class")
public abstract class Action implements Serializable{
	
	protected final String type;
	
	public Action(String type) {
		this.type = type;
	}
    
    public static Action idle(){
        return new Idle();
    }
    public static Action move(){
        return new Move();
    }
    public static Action crouch(){
        return new Crouch();
    }
    public static Action shoot(long shootTime, Vector[] targets){
        return new Shoot(shootTime, targets, "shoot");
    }
	public String getType() {
		return type;
	}
	
	@Override
	public boolean equals(Object obj) {
		return ((Action)obj).type.equals(type);
	}
	
	@Override
	public int hashCode() {
		return type.hashCode();
	}

}
