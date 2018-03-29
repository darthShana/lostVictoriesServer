package com.lostVictories.service;

import com.jme3.lostVictories.network.messages.*;
import com.jme3.math.Vector3f;
import com.lostVictories.api.EquipmentCollectionRequest;
import com.lostVictories.api.LostVictoryMessage;
import io.grpc.stub.StreamObserver;
import lostVictories.dao.CharacterDAO;
import lostVictories.dao.EquipmentDAO;
import lostVictories.messageHanders.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.lostVictories.service.LostVictoriesService.uuid;

/**
 * Created by dharshanar on 27/05/17.
 */
public class CollectEquipmentMessageHandler {

    private static Logger log = LoggerFactory.getLogger(com.jme3.lostVictories.network.messages.wrapper.EquipmentCollectionRequest.class);

    private final CharacterDAO characterDAO;
    private final EquipmentDAO equipmentDAO;
    private final MessageRepository messageRepository;

    public CollectEquipmentMessageHandler(CharacterDAO characterDAO, EquipmentDAO equipmentDAO, MessageRepository messageRepository) {
        this.characterDAO = characterDAO;
        this.equipmentDAO = equipmentDAO;
        this.messageRepository = messageRepository;
    }

    public void handle(EquipmentCollectionRequest request, StreamObserver<LostVictoryMessage> responseObserver) {
        Set<com.jme3.lostVictories.network.messages.wrapper.LostVictoryMessage> ret = new HashSet<>();
        UUID equipmentId = uuid(request.getEquipmentID());
        UUID characterID = uuid(request.getCharacterID());

        UnClaimedEquipmentMessage equipment = equipmentDAO.get(equipmentId);
        CharacterMessage character = characterDAO.getCharacter(characterID);
        log.info("received equipment pickup:"+equipmentId+" for character "+characterID);

        if(equipment==null || character==null){
            return;
        }

        Vector3f l1 = new Vector3f(equipment.getLocation().x, 0, equipment.getLocation().z);
        Vector3f l2 = new Vector3f(character.getLocation().x, 0, character.getLocation().z);
        if(l1.distance(l2)>2){
            if(CharacterType.AVATAR == character.getCharacterType()){
                messageRepository.addMessage(uuid(request.getClientID()), "Item is too far to collect.");
            }
            return;
        }

        Weapon drop = character.switchWeapon(equipment);
        if(drop!=null){
            equipmentDAO.addUnclaimedEquipment(new UnClaimedEquipmentMessage(UUID.randomUUID(), drop, character.getLocation(), new Vector(0, 0, 0)));
        }
        equipmentDAO.delete(equipment);
        characterDAO.putCharacter(character.getId(), character);
        responseObserver.onNext(LostVictoryMessage.newBuilder().build());
        responseObserver.onCompleted();
    }
}
