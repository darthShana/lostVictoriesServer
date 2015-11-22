package com.jme3.lostVictories.objectives;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ObjectNode;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.Country;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.math.Vector3f;

public class CaptureTown extends Objective {

	private static Logger log = Logger.getLogger(CaptureTown.class);
	private Set<GameSector> gameSectors;
	public Rectangle mapBounds = new Rectangle(-512, -512, 1024, 1024);
	private long startTime;
	
	private CaptureTown() {}
	
	public CaptureTown(long startTime){
		this.startTime = startTime;
	}
	
	@Override
	public void runObjective(CharacterMessage c, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
		if(System.currentTimeMillis()-startTime<(60*1000)){
			return;
		}
		
		if(gameSectors==null){
            gameSectors = calculateGameSector(houseDAO);
        }
		
		GameSector toSecure = findClosestUnsecuredGameSector(c, gameSectors);
        
        if(toSecure==null){
            return;
        }
		
		if(!c.getUnitsUnderCommand().isEmpty()){
			CharacterMessage unit = findUnitWithLeastEquipment(c, characterDAO);
			if(unit !=null && !isBusy(unit) && RankMessage.LIEUTENANT == unit.getRank()){
				log.info(c.getCountry()+": assigning new sector:"+toSecure.rect+" houses:"+toSecure.houses.size());
				SecureSector i = new SecureSector(toSecure.getHouses());
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
	
	private GameSector findClosestUnsecuredGameSector(CharacterMessage character, Set<GameSector> gameSectors) {
        GameSector closest = null;
        for(GameSector gameSector:gameSectors){
            if(gameSector.isUnsecured(character.getCountry())){
                if(closest==null || closest.location().distance(character.getLocation().toVector())>gameSector.location().distance(character.getLocation().toVector())){
                    closest = gameSector;
                }
            }
        }
        
        return closest;
    }
	
	private Set<GameSector> calculateGameSector(HouseDAO houseDAO) {
        Set<GameSector> ret = new HashSet<GameSector>();
        
        for(int y = mapBounds.y;y<=mapBounds.getMaxY();y=y+400){
            for(int x = mapBounds.x;x<=mapBounds.getMaxX();x=x+400){
                ret.add(new GameSector(new Rectangle(x, y, 400, 400)));
            }
        }
        
        for(HouseMessage house:houseDAO.getAllHouses()){
            for(GameSector sector:ret){
                if(sector.containsHouse(house)){
                    sector.add(house);
                }
            }
        }
        
        return ret;
    }
	
	private static class GameSector {
        private final Rectangle rect;
        private final Set<HouseMessage> houses = new HashSet<HouseMessage>();

        public GameSector(Rectangle rect) {
            this.rect = rect;
        }

        private boolean containsHouse(HouseMessage house) {
            return rect.contains(house.getLocation().x, house.getLocation().z);
        }

        private void add(HouseMessage house) {
            houses.add(house);
        }

        private boolean isUnsecured(Country country) {
            for(HouseMessage h:houses){
                if(h.getOwner()!=country){
                    return true;
                }
            }
            return false;
        }

        private Vector3f location() {
            return new Vector3f((float)rect.getCenterX(), 0, (float)rect.getCenterY());
        }
        
        Set<HouseMessage> getHouses(){
            return houses;
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
		node.put("startTime", startTime);
        node.put("classType", getClass().getName());
        return MAPPER.writeValueAsString(node);
	}
	
	@Override
	public boolean clashesWith(Class<? extends Objective> newObjective) {
		return false;
	}

}
