package lostVictories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.lostVictories.service.LostVictoriesServiceImpl;
import lostVictories.dao.CharacterDAO;
import lostVictories.dao.GameRequestDAO;
import lostVictories.dao.HouseDAO;
import lostVictories.dao.PlayerUsageDAO;
import lostVictories.messageHanders.MessageRepository;

import lostVictories.service.LostVictoryService;
import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.AchievementStatus;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.Country;
import com.jme3.lostVictories.network.messages.GameStatistics;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.objectives.SecureSectorState;

public class WorldRunner implements Runnable{


	private static WorldRunner instance;
	private Map<Country, Integer> victoryPoints = new EnumMap<Country, Integer>(Country.class);
	private Map<Country, Integer> manPower = new EnumMap<Country, Integer>(Country.class);

	private Map<Country, Integer> structureOwnership = new EnumMap<>(Country.class);
	private Map<Country, Integer> nextRespawnTime = new EnumMap<>(Country.class);
	private Map<Country, WeaponsFactory> weaponsFactory = new HashMap<Country, WeaponsFactory>();
	private Map<Country, VehicleFactory> vehicleFactory = new HashMap<Country, VehicleFactory>();

	private Map<UUID, AchievementStatus> achivementCache = new HashMap<UUID, AchievementStatus>();

	private String gameName;

	private LostVictoriesServiceImpl lostVictoryService;

	public static WorldRunner instance(String gameName) {
		if(instance==null){
			instance = new WorldRunner(gameName);
		}
		return instance;
	}

	private WorldRunner(String gameName) {
		this.gameName = gameName;
		victoryPoints.put(Country.AMERICAN, 5000);
        victoryPoints.put(Country.GERMAN, 5000);
        vehicleFactory.put(Country.AMERICAN, new VehicleFactory(Country.AMERICAN));
        vehicleFactory.put(Country.GERMAN, new VehicleFactory(Country.GERMAN));
        weaponsFactory.put(Country.AMERICAN, new WeaponsFactory(Country.AMERICAN));
        weaponsFactory.put(Country.GERMAN, new WeaponsFactory(Country.GERMAN));
	}

	@Override
	public void run() {
		try {
            structureOwnership = lostVictoryService.runWorld(victoryPoints, manPower, weaponsFactory, vehicleFactory, nextRespawnTime, gameName);
        }catch (Exception e){
		    e.printStackTrace();
        }
    }





	public GameStatistics getStatistics(Country country) {
		GameStatistics statistics = new GameStatistics();
		
		final Country other;
		if(country==Country.GERMAN){
			other = Country.AMERICAN;
		}else{
			other = Country.GERMAN;
		}
		
		statistics.setVictorypoints(victoryPoints.get(country), victoryPoints.get(other));
		statistics.setHousesCaptured(structureOwnership.get(country), structureOwnership.get(other));
		statistics.setAvatarRespawnEstimate(nextRespawnTime.get(country));
		
		return statistics;
	}

	public AchievementStatus getAchivementStatus(CharacterMessage avatar, CharacterDAO characterDAO) {
		AchievementStatus achivementStatus = achivementCache .get(avatar.getId());
		if(achivementStatus==null || System.currentTimeMillis()-achivementStatus.getSentTime()>2000){
			RankMessage rank = avatar.getRank();
			achivementStatus = new AchievementStatus(rank.getAchivementMessage(), avatar.totalKillCount(characterDAO), rank.getTotalAchivementCount(), System.currentTimeMillis());
			achivementCache.put(avatar.getId(), achivementStatus);
		}
		return achivementStatus;
	}


	public void setLostVictoryService(LostVictoriesServiceImpl lostVictoryService) {
		this.lostVictoryService = lostVictoryService;
	}
}
