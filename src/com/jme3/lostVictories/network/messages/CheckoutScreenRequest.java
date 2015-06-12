package com.jme3.lostVictories.network.messages;

public class CheckoutScreenRequest extends LostVictoryMessage {
    
	public final long x;
	public final long y;
	public final long z;
	public final String clientID;

    public CheckoutScreenRequest(String clientID, long x, long y, long z) {
        super(clientID);
        this.clientID = clientID;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
