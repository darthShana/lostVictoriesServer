package com.jme3.lostVictories.network.messages;

import java.util.UUID;

public class DeathNotificationRequest extends LostVictoryMessage {

	private final UUID killer;
    private final UUID victim;

    public DeathNotificationRequest(UUID clientID, UUID killer, UUID victim) {
        super(clientID);
        this.killer = killer;
        this.victim = victim;        
    }
    
    public UUID getKiller() {
		return killer;
	}
    
    public UUID getVictim() {
		return victim;
	}
}
