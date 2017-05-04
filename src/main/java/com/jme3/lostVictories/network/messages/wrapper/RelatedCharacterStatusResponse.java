package com.jme3.lostVictories.network.messages.wrapper;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.objectives.CleanupBeforeTransmitting;
import com.jme3.lostVictories.objectives.Objective;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import static lostVictories.CharacterRunner.toJsonNodeSafe;
import static lostVictories.dao.CharacterDAO.MAPPER;

/**
 * Created by dharshanar on 11/04/17.
 */
public class RelatedCharacterStatusResponse extends LostVictoryMessage{

    private CharacterMessage unit;


    public RelatedCharacterStatusResponse(CharacterMessage next) {
        next.clearObjectives();
        unit = next;
    }

    public CharacterMessage getCharacter() {
        return unit;
    }
}
