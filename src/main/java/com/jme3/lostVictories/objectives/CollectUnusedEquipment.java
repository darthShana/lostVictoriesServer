package com.jme3.lostVictories.objectives;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import java.util.Map;
import java.util.UUID;

/**
 * Created by dharshana on 7/07/17.
 */
public class CollectUnusedEquipment extends Objective implements PassiveObjective {
    @Override
    public void runObjective(CharacterMessage c, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills) {

    }

    @Override
    public boolean clashesWith(Class<? extends Objective> newObjective) {
        return false;
    }
}
