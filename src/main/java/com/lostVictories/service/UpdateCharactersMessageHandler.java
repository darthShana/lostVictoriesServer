package com.lostVictories.service;

import com.jme3.lostVictories.network.messages.*;
import com.jme3.lostVictories.network.messages.AchievementStatus;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.Country;
import com.jme3.lostVictories.network.messages.GameStatistics;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.UnClaimedEquipmentMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.actions.*;
import com.jme3.lostVictories.network.messages.wrapper.*;
import com.jme3.lostVictories.network.messages.wrapper.CharacterStatusResponse;
import com.jme3.lostVictories.network.messages.wrapper.EquipmentStatusResponse;
import com.jme3.lostVictories.network.messages.wrapper.GameStatsResponse;
import com.jme3.lostVictories.network.messages.wrapper.HouseStatusResponse;
import com.jme3.lostVictories.network.messages.wrapper.RelatedCharacterStatusResponse;
import com.lostVictories.api.*;
import com.lostVictories.api.Action;
import com.lostVictories.api.LostVictoryMessage;
import com.lostVictories.api.UpdateCharactersRequest;
import io.grpc.stub.StreamObserver;
import lostVictories.WorldRunner;
import lostVictories.dao.CharacterDAO;
import lostVictories.dao.EquipmentDAO;
import lostVictories.dao.HouseDAO;
import lostVictories.messageHanders.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.lostVictories.service.LostVictoriesService.uuid;

/**
 * Created by dharshanar on 27/05/17.
 */
public class UpdateCharactersMessageHandler {

    public static final long CHECKOUT_TIMEOUT = 2*1000;

    private CharacterDAO characterDAO;
    private static Logger log = Logger.getLogger(UpdateCharactersMessageHandler.class);
    private HouseDAO houseDAO;
    private EquipmentDAO equipmentDAO;
    private WorldRunner worldRunner;
    private MessageRepository messageRepository;
    MessageMapper mp = new MessageMapper();

    public UpdateCharactersMessageHandler(CharacterDAO characterDAO, HouseDAO houseDAO, EquipmentDAO equipmentDAO, WorldRunner worldRunner, MessageRepository messageRepository) {
        this.characterDAO = characterDAO;
        this.houseDAO = houseDAO;
        this.equipmentDAO = equipmentDAO;
        this.worldRunner = worldRunner;
        this.messageRepository = messageRepository;
    }

    public void handle(UpdateCharactersRequest msg, SafeStreamObserver responseObserver, Set<SafeStreamObserver> clientObserverMap) throws IOException {

        UUID characterId = uuid(msg.getCharacter().getId());
        UUID clientID = uuid(msg.getClientID());

        com.lostVictories.api.CharacterMessage sentFromClient = msg.getCharacter();

        CharacterMessage serverVersion = characterDAO.getCharacter(characterId, true);
        if(serverVersion==null){
            log.info("unknown character id:"+ characterId);
            return;
        }

        if(serverVersion.isAvailableForUpdate(clientID, sentFromClient, CHECKOUT_TIMEOUT)){
            serverVersion.updateState(sentFromClient, clientID, System.currentTimeMillis());
            CharacterMessage characterMessage = characterDAO.updateCharacterState(serverVersion);
            if(characterMessage!=null) {
                serverVersion = characterMessage;
            }
        }

        CharacterMessage storedAvatar = characterDAO.getCharacter(uuid(msg.getAvatar()));
        Vector v = storedAvatar.getLocation();
        Map<UUID, CharacterMessage> inRange = characterDAO.getAllCharacters(v.x, v.y, v.z, CheckoutScreenMessageHandler.CLIENT_RANGE).stream()
                .collect(Collectors.toMap(c->c.getId(), Function.identity()));

        if(inRange.containsKey(serverVersion.getId())) {
            responseObserver.onNext(mp.toMessage(serverVersion));
        }

        final CharacterMessage toSend = serverVersion;

        clientObserverMap.stream()
                .filter(entry->!entry.getClientID().equals(clientID))
                .filter(ee->characterDAO.isInRangeOf(toSend.getLocation(), ee.getClientID(), CheckoutScreenMessageHandler.CLIENT_RANGE))
                .forEach(e->{
                    e.onNext(mp.toMessage(toSend));
                });

        if(msg.getClientStartTime()>5000 && characterId.equals(uuid(msg.getAvatar()))) {
            inRange.values().stream()
                    .filter(cc -> cc.isAvailableForCheckout(5000))
                    .filter(c->!c.isDead())
                    .forEach(c -> responseObserver.onNext(mp.toMessage(c)));
        }

        if(!serverVersion.isDead()) {
            characterDAO.getAllCharacters(serverVersion.getUnitsUnderCommand()).values().stream()
                    .filter(u -> u != null && !inRange.containsKey(u.getId()))
                    .forEach(r -> responseObserver.onNext(mp.toMessage(r, true)));
            if(serverVersion.getCommandingOfficer()!=null){
                CharacterMessage commandingOfficer = characterDAO.getCharacter(serverVersion.getCommandingOfficer());
                if(commandingOfficer!=null && !inRange.containsKey(commandingOfficer.getId())){
                    responseObserver.onNext(mp.toMessage(commandingOfficer, true));
                }
            }

        }


        if(characterId.equals(uuid(msg.getAvatar()))){
            GameStatistics statistics = worldRunner.getStatistics(storedAvatar.getCountry());
            AchievementStatus achievementStatus = worldRunner.getAchivementStatus(storedAvatar, characterDAO);
            equipmentDAO.getUnClaimedEquipment(v.x, v.y, v.z, CheckoutScreenMessageHandler.CLIENT_RANGE).forEach(e->responseObserver.onNext(mp.toMessage(e)));

            responseObserver.onNext(mp.toMessage(new GameStatsResponse(messageRepository.popMessages(clientID), statistics, achievementStatus)));

            if(storedAvatar.getBoardedVehicle()!=null){
                CharacterMessage vehicle = characterDAO.getCharacter(storedAvatar.getBoardedVehicle());
                if(vehicle!=null && vehicle.getCheckoutClient()!=null && !vehicle.getCheckoutClient().equals(clientID)){
                    log.debug("force checkout of vehicle:"+vehicle.getId());
                    vehicle.setCheckoutClient(clientID);
                    vehicle.setCheckoutTime(System.currentTimeMillis());
                    List<CharacterMessage> values = new ArrayList<>();
                    values.add(vehicle);
                    characterDAO.save(values);
                    responseObserver.onNext(mp.toMessage(vehicle));
                }

            }
        }
    }




}
