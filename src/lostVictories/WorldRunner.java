package lostVictories;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.jme3.lostVictories.network.messages.Country;
import com.jme3.lostVictories.network.messages.HouseMessage;

public class WorldRunner implements Runnable{

	private CharacterDAO characterDAO;
	private HouseDAO houseDAO;
	Map<Country, Integer> victoryPoints = new EnumMap<Country, Integer>(Country.class);

	public static WorldRunner instance(CharacterDAO characterDAO, HouseDAO houseDAO) {
		return new WorldRunner(characterDAO, houseDAO);
	}

	private WorldRunner(CharacterDAO characterDAO, HouseDAO houseDAO) {
		this.characterDAO = characterDAO;
		this.houseDAO = houseDAO;
		victoryPoints.put(Country.AMERICAN, 1000);
        victoryPoints.put(Country.GERMAN, 1000);
	}

	@Override
	public void run() {
		Set<HouseMessage> dchanged = houseDAO.getAllHouses().stream().filter(h->h.chechOwnership(characterDAO)).collect(Collectors.toSet());
		houseDAO.save(dchanged);
		
//		if(structureOwnership.get(c)<(capturedStructureCount/2)){
//            victoryPoints.put(c, victoryPoints.get(c)-((capturedStructureCount/2)-structureOwnership.get(c)));
//        }
		
	}


}
