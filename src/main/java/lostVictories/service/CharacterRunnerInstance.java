package lostVictories.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.objectives.Objective;
import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;
import lostVictories.dao.PlayerUsageDAO;
import lostVictories.messageHanders.CharacterCatch;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static lostVictories.dao.CharacterDAO.MAPPER;

/**
 * Created by dharshanar on 6/05/17.
 */
public class CharacterRunnerInstance {

    private static Logger log = Logger.getLogger(CharacterRunnerInstance.class);


    public void doRunCharacter(CharacterMessage _character, CharacterDAO characterDAO, HouseDAO houseDAO, PlayerUsageDAO playerUsageDAO) {
        Map<UUID, CharacterMessage> toSave = new HashMap<UUID, CharacterMessage>();
        Map<UUID, UUID> kills = new HashMap<>();

        CharacterMessage character = characterDAO.getCharacter(_character.getId(), true);
        if(character!=null && !character.isDead() && character.isAvailableForCheckout(5000)){
            runCharacterBehavior(character, toSave, kills, characterDAO, playerUsageDAO, houseDAO);
        }
        characterDAO.updateCharacterStateNoCheckout(toSave);
        kills.entrySet().stream().forEach(entry->doKill(entry.getKey(), entry.getValue(), characterDAO));
    }

    private void doKill(UUID _killer, UUID _victim, CharacterDAO characterDAO) {
        Map<UUID, CharacterMessage> toSave = new HashMap<UUID, CharacterMessage>();
        CharacterCatch catche = new CharacterCatch(characterDAO);
        CharacterMessage victim = catche.getCharacter(_victim);
        if(victim==null || victim.isDead()){
            return;
        }
        log.info("killed in server:"+victim.getId());

        CharacterMessage killer = catche.getCharacter(_killer);
        victim.kill();
        killer.incrementKills(victim.getId());
        toSave.put(killer.getId(), killer);
        toSave.put(victim.getId(), victim);

        victim.replaceMe(catche, toSave);

        characterDAO.saveCommandStructure(toSave);


    }

    private void runCharacterBehavior(CharacterMessage c, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills, CharacterDAO characterDAO, PlayerUsageDAO playerUsageDAO, HouseDAO houseDAO) {
        Map<String, JsonNode> objectives = c.readObjectives().entrySet().stream().collect(Collectors.toMap(e->e.getKey(), e->toJsonNodeSafe(e.getValue())));
        String cot = (c.getCheckoutTime()!=null)?(System.currentTimeMillis()-c.getCheckoutTime())+"":"";
//		System.out.println("runCharacterBehavior:"+c.getId()+" version:"+c.getVersion()+" checkout client:"+c.getCheckoutClient()+"cheout time:"+cot);
        if(c.getCheckoutClient()!=null){
            if(CharacterType.AVATAR==c.getCharacterType() && c.getUserID()!=null){
                playerUsageDAO.registerStopGame(c.getUserID(), System.currentTimeMillis());
            }
            c.setCheckoutClient(null);
            characterDAO.putCharacter(c.getId(), c);
            return;

        }


        for(Map.Entry<String, JsonNode> entry:objectives.entrySet()){
            try{
                if(entry.getValue().get("class")==null){
                    System.out.println("obj with null class");
                }
                Class objectiveClass = Class.forName(entry.getValue().get("class").asText());
                Objective objective = (Objective) MAPPER.treeToValue(entry.getValue(), objectiveClass);
                objective.runObjective(c, entry.getKey(), characterDAO, houseDAO, toSave, kills);
                //should not need to do this.....
                c.updateObjective(entry.getKey(), MAPPER.writeValueAsString(objective));
                if(objective.isComplete){
                    c.removeObjective(entry.getKey());

                }
                toSave.put(c.getId(), c);
            }catch(ClassNotFoundException e){
                log.trace(entry.getValue().get("class")+ " not found on Character runner");
            } catch (JsonParseException e) {
                throw new RuntimeException(e);
            } catch (JsonMappingException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public static JsonNode toJsonNodeSafe(String s) {
        try {
            return MAPPER.readTree(s);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
