package com.jme3.lostVictories.objectives;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.Vector;
import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AttackTargetsInDirection extends Objective{

    Set<Vector> lastKnownLocations;
    TravelObjective travelObjective;


    @Override
    public void runObjective(CharacterMessage c, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills) {
        if(travelObjective == null && !lastKnownLocations.isEmpty()){
            travelObjective = new TravelObjective(c, lastKnownLocations.iterator().next(), null);
        }
        if(travelObjective!=null && !travelObjective.isComplete){
            travelObjective.runObjective(c, uuid, characterDAO, houseDAO, toSave, kills);
        }else{
            isComplete = true;
        }
    }

    @Override
    public boolean clashesWith(Class<? extends Objective> newObjective) {
        if(newObjective.isAssignableFrom(CaptureStructure.class)){
            return true;
        }
        if(newObjective.isAssignableFrom(TransportSquad.class)){
            return true;
        }
        if(newObjective.isAssignableFrom(FollowCommander.class)){
            return true;
        }
        if(newObjective.isAssignableFrom(TravelObjective.class)){
            return true;
        }

        return false;
    }
}
