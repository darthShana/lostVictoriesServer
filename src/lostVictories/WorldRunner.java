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

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.GameRequestDAO;
import lostVictories.dao.GameStatusDAO;
import lostVictories.dao.HouseDAO;
import lostVictories.dao.PlayerUsageDAO;
import lostVictories.messageHanders.MessageRepository;

import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.AchivementStatus;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.Country;
import com.jme3.lostVictories.network.messages.GameStatistics;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.objectives.SecureSectorState;

public class WorldRunner implements Runnable{

	private static final int COST_OF_UNIT = 500;

	private static Logger log = Logger.getLogger(WorldRunner.class); 
	
	private CharacterDAO characterDAO;
	private HouseDAO houseDAO;
	private GameStatusDAO gameStatusDAO;
	
	private static WorldRunner instance;
	private Map<Country, Integer> victoryPoints = new EnumMap<Country, Integer>(Country.class);
	private Map<Country, Long> manPower = new EnumMap<Country, Long>(Country.class);

	private Map<Country, Long> structureOwnership = new EnumMap<Country, Long>(Country.class);
	private Map<Country, Long> nextRespawnTime = new EnumMap<Country, Long>(Country.class);
	private Map<Country, WeaponsFactory> weaponsFactory = new HashMap<Country, WeaponsFactory>();
	private Map<Country, VehicleFactory> vehicleFactory = new HashMap<Country, VehicleFactory>();

	private Map<UUID, AchivementStatus> achivementCache = new HashMap<UUID, AchivementStatus>();

	private GameRequestDAO gameRequestDAO;

	private String gameName;

	private MessageRepository messageRepository;

	private PlayerUsageDAO playerUsageDAO;

	public static WorldRunner instance(String gameName, CharacterDAO characterDAO, HouseDAO houseDAO, GameStatusDAO gameStatusDAO, GameRequestDAO gameRequestDAO, PlayerUsageDAO playerUsageDAO, MessageRepository messageRepository) {
		if(instance==null){
			instance = new WorldRunner(gameName, characterDAO, houseDAO, gameStatusDAO, gameRequestDAO, playerUsageDAO, messageRepository);
		}
		return instance;
	}

	private WorldRunner(String gameName, CharacterDAO characterDAO, HouseDAO houseDAO, GameStatusDAO gameStatusDAO, GameRequestDAO gameRequestDAO, PlayerUsageDAO playerUsageDAO, MessageRepository messageRepository) {
		this.gameName = gameName;
		this.characterDAO = characterDAO;
		this.houseDAO = houseDAO;
		this.gameStatusDAO = gameStatusDAO;
		this.gameRequestDAO = gameRequestDAO;
		this.playerUsageDAO = playerUsageDAO;
		this.messageRepository = messageRepository;
		victoryPoints.put(Country.AMERICAN, 5000);
        victoryPoints.put(Country.GERMAN, 5000);
        vehicleFactory.put(Country.AMERICAN, new VehicleFactory(Country.AMERICAN));
        vehicleFactory.put(Country.GERMAN, new VehicleFactory(Country.GERMAN));
        weaponsFactory.put(Country.AMERICAN, new WeaponsFactory(Country.AMERICAN));
        weaponsFactory.put(Country.GERMAN, new WeaponsFactory(Country.GERMAN));
	}

