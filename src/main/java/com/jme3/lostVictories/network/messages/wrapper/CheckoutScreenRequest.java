package com.jme3.lostVictories.network.messages.wrapper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.jme3.lostVictories.network.messages.wrapper.LostVictoryMessage;

import java.util.UUID;

public class CheckoutScreenRequest extends LostVictoryMessage {
    
	public UUID avatar;

	private CheckoutScreenRequest(){}

    public CheckoutScreenRequest(UUID clientID, UUID avatar) {
        super(clientID);
        this.avatar = avatar;
    }
}
