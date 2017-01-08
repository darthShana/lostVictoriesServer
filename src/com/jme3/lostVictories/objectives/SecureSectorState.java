package com.jme3.lostVictories.objectives;

import static lostVictories.CharacterRunner.fromStringToObjective;

import java.io.IOException;
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
		public void runObjective(CharacterMessage c, String uuid, Vector centre, Set<UUID> houses, Map<String, String> issuedOrders, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
			c.getUnitsUnderCommand().stream()				
				.filter(id->!issuedOrders.containsKey(id.toString()))
				.map(id->characterDAO.getCharacter(id))
				.filter(cc->cc!=null)//should never happen but does need to fix
				.forEach(new Consumer<CharacterMessage>() {

				@Override
				public void accept(CharacterMessage unit) {
					try {
						TransportSquad deployToSector = new TransportSquad(centre);
						System.out.println(unit.getId()+" new transport to:"+centre);

						unit.addObjective(UUID.randomUUID(), deployToSector.asJSON());
						toSave.put(unit.getId(), unit);
						issuedOrders.put(unit.getId().toString(), deployToSector.asJSON());	
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}					
			});
			try {
				if(!issuedOrders.containsKey(c.getId().toString())){
					TravelObjective t = new TravelObjective(centre, null);
					issuedOrders.put(c.getId().toString(), t.asJSON());
				}

				Objective fromStringToObjective = fromStringToObjective(issuedOrders.get(c.getId().toString()));
				fromStringToObjective.runObjective(c, uuid, characterDAO, houseDAO, toSave);
				issuedOrders.put(c.getId().toString(), fromStringToObjective.asJSON());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			toSave.put(c.getId(), c);
		}

		@Override
		public SecureSectorState tansition(CharacterMessage c, String uuid, Vector centre, Set<UUID> houses, Map<String, String> issuedOrders, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
			if(fromStringToObjective(issuedOrders.get(c.getId().toString())).isComplete){
				return CAPTURE_HOUSES;
			}
			return DEPLOY_TO_SECTOR;
		}
	}, 
	
	CAPTURE_HOUSES {
		@Override
		public void runObjective(CharacterMessage c, String uuid, Vector centre, Set<UUID> houses, Map<String, String> issuedOrders, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
			Predicate<HouseMessage> houseToCapture = new Predicate<HouseMessage>() {
				
				@Override
				public boolean test(HouseMessage t) {
					return t.getOwner()==c.getCountry();
				}
			};
			
			for(Iterator<Entry<String, String>> it = issuedOrders.entrySet().iterator();it.hasNext();){
				Objective o = fromStringToObjective(it.next().getValue());
				if(o instanceof CaptureStructure){
					if(houseDAO.getHouse(UUID.fromString(((CaptureStructure)o).structure)).getOwner() == c.getCountry()){
						it.remove();
					}
				}
				
			}
			
			c.getUnitsUnderCommand().stream()
				
				.filter(id->!issuedOrders.containsKey(id.toString()))
				.map(id->characterDAO.getCharacter(id))
				.filter(cc->cc!=null)//should never happen but does need to fix
				.forEach(new Consumer<CharacterMessage>() {

				@Override
				public void accept(CharacterMessage unit) {
					HouseMessage house = findClosestHouse(unit, houses.stream().map(h->houseDAO.getHouse(h)).collect(Collectors.toSet()), houseToCapture);
					if(house!=null){
						try {
							CaptureStructure captureStructure = new CaptureStructure(house.getId().toString());
							unit.addObjective(UUID.randomUUID(), captureStructure.asJSON());
							toSave.put(unit.getId(), unit);
							issuedOrders.put(unit.getId().toString(), captureStructure.asJSON());
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}
			});
		}

		@Override
		public SecureSectorState tansition(CharacterMessage c, String uuid, Vector centre, Set<UUID> houses, Map<String, String> issuedOrders, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave) {
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

	public abstract void runObjective(CharacterMessage c, String uuid, Vector centre, Set<UUID> houses, Map<String, String> issuedOrders, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave);

	public abstract SecureSectorState tansition(CharacterMessage c, String uuid, Vector centre, Set<UUID> houses, Map<String, String> issuedOrders, CharacterDAO characterDAO, HouseDAO houseDAO, Map<UUID, CharacterMessage> toSave);

}