	@Override
	public void run() {
		try{
			Set<HouseMessage> allHouses = houseDAO.getAllHouses();
			Set<HouseMessage> dchanged = allHouses.stream().filter(h->h.chechOwnership(characterDAO)).collect(Collectors.toSet());
			houseDAO.save(dchanged);
			
			structureOwnership = allHouses.stream().filter(h->h.isOwned()).collect(Collectors.groupingBy(HouseMessage::getOwner, Collectors.counting()));
			long capturedStructureCount = structureOwnership.values().stream().reduce(0l, (a, b)->a+b);
			
			for(Country c: victoryPoints.keySet()){
				long numberOfProperties = structureOwnership.get(c)!=null?structureOwnership.get(c):0;
				log.trace(c+":"+numberOfProperties);
				if(numberOfProperties<capturedStructureCount/2f){
					victoryPoints.put(c, (int) (victoryPoints.get(c)-((capturedStructureCount/2f)-numberOfProperties)));
				}
			}

			for(Country c:structureOwnership.keySet()){

				if(!manPower.containsKey(c)){
					manPower.put(c, 0l);
				}
				manPower.put(c, manPower.get(c)+structureOwnership.get(c));
				if(manPower.get(c)>(structureOwnership.get(c)*COST_OF_UNIT)){
					manPower.put(c, structureOwnership.get(c)*COST_OF_UNIT);
				}

				if(structureOwnership.get(c)>0){
					nextRespawnTime.put(c, (COST_OF_UNIT-manPower.get(c))/structureOwnership.get(c)*2);
				}
			}
			
			Set<CharacterMessage> allCharacters = characterDAO.getAllCharacters();
			AvatarStore avatarStore = new AvatarStore(characterDAO);
			
			for(Country c:weaponsFactory.keySet()){
				weaponsFactory.get(c).updateSenses(allCharacters);
				vehicleFactory.get(c).updateSenses(allCharacters);
			}
			
			List<CharacterMessage> list = new ArrayList<CharacterMessage>(allCharacters);
			list.sort((c1, c2)->c1.getRank()!=RankMessage.CADET_CORPORAL&&c2.getRank()==RankMessage.CADET_CORPORAL?1:-1);
			
			for(CharacterMessage c: list){
                if(!c.isDead()){
                    if(!c.isFullStrength() && hasManPowerToReenforce(c.getCountry())){
                    	log.debug("found understrenth unit to reenfoce:"+c.getId()+" rank"+c.getRank());
                        Optional<CharacterMessage> deadAvatars = avatarStore.getDeadAvatars(c.getCountry());
						if(deadAvatars.isPresent() && c.getCharacterType()!=CharacterType.AVATAR){
							log.debug("in here test reincarnate avatar");
							Collection<CharacterMessage> toUpdate = new ArrayList<CharacterMessage>();
							CharacterMessage replaceWithAvatar = avatarStore.reincarnateAvatar(deadAvatars.get(), c, toUpdate);
							if(replaceWithAvatar!=null){
								log.debug("character:"+c.getId()+" replaced by avatar:"+deadAvatars.get().getId());
								characterDAO.delete(c);
								toUpdate.addAll(replaceWithAvatar.reenforceCharacter(c.getLocation().add(15, 7, 15), weaponsFactory.get(c.getCountry()), vehicleFactory.get(c.getCountry()), characterDAO));
							}
							characterDAO.save(toUpdate);							
                        }else{
                        	log.debug("in here test reenforce:"+c.getId());
                        	HouseMessage point = SecureSectorState.findClosestHouse(c, houseDAO.getAllHouses(), h -> h.getOwner()==c.getCountry());
                        	if(point!=null){
	                            c.reenforceCharacter(point.getLocation(), weaponsFactory.get(c.getCountry()), vehicleFactory.get(c.getCountry()), characterDAO);
	                            characterDAO.updateCharactersUnderCommand(c);
                        	}
                        }
						characterDAO.refresh();
                        reduceManPower(c.getCountry());
                        
                    }
                }else if(c.getTimeOfDeath()<(System.currentTimeMillis()-60000) && c.getCharacterType()!=CharacterType.AVATAR){
//                	log.debug("removing dead character:"+c.getId()+" co:"+c.getCommandingOfficer());
//                	characterDAO.delete(c);
                }else if(c.getCommandingOfficer()==null && c.getRank()==RankMessage.PRIVATE){
                	CharacterMessage newCo = characterDAO.findClosestCharacter(c, RankMessage.CADET_CORPORAL);
                	if(newCo!=null){
                		log.debug("found orphan unit:"+c.getId()+" adopted by:"+newCo.getId());
                		newCo.addopt(c);
                		characterDAO.putCharacter(newCo.getId(), newCo);
                		characterDAO.putCharacter(c.getId(), c);
                	}
                }
            }
			
			for(CharacterMessage avatar: avatarStore.getLivingAvatars()){
				if(avatar.hasAchivedRankObjectives(characterDAO)){
					log.info("promoting avatar:"+avatar.getId());
					UUID coId = avatar.getCommandingOfficer();
					if(coId!=null){
						CharacterMessage co = characterDAO.getCharacter(coId);
						if(CharacterType.AVATAR != co.getCharacterType()){
							Set<CharacterMessage> promotions = avatar.promoteAvatar(co, characterDAO);
							characterDAO.saveCommandStructure(promotions.stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity())));
							characterDAO.refresh();
						}
					}
					if(avatar.getCheckoutClient().equals(avatar.getId())){
						messageRepository.addMessage(avatar.getCheckoutClient(), "Congradulations! You have been promoted to:"+avatar.getRank());
					}
				}
			}
			
			log.trace("german vp:"+victoryPoints.get(Country.GERMAN));
			log.trace("american vp:"+victoryPoints.get(Country.AMERICAN));
			if(victoryPoints.get(Country.GERMAN)<=0){
				gameStatusDAO.recordAmericanVictory();
				playerUsageDAO.endAllGameSessions(System.currentTimeMillis());
				UUID gameRequest = gameRequestDAO.getGameRequest(gameName);
				gameRequestDAO.updateGameeRequest(gameRequest, "COMPLETED");
				
			}
			if(victoryPoints.get(Country.AMERICAN)<=0){
				gameStatusDAO.recordGermanVictory();
				playerUsageDAO.endAllGameSessions(System.currentTimeMillis());
				UUID gameRequest = gameRequestDAO.getGameRequest(gameName);
				gameRequestDAO.updateGameeRequest(gameRequest, "COMPLETED");
			}
		}catch(Throwable e){
			e.printStackTrace();
		}	
		
	}
	
	void reduceManPower(Country country) {
        if(manPower.get(country)==null){
            manPower.put(country, 0l);
        }
        manPower.put(country, manPower.get(country)-COST_OF_UNIT);
    }
	
    boolean hasManPowerToReenforce(Country country) {
        return manPower.get(country)!=null && manPower.get(country)>=COST_OF_UNIT;
     
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

	public AchivementStatus getAchivementStatus(CharacterMessage avatar) {
		AchivementStatus achivementStatus = achivementCache .get(avatar.getId());
		if(achivementStatus==null || System.currentTimeMillis()-achivementStatus.getSentTime()>2000){
			RankMessage rank = avatar.getRank();
			achivementStatus = new AchivementStatus(rank.getAchivementMessage(), avatar.totalKillCount(characterDAO), rank.getTotalAchivementCount(), System.currentTimeMillis());
			achivementCache.put(avatar.getId(), achivementStatus);
		}
		return achivementStatus;
	}


}
