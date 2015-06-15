package com.jme3.lostVictories.network.messages;

import java.util.UUID;

public class CheckoutScreenRequest extends LostVictoryMessage {
    
	private static final long serialVersionUID = 9030116083584783292L;

	public final long x;
	public final long y;
	public final long z;

    public CheckoutScreenRequest(UUID clientID, long x, long y, long z) {
        super(clientID);
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
