package com.lostVictories.service;

import com.jme3.lostVictories.network.messages.*;
import com.jme3.lostVictories.network.messages.wrapper.GenericLostVictoryResponse;
import com.lostVictories.api.DeathNotificationRequest;
import com.lostVictories.api.LostVictoryMessage;
import io.grpc.stub.StreamObserver;
import lostVictories.dao.CharacterDAO;
import lostVictories.dao.EquipmentDAO;
import lostVictories.messageHanders.CharacterCatch;
import org.apache.log4j.Logger;

import java.util.*;

import static com.lostVictories.service.LostVictoriesService.uuid;

/**
 * Created by dharshanar on 27/05/17.
 */
public class DeathNotificationMessageHandler {

    private static Logger log = Logger.getLogger(DeathNotificationMessageHandler.class);
    private final CharacterDAO characterDAO;
    private final EquipmentDAO equipmentDAO;

    public DeathNotificationMessageHandler(CharacterDAO characterDAO, EquipmentDAO equipmentDAO) {
        this.characterDAO = characterDAO;
        this.equipmentDAO = equipmentDAO;
    }

    public void handle(DeathNotificationRequest request, StreamObserver<LostVictoryMessage> responseObserver) {
        Set<com.jme3.lostVictories.network.messages.wrapper.LostVictoryMessage> ret = new HashSet<>();
        Map<UUID, CharacterMessage> toSave = new HashMap<UUID, CharacterMessage>();
        CharacterCatch catche = new CharacterCatch(characterDAO);
        CharacterMessage victim = catche.getCharacter(uuid(request.getVictim()));
        if(victim==null || victim.isDead()){
            return;
        }
        log.info("received death notification:"+victim.getId()+" loc:"+victim.getLocation());

        CharacterMessage killer = catche.getCharacter(uuid(request.getKiller()));
        victim.kill();
        killer.incrementKills(victim.getId());
        toSave.put(killer.getId(), killer);
        toSave.put(victim.getId(), victim);

        victim.replaceMe(catche, toSave);

        characterDAO.saveCommandStructure(toSave);


        if(victim.getWeapon().isReusable() && (victim.getCharacterType()== CharacterType.SOLDIER || victim.getCharacterType()==CharacterType.AVATAR)){
            equipmentDAO.addUnclaiimedEquipment(new UnClaimedEquipmentMessage(UUID.randomUUID(), victim.getWeapon(), victim.getLocation(), new com.jme3.lostVictories.network.messages.Vector(0, 0, 0)));
        }

        responseObserver.onCompleted();
    }
}
