package com.lostVictories.service;

import com.google.protobuf.ByteString;
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
import lostVictories.service.WorldRunnerInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by dharshanar on 26/05/17.
 */
public class LostVictoriesService {

    private static Logger log = LoggerFactory.getLogger(LostVictoriesService.class);

    private final JedisPool jedisPool;
    private final String nameSpace;
    private final GameRequestDAO gameRequestDAO;
    private final PlayerUsageDAO playerUsageDAO;
    private final MessageRepository messageRepository;
    private final WorldRunner worldRunner;

    public LostVictoriesService(JedisPool jedisPool, String nameSpace, GameRequestDAO gameRequestDAO, PlayerUsageDAO playerUsageDAO, MessageRepository messageRepository, WorldRunner worldRunner) {
        this.jedisPool = jedisPool;
        this.nameSpace = nameSpace;
        this.gameRequestDAO = gameRequestDAO;
        this.playerUsageDAO = playerUsageDAO;
        this.messageRepository = messageRepository;
        this.worldRunner = worldRunner;
    }


    public void checkoutSceen(CheckoutScreenRequest request, StreamObserver<LostVictoryCheckout> responseObserver) {
        Jedis jedis = jedisPool.getResource();
        try {
            CharacterDAO characterDAO = new CharacterDAO(jedis, nameSpace);
            HouseDAO houseDAO = new HouseDAO(jedis, nameSpace);
            EquipmentDAO equipmentDAO = new EquipmentDAO(jedis, nameSpace);
            TreeDAO treeDAO = new TreeDAO(jedis, nameSpace);
            CheckoutScreenMessageHandler checkoutScreenMessageHandler = new CheckoutScreenMessageHandler(characterDAO, houseDAO, equipmentDAO, treeDAO, playerUsageDAO);
            checkoutScreenMessageHandler.handle(request, responseObserver);
        }finally {
            jedis.close();
        }
    }

    public void updateLocalCharacters(UpdateCharactersRequest updateCharactersRequest, SafeStreamObserver responseObserver, Map<UUID, SafeStreamObserver<LostVictoryMessage>> clientObserverMap) {
        Jedis jedis = jedisPool.getResource();
        try {
            CharacterDAO characterDAO = new CharacterDAO(jedis, nameSpace);
            HouseDAO houseDAO = new HouseDAO(jedis, nameSpace);
            EquipmentDAO equipmentDAO = new EquipmentDAO(jedis, nameSpace);
            UpdateCharactersMessageHandler updateCharacterMessageHandler = new UpdateCharactersMessageHandler(characterDAO, houseDAO, equipmentDAO, worldRunner, messageRepository);
            updateCharacterMessageHandler.handle(updateCharactersRequest, responseObserver, clientObserverMap);
        }catch (IOException e){
            throw new RuntimeException(e);
        }finally {
            jedis.close();
        }
    }

