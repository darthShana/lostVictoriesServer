package lostVictories.service;

import com.jme3.lostVictories.network.messages.*;
import com.jme3.lostVictories.objectives.SecureSectorState;
import lostVictories.AvatarStore;
import lostVictories.VehicleFactory;
import lostVictories.WeaponsFactory;
import lostVictories.dao.*;
import lostVictories.messageHanders.MessageRepository;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by dharshanar on 7/05/17.
 */
public class WorldRunnerInstance {

    private static Logger log = Logger.getLogger(WorldRunnerInstance.class);
    private static final int COST_OF_UNIT = 500;


    public Map<Country, Long> runWorld(CharacterDAO characterDAO, HouseDAO houseDAO, Map<Country, Integer> victoryPoints, Map<Country, Long> manPower, Map<Country, WeaponsFactory> weaponsFactory, Map<Country, VehicleFactory> vehicleFactory, Map<Country, Long> nextRespawnTime, MessageRepository messageRepository, GameStatusDAO gameStatusDAO, PlayerUsageDAO playerUsageDAO, GameRequestDAO gameRequestDAO, String gameName) throws IOException {
            Set<HouseMessage> allHouses = houseDAO.getAllHouses();
            Set<HouseMessage> dchanged = allHouses.stream().filter(h->h.chechOwnership(characterDAO)).collect(Collectors.toSet());
            houseDAO.save(dchanged);

            Map<Country, Long> structureOwnership = allHouses.stream().filter(h->h.isOwned()).collect(Collectors.groupingBy(HouseMessage::getOwner, Collectors.counting()));
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
            list.sort((c1, c2)->c1.getRank()!= RankMessage.CADET_CORPORAL&&c2.getRank()==RankMessage.CADET_CORPORAL?1:-1);

            for(CharacterMessage c: list){
                if(!c.isDead()){
                    if(!c.isFullStrength() && hasManPowerToReenforce(c.getCountry(), manPower)){
                        log.debug("found understrenth unit to reenfoce:"+c.getId()+" rank"+c.getRank());
                        Optional<CharacterMessage> deadAvatars = avatarStore.getDeadAvatars(c.getCountry());
                        if(deadAvatars.isPresent() && c.getCharacterType()!= CharacterType.AVATAR){
                            log.debug("in here test reincarnate avatar:"+c.getId());
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
                        reduceManPower(c.getCountry(), manPower);

                    }
                }else if(c.getTimeOfDeath()<(System.currentTimeMillis()-60000) && c.getCharacterType()!=CharacterType.AVATAR){
                    log.debug("removing dead character:"+c.getId()+" co:"+c.getCommandingOfficer());
                    characterDAO.delete(c);
                }else if(c.getCommandingOfficer()==null && c.getRank()==RankMessage.PRIVATE && !c.isDead()){
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
                    if(avatar.getCheckoutClient()!=null && avatar.getCheckoutClient().equals(avatar.getId())){
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
            return structureOwnership;


    }

    void reduceManPower(Country country, Map<Country, Long> manPower) {
        if(manPower.get(country)==null){
            manPower.put(country, 0l);
        }
        manPower.put(country, manPower.get(country)-COST_OF_UNIT);
    }

    private boolean hasManPowerToReenforce(Country country, Map<Country, Long> manPower) {
        return manPower.get(country)!=null && manPower.get(country)>=COST_OF_UNIT;

    }
}
