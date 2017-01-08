package com.jme3.lostVictories.objectives;

import static lostVictories.CharacterRunner.fromStringToObjective;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ObjectNode;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;

public class TransportSquad extends Objective{
	private static Logger log = Logger.getLogger(TransportSquad.class);

	private Vector destination;
	Map<String, String> issuedOrders = new HashMap<>();
	
	@SuppressWarnings("unused")
	private TransportSquad() {}
	
	public TransportSquad(Vector destination){
		this.destination = destination;
	}
	
	@Override
	public void runObjective(CharacterMessage c, String uuid,CharacterDAO characterDAO, HouseDAO houseDAO,Map<UUID, CharacterMessage> toSave)  {
		c.getUnitsUnderCommand().stream()
			.filter(id->!issuedOrders.containsKey(id.toString()))
			.map(id->characterDAO.getCharacter(id))
			.filter(cc->cc!=null)//should never happen but does need to fix
			.forEach(new Consumer<CharacterMessage>() {

			@Override
			public void accept(CharacterMessage c) {				
				try {
					TravelObjective t = new TravelObjective(destination, null);
					c.addObjective(UUID.randomUUID(), t.asJSON());
					issuedOrders.put(c.getId().toString(), t.asJSON());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}				
			}
		});
		try {
			if(!issuedOrders.containsKey(c.getId().toString())){			
				TravelObjective t = new TravelObjective(destination, null);
				issuedOrders.put(c.getId().toString(), t.asJSON());
			}
			
			Vector3f before = c.getLocation().toVector();
			Objective fromStringToObjective = fromStringToObjective(issuedOrders.get(c.getId().toString()));
			fromStringToObjective.runObjective(c, uuid, characterDAO, houseDAO, toSave);
			issuedOrders.put(c.getId().toString(), fromStringToObjective.asJSON());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		toSave.put(c.getId(), c);
	}

	@Override
	public String asJSON() throws JsonGenerationException,JsonMappingException, IOException {
		ObjectNode node = MAPPER.createObjectNode();
        JsonNode d = MAPPER.valueToTree(destination);
        node.put("destination", d);
        node.put("classType", getClass().getName());
        JsonNode _issuedOrders = MAPPER.valueToTree(issuedOrders);
        node.put("issuedOrders", _issuedOrders);
        return MAPPER.writeValueAsString(node);
	}

	@Override
	public boolean clashesWith(Class<? extends Objective> newObjective) {
		return true;
	}

}
