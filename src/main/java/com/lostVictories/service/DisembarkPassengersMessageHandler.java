package com.lostVictories.service;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.wrapper.GenericLostVictoryResponse;
import com.lostVictories.api.DisembarkPassengersRequest;
import com.lostVictories.api.LostVictoryMessage;
import io.grpc.stub.StreamObserver;
import lostVictories.dao.CharacterDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.lostVictories.service.LostVictoriesService.uuid;

/**
 * Created by dharshanar on 27/05/17.
 */
public class DisembarkPassengersMessageHandler {
    private Logger log = LoggerFactory.getLogger(DisembarkPassengersMessageHandler.class);
    private CharacterDAO characterDAO;

    public DisembarkPassengersMessageHandler(CharacterDAO characterDAO) {
        this.characterDAO = characterDAO;
    }

    public void handle(DisembarkPassengersRequest request, StreamObserver<LostVictoryMessage> responseObserver) throws IOException {

        log.debug("received disembark request from:"+request.getVehicleID());
        CharacterMessage vehicle = characterDAO.getCharacter(uuid(request.getVehicleID()));
        characterDAO.save(vehicle.disembarkPassengers(characterDAO, true));
        responseObserver.onCompleted();
    }
}
