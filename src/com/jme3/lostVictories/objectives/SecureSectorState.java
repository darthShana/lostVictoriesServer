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
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;

public enum SecureSectorState {
	DEPLOY_TO_SECTOR {
		@Override
		public void runObjective(CharacterMessage c, String uuid, Vector centre, Set<UUID> houses, Map<UUID, Objective> issuedOrders, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
			c.getUnitsUnderCommand().stream()				
				.filter(id->!issuedOrders.containsKey(id))
				.map(id->characterDAO.getCharacter(id))
				.forEach(new Consumer<CharacterMessage>() {

				@Override
				public void accept(CharacterMessage unit) {
					try {
						TransportSquad deployToSector = new TransportSquad(centre);
						unit.addObjective(UUID.randomUUID(), deployToSector);
						toSave.put(unit.getId(), unit);
						issuedOrders.put(unit.getId(), deployToSector);	
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}			
			});
			if(!issuedOrders.containsKey(c.getId())){
				TravelObjective t = new TravelObjective(centre, null);
				issuedOrders.put(c.getId(), t);
			}

			Objective fromStringToObjective = issuedOrders.get(c.getId());
			fromStringToObjective.runObjective(c, uuid, characterDAO, houseDAO, toSave);
			if(fromStringToObjective.isComplete){
				System.out.println(fromStringToObjective+"is complete");
			}
			toSave.put(c.getId(), c);
		}

		@Override
		public SecureSectorState tansition(CharacterMessage c, String uuid, Vector centre, Set<UUID> houses, Map<UUID, Objective> issuedOrders, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
			if(issuedOrders.get(c.getId()).isComplete){
				System.out.println(((TravelObjective)(issuedOrders.get(c.getId()))).destination+" complete changing to houses capture mode");
				return CAPTURE_HOUSES;
			}
			return DEPLOY_TO_SECTOR;
		}
	}, 
	
	CAPTURE_HOUSES {
		@Override
		public void runObjective(CharacterMessage c, String uuid, Vector centre, Set<UUID> houses, Map<UUID, Objective> issuedOrders, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
			final Set<String> exclude = new HashSet<>();
			Predicate<HouseMessage> houseToCapture = new Predicate<HouseMessage>() {				
				@Override
				public boolean test(HouseMessage t) {
					return t.getOwner()!=c.getCountry() && !exclude.contains(t.getId().toString());
				}
			};
			
			for(Iterator<Entry<UUID, Objective>> it = issuedOrders.entrySet().iterator();it.hasNext();){
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
				.filter(id->!issuedOrders.containsKey(id))
				.map(id->characterDAO.getCharacter(id))
				.forEach(new Consumer<CharacterMessage>() {

				@Override
				public void accept(CharacterMessage unit) {
					HouseMessage house = findClosestHouse(unit, houses.stream().map(h->houseDAO.getHouse(h)).collect(Collectors.toSet()), houseToCapture);
					if(house!=null){
						try {
							System.out.println(unit.getId()+"CaptureStructure:"+house.getId().toString());

							CaptureStructure captureStructure = new CaptureStructure(house.getId().toString());
							unit.addObjective(UUID.randomUUID(), captureStructure);
							toSave.put(unit.getId(), unit);
							issuedOrders.put(unit.getId(), captureStructure);
							exclude.add(house.getId().toString());
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}
			});
		}

		@Override
		public SecureSectorState tansition(CharacterMessage c, String uuid, Vector centre, Set<UUID> houses, Map<UUID, Objective> issuedOrders, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
			return CAPTURE_HOUSES;
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

	public abstract void runObjective(CharacterMessage c, String uuid, Vector centre, Set<UUID> houses, Map<UUID, Objective> issuedOrders, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave);

	public abstract SecureSectorState tansition(CharacterMessage c, String uuid, Vector centre, Set<UUID> houses, Map<UUID, Objective> issuedOrders, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave);

}
