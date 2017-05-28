package com.lostVictories.service;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.lostVictories.api.LostVictoryMessage;
import com.lostVictories.api.PassengerDeathNotificationRequest;
import io.grpc.stub.StreamObserver;
import lostVictories.dao.CharacterDAO;
import lostVictories.messageHanders.CharacterCatch;
import org.apache.log4j.Logger;

import java.util.*;

import static com.lostVictories.service.LostVictoriesService.uuid;

/**
 * Created by dharshanar on 27/05/17.
 */
public class PassengerDeathNotificationMessageHandler {
    private static Logger log = Logger.getLogger(PassengerDeathNotificationMessageHandler.class);
    private CharacterDAO characterDAO;

    public PassengerDeathNotificationMessageHandler(CharacterDAO characterDAO) {
        this.characterDAO = characterDAO;
    }

    public void handle(PassengerDeathNotificationRequest request, StreamObserver<LostVictoryMessage> responseObserver) {
        Set<com.jme3.lostVictories.network.messages.wrapper.LostVictoryMessage> ret = new HashSet<>();
        CharacterCatch catche = new CharacterCatch(characterDAO);
        Map<UUID, CharacterMessage> toSave = new HashMap<UUID, CharacterMessage>();

        CharacterMessage vehicle = catche.getCharacter(uuid(request.getVictim()));
        if(vehicle==null || vehicle.isDead()){
            return;
        }
        log.info("received gunner death notification:"+vehicle.getId());

        CharacterMessage killer = catche.getCharacter(uuid(request.getKiller()));
        CharacterMessage victim = vehicle.killPassenger(catche);
        log.info("killed passenger:"+victim.getId());

        toSave.put(vehicle.getId(), vehicle);
        if(victim!=null){
            victim.kill();
            killer.incrementKills(victim.getId());
            victim.replaceMe(catche, toSave);
            toSave.put(victim.getId(), victim);
            toSave.put(killer.getId(), killer);
        }

        characterDAO.saveCommandStructure(toSave);
        responseObserver.onCompleted();
    }
}
