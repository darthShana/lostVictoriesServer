package com.jme3.lostVictories.objectives;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ObjectNode;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.math.Vector3f;

public class SecureSector extends Objective {

	private static Logger log = Logger.getLogger(SecureSector.class);
	
	private Set<UUID> houses = new HashSet<UUID>();
	
	private SecureSector() {}
	
	public SecureSector(Set<HouseMessage> houses) {
		this.houses = houses.stream().map(h->h.getId()).collect(Collectors.toSet());
	}

	@Override
	public void runObjective(CharacterMessage c, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
		Set<UUID> assigned = new HashSet<UUID>();
		Set<HouseMessage> houses2 = houseDAO.getHouses(houses);
		for(CharacterMessage unit:characterDAO.getAllCharacters(c.getUnitsUnderCommand()).values()){
			if(!isBusy(unit) && RankMessage.CADET_CORPORAL==unit.getRank())	{
				Set<HouseMessage> hh = houses2.stream().filter(h->!assigned.contains(h.getId())).collect(Collectors.toSet());
				HouseMessage closest = findClosestHouse(c, hh, assigned, h -> h.getOwner()!=c.getCountry());
				if(closest!=null){
					try {
						unit.addObjective(UUID.randomUUID(), new CaptureStructure(closest.getId().toString()).asJSON());
						toSave.put(unit.getId(), unit);
						assigned.add(closest.getId());
						log.debug(c.getCountry()+"- setting new structure to capture:"+closest.getId());
					} catch (Exception e) {
						throw new RuntimeException(e);
					} 
				}
			}
		}
		if(houses2.stream().noneMatch(h->h.getOwner()!=c.getCountry())){
			log.info(c.getCountry()+"- sector secured:");
			isComplete = true;
		}
	}

	public static HouseMessage findClosestHouse(CharacterMessage c, Set<HouseMessage> allHouses, Set<UUID> assigned, Predicate<HouseMessage> pred) {
		HouseMessage closest = null;
		Vector3f characterLocation = new Vector3f(c.getLocation().x, c.getLocation().y, c.getLocation().z);
		
		for(HouseMessage house:allHouses){
			if(pred.test(house)){
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
		return closest;
	}
	
	public String asJSON() throws JsonGenerationException, JsonMappingException, IOException{
		ObjectNode node = MAPPER.createObjectNode();
		JsonNode valueToTree = MAPPER.valueToTree(houses);
		node.put("classType", getClass().getName());
		node.put("houses", valueToTree);
        return MAPPER.writeValueAsString(node);
	}

	@Override
	public boolean clashesWith(Class<? extends Objective> newObjective) {
		return false;
	}
	
}
