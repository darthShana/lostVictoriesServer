package com.jme3.lostVictories.network.messages;

public class CheckoutScreenRequest extends LostVictoryMessage {
    
	private final long x;
    private final long y;
    private final long z;

    public CheckoutScreenRequest(long x, long y, long z) {
        super("[chechoutScreen]");
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
