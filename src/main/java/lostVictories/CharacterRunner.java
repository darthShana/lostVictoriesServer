package lostVictories;

import static lostVictories.dao.CharacterDAO.MAPPER;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

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

public class CharacterRunner implements Runnable{

	private static Logger log = Logger.getLogger(CharacterRunner.class);

	private static CharacterRunner instance;	
	private CharacterDAO characterDAO;
	private HouseDAO houseDAO;
	private PlayerUsageDAO playerUsageDAO;

	private CharacterRunner(CharacterDAO characterDAO, HouseDAO houseDAO, PlayerUsageDAO playerUsageDAO) {
		this.characterDAO = characterDAO;
		this.houseDAO = houseDAO;
		this.playerUsageDAO = playerUsageDAO;
	}

	public static CharacterRunner instance(CharacterDAO characterDAO, HouseDAO houseDAO, PlayerUsageDAO playerUsageDAO) {
		if(instance==null){
			instance = new CharacterRunner(characterDAO, houseDAO, playerUsageDAO);
		}
		return instance;
	}

	@Override
	public void run() {
		try{
			Map<UUID, CharacterMessage> toSave = new HashMap<UUID, CharacterMessage>();
			characterDAO.getAllCharacters().parallelStream()
				.filter(c->!c.isDead())
				.filter(c->c.isAvailableForCheckout())
				.forEach(c->runCharacterBehavior(c, toSave, characterDAO, playerUsageDAO));
			try {
				characterDAO.updateCharacterStateNoCheckout(toSave);
				characterDAO.refresh();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}catch(Throwable e){
			e.printStackTrace();
		}
	}

	private void runCharacterBehavior(CharacterMessage c, Map<UUID, CharacterMessage> toSave, CharacterDAO characterDAO, PlayerUsageDAO playerUsageDAO) {
		Map<String, JsonNode> objectives = c.getObjectives().entrySet().stream().collect(Collectors.toMap(e->e.getKey(), e->toJsonNodeSafe(e.getValue())));
		if(c.getCheckoutClient()!=null){
			if(CharacterType.AVATAR==c.getCharacterType() && c.getUserID()!=null){
				playerUsageDAO.registerStopGame(c.getUserID(), System.currentTimeMillis());
			}
			c.setCheckoutClient(null);
			characterDAO.putCharacter(c.getId(), c);
			return;
			
		}
		
		
		for(Entry<String, JsonNode> entry:objectives.entrySet()){
			try{
				if(entry.getValue().get("class")==null){
					System.out.println("obj with null class");
				}
				Class objectiveClass = Class.forName(entry.getValue().get("class").asText());
				Objective objective = (Objective) MAPPER.treeToValue(entry.getValue(), objectiveClass);
				objective.runObjective(c, entry.getKey(), characterDAO, houseDAO, toSave);
				//should not need to do this.....
				c.getObjectives().put(entry.getKey(), MAPPER.writeValueAsString(objective));
				if(objective.isComplete){
					c.getObjectives().remove(entry.getKey());
					
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
	
	public static Objective fromStringToObjective(String jsonString){
		JsonNode jsonNodeSafe = toJsonNodeSafe(jsonString);
		try {
			Class objectiveClass = Class.forName(jsonNodeSafe.get("class").asText());
			return (Objective) MAPPER.treeToValue(jsonNodeSafe, objectiveClass);
		} catch (ClassNotFoundException | IOException e) {
			throw new RuntimeException(e);
		}
		
	}

	private static JsonNode toJsonNodeSafe(String s) {
		try {
			return MAPPER.readTree(s);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
