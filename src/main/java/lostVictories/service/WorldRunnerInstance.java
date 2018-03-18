package lostVictories.service;

import com.jme3.lostVictories.network.messages.*;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.wrapper.GameStatsResponse;
import com.jme3.lostVictories.objectives.SecureSectorState;
import com.jme3.math.Vector3f;
import com.lostVictories.api.LostVictoryStatusMessage;
import com.lostVictories.service.MessageMapper;
import com.lostVictories.service.SafeStreamObserver;
import lostVictories.AvatarStore;
import lostVictories.NavMeshStore;
import lostVictories.VehicleFactory;
import lostVictories.WeaponsFactory;
import lostVictories.dao.*;
import lostVictories.messageHanders.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by dharshanar on 7/05/17.
 */
public class WorldRunnerInstance {

    private static Logger log = LoggerFactory.getLogger(WorldRunnerInstance.class);
    private static final int COST_OF_UNIT = 500;
    MessageMapper mp = new MessageMapper();


    public Map<Country, Integer> runWorld(CharacterDAO characterDAO, HouseDAO houseDAO, GameRequestDAO gameRequestDAO, PlayerUsageDAO playerUsageDAO, EquipmentDAO equipmentDAO, Map<Country, Integer> victoryPoints, Map<Country, Integer> manPower, Map<Country, WeaponsFactory> weaponsFactory, Map<Country, VehicleFactory> vehicleFactory, Map<Country, Integer> nextRespawnTime, MessageRepository messageRepository, String gameName, Map<UUID, SafeStreamObserver<LostVictoryStatusMessage>> clientObserverMap) throws IOException {
        Set<HouseMessage> allHouses = houseDAO.getAllHouses();
        Set<HouseMessage> dchanged = allHouses.stream().filter(h->h.checkOwnership(characterDAO)).collect(Collectors.toSet());
        houseDAO.save(dchanged);
        clientObserverMap.values().stream().forEach(client->{
            dchanged.forEach(h->client.onNext(mp.toMessageStatus(h)));
        });

        Map<Country, Long> collect = allHouses.stream().filter(h -> h.isOwned()).collect(Collectors.groupingBy(HouseMessage::getOwner, Collectors.counting()));
        Map<Country, Integer> structureOwnership = collect.entrySet().stream().collect(Collectors.toMap(e->e.getKey(), e->e.getValue().intValue()));
        long capturedStructureCount = structureOwnership.values().stream().reduce(0, (a, b)->a+b);

        for(Country c: victoryPoints.keySet()){
            long numberOfProperties = structureOwnership.get(c)!=null?structureOwnership.get(c):0;
            log.trace(c+":"+numberOfProperties);
            if(numberOfProperties<capturedStructureCount/2f){
                victoryPoints.put(c, (int) (victoryPoints.get(c)-((capturedStructureCount/2f)-numberOfProperties)));
            }
        }

        for(Country c:structureOwnership.keySet()){

            if(!manPower.containsKey(c)){
                manPower.put(c, 0);
            }
            manPower.put(c, (int) (manPower.get(c)+structureOwnership.get(c)));
            if(manPower.get(c)>(structureOwnership.get(c)*COST_OF_UNIT)){
                manPower.put(c, (int) (structureOwnership.get(c)*COST_OF_UNIT));
            }

            if(structureOwnership.get(c)>0){
                nextRespawnTime.put(c, (int) ((COST_OF_UNIT-manPower.get(c))/structureOwnership.get(c)*2));
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
                            characterDAO.delete(c);
                            toUpdate.addAll(replaceWithAvatar.reenforceCharacter(c.getLocation(), weaponsFactory.get(c.getCountry()), vehicleFactory.get(c.getCountry()), characterDAO));
                            log.debug("character:"+c.getId()+" replaced by avatar:"+deadAvatars.get().getId()+" loc:"+replaceWithAvatar.getLocation());
                        }
                        characterDAO.save(toUpdate);
                    }else{
                        log.debug("in here test reenforce:"+c.getId());
                        HouseMessage house = SecureSectorState.findClosestHouse(c, houseDAO.getAllHouses(), h -> h.getOwner()==c.getCountry());
                        Vector3f point = NavMeshStore.intstace().warp(house.getLocation());
                        if(point!=null){
                            c.reenforceCharacter(new Vector(point), weaponsFactory.get(c.getCountry()), vehicleFactory.get(c.getCountry()), characterDAO);
                            characterDAO.updateCharactersUnderCommand(c);
                        }
                    }
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
                    }
                }
                if(avatar.getCheckoutClient()!=null && avatar.getCheckoutClient().equals(avatar.getId())){
                    messageRepository.addMessage(avatar.getCheckoutClient(), "Congratulations! You have been promoted to:"+avatar.getRank());
                }
            }
        }

        equipmentDAO.getAllUnclaimedEquipment().stream()
                .filter(e->(System.currentTimeMillis()-e.getCreationTime())>60000)
                .forEach(e->equipmentDAO.delete(e));

        if(victoryPoints.get(Country.GERMAN)<=0){
            playerUsageDAO.endAllGameSessions(System.currentTimeMillis());
            UUID gameRequest = gameRequestDAO.getGameRequest(gameName);
            if(gameRequest!=null) {
                gameRequestDAO.recordAmericanVictory(gameRequest);
            }
        }
        if(victoryPoints.get(Country.AMERICAN)<=0){
            playerUsageDAO.endAllGameSessions(System.currentTimeMillis());
            UUID gameRequest = gameRequestDAO.getGameRequest(gameName);
            if(gameRequest!=null) {
                gameRequestDAO.recordGermanVictory(gameRequest);
            }
        }

        //
//            AchievementStatus achievementStatus = worldRunner.getAchivementStatus(storedAvatar, characterDAO);
//            equipmentDAO.getUnClaimedEquipment(v.x, v.y, v.z, CheckoutScreenMessageHandler.CLIENT_RANGE).forEach(e->responseObserver.onNext(mp.toMessage(e)));

//

        clientObserverMap.values().stream().forEach(client->{
            CharacterMessage character = characterDAO.getCharacter(client.getClientID());
            GameStatistics statistics = getStatistics(character.getCountry(), victoryPoints, structureOwnership, nextRespawnTime);
            client.onNext(mp.toMessage(new GameStatsResponse(messageRepository.popMessages(client.getClientID()), statistics, getAchivementStatus(character, characterDAO))));
        });

        if(victoryPoints.get(Country.GERMAN)<=-50 || victoryPoints.get(Country.AMERICAN)<=-50){
            System.exit(0);
        }
        return structureOwnership;


    }

    void reduceManPower(Country country, Map<Country, Integer> manPower) {
        if(manPower.get(country)==null){
            manPower.put(country, 0);
        }
        manPower.put(country, manPower.get(country)-COST_OF_UNIT);
    }

    private boolean hasManPowerToReenforce(Country country, Map<Country, Integer> manPower) {
        return manPower.get(country)!=null && manPower.get(country)>=COST_OF_UNIT;

    }

    public GameStatistics getStatistics(Country country, Map<Country, Integer> victoryPoints, Map<Country, Integer> structureOwnership, Map<Country, Integer> nextRespawnTime) {
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
        RankMessage rank = avatar.getRank();
        return new AchievementStatus(rank.getAchivementMessage(), avatar.totalKillCount(characterDAO), rank.getTotalAchivementCount(), System.currentTimeMillis());

    }
}
