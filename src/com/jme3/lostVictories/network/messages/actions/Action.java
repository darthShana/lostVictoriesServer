package com.jme3.lostVictories.network.messages.actions;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import com.jme3.lostVictories.network.messages.Vector;

@JsonTypeInfo(  
    use = JsonTypeInfo.Id.NAME,  
    include = JsonTypeInfo.As.PROPERTY,  
    property = "type")  
@JsonSubTypes({  
    @Type(value = Idle.class, name = "idle"),  
    @Type(value = Move.class, name = "move"),
    @Type(value = ManualControl.class, name = "manualControl"),
    @Type(value = Shoot.class, name = "shoot"),
    @Type(value = Crouch.class, name = "crouch"),
    @Type(value = SetupWeapon.class, name = "setupWeapon")})  
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
