package com.jme3.lostVictories.objectives;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;

public enum SecureSectorState {
	WAIT_FOR_REENFORCEMENTS {
		@Override
		public void runObjective(CharacterMessage c, String uuid, SecureSector objective, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills) {			
		}

		@Override
		public SecureSectorState transition(CharacterMessage c, String uuid, SecureSector objective, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
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
			if(objective.issuedOrders.get(c.getId()).isComplete){
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
			if(c.getCurrentStrength(characterDAO)<=objective.minimumFightingStrenght){
				return RETREAT;
			}
			if(objective.issuedOrders.get(c.getId()).isComplete){
				return CAPTURE_HOUSES;
			}
			return DEPLOY_TO_SECTOR;
		}
	}, 
	
	CAPTURE_HOUSES {
		@Override
		public void runObjective(CharacterMessage c, String uuid, SecureSector objective, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills) {
			final Set<String> exclude = new HashSet<>();
			Predicate<HouseMessage> houseToCapture = new Predicate<HouseMessage>() {				
				@Override
				public boolean test(HouseMessage t) {
					return t.getOwner()!=c.getCountry() && !exclude.contains(t.getId().toString());
				}
			};
			
			for(Iterator<Entry<UUID, Objective>> it = objective.issuedOrders.entrySet().iterator();it.hasNext();){
				Objective o = it.next().getValue();
				if(o instanceof CaptureStructure){
					HouseMessage house = houseDAO.getHouse(UUID.fromString(((CaptureStructure)o).structure));
					if(house.getOwner() == c.getCountry()){
						it.remove();
					}else{
						exclude.add(house.getId().toString());
					}
				}				
			}
			
			c.getUnitsUnderCommand().stream()				
				.filter(id->!objective.issuedOrders.containsKey(id))
				.map(id->characterDAO.getCharacter(id))
				.forEach(unit -> {
					HouseMessage house = findClosestHouse(unit, objective.houses.stream().map(h->houseDAO.getHouse(h)).collect(Collectors.toSet()), houseToCapture);
					if(house!=null){
						try {
							System.out.println(unit.getId()+"CaptureStructure:"+house.getId().toString());

							CaptureStructure captureStructure = new CaptureStructure(house.getId().toString());
							unit.addObjective(UUID.randomUUID(), captureStructure);
							toSave.put(unit.getId(), unit);
							objective.issuedOrders.put(unit.getId(), captureStructure);
							exclude.add(house.getId().toString());
						} catch (IOException e) {
							throw new RuntimeException(e);
						}

					}
				});
		}

		public SecureSectorState transition(CharacterMessage c, String uuid, SecureSector objective, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
			if(c.getCurrentStrength(characterDAO)<=objective.minimumFightingStrenght){
				return RETREAT;
			}
			if(objective.issuedOrders.values().stream().anyMatch(o->!o.isComplete)){
                return CAPTURE_HOUSES;
            }
			return DEFEND_SECTOR;
		}
	},

    DEFEND_SECTOR {
        @Override
        public void runObjective(CharacterMessage c, String uuid, SecureSector objective, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills) {

        }

        @Override
        public SecureSectorState transition(CharacterMessage c, String uuid, SecureSector objective, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
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
					unit.addObjective(UUID.randomUUID(), deployToSector);
					toSave.put(unit.getId(), unit);
					objective.issuedOrders.put(unit.getId(), deployToSector);	
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});

		if(!objective.issuedOrders.containsKey(c.getId())){
			Objective t = null;
			if(CharacterType.AVATAR == c.getCharacterType() || CharacterType.SOLDIER == c.getCharacterType()){
				t = new TravelObjective(c, location, null);
			}else{
				t = new NavigateObjective(location, null);
			}
			objective.issuedOrders.put(c.getId(), t);
		}

		Objective fromStringToObjective = objective.issuedOrders.get(c.getId());
		fromStringToObjective.runObjective(c, uuid, characterDAO, houseDAO, toSave, kills);
		toSave.put(c.getId(), c);
	}

	public abstract void runObjective(CharacterMessage c, String uuid, SecureSector objective, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, UUID> kills);

	public abstract SecureSectorState transition(CharacterMessage c, String uuid, SecureSector objective, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave);

}
