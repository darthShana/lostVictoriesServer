package com.jme3.lostVictories.objectives;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.jme3.lostVictories.network.messages.*;
import com.jme3.lostVictories.network.messages.Vector;
import org.apache.log4j.Logger;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.jme3.math.Vector3f;

public class CaptureTown extends Objective {

	@JsonIgnore
	private static Logger log = Logger.getLogger(CaptureTown.class);
	@JsonIgnore
	private Set<GameSector> gameSectors;
	@JsonIgnore
	public Rectangle mapBounds = new Rectangle(-512, -512, 1024, 1024);
	private long startTime;
	
	@SuppressWarnings("unused")
	private CaptureTown() {}
	
	public CaptureTown(long startTime){
		this.startTime = startTime;
	}
	
	@Override
	public void runObjective(CharacterMessage c, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills) {
		if(System.currentTimeMillis()-startTime<(60*1000)){
			return;
		}
		
		if(gameSectors==null){
            gameSectors = calculateGameSectors(houseDAO);
        }
		
        Set<GameSector> exclude = new HashSet<>();
		
		for(UUID cid:c.getUnitsUnderCommand()){
			CharacterMessage unit = characterDAO.getCharacter(cid);
			if(unit !=null && !unit.isBusy() && RankMessage.LIEUTENANT == unit.getRank()){
				GameSector toSecure = findClosestUnsecuredGameSector(unit, gameSectors, exclude);
				if(toSecure==null){
					continue;
				}
				exclude.add(toSecure);
				log.info(c.getCountry()+": assigning new sector:"+toSecure.rects.iterator().next()+" houses:"+toSecure.houses.size());
				SecureSector i = new SecureSector(toSecure.getHouses(), 10, 5, c.getLocation());
				try {
					unit.addObjective(UUID.randomUUID(), i);
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
	
	private GameSector findClosestUnsecuredGameSector(CharacterMessage character, Set<GameSector> gameSectors, Set<GameSector> exclude) {
        GameSector closest = null;
        for(GameSector gameSector:gameSectors){
            if(gameSector.isUnsecured(character.getCountry())){
                if(!exclude.contains(gameSector) && 
                		(closest==null || closest.location().distance(character.getLocation().toVector())>gameSector.location().distance(character.getLocation().toVector()))){
                    closest = gameSector;
                }
            }
        }
        
        return closest;
    }
	
	Set<GameSector> calculateGameSectors(HouseDAO houseDAO) {
        Set<GameSector> remaining = new HashSet<GameSector>();
        
        for(int y = mapBounds.y;y<=mapBounds.getMaxY();y=y+50){
            for(int x = mapBounds.x;x<=mapBounds.getMaxX();x=x+50){
                remaining.add(new GameSector(new Rectangle(x, y, 50, 50)));
            }
        }
        
        for(HouseMessage house:houseDAO.getAllHouses()){
            for(GameSector sector:remaining){
                if(sector.containsHouse(house)){
                    sector.add(house);
                }
            }
        }
        
        remaining = remaining.stream().filter(s->!s.houses.isEmpty()).collect(Collectors.toSet());
        
        //merge joining houses together with a limit on the number of houses
        Set<GameSector> merged = new HashSet<GameSector>();
        GameSector next = remaining.iterator().next();
		merged.add(next);
		remaining.remove(next);
		
        while(!remaining.isEmpty()){
        	boolean foundMerge = false;
        	for(GameSector sector:merged){
        		Optional<GameSector> neighbour = findNeighbouringSector(sector, remaining);
	        	if(neighbour.isPresent()){
	        		sector.merge(neighbour.get());
	        		remaining.remove(neighbour.get());
	        		foundMerge = true;
	        	}
        	}
        	if(!foundMerge){
        		next = remaining.iterator().next();
        		merged.add(next);
        		remaining.remove(next);
        	}
        		
        }       
        return merged;
    }
	
	Optional<GameSector> findNeighbouringSector(GameSector sector, Set<GameSector> ret) {
		return ret.stream().filter(s->sector.isJoinedTo(s)).findFirst();
	}

	static class GameSector {
        private final Set<Rectangle> rects = new HashSet<Rectangle>();
        private final Set<HouseMessage> houses = new HashSet<HouseMessage>();

        public GameSector(Rectangle rect) {
            this.rects.add(rect);
        }

        public boolean isJoinedTo(GameSector s) {
			for(Rectangle r1: rects){
				for(Rectangle r2:s.rects){
					if(new Rectangle(r1.x-1, r1.y-1, r1.width+2, r1.height+2).intersects(r2)){
						return true;
					}
				}
				
			}
			return false;
		}

		public void merge(GameSector neighbour) {
			houses.addAll(neighbour.houses);
			rects.addAll(neighbour.rects);
			
		}

		private boolean containsHouse(HouseMessage house) {
            return rects.stream().filter(r->r.contains(house.getLocation().x, house.getLocation().z)).findAny().isPresent();
        }

		public boolean containsPoint(Vector centre) {
        	if(rects.isEmpty()){
        		return false;
			}
			Rectangle union = null;
        	for(Iterator<Rectangle> it = rects.iterator();it.hasNext();){
        		if(union == null){
        			union = it.next();
				}else{
					union.add(it.next());
				}
			}
			return union.contains(centre.x, centre.z);
		}

        void add(HouseMessage house) {
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
        	Rectangle rect = rects.iterator().next();
            return new Vector3f((float)rect.getCenterX(), 0, (float)rect.getCenterY());
        }
        
        Set<HouseMessage> getHouses(){
            return houses;
        }

	}
	
	@Override
	public boolean clashesWith(Class<? extends Objective> newObjective) {
		return false;
	}

}
