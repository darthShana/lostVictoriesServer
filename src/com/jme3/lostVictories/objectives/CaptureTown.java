package com.jme3.lostVictories.objectives;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ObjectNode;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.RankMessage;

public class CaptureTown extends Objective {

	@Override
	public void runObjective(CharacterMessage c, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
		if(!c.getUnitsUnderCommand().isEmpty()){
			CharacterMessage unit = findUnitWithLeastEquipment(c, characterDAO);
			if(unit !=null && !isBusy(unit) && RankMessage.LIEUTENANT == unit.getRank()){
				IncreasePerimeter i = new IncreasePerimeter();
				try {
					unit.addObjective(UUID.randomUUID(), i.asJSON());
					toSave.put(unit.getId(), unit);
				} catch (JsonGenerationException e) {
					throw new RuntimeException(e);
				} catch (JsonMappingException e) {
					throw new RuntimeException(e);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private CharacterMessage findUnitWithLeastEquipment(CharacterMessage c, CharacterDAO characterDAO) {
		CharacterMessage ret = null;
		int vehicleCount = 0;
		for(CharacterMessage unit:characterDAO.getAllCharacters(c.getUnitsUnderCommand()).values()){
			int v = getVehicleCount(unit, characterDAO);
			if(ret==null || v<vehicleCount){
				ret = unit;
				vehicleCount = v;
			}
		}
		return ret;
	}
	
	private int getVehicleCount(CharacterMessage unit, CharacterDAO characterDAO) {
		int i = 0;
		if(unit.getCharacterType()!=CharacterType.SOLDIER){
			i++;
		}
		for(CharacterMessage c: characterDAO.getAllCharacters(unit.getUnitsUnderCommand()).values()){
			i = i+getVehicleCount(c, characterDAO);
		}
		return i;
	}

	public String asJSON() throws JsonGenerationException, JsonMappingException, IOException{
		ObjectNode node = MAPPER.createObjectNode();
        node.put("classType", getClass().getName());
        return MAPPER.writeValueAsString(node);
	}
	
	@Override
	public boolean clashesWith(Class<? extends Objective> newObjective) {
		return false;
	}

}
