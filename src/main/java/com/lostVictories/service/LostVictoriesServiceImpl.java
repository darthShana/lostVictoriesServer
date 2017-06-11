package com.lostVictories.service;

import com.jme3.lostVictories.network.messages.*;
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
import org.apache.log4j.Logger;
import redis.clients.jedis.JedisPool;

import java.util.*;

import static com.lostVictories.service.LostVictoriesService.uuid;

/**
 * Created by dharshanar on 26/05/17.
 */
public class LostVictoriesServiceImpl extends LostVictoriesServerGrpc.LostVictoriesServerImplBase {

    private static Logger log = Logger.getLogger(LostVictoriesServiceImpl.class);
    LostVictoriesService lostVictoriesSerice;

    Set<SafeStreamObserver> clientObserverSet = new HashSet<>();
    Set<UUID> clientIDSet = new HashSet<>();

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

                UUID uuid = uuid(updateCharactersRequest.getClientID());
                if(!clientIDSet.contains(uuid)){
                    log.debug("registering new client:"+uuid);
                    safeStreamObserver.setClientID(uuid);
                    clientIDSet.add(uuid);
                    clientObserverSet.add(safeStreamObserver);
                }
                lostVictoriesSerice.updateLocalCharacters(updateCharactersRequest, safeStreamObserver, clientObserverSet);
            }

            @Override
            public void onError(Throwable throwable) {
                if(clientObserverSet.remove(safeStreamObserver)){
                    log.debug("de-registering client:"+safeStreamObserver.getClientID());
                    clientIDSet.remove(safeStreamObserver.getClientID());
                }
            }

            @Override
            public void onCompleted() {
                if(clientObserverSet.remove(safeStreamObserver)){
                    log.debug("de-registering client:"+safeStreamObserver.getClientID());
                    clientIDSet.remove(safeStreamObserver.getClientID());
                }
            }
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

    public void runWorld(Map<Country, Integer> victoryPoints, Map<Country, Integer> manPower, Map<Country, WeaponsFactory> weaponsFactory, Map<Country, VehicleFactory> vehicleFactory, Map<Country, Integer> nextRespawnTime, String gameName) {
        lostVictoriesSerice.runWorld(victoryPoints, manPower, weaponsFactory, vehicleFactory, nextRespawnTime, gameName, clientObserverSet);
    }
}
