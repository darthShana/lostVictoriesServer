package com.jme3.lostVictories.network.messages;

import com.jme3.math.Vector3f;

import java.util.UUID;

public class BunkerMessage implements Structure{


    private final Quaternion rotation;
    private Vector location;
    private UUID id;

    public BunkerMessage(UUID id, Vector location, Quaternion rotation) {
        this.id = id;
        this.location = location;
        this.rotation = rotation;
    }

    public Vector getLocation() {
        return location;
    }

    public Quaternion getRotation() {
        return rotation;
    }

    public Vector getEntryPoint() {
        Vector3f v = new com.jme3.math.Quaternion().mult(Vector3f.UNIT_Z.mult(7));
        return new Vector(this.location.toVector().add(v));
    }

    @Override
    public Country getOwner() {
        return null;
    }

    public UUID getId() {
        return id;
    }
}
