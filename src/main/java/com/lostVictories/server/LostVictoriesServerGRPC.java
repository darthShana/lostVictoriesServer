package com.lostVictories.server;

import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.lostVictories.service.LostVictoriesServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lostVictories.CharacterRunner;
import lostVictories.NavMeshStore;
import lostVictories.WorldRunner;
import lostVictories.dao.*;
import lostVictories.messageHanders.MessageRepository;
import lostVictories.service.LostVictoryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by dharshanar on 26/05/17.
 */
public class LostVictoriesServerGRPC {

    private static Logger log = LoggerFactory.getLogger(LostVictoriesServerGRPC.class);

    private int port;
    private String characterIndexName;
    private String houseIndexName;
    private String treeIndexName;
    private String equipmentIndexName;

    private String instance;

    private String gameName;
    private LostVictoryService service;

    public LostVictoriesServerGRPC(String instance, int port){
        this.gameName = instance;
        this.instance = instance.toLowerCase().replace(' ', '_');
        characterIndexName = this.instance+"_unit_status";
        houseIndexName = this.instance+"_house_status";
        treeIndexName = this.instance+"_tree_status";
        equipmentIndexName = this.instance+"_equipment_status";
        this.port = port;
    }

    public void run() throws IOException, InterruptedException {
        System.out.println("Starting server......");


        GameRequestDAO gameRequestDAO = new GameRequestDAO();
        PlayerUsageDAO playerUsageDAO = new PlayerUsageDAO();
        MessageRepository messageRepository = new MessageRepository();
        WorldRunner worldRunner = WorldRunner.instance(gameName);

        NavMeshStore pathFinder = NavMeshStore.intstace();

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(100);
        jedisPoolConfig.setMinIdle(100);
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, "localhost");
        service = new LostVictoryService(jedisPool, instance, gameRequestDAO, playerUsageDAO, messageRepository, worldRunner);
        service.loadScene(pathFinder);


        LostVictoriesServiceImpl grpcService = new LostVictoriesServiceImpl(jedisPool, instance, gameRequestDAO, playerUsageDAO, messageRepository, worldRunner);
        Server server = ServerBuilder.forPort(port)
                .addService(grpcService)
                .build();

        log.info("starting server services.");
        worldRunner.setLostVictoryService(grpcService);
        ScheduledExecutorService worldRunnerService = Executors.newSingleThreadScheduledExecutor();
        worldRunnerService.scheduleAtFixedRate(worldRunner, 0, 2, TimeUnit.SECONDS);
        ScheduledExecutorService characterRunnerService = Executors.newSingleThreadScheduledExecutor();
        CharacterRunner characterRunner = CharacterRunner.instance(service, jedisPool, instance);
        characterRunnerService.scheduleAtFixedRate(characterRunner, 1, 2, TimeUnit.SECONDS);


        server.start();

        UUID gameID = null;
        try{
            gameID = UUID.fromString(System.getenv("GAME_ID"));
            log.info("starting game request:"+gameID+" for game:"+gameName);
        }catch(Exception e){
            log.info("cant find game request for :"+gameName);
        }

        if(gameID!=null){
            gameRequestDAO.updateGameStatus(gameID);
        }
        log.info("Listening on "+port);
        System.out.println("Server started......");


        server.awaitTermination();

    }


    public static void main (String[] args) throws IOException, InterruptedException {
        if(args.length==2) {
            new LostVictoriesServerGRPC(args[0], Integer.parseInt(args[1])).run();
        }else if(System.getenv("GAME_NAME")!=null && System.getenv("GAME_PORT")!=null){
            System.out.println("starting game from env:"+System.getenv("GAME_NAME"));
            new LostVictoriesServerGRPC(System.getenv("GAME_NAME"), Integer.parseInt(System.getenv("GAME_PORT"))).run();
        }else{
            new LostVictoriesServerGRPC("test_lost_victories1", 5055).run();
        }

    }
}
