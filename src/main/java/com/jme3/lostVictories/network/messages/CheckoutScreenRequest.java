package com.jme3.lostVictories.network.messages;

import com.jme3.lostVictories.network.messages.wrapper.LostVictoryMessage;

import java.util.UUID;

public class CheckoutScreenRequest extends LostVictoryMessage {
    
	public final UUID avatar;

    public CheckoutScreenRequest(UUID clientID, UUID avatar) {
        super(clientID);
        this.avatar = avatar;
    }
}
