package com.jme3.lostVictories.network.messages.wrapper;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.jme3.lostVictories.network.messages.UnClaimedEquipmentMessage;

import java.util.Set;

/**
 * Created by dharshanar on 1/04/17.
 */
public class EquipmentStatusResponse extends LostVictoryMessage {
    private Set<UnClaimedEquipmentMessage> unClaimedEquipment;

    public EquipmentStatusResponse(Set<UnClaimedEquipmentMessage> unClaimedEquipment) {
        this.unClaimedEquipment = unClaimedEquipment;
    }
}
