package com.jme3.lostVictories.objectives;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import com.jme3.lostVictories.network.messages.Vector;

public class SecureSector extends Objective {

	private static Logger log = Logger.getLogger(SecureSector.class);
	
	private Set<UUID> houses = new HashSet<UUID>();
	private Vector centre;
	Map<String, String> issuedOrders = new HashMap<>();
	private SecureSectorState state = SecureSectorState.DEPLOY_TO_SECTOR;
	
	@SuppressWarnings("unused")
	private SecureSector() {}
	
	public SecureSector(Set<HouseMessage> houses) {
		this.houses = houses.stream().map(h->h.getId()).collect(Collectors.toSet());
		float totalX = 0, totalY = 0,totalZ = 0;
		for(HouseMessage h:houses){
            totalX+=h.getLocation().x;
            totalY+=h.getLocation().y;
            totalZ+=h.getLocation().z;
        }
        final float x = totalX/houses.size();
        final float y = totalY/houses.size();
        final float z = totalZ/houses.size();
        centre = new Vector(x, y, z);
        log.debug("securing sector:"+centre);
	}

	@Override
	public void runObjective(CharacterMessage c, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
		if(centre.x-87<1 && centre.z-(-326)<1){
			log.debug(c.getCountry()+": securing sector:"+c.getLocation()+":"+state);
		}
		state.runObjective(c, uuid, centre, houses, issuedOrders, characterDAO, houseDAO, toSave);
		SecureSectorState newState = state.tansition(c, uuid, centre, houses, issuedOrders, characterDAO, houseDAO, toSave);
		if(newState!=state){
			issuedOrders.clear();          
            state = newState;            
        }
	}

	
	
	public String asJSON() throws JsonGenerationException, JsonMappingException, IOException{
		ObjectNode node = MAPPER.createObjectNode();
		JsonNode _houses = MAPPER.valueToTree(houses);
		JsonNode _centre = MAPPER.valueToTree(centre);
		JsonNode _issuedOrders = MAPPER.valueToTree(issuedOrders);
		node.put("classType", getClass().getName());
		node.put("houses", _houses);
		node.put("centre", _centre);
		node.put("issuedOrders", _issuedOrders);
        return MAPPER.writeValueAsString(node);
	}

	@Override
	public boolean clashesWith(Class<? extends Objective> newObjective) {
		return true;
	}
	
}
