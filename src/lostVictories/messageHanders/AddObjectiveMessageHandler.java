package lostVictories.messageHanders;

import static com.jme3.lostVictories.objectives.Objective.MAPPER;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;

import com.jme3.lostVictories.network.messages.AddObjectiveRequest;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;
import com.jme3.lostVictories.objectives.Objective;

import lostVictories.dao.CharacterDAO;

public class AddObjectiveMessageHandler {
	
	private static Logger log = Logger.getLogger(AddObjectiveMessageHandler.class);

	private CharacterDAO characterDAO;

	public AddObjectiveMessageHandler(CharacterDAO characterDAO) {
		this.characterDAO = characterDAO;
	}

	public LostVictoryMessage handle(AddObjectiveRequest msg) {
		CharacterMessage character = characterDAO.getCharacter(msg.getCharacter());
		
		try{
     		Class newObjective = Class.forName(toJsonNodeSafe(msg.getObjective()).get("classType").asText());
			
			Map<String, JsonNode> objectives = character.getObjectives().entrySet().stream().collect(Collectors.toMap(e->e.getKey(), e->toJsonNodeSafe(e.getValue())));
			for(Entry<String, JsonNode> entry:objectives.entrySet()){
				Class objectiveClass = getObjectiveClassSafe(entry);
				if(objectiveClass!=null){
					Objective objective = (Objective) MAPPER.treeToValue(entry.getValue(), objectiveClass);
					if(objective.clashesWith(newObjective) || newObjective == objectiveClass){
						character.getObjectives().remove(entry.getKey());
					}
				}
			}
		}catch(ClassNotFoundException e){
			log.debug(e);
		} catch (JsonParseException e) {
			log.debug(e);
		} catch (JsonMappingException e) {
			log.debug(e);
		} catch (IOException e) {
			log.debug(e);
		}
		
		character.addObjective(msg.getIdentity(), msg.getObjective());
		characterDAO.putCharacter(character.getId(), character);
		return new LostVictoryMessage(UUID.randomUUID());
	}

	private Class<?> getObjectiveClassSafe(Entry<String, JsonNode> entry) {
		try {
			return Class.forName(entry.getValue().get("classType").asText());
		} catch (ClassNotFoundException e) {
			return null;
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
