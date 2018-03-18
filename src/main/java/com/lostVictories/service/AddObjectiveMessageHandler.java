package com.lostVictories.service;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.wrapper.GenericLostVictoryResponse;
import com.lostVictories.api.AddObjectiveRequest;
import com.lostVictories.api.LostVictoryMessage;
import io.grpc.stub.StreamObserver;
import lostVictories.dao.CharacterDAO;

import java.util.HashSet;
import java.util.Set;

import static com.lostVictories.service.LostVictoriesService.uuid;

/**
 * Created by dharshanar on 27/05/17.
 */
public class AddObjectiveMessageHandler {
    private CharacterDAO characterDAO;

    public AddObjectiveMessageHandler(CharacterDAO characterDAO) {
        this.characterDAO = characterDAO;
    }

    public void handle(AddObjectiveRequest request, StreamObserver<LostVictoryMessage> responseObserver) {
        CharacterMessage character = characterDAO.getCharacter(uuid(request.getCharacterId()));
        character.addObjective(uuid(request.getIdentity()), request.getObjective());
        characterDAO.putCharacter(character.getId(), character);
        responseObserver.onNext(LostVictoryMessage.newBuilder().build());
        responseObserver.onCompleted();
    }
}
