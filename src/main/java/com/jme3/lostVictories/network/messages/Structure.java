package com.jme3.lostVictories.network.messages;

import java.util.UUID;

public interface Structure {

    UUID getId();

    Vector getLocation();

    Country getOwner();
}
