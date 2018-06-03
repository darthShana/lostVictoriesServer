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
	public Rectangle mapBounds = new Rectangle(-512, -512, 1024, 1024);

	private Map<UUID, GameSector> sectorAssignments = new HashMap<>();
	private Set<GameSector> attempted = new HashSet<>();
	private Set<GameSector> sectors = new HashSet<>();

	@Override
	public void runObjective(CharacterMessage c, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills) {

		
		if(sectors.isEmpty()){
            sectors = calculateGameSectors(houseDAO);
            System.out.println("calculated game sectors:"+sectors);
        }

        sectorAssignments.entrySet().removeIf(e->!c.getUnitsUnderCommand().contains(e.getKey()));

		c.getUnitsUnderCommand().stream().map(characterDAO::getCharacter)
                .filter(Objects::nonNull)
                .filter(unit->!unit.isBusy())
                .filter(unit->unit.getCurrentStrength(characterDAO)>=15)
                .filter(unit->RankMessage.LIEUTENANT==unit.getRank())
                .sorted(Comparator.comparingInt(unit -> -unit.getCurrentStrength(characterDAO)))
                .forEach(unit->{

                    System.out.println("sectorAssignments:"+sectorAssignments);
                    GameSector toSecure = findClosestUnsecuredGameSector(unit, sectors, attempted, houseDAO);
                    if(toSecure==null) {
                        toSecure = findClosestUnsecuredGameSector(unit, sectors, new HashSet<>(sectorAssignments.values()), houseDAO);
                    }

                    if(toSecure!=null){
                        log.info(c.getCountry()+": assigning new sector:"+toSecure.rects.iterator().next()+" houses:"+toSecure.structures.size()+" platoon strength:"+unit.getCurrentStrength(characterDAO));
                        SecureSector i = new SecureSector(toSecure.getHouses(houseDAO), toSecure.getBunkers(houseDAO),15, 5, c.getLocation());
                        sectorAssignments.put(unit.getId(), toSecure);
                        attempted.add(toSecure);
                        try {
                            unit.addObjective(UUID.randomUUID(), i);
                            toSave.put(unit.getId(), unit);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                });

	}
	
	GameSector findClosestUnsecuredGameSector(CharacterMessage character, Set<GameSector> gameSectors, Set<GameSector> exclude, HouseDAO houseDAO) {
        GameSector closest = null;
        for(GameSector gameSector:gameSectors){
            if(gameSector.isUnsecured(character.getCountry(), houseDAO)){
                if(!exclude.contains(gameSector) &&
                		(closest==null || weightedDistance(character, closest) > weightedDistance(character, gameSector))){
                    closest = gameSector;
                }
            }
        }
        
        return closest;
    }

	private float weightedDistance(CharacterMessage character, GameSector closest) {
		return closest.location().distance(character.getLocation().toVector())/closest.getHousesCount();
	}

	Set<GameSector> calculateGameSectors(HouseDAO houseDAO) {
        final List<GameSector> sectors = new ArrayList<>();
        
        for(int y = mapBounds.y;y<=mapBounds.getMaxY();y=y+33){
            for(int x = mapBounds.x;x<=mapBounds.getMaxX();x=x+33){
                sectors.add(new GameSector(new Rectangle(x, y, 33, 33)));
            }
        }

        Consumer<Structure> structureVisitor = structure -> {
            sectors.stream().filter(s->s.containsHouse(structure)).findFirst().ifPresent(s->s.add(structure));
        };

        houseDAO.getAllHouses().forEach(structureVisitor);
        houseDAO.getAllBunkers().forEach(structureVisitor);


        List<GameSector> remaining = sectors.stream().filter(s->!s.structures.isEmpty()).collect(Collectors.toList());
        
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
	
	Optional<GameSector> findNeighbouringSector(GameSector sector, List<GameSector> ret) {
		return ret.stream().filter(s->sector.isJoinedTo(s)).findFirst();
	}

    @Override
    public boolean clashesWith(Class<? extends Objective> newObjective) {
        return false;
    }

}
