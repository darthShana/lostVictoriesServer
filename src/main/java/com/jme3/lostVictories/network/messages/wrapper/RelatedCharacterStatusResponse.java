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

        Map<String, JsonNode> objectives = next.getObjectives().entrySet().stream().collect(Collectors.toMap(e->e.getKey(), e->toJsonNodeSafe(e.getValue())));


        for(Map.Entry<String, JsonNode> entry:objectives.entrySet()){
            try{
                Class objectiveClass = Class.forName(entry.getValue().get("class").asText());
                Objective objective = (Objective) MAPPER.treeToValue(entry.getValue(), objectiveClass);
                if(objective instanceof CleanupBeforeTransmitting){
                    ((CleanupBeforeTransmitting)objective).cleanupBeforeTransmitting();
                    next.getObjectives().put(entry.getKey(), MAPPER.writeValueAsString(objective));
                }
            }catch(ClassNotFoundException e){
                //its ok we tried
            } catch (JsonParseException e) {
                throw new RuntimeException(e);
            } catch (JsonMappingException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        unit = next;
    }

    public CharacterMessage getCharacter() {
        return unit;
    }
}
