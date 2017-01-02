package lostVictories;

import static com.jme3.lostVictories.objectives.Objective.MAPPER;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.objectives.Objective;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

public class CharacterRunner implements Runnable{

	private static Logger log = Logger.getLogger(CharacterRunner.class);

	private static CharacterRunner instance;	
	private CharacterDAO characterDAO;
	private HouseDAO houseDAO;

	private CharacterRunner(CharacterDAO characterDAO, HouseDAO houseDAO) {
		this.characterDAO = characterDAO;
		this.houseDAO = houseDAO;
	}

	public static CharacterRunner instance(CharacterDAO characterDAO, HouseDAO houseDAO) {
		if(instance==null){
			instance = new CharacterRunner(characterDAO, houseDAO);
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
				.forEach(c->runCharacterBehavior(c, toSave));
			try {
				characterDAO.updateCharacterStateNoCheckout(toSave);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}catch(Throwable e){
			e.printStackTrace();
		}
	}

	private void runCharacterBehavior(CharacterMessage c, Map<UUID, CharacterMessage> toSave) {
		Map<String, JsonNode> objectives = c.getObjectives().entrySet().stream().collect(Collectors.toMap(e->e.getKey(), e->toJsonNodeSafe(e.getValue())));
		if(c.getCheckoutClient()!=null){
			c.setCheckoutClient(null);
			toSave.put(c.getId(), c);
		}
		
		for(Entry<String, JsonNode> entry:objectives.entrySet()){
			try{
				Class objectiveClass = Class.forName(entry.getValue().get("classType").asText());
				Objective objective = (Objective) MAPPER.treeToValue(entry.getValue(), objectiveClass);
				objective.runObjective(c, entry.getKey(), characterDAO, houseDAO, toSave);
				c.getObjectives().put(entry.getKey(), objective.asJSON());
				if(objective.isComplete){
					c.getObjectives().remove(entry.getKey());
					toSave.put(c.getId(), c);
				}
			}catch(ClassNotFoundException e){
				log.trace(entry.getValue().get("classType")+ " not found on Character runner");
			} catch (JsonParseException e) {
				throw new RuntimeException(e);
			} catch (JsonMappingException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private JsonNode toJsonNodeSafe(String s) {
		try {
			return MAPPER.readTree(s);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
