package com.jme3.lostVictories.network.messages;

import java.util.UUID;

public class CheckoutScreenRequest extends LostVictoryMessage {
    
	private static final long serialVersionUID = 9030116083584783292L;
	public final UUID avatar;

    public CheckoutScreenRequest(UUID clientID, UUID avatar) {
        super(clientID);
        this.avatar = avatar;
    }
}
