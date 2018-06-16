package com.jme3.lostVictories.objectives;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.jme3.lostVictories.network.messages.*;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaptureTown extends Objective {

	@JsonIgnore
	private static Logger log = LoggerFactory.getLogger(CaptureTown.class);

	private Map<UUID, UUID> sectorAssignments = new HashMap<>();
	private Set<UUID> attempted = new HashSet<>();

//    @JsonIgnore
//	private Integer previousValue;

    @Override
	public void runObjective(CharacterMessage c, String uuid, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills) {

		
        Collection<GameSector> sectors = houseDAO.getGameSectors();

        sectorAssignments.entrySet().removeIf(e->!c.getUnitsUnderCommand().contains(e.getKey()));

//        Map<UUID, Integer> collect = c.getUnitsUnderCommand().stream().map(characterDAO::getCharacter)
//                .filter(Objects::nonNull).collect(Collectors.toMap(unit -> unit.getId(), unit -> unit.getCurrentStrength(characterDAO)));

//        Integer reduce = collect.values().stream().reduce(0, (a, b) -> a + b);
//        if(reduce!=previousValue){
//            collect.entrySet().forEach(unitEntry->{
//                System.out.println(unitEntry.getKey()+" ->"+unitEntry.getValue());
//            });
//        }
//        previousValue = reduce;

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
                        log.info(c.getCountry()+": assigning new sector:"+toSecure.rects.iterator().next()+" houses:"+toSecure.houses.size()+" platoon strength:"+unit.getCurrentStrength(characterDAO));
                        SecureSector i = new SecureSector(toSecure.getId(),15, 5, c.getLocation());
                        sectorAssignments.put(unit.getId(), toSecure.id);
                        attempted.add(toSecure.id);
                        try {
                            unit.addObjective(UUID.randomUUID(), i);
                            toSave.put(unit.getId(), unit);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                });

	}
	
	GameSector findClosestUnsecuredGameSector(CharacterMessage character, Collection<GameSector> gameSectors, Set<UUID> exclude, HouseDAO houseDAO) {
        GameSector closest = null;
        for(GameSector gameSector:gameSectors){
            if(gameSector.isUnsecured(character.getCountry(), houseDAO)){
                if(!exclude.contains(gameSector.id) &&
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


    @Override
    public boolean clashesWith(Class<? extends Objective> newObjective) {
        return false;
    }

}
