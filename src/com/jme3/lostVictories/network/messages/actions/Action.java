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
    @Type(value = Shoot.class, name = "shoot")})  
public abstract class Action implements Serializable{
    
    public static Action idle(){
        return new Idle();
    }
    public static Action move(){
        return new Move();
    }
    public static Action shoot(long shootTime, Vector[] targets){
        return new Shoot(shootTime, targets);
    }
}
