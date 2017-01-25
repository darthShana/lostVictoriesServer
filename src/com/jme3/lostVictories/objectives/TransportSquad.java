package com.jme3.lostVictories.objectives;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.Vector;

public class TransportSquad extends Objective{
	@JsonIgnore
	private static Logger log = Logger.getLogger(TransportSquad.class);

	Vector destination;
	Map<UUID, Objective> issuedOrders = new HashMap<>();
	
	@SuppressWarnings("unused")
	private TransportSquad() {}
	
	public TransportSquad(Vector destination){
		this.destination = destination;
	}
	
	@Override
	public void runObjective(CharacterMessage c, String uuid,CharacterDAO characterDAO, HouseDAO houseDAO,Map<UUID, CharacterMessage> toSave)  {
		c.getUnitsUnderCommand().stream()
			.filter(id->!issuedOrders.containsKey(id))
			.map(id->characterDAO.getCharacter(id))
			.forEach(new Consumer<CharacterMessage>() {

			@Override
			public void accept(CharacterMessage c) {				
				try {
					TravelObjective t = new TravelObjective(destination, null);
					c.addObjective(UUID.randomUUID(), t);
					issuedOrders.put(c.getId(), t);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}				
			}
		});
		if(!issuedOrders.containsKey(c.getId())){			
			TravelObjective t = new TravelObjective(destination, null);
			issuedOrders.put(c.getId(), t);
		}
		
		Objective fromStringToObjective = issuedOrders.get(c.getId());
		fromStringToObjective.runObjective(c, uuid, characterDAO, houseDAO, toSave);		
	}

	@Override
	public boolean clashesWith(Class<? extends Objective> newObjective) {
		if(newObjective.isAssignableFrom(TravelObjective.class)){
			return true;
		}
		if(newObjective.isAssignableFrom(CaptureStructure.class)){
			return true;
		}
		return false;
	}

}
