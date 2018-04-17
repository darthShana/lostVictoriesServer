package com.jme3.lostVictories.objectives;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jme3.lostVictories.network.messages.*;
import com.jme3.lostVictories.network.messages.Vector;
import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.jme3.math.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum SecureSectorState {

    WAIT_FOR_REENFORCEMENTS {
		@Override
		public void runObjective(CharacterMessage c, String uuid, SecureSector objective, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills) {			
		}

		@Override
		public SecureSectorState transition(CharacterMessage c, String uuid, SecureSector objective, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
            if(objective.boundary.contains(new Point2D.Float(c.getLocation().x, c.getLocation().z))){
                return CAPTURE_HOUSES;
            }
		    if(c.getCurrentStrength(characterDAO)>=objective.deploymentStrength){
				return DEPLOY_TO_SECTOR;
			}
			return WAIT_FOR_REENFORCEMENTS;
		}
	},
	RETREAT {
		@Override
		public void runObjective(CharacterMessage c, String uuid, SecureSector objective, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills) {
			sendEveryoneTo(c, uuid, objective, characterDAO, houseDAO, toSave, kills, objective.homeBase);			
		}

		@Override
		public SecureSectorState transition(CharacterMessage c, String uuid, SecureSector objective, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
			if(objective.embededObjective.isComplete){
				return WAIT_FOR_REENFORCEMENTS;
			}
			return RETREAT;
		}
	},
	DEPLOY_TO_SECTOR {
		@Override
		public void runObjective(CharacterMessage c, String uuid, SecureSector objective, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills) {
			sendEveryoneTo(c, uuid, objective, characterDAO, houseDAO, toSave, kills, objective.centre);
		}

		@Override
		public SecureSectorState transition(CharacterMessage c, String uuid, SecureSector objective, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
			if(c.getCurrentStrength(characterDAO)<=objective.minimumFightingStrength){
				return RETREAT;
			}
			if(objective.embededObjective.isComplete){
				return CAPTURE_HOUSES;
			}
			if(objective.boundary.contains(new Point2D.Float(c.getLocation().x, c.getLocation().z))){
                return CAPTURE_HOUSES;
            }
			return DEPLOY_TO_SECTOR;
		}
	}, 
	
	CAPTURE_HOUSES {
		@Override
		public void runObjective(CharacterMessage c, String uuid, SecureSector objective, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills) {
			final Set<String> exclude = new HashSet<>();
			Predicate<HouseMessage> houseToCapture = t -> t.getOwner()!=c.getCountry() && !exclude.contains(t.getId().toString());

            HashSet<UUID> availableUnits = new HashSet<>(c.getUnitsUnderCommand());
            Map<UUID, CharacterMessage> allCharacters = characterDAO.getAllCharacters(availableUnits);

            for(Iterator<Entry<UUID, UUID>> it = objective.issuedOrders.entrySet().iterator();it.hasNext();){
                Entry<UUID, UUID> next = it.next();

                CharacterMessage characterMessage = allCharacters.get(next.getKey());
                if(characterMessage!=null) {
                    Objective o = characterMessage.getObjectiveSafe(next.getValue());
                    if (o instanceof CaptureStructure) {
                        HouseMessage house = houseDAO.getHouse(UUID.fromString(((CaptureStructure) o).structure));
                        if (!o.isComplete) {
                            availableUnits.remove(next.getKey());
                        }
                        exclude.add(house.getId().toString());

                    }
                }
			}

            availableUnits.stream()
				.map(id->characterDAO.getCharacter(id)).filter(ch->ch!=null)
				.forEach(unit -> {
					HouseMessage house = findClosestHouse(unit, objective.houses.stream().map(h->houseDAO.getHouse(h)).collect(Collectors.toSet()), houseToCapture);
					if(house!=null){
						try {
							CaptureStructure captureStructure = new CaptureStructure(house.getId().toString());
                            UUID id = UUID.randomUUID();
                            unit.addObjective(id, captureStructure);
							toSave.put(unit.getId(), unit);
							objective.issuedOrders.put(unit.getId(), id);
							exclude.add(house.getId().toString());
						} catch (IOException e) {
							throw new RuntimeException(e);
						}

					}
				});
		}

		public SecureSectorState transition(CharacterMessage c, String uuid, SecureSector objective, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
			if(!objective.boundary.contains(new Point2D.Float(c.getLocation().x, c.getLocation().z)) && c.getCurrentStrength(characterDAO)<=objective.minimumFightingStrength){
				return RETREAT;
			}
            Map<UUID, CharacterMessage> allCharacters = characterDAO.getAllCharacters(objective.issuedOrders.keySet());
            if(objective.issuedOrders.entrySet().stream()
                    .map(e-> allCharacters.get(e.getKey()).getObjectiveSafe(e.getValue()))
                    .filter(o -> o!=null)
                    .anyMatch(o->!o.isComplete)){
                return CAPTURE_HOUSES;
            }
			return DEFEND_SECTOR;
		}
	},

    DEFEND_SECTOR {
        @Override
        public void runObjective(CharacterMessage c, String uuid, SecureSector objective, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills) {
            final List<BunkerMessage> bunkers = new ArrayList<>();
            bunkers.addAll(houseDAO.getBunkers(objective.bunkers));

            characterDAO.getAllCharacters(c.getUnitsUnderCommand()).entrySet().stream()
                    .filter(entry -> !objective.issuedOrders.containsKey(entry.getKey()))
                    .forEach(entry -> {
                        if(!bunkers.isEmpty()){
                            BunkerMessage bunkerMessage = bunkers.get(0);
                            log.info(entry.getKey()+" moving to bunker:"+ bunkerMessage.getLocation());
                            UUID id = UUID.randomUUID();
                            TransportSquad travelObjective = new TransportSquad(bunkerMessage.getEntryPoint());
                            objective.issuedOrders.put(entry.getKey(), id);
                            try {
                                entry.getValue().addObjective(id, travelObjective);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                            toSave.put(entry.getKey(), entry.getValue());
                            bunkers.remove(0);
                        }
                    });
            if(objective.securedHouseCount==null) {
                objective.securedHouseCount = objective.houses.stream().map(hid -> houseDAO.getHouse(hid)).filter(h -> c.getCountry() == h.getOwner()).count();
            }
        }

        @Override
        public SecureSectorState transition(CharacterMessage c, String uuid, SecureSector objective, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
            long currentHouseCount = objective.houses.stream().map(hid->houseDAO.getHouse(hid)).filter(h->c.getCountry()==h.getOwner()).count();
            if(currentHouseCount<objective.securedHouseCount){
                return CAPTURE_HOUSES;
            }
            return DEFEND_SECTOR;
        }
    };
	
	public static HouseMessage findClosestHouse(CharacterMessage c, Set<HouseMessage> allHouses, Predicate<HouseMessage> pred) {
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

	private static void sendEveryoneTo(CharacterMessage c, String uuid, SecureSector objective, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills, Vector location) {
		c.getUnitsUnderCommand().stream()				
			.filter(id->!objective.issuedOrders.containsKey(id))
			.map(id->characterDAO.getCharacter(id))
			.filter(unit->unit.getRank()==RankMessage.CADET_CORPORAL)
			.forEach(unit -> {
				try {
					TransportSquad deployToSector = new TransportSquad(location);
                    UUID id = UUID.randomUUID();
                    unit.addObjective(id, deployToSector);
					toSave.put(unit.getId(), unit);
					objective.issuedOrders.put(unit.getId(), id);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});

		if(objective.embededObjective == null){
			Objective t;
			if(CharacterType.AVATAR == c.getCharacterType() || CharacterType.SOLDIER == c.getCharacterType()){
				t = new TravelObjective(c, location, null);
			}else{
				t = new NavigateObjective(location, null);
			}
			objective.embededObjective = t;
		}

		objective.embededObjective.runObjective(c, uuid, characterDAO, houseDAO, toSave, kills);
		toSave.put(c.getId(), c);
	}

	public abstract void runObjective(CharacterMessage c, String uuid, SecureSector objective, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills);

	public abstract SecureSectorState transition(CharacterMessage c, String uuid, SecureSector objective, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave);

    private static Logger log = LoggerFactory.getLogger(SecureSectorState.class);

}