    public static ByteString bytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return ByteString.copyFrom(bb.array());
    }

    public static UUID uuid(ByteString byteString){
        if (byteString==null){
            return null;
        }
        ByteBuffer bb = byteString.asReadOnlyByteBuffer();
        long high = bb.getLong();
        long low = bb.getLong();
        return new UUID(high, low);
    }


    public void deathNotification(DeathNotificationRequest request, StreamObserver<LostVictoryMessage> responseObserver) {
        Jedis jedis = jedisPool.getResource();
        try {
            CharacterDAO characterDAO = new CharacterDAO(jedis, nameSpace);
            EquipmentDAO equipmentDAO = new EquipmentDAO(jedis, nameSpace);
            DeathNotificationMessageHandler checkoutScreenMessageHandler = new DeathNotificationMessageHandler(characterDAO, equipmentDAO);
            checkoutScreenMessageHandler.handle(request, responseObserver);
        }finally {
            jedis.close();
        }
    }

    public void gunnerDeathNotification(PassengerDeathNotificationRequest request, StreamObserver<LostVictoryMessage> responseObserver) {
        Jedis jedis = jedisPool.getResource();
        try {
            CharacterDAO characterDAO = new CharacterDAO(jedis, nameSpace);
            PassengerDeathNotificationMessageHandler messageHandler = new PassengerDeathNotificationMessageHandler(characterDAO);
            messageHandler.handle(request, responseObserver);
        }finally {
            jedis.close();
        }
    }

    public void addObjective(AddObjectiveRequest request, StreamObserver<LostVictoryMessage> responseObserver) {
        Jedis jedis = jedisPool.getResource();
        try {
            CharacterDAO characterDAO = new CharacterDAO(jedis, nameSpace);
            AddObjectiveMessageHandler messageHandler = new AddObjectiveMessageHandler(characterDAO);
            messageHandler.handle(request, responseObserver);
        }finally {
            jedis.close();
        }
    }

    public void requestEquipmentCollection(EquipmentCollectionRequest request, StreamObserver<LostVictoryMessage> responseObserver) {
        Jedis jedis = jedisPool.getResource();
        try {
            CharacterDAO characterDAO = new CharacterDAO(jedis, nameSpace);
            EquipmentDAO equipmentDAO = new EquipmentDAO(jedis, nameSpace);
            CollectEquipmentMessageHandler messageHandler = new CollectEquipmentMessageHandler(characterDAO, equipmentDAO, messageRepository);
            messageHandler.handle(request, responseObserver);
        }finally {
            jedis.close();
        }
    }

    public void boardVehicle(BoardVehicleRequest request, StreamObserver<LostVictoryMessage> responseObserver) {
        Jedis jedis = jedisPool.getResource();
        try {
            CharacterDAO characterDAO = new CharacterDAO(jedis, nameSpace);
            BoardingVehicleMessageHandler messageHandler = new BoardingVehicleMessageHandler(characterDAO, messageRepository);
            messageHandler.handle(request, responseObserver);
        }catch(IOException e){
            throw new RuntimeException(e);
        }finally {
            jedis.close();
        }
    }

    public void disembarkPassengers(DisembarkPassengersRequest request, StreamObserver<LostVictoryMessage> responseObserver) {
        Jedis jedis = jedisPool.getResource();
        try {
            CharacterDAO characterDAO = new CharacterDAO(jedis, nameSpace);
            DisembarkPassengersMessageHandler messageHandler = new DisembarkPassengersMessageHandler(characterDAO);
            messageHandler.handle(request, responseObserver);
        }catch(IOException e){
            throw new RuntimeException(e);
        }finally {
            jedis.close();
        }
    }

    public Map<com.jme3.lostVictories.network.messages.Country, Integer> runWorld(Map<com.jme3.lostVictories.network.messages.Country, Integer> victoryPoints, Map<com.jme3.lostVictories.network.messages.Country, Integer> manPower, Map<com.jme3.lostVictories.network.messages.Country, WeaponsFactory> weaponsFactory, Map<com.jme3.lostVictories.network.messages.Country, VehicleFactory> vehicleFactory, Map<com.jme3.lostVictories.network.messages.Country, Integer> nextRespawnTime, String gameName, Map<UUID, SafeStreamObserver<LostVictoryStatusMessage>> clientObserverMap) {

        try (Jedis jedis = jedisPool.getResource()){
            CharacterDAO characterDAO = new CharacterDAO(jedis, nameSpace);
            HouseDAO houseDAO = new HouseDAO(jedis, nameSpace);
            EquipmentDAO equipmentDAO = new EquipmentDAO(jedis, nameSpace);
            return new WorldRunnerInstance().runWorld(characterDAO, houseDAO, gameRequestDAO, playerUsageDAO, equipmentDAO, victoryPoints, manPower, weaponsFactory, vehicleFactory, nextRespawnTime, messageRepository, gameName, clientObserverMap);
        }catch(Throwable e){
            throw new RuntimeException(e);
        }
    }

    public void joinGame(JoinRequest request, StreamObserver<JoinResponse> responseObserver) {
        try (Jedis jedis = jedisPool.getResource()){
            log.info("joining game for user:"+request.getUserID()+" for country:"+request.getCountry());
            CharacterDAO characterDAO = new CharacterDAO(jedis, nameSpace);
            UUID characterID =  characterDAO.joinGame(UUID.fromString(request.getUserID()), com.jme3.lostVictories.network.messages.Country.valueOf(request.getCountry()));
            if(characterID!=null) {
                responseObserver.onNext(JoinResponse.newBuilder()
                        .setCharacterID(characterID.toString())
                        .build());
            }
            responseObserver.onCompleted();
        }catch(Throwable e){
            throw new RuntimeException(e);
        }
    }
}
