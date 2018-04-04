package com.jme3.lostVictories.objectives;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.jme3.lostVictories.network.messages.*;
import com.jme3.lostVictories.network.messages.Vector;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.jme3.math.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaptureTown extends Objective {

	@JsonIgnore
	private static Logger log = LoggerFactory.getLogger(CaptureTown.class);
	@JsonIgnore
	private Set<GameSector> gameSectors;
	@JsonIgnore
	public Rectangle mapBounds = new Rectangle(-512, -512, 1024, 1024);
	
	@Override
	public void runObjective(CharacterMessage c, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills) {

		
		if(gameSectors==null){
            gameSectors = calculateGameSectors(houseDAO);
        }
		
        Set<GameSector> exclude = new HashSet<>();

		c.getUnitsUnderCommand().stream().map(id->characterDAO.getCharacter(id))
                .filter(unit->unit!=null)
                .filter(unit->!unit.isBusy())
                .filter(unit->RankMessage.LIEUTENANT==unit.getRank())
                .sorted(Comparator.comparingInt(unit -> -unit.getCurrentStrength(characterDAO)))
                .forEach(unit->{

                    GameSector toSecure = findClosestUnsecuredGameSector(unit, gameSectors, exclude);
                    if(toSecure!=null){
                        exclude.add(toSecure);
                        log.info(c.getCountry()+": assigning new sector:"+toSecure.rects.iterator().next()+" houses:"+toSecure.structures.size());
                        SecureSector i = new SecureSector(toSecure.getHouses(), toSecure.getBunkers(),10, 5, c.getLocation());
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

                });

	}
	
	GameSector findClosestUnsecuredGameSector(CharacterMessage character, Set<GameSector> gameSectors, Set<GameSector> exclude) {
        GameSector closest = null;
        for(GameSector gameSector:gameSectors){
            if(gameSector.isUnsecured(character.getCountry())){
                if(!exclude.contains(gameSector) &&
                		(closest==null || weightedDistance(character, closest) > weightedDistance(character, gameSector))){
                    closest = gameSector;
                }
            }
        }
        
        return closest;
    }

	private float weightedDistance(CharacterMessage character, GameSector closest) {
		return closest.location().distance(character.getLocation().toVector())/closest.getHouses().size();
	}

	Set<GameSector> calculateGameSectors(HouseDAO houseDAO) {
        final Set<GameSector> sectors = new HashSet<>();
        
        for(int y = mapBounds.y;y<=mapBounds.getMaxY();y=y+50){
            for(int x = mapBounds.x;x<=mapBounds.getMaxX();x=x+50){
                sectors.add(new GameSector(new Rectangle(x, y, 50, 50)));
            }
        }

        Consumer<Structure> structureVisitor = structure -> {
            sectors.stream().filter(s->s.containsHouse(structure)).findFirst().ifPresent(s->s.add(structure));
        };

        houseDAO.getAllHouses().forEach(structureVisitor);
        houseDAO.getAllBunkers().forEach(structureVisitor);


        Set<GameSector> remaining = sectors.stream().filter(s->!s.structures.isEmpty()).collect(Collectors.toSet());
        
        //merge joining houses together with a limit on the number of houses
        Set<GameSector> merged = new HashSet<>();
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
        private Set<Rectangle> rects = new HashSet<>();
        private Set<Structure> structures = new HashSet<>();

        GameSector(){}

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
            structures.addAll(neighbour.structures);
			rects.addAll(neighbour.rects);
			
		}

		private boolean containsHouse(Structure house) {
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

        void add(Structure house) {
            structures.add(house);
        }

        private boolean isUnsecured(Country country) {
            for(Structure h:structures){
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
            return structures.stream()
                    .filter(s->s instanceof HouseMessage)
                    .map(h->(HouseMessage)h).collect(Collectors.toSet());
        }

        Set<BunkerMessage> getBunkers(){
            return structures.stream()
                    .filter(s->s instanceof BunkerMessage)
                    .map(b->(BunkerMessage)b).collect(Collectors.toSet());
        }

	}

    @Override
    public boolean clashesWith(Class<? extends Objective> newObjective) {
        return false;
    }

}
