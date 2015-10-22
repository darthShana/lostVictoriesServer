package com.jme3.lostVictories.objectives;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.math.Vector3f;

public class IncreasePerimeter extends Objective {

	@Override
	public void runObjective(CharacterMessage c, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
		for(CharacterMessage unit:characterDAO.getAllCharacters(c.getUnitsUnderCommand()).values()){
//			if("2fbe421f-f701-49c9-a0d4-abb0fa904204".equals(unit.getId().toString())){
//				System.out.println("in here test");
//			}
			boolean isBusy = unit.getObjectives().values().stream().map(s->toJsonNodeSafe(s)).anyMatch(n->!isPassiveObjective(n));
			if(!isBusy)	{
				HouseMessage closest = null;
				Vector3f characterLocation = new Vector3f(c.getLocation().x, c.getLocation().y, c.getLocation().z);
				for(HouseMessage house:houseDAO.getAllHouses()){
					if(house.getOwner()!=c.getCountry()){
						if(closest==null){
							closest = house;
						}else{
							Vector3f currentTarget = new Vector3f(closest.getLocation().x, closest.getLocation().y, closest.getLocation().z);
							Vector3f newTarget = new Vector3f(house.getLocation().x, house.getLocation().y, house.getLocation().z);
							if(characterLocation.distance(newTarget)<characterLocation.distance(currentTarget)){
								closest = house;
							}
						}
					}
				}
				if(closest!=null){
					try {
						unit.addObjective(UUID.randomUUID(), new CaptureStructure(closest.getId().toString()).asJSON());
						toSave.put(unit.getId(), unit);
					} catch (Exception e) {
						throw new RuntimeException(e);
					} 
				}
			}
		}
	}
	
	private boolean isPassiveObjective(JsonNode n) {
		String s = n.get("classType").asText();
		return "com.jme3.lostVictories.objectives.SurvivalObjective".equals(s) || "com.jme3.lostVictories.objectives.RemanVehicle".equals(s);
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
