package com.lostVictories.service;

import com.jme3.lostVictories.network.messages.Country;
import com.lostVictories.api.*;
import com.lostVictories.api.AddObjectiveRequest;
import com.lostVictories.api.BoardVehicleRequest;
import com.lostVictories.api.CheckoutScreenRequest;
import com.lostVictories.api.DeathNotificationRequest;
import com.lostVictories.api.DisembarkPassengersRequest;
import com.lostVictories.api.EquipmentCollectionRequest;
import com.lostVictories.api.PassengerDeathNotificationRequest;
import io.grpc.stub.StreamObserver;
import lostVictories.VehicleFactory;
import lostVictories.WeaponsFactory;
import lostVictories.WorldRunner;
import lostVictories.dao.*;
import lostVictories.messageHanders.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

import java.util.*;

import static com.lostVictories.service.LostVictoriesService.uuid;

/**
 * Created by dharshanar on 26/05/17.
 */
public class LostVictoriesServiceImpl extends LostVictoriesServerGrpc.LostVictoriesServerImplBase {

    private static Logger log = LoggerFactory.getLogger(LostVictoriesServiceImpl.class);
    LostVictoriesService lostVictoriesService;

    Map<UUID, SafeStreamObserver<LostVictoryMessage>> characterObserverMap = new HashMap<>();
    Map<UUID, SafeStreamObserver<LostVictoryStatusMessage>> gameStatusObserverMap = new HashMap<>();

    public LostVictoriesServiceImpl(JedisPool jedisPool, String instance, TreeDAO treeDAO, EquipmentDAO equipmentDAO, GameRequestDAO gameRequestDAO, PlayerUsageDAO playerUsageDAO, MessageRepository messageRepository, WorldRunner worldRunner) {
        lostVictoriesService = new LostVictoriesService(jedisPool, instance, treeDAO, equipmentDAO, gameRequestDAO, playerUsageDAO, messageRepository, worldRunner);
    }

    @Override
    public void checkoutSceen(CheckoutScreenRequest request, StreamObserver<LostVictoryCheckout> responseObserver) {
        lostVictoriesService.checkoutSceen(request, responseObserver);
    }

    @Override
    public StreamObserver<UpdateCharactersRequest> updateLocalCharacters(StreamObserver<LostVictoryMessage> responseObserver) {
        SafeStreamObserver safeStreamObserver = new SafeStreamObserver<>(responseObserver);
        return new StreamObserver<UpdateCharactersRequest>() {
            @Override
            public void onNext(UpdateCharactersRequest updateCharactersRequest) {

                UUID uuid = uuid(updateCharactersRequest.getClientID());
                if(!characterObserverMap.containsKey(uuid)){
                    log.debug("registering new character client:"+uuid);
                    safeStreamObserver.setClientID(uuid);
                    characterObserverMap.put(uuid, safeStreamObserver);
                }
                lostVictoriesService.updateLocalCharacters(updateCharactersRequest, safeStreamObserver, characterObserverMap);
            }

            @Override
            public void onError(Throwable throwable) {
                if(characterObserverMap.remove(safeStreamObserver.getClientID())!=null){
                    log.debug("de-registering client:"+safeStreamObserver.getClientID());
                }
            }

            @Override
            public void onCompleted() {
                if(characterObserverMap.remove(safeStreamObserver.getClientID())!=null){
                    log.debug("de-registering client:"+safeStreamObserver.getClientID());
                }
            }
        };
    }

    @Override
    public StreamObserver<RegisterClientRequest> registerClient(StreamObserver<LostVictoryStatusMessage> responseObserver) {
        SafeStreamObserver safeStreamObserver = new SafeStreamObserver<>(responseObserver);
        return new StreamObserver<RegisterClientRequest>() {
            @Override
            public void onNext(RegisterClientRequest updateCharactersRequest) {

                UUID uuid = uuid(updateCharactersRequest.getClientID());
                if(!gameStatusObserverMap.containsKey(uuid)){
                    safeStreamObserver.setClientID(uuid);
                    gameStatusObserverMap.put(uuid, safeStreamObserver);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                gameStatusObserverMap.remove(safeStreamObserver);
            }

            @Override
            public void onCompleted() {
                gameStatusObserverMap.remove(safeStreamObserver);
            }
        };
    }

    @Override
    public void deathNotification(DeathNotificationRequest request, StreamObserver<LostVictoryMessage> responseObserver) {
        lostVictoriesService.deathNotification(request, responseObserver);
    }

    @Override
    public void gunnerDeathNotification(PassengerDeathNotificationRequest request, StreamObserver<LostVictoryMessage> responseObserver) {
        lostVictoriesService.gunnerDeathNotification(request, responseObserver);
    }

    @Override
    public void addObjective(AddObjectiveRequest request, StreamObserver<LostVictoryMessage> responseObserver) {
        lostVictoriesService.addObjective(request, responseObserver);
    }

    @Override
    public void requestEquipmentCollection(EquipmentCollectionRequest request, StreamObserver<LostVictoryMessage> responseObserver) {
        lostVictoriesService.requestEquipmentCollection(request, responseObserver);
    }

    @Override
    public void boardVehicle(BoardVehicleRequest request, StreamObserver<LostVictoryMessage> responseObserver) {
        lostVictoriesService.boardVehicle(request, responseObserver);
    }

    @Override
    public void disembarkPassengers(DisembarkPassengersRequest request, StreamObserver<LostVictoryMessage> responseObserver) {
        lostVictoriesService.disembarkPassengers(request, responseObserver);
    }

    @Override
    public void joinGame(JoinRequest request, StreamObserver<JoinResponse> responseObserver) {
        lostVictoriesService.joinGame(request, responseObserver);
    }

    public Map<Country, Integer> runWorld(Map<Country, Integer> victoryPoints, Map<Country, Integer> manPower, Map<Country, WeaponsFactory> weaponsFactory, Map<Country, VehicleFactory> vehicleFactory, Map<Country, Integer> nextRespawnTime, String gameName) {
        return lostVictoriesService.runWorld(victoryPoints, manPower, weaponsFactory, vehicleFactory, nextRespawnTime, gameName, gameStatusObserverMap);
    }
}
