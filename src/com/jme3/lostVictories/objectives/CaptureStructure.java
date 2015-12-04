package com.jme3.lostVictories.objectives;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ObjectNode;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;

public class CaptureStructure extends Objective{

	private static Logger log = Logger.getLogger(CaptureStructure.class);
	String structure;
	
	public CaptureStructure(String structure){
		this.structure = structure;
	}
	
	private CaptureStructure() {}

	@Override
	public void runObjective(CharacterMessage c, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
		HouseMessage house = houseDAO.getHouse(UUID.fromString(structure));
		if(house.getOwner()==c.getCountry()){
			log.debug("completed stucture capture:"+structure);
			isComplete = true;
			return;
		}
		
		Vector3f[] v = new Vector3f[]{
				house.getLocation().toVector().add(new Vector3f(15, 0, 15)), 
				house.getLocation().toVector().add(new Vector3f(-15, 0, 15)), 
				house.getLocation().toVector().add(new Vector3f(15, 0, -15)), 
				house.getLocation().toVector().add(new Vector3f(-15, 0, -15))};
        Vector3f shortest = null;
        for(Vector3f t:v){
            if(shortest == null || c.getLocation().toVector().distance(shortest)>c.getLocation().toVector().distance(t)){
                shortest = t;
            }
        }
		
		try {
			if(!isBusy(c)){
				TravelObjective t = new TravelObjective(new Vector(shortest), null);
				c.addObjective(UUID.randomUUID(), t.asJSON());
				toSave.put(c.getId(), c);
			}
			for(UUID u:c.getUnitsUnderCommand()){
				CharacterMessage unit = characterDAO.getCharacter(u);
				if(!isBusy(unit) && CharacterType.SOLDIER==unit.getCharacterType()){
					TravelObjective t = new TravelObjective(new Vector(shortest), null);
					unit.addObjective(UUID.randomUUID(), t.asJSON());
					toSave.put(unit.getId(), unit);
				}
			}
		} catch (JsonGenerationException e) {
			throw new RuntimeException(e);
		} catch (JsonMappingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String asJSON() throws JsonGenerationException, JsonMappingException, IOException{
		ObjectNode node = MAPPER.createObjectNode();
        node.put("structure", structure);
        node.put("classType", getClass().getName());

        return MAPPER.writeValueAsString(node);
	}

	@Override
	public boolean clashesWith(Class<? extends Objective> newObjective) {
		if(newObjective.isAssignableFrom(TravelObjective.class)){
			return true;
		}
		return false;
	}
}
