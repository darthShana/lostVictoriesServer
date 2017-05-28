package com.jme3.lostVictories.network.messages.actions;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

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
    public static Action setupWeapon(){ return new SetupWeapon();}
    public static Action shoot(long shootTime, Vector[] targets){
        return new Shoot(shootTime, targets, "shoot");
    }
    public static Action shoot(long shootTime, Set<Vector> targets){
        return new Shoot(shootTime, targets.toArray(new Vector[]{}), "shoot");
    }
    public static Action manualControl(String stearing, String gear) { return new ManualControl(stearing, gear, "manualControl");}
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

    public abstract com.lostVictories.api.Action toMessage();

    public static Action fromMessage(com.lostVictories.api.Action action) {
        switch (action.getActionType()){
            case IDLE:
                return idle();
            case MOVE:
                return move();
            case CROUCH:
                return crouch();
            case SETUP_WEAPON:
                return setupWeapon();
            case SHOOT:
                return shoot(action.getShootTime(), action.getTargetsList().stream().map(t->new Vector(t)).collect(Collectors.toSet()));
            case MANUAL_CONTROL:
                return manualControl(action.getSteering(), action.getGear());

        }
        throw new RuntimeException("unknow action type:"+action.getActionType());

    }
}
