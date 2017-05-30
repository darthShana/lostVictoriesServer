package com.lostVictories.service;

import com.jme3.lostVictories.network.messages.*;
import com.jme3.lostVictories.network.messages.AchievementStatus;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.GameStatistics;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.UnClaimedEquipmentMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.wrapper.*;
import com.jme3.lostVictories.network.messages.wrapper.CharacterStatusResponse;
import com.jme3.lostVictories.network.messages.wrapper.EquipmentStatusResponse;
import com.jme3.lostVictories.network.messages.wrapper.GameStatsResponse;
import com.jme3.lostVictories.network.messages.wrapper.HouseStatusResponse;
import com.jme3.lostVictories.network.messages.wrapper.RelatedCharacterStatusResponse;
import com.lostVictories.api.*;
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
    private static Logger log = Logger.getLogger(lostVictories.messageHanders.UpdateCharactersMessageHandler.class);
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

    public void handle(UpdateCharactersRequest msg, StreamObserver<LostVictoryMessage> responseObserver) throws IOException {
        Map<UUID, com.lostVictories.api.CharacterMessage> sentFromClient = new HashMap<>();
        sentFromClient.put(uuid(msg.getCharacter().getId()), msg.getCharacter());

        CharacterMessage ss = characterDAO.getCharacter(uuid(msg.getCharacter().getId()), true);
        if(ss==null){
            log.info("unknown character id:"+uuid(msg.getCharacter().getId()));
            return;
        }

        Map<UUID, CharacterMessage> serverVersion = new HashMap<>();
        serverVersion.put(ss.getId(), ss);

        UUID clientID = uuid(msg.getClientID());
        Map<UUID, CharacterMessage> toSave = serverVersion.values().stream()
                .filter(c->c.isAvailableForUpdate(clientID, sentFromClient.get(c.getId()), CHECKOUT_TIMEOUT))
                .collect(Collectors.toMap(c->c.getId(), Function.identity()));

        toSave.values().stream().forEach(c->c.updateState(sentFromClient.get(c.getId()), clientID, System.currentTimeMillis()));


        CharacterMessage storedAvatar = characterDAO.getCharacter(uuid(msg.getAvatar()));
        Vector v = storedAvatar.getLocation();
        Map<UUID, CharacterMessage> inRange = characterDAO.getAllCharacters(v.x, v.y, v.z, lostVictories.messageHanders.CheckoutScreenMessageHandler.CLIENT_RANGE).stream().collect(Collectors.toMap(c->c.getId(), Function.identity()));

        Map<UUID, CharacterMessage> toReturn = toSave.values().stream()
                .map(s->characterDAO.updateCharacterState(s))
                .filter(u->u!=null)
                .filter(b->inRange.containsKey(b.getId()))
                .collect(Collectors.toMap(c->c.getId(), Function.identity()));

        toReturn.values().stream().forEach(c->{
            responseObserver.onNext(mp.toMessage(c));
        });

        serverVersion.values().stream()
                .filter(b->inRange.containsKey(b.getId()) && !toReturn.containsKey(b.getId())).forEach(c->{
            toReturn.put(c.getId(), c);
            responseObserver.onNext(mp.toMessage(c));
        });

        if(msg.getClientStartTime()>5000) {

            if(sentFromClient.containsKey(uuid(msg.getAvatar()))) {
                inRange.values().stream().filter(c -> !toReturn.containsKey(c.getId())).filter(cc -> !cc.isCheckedOutBy(clientID, CHECKOUT_TIMEOUT)).forEach(c -> {
                    toReturn.put(c.getId(), c);
                    responseObserver.onNext(mp.toMessage(c));
                });
            }
            toReturn.values().stream()
                .filter(u->!u.isDead()).map(c->c.getUnitsUnderCommand()).filter(u->!toReturn.containsKey(u))
                .map(u->characterDAO.getAllCharacters(u).values()).flatMap(l->l.stream())
                .filter(u->u!=null && !inRange.containsKey(u.getId())).forEach(r->responseObserver.onNext(mp.toMessage(r, true)));
            toReturn.values().stream()
                .filter(c->!c.isDead()).map(c->c.getCommandingOfficer()).filter(u->u!=null && !toReturn.containsKey(u))
                .map(u->characterDAO.getCharacter(u)).filter(u->u!=null && !inRange.containsKey(u.getId())).forEach(r->responseObserver.onNext(mp.toMessage(r, true)));
        }

        if(sentFromClient.containsKey(uuid(msg.getAvatar()))){
            GameStatistics statistics = worldRunner.getStatistics(storedAvatar.getCountry());
            AchievementStatus achievementStatus = worldRunner.getAchivementStatus(storedAvatar, characterDAO);
            equipmentDAO.getUnClaimedEquipment(v.x, v.y, v.z, lostVictories.messageHanders.CheckoutScreenMessageHandler.CLIENT_RANGE).forEach(e->responseObserver.onNext(mp.toMessage(e)));
            houseDAO.getAllHouses().forEach(h->responseObserver.onNext(mp.toMessage(h)));

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
