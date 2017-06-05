package com.lostVictories.service;

import com.lostVictories.api.*;
import io.grpc.stub.StreamObserver;
import lostVictories.WorldRunner;
import lostVictories.dao.*;
import lostVictories.messageHanders.MessageRepository;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.lostVictories.service.LostVictoriesService.uuid;

/**
 * Created by dharshanar on 26/05/17.
 */
public class LostVictoriesServiceImpl extends LostVictoriesServerGrpc.LostVictoriesServerImplBase {

    LostVictoriesService lostVictoriesSerice;
    Map<UUID, SafeStreamObserver> clientObserverMap = new HashMap<>();

    public LostVictoriesServiceImpl(JedisPool jedisPool, String instance, HouseDAO houseDAO, TreeDAO treeDAO, EquipmentDAO equipmentDAO, GameStatusDAO gameStatusDAO, GameRequestDAO gameRequestDAO, PlayerUsageDAO playerUsageDAO, MessageRepository messageRepository, WorldRunner worldRunner) {
        lostVictoriesSerice = new LostVictoriesService(jedisPool, instance, houseDAO, treeDAO, equipmentDAO, gameStatusDAO, gameRequestDAO, playerUsageDAO, messageRepository, worldRunner);
    }

    @Override
    public void checkoutSceen(CheckoutScreenRequest request, StreamObserver<LostVictoryMessage> responseObserver) {
        lostVictoriesSerice.checkoutSceen(request, responseObserver);
    }

    @Override
    public StreamObserver<UpdateCharactersRequest> updateLocalCharacters(StreamObserver<LostVictoryMessage> responseObserver) {
        SafeStreamObserver safeStreamObserver = new SafeStreamObserver(responseObserver);
        return new StreamObserver<UpdateCharactersRequest>() {
            @Override
            public void onNext(UpdateCharactersRequest updateCharactersRequest) {

                if(!clientObserverMap.containsKey(uuid(updateCharactersRequest.getClientID()))){
                    clientObserverMap.put(uuid(updateCharactersRequest.getClientID()), safeStreamObserver);
                }
                lostVictoriesSerice.updateLocalCharacters(updateCharactersRequest, safeStreamObserver, clientObserverMap);
            }

            @Override
            public void onError(Throwable throwable) { throwable.printStackTrace(); }

            @Override
            public void onCompleted() {}
        };
    }

    @Override
    public void deathNotification(DeathNotificationRequest request, StreamObserver<LostVictoryMessage> responseObserver) {
        lostVictoriesSerice.deathNotification(request, responseObserver);
    }

    @Override
    public void gunnerDeathNotification(PassengerDeathNotificationRequest request, StreamObserver<LostVictoryMessage> responseObserver) {
        lostVictoriesSerice.gunnerDeathNotification(request, responseObserver);
    }

    @Override
    public void addObjective(AddObjectiveRequest request, StreamObserver<LostVictoryMessage> responseObserver) {
        lostVictoriesSerice.addObjective(request, responseObserver);
    }

    @Override
    public void requestEquipmentCollection(EquipmentCollectionRequest request, StreamObserver<LostVictoryMessage> responseObserver) {
        lostVictoriesSerice.requestEquipmentCollection(request, responseObserver);
    }

    @Override
    public void boardVehicle(BoardVehicleRequest request, StreamObserver<LostVictoryMessage> responseObserver) {
        lostVictoriesSerice.boardVehicle(request, responseObserver);
    }

    @Override
    public void disembarkPassengers(DisembarkPassengersRequest request, StreamObserver<LostVictoryMessage> responseObserver) {
        lostVictoriesSerice.disembarkPassengers(request, responseObserver);
    }
}
