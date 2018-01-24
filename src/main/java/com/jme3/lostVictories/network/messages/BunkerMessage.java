package com.jme3.lostVictories.network.messages;

import java.util.UUID;

public class BunkerMessage implements Structure{

    private Vector location;
    private UUID id;

    public BunkerMessage(UUID id, Vector location) {
        this.id = id;
        this.location = location;
    }

    public Vector getLocation() {
        return location;
    }

    @Override
    public Country getOwner() {
        return null;
    }

    public UUID getId() {
        return id;
    }
}
