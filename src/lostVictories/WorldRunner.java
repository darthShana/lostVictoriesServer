package lostVictories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.Country;
import com.jme3.lostVictories.network.messages.GameStatistics;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.RankMessage;

public class WorldRunner implements Runnable{

	private static Logger log = Logger.getLogger(WorldRunner.class); 
	
	private CharacterDAO characterDAO;
	private HouseDAO houseDAO;
	private static WorldRunner instance;
	Map<Country, Integer> victoryPoints = new EnumMap<Country, Integer>(Country.class);
	Map<Country, Long> manPower = new EnumMap<Country, Long>(Country.class);

	private Map<Country, Long> structureOwnership = new EnumMap<Country, Long>(Country.class);
	private Map<Country, Long> nextRespawnTime = new EnumMap<Country, Long>(Country.class);

	public static WorldRunner instance(CharacterDAO characterDAO, HouseDAO houseDAO) {
		if(instance==null){
			instance = new WorldRunner(characterDAO, houseDAO);
		}
		return instance;
	}

	private WorldRunner(CharacterDAO characterDAO, HouseDAO houseDAO) {
		this.characterDAO = characterDAO;
		this.houseDAO = houseDAO;
		victoryPoints.put(Country.AMERICAN, 1000);
        victoryPoints.put(Country.GERMAN, 1000);
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
				if(numberOfProperties<capturedStructureCount/2f){
					victoryPoints.put(c, (int) (victoryPoints.get(c)-((capturedStructureCount/2f)-numberOfProperties)));
				}
			}
			
			for(Country c:structureOwnership.keySet()){
	//          if(structureOwnership.get(c) == structureCount){
	//              winner = c;
	//          }
	          if(!manPower.containsKey(c)){
	              manPower.put(c, 0l);
	          }
	          manPower.put(c, manPower.get(c)+structureOwnership.get(c));
	          if(manPower.get(c)>(structureOwnership.get(c)*100)){
	              manPower.put(c, structureOwnership.get(c)*100);
	          }
	          
	          if(structureOwnership.get(c)>0){
	              nextRespawnTime.put(c, (100-manPower.get(c))/structureOwnership.get(c)*2);
	          }
			}
			
			Set<CharacterMessage> allCharacters = characterDAO.getAllCharacters();
			AvatarStore avatarStore = new AvatarStore(allCharacters);
			
			List<CharacterMessage> list = new ArrayList<CharacterMessage>(allCharacters);
			list.sort((c1, c2)->c1.getRank()!=RankMessage.CADET_CORPORAL&&c2.getRank()==RankMessage.CADET_CORPORAL?1:-1);
			
			for(CharacterMessage c: list){
                if(!c.isDead()){
                    if(!c.isFullStrength() && hasManPowerToReenforce(c.getCountry())){
                    	log.debug("found understrenth unit to reenfoce:"+c.getId()+" rank"+c.getRank());
                        Optional<UUID> deadAvatars = avatarStore.getDeadAvatars(c.getCountry());
						if(deadAvatars.isPresent()){
							log.debug("in here test reincarnate2");
                            CharacterMessage reincarnateAvatar = avatarStore.reincarnateAvatar(deadAvatars.get(), c);
							characterDAO.saveAndRefresh(reincarnateAvatar);
                            characterDAO.delete(c);
                            characterDAO.save(reincarnateAvatar.reenforceCharacter(c.getLocation().add(0, 5, 15)));
                        }else{
                        	log.debug("in here test reenforce:"+c.getId());
                            Collection<CharacterMessage> reenforceCharacter = c.reenforceCharacter(c.getLocation().add(0, 5, 15));
                            characterDAO.updateCharactersUnderCommand(c);
							characterDAO.save(reenforceCharacter);
                        }
                        reduceManPower(c.getCountry());
                        
                    }
                }else if(c.getTimeOfDeath()<(System.currentTimeMillis()-60000)){
                	System.out.println("removing dead character:"+c.getId());
                	characterDAO.delete(c);
                }
            }
			
			for(CharacterMessage avatar: avatarStore.getLivingAvatars()){
				if(avatar.hasAchivedRankObjectives()){
					log.info("promoting avatar:"+avatar.getId());
					UUID coId = avatar.getCommandingOfficer();
					if(coId!=null){
						CharacterMessage co = characterDAO.getCharacter(coId);
						Set<CharacterMessage> promotions = avatar.promoteCharacter(co, characterDAO);
						characterDAO.save(promotions);
					}
				}
			}
			
			log.trace("german vp:"+victoryPoints.get(Country.GERMAN));
			log.trace("american vp:"+victoryPoints.get(Country.AMERICAN));
		}catch(Throwable e){
			e.printStackTrace();
		}	
		
	}
	
	void reduceManPower(Country country) {
        if(manPower.get(country)==null){
            manPower.put(country, 0l);
        }
        manPower.put(country, manPower.get(country)-100);
    }
	
    boolean hasManPowerToReenforce(Country country) {
        return manPower.get(country)!=null && manPower.get(country)>=100;
     
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
		
		return statistics;
	}


}
