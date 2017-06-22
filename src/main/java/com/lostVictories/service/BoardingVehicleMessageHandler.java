package com.lostVictories.service;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.wrapper.GenericLostVictoryResponse;
import com.lostVictories.api.BoardVehicleRequest;
import com.lostVictories.api.LostVictoryMessage;
import io.grpc.stub.StreamObserver;
import lostVictories.dao.CharacterDAO;
import lostVictories.messageHanders.MessageRepository;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

import static com.lostVictories.service.LostVictoriesService.uuid;

/**
 * Created by dharshanar on 27/05/17.
 */
public class BoardingVehicleMessageHandler {

    Logger log = Logger.getLogger(BoardingVehicleMessageHandler.class);
    private final CharacterDAO characterDAO;
    private final MessageRepository messageRepository;

    public BoardingVehicleMessageHandler(CharacterDAO characterDAO, MessageRepository messageRepository) {
        this.characterDAO = characterDAO;
        this.messageRepository = messageRepository;
    }

    public void handle(BoardVehicleRequest request, StreamObserver<LostVictoryMessage> responseObserver) throws IOException {
        UUID clientId = uuid(request.getClientID());
        UUID vehicleId = uuid(request.getVehicleID());
        UUID characterID = uuid(request.getCharacterID());

        CharacterMessage vehicle = characterDAO.getCharacter(vehicleId);
        CharacterMessage passenger = characterDAO.getCharacter(characterID);
        log.debug("received boarding request for passenger:"+passenger.getId()+" vehicle:"+vehicleId);
        if(vehicle.isDead()){
            if(passenger.getId().equals(clientId)){
                messageRepository.addMessage(clientId, "Vehicle has been destroyed.");
            }
            log.debug("vehicle:"+vehicle.getId()+" has been destroyed");
            return;
        }
        if(vehicle.getLocation().distance(passenger.getLocation())>7.5f){
            if(passenger.getId().equals(clientId)){
                messageRepository.addMessage(clientId, "Vehicle is too far to get in.");
            }
            log.debug("passenger:"+passenger.getId()+" is too far to get in");
            return;
        }
        if(vehicle.getPassengers().contains(characterID)){
            if(passenger.getId().equals(clientId)){
                messageRepository.addMessage(clientId, "Avatar already onboard vehicle.");
            }
            log.debug("passenger:"+passenger.getId()+" is is already onboard vehicle");
            return;
        }

        log.debug("passenger:"+passenger.getId()+" checkes passed about to board vehicle:"+vehicleId);
        Map<UUID, CharacterMessage> toSave = new HashMap<UUID, CharacterMessage>();
        passenger.boardVehicle(vehicle, characterDAO, toSave);
        log.debug("passenger:"+passenger.getId()+" boarding complete");

        if(clientId.equals(passenger.getId()) && passenger.getCharacterType()==CharacterType.AVATAR){
            log.debug("checkout boarded vehicle:"+vehicleId+" by client:"+clientId);
            CharacterMessage savedVehicle = toSave.get(vehicleId);
            savedVehicle.setCheckoutClient(clientId);
            savedVehicle.setCheckoutTime(System.currentTimeMillis());
        }

        log.debug("vehicle new passegers:"+toSave.get(vehicleId).getPassengers());
        characterDAO.save(toSave.values());
        responseObserver.onCompleted();
    }
}
