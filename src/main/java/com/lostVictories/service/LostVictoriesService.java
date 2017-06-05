package com.lostVictories.service;

import com.google.protobuf.ByteString;
import com.lostVictories.api.*;
import io.grpc.stub.StreamObserver;
import lostVictories.WorldRunner;
import lostVictories.dao.*;
import lostVictories.messageHanders.MessageRepository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;

/**
 * Created by dharshanar on 26/05/17.
 */
public class LostVictoriesService {

    private final JedisPool jedisPool;
    private final String nameSpace;
    private final HouseDAO houseDAO;
    private final TreeDAO treeDAO;
    private final EquipmentDAO equipmentDAO;
    private final GameStatusDAO gameStatusDAO;
    private final GameRequestDAO gameRequestDAO;
    private final PlayerUsageDAO playerUsageDAO;
    private final MessageRepository messageRepository;
    private final WorldRunner worldRunner;

    public LostVictoriesService(JedisPool jedisPool, String nameSpace, HouseDAO houseDAO, TreeDAO treeDAO, EquipmentDAO equipmentDAO, GameStatusDAO gameStatusDAO, GameRequestDAO gameRequestDAO, PlayerUsageDAO playerUsageDAO, MessageRepository messageRepository, WorldRunner worldRunner) {
        this.jedisPool = jedisPool;
        this.nameSpace = nameSpace;
        this.houseDAO = houseDAO;
        this.treeDAO = treeDAO;
        this.equipmentDAO = equipmentDAO;
        this.gameStatusDAO = gameStatusDAO;
        this.gameRequestDAO = gameRequestDAO;
        this.playerUsageDAO = playerUsageDAO;
        this.messageRepository = messageRepository;
        this.worldRunner = worldRunner;
    }


    public void checkoutSceen(CheckoutScreenRequest request, StreamObserver<LostVictoryMessage> responseObserver) {
        Jedis jedis = jedisPool.getResource();
        try {
            CharacterDAO characterDAO = new CharacterDAO(jedis, nameSpace);
            CheckoutScreenMessageHandler checkoutScreenMessageHandler = new CheckoutScreenMessageHandler(characterDAO, houseDAO, equipmentDAO, treeDAO, playerUsageDAO);
            checkoutScreenMessageHandler.handle(request, responseObserver);
        }finally {
            jedis.close();
        }
    }

    public void updateLocalCharacters(UpdateCharactersRequest updateCharactersRequest, SafeStreamObserver responseObserver, Map<UUID, SafeStreamObserver> clientObserverMap) {
        Jedis jedis = jedisPool.getResource();
        try {
            CharacterDAO characterDAO = new CharacterDAO(jedis, nameSpace);
            UpdateCharactersMessageHandler checkoutScreenMessageHandler = new UpdateCharactersMessageHandler(characterDAO, houseDAO, equipmentDAO, worldRunner, messageRepository);
            checkoutScreenMessageHandler.handle(updateCharactersRequest, responseObserver, clientObserverMap);
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
}
