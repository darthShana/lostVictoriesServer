package com.lostVictories.server;

import com.lostVictories.service.LostVictoriesServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lostVictories.CharacterRunner;
import lostVictories.WorldRunner;
import lostVictories.dao.*;
import lostVictories.messageHanders.MessageRepository;
import lostVictories.service.LostVictoryService;
import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
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

    private static Logger log = Logger.getLogger(LostVictoriesServerGRPC.class);

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

        Client esClient = getESClient();
        IndicesAdminClient adminClient = esClient.admin().indices();
        HouseDAO houseDAO = new HouseDAO(esClient, houseIndexName);
        TreeDAO treeDAO = new TreeDAO(esClient, treeIndexName);
        EquipmentDAO equipmentDAO = new EquipmentDAO(esClient, equipmentIndexName);
        GameRequestDAO gameRequestDAO = new GameRequestDAO(esClient);
        PlayerUsageDAO playerUsageDAO = new PlayerUsageDAO(esClient, gameName);
        MessageRepository messageRepository = new MessageRepository();
        WorldRunner worldRunner = WorldRunner.instance(gameName);

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(1024);
        jedisPoolConfig.setMinIdle(1024);
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, "localhost" );
        service = new LostVictoryService(jedisPool, instance, houseDAO, treeDAO, equipmentDAO, gameRequestDAO, playerUsageDAO, messageRepository, worldRunner);


        boolean existing = createIndices(adminClient, service, houseDAO, treeDAO);


        LostVictoriesServiceImpl grpcService = new LostVictoriesServiceImpl(jedisPool, instance, houseDAO, treeDAO, equipmentDAO, gameRequestDAO, playerUsageDAO, messageRepository, worldRunner);
        Server server = ServerBuilder.forPort(port)
                .addService(grpcService)
                .build();

        worldRunner.setLostVictoryService(grpcService);
        ScheduledExecutorService worldRunnerService = Executors.newScheduledThreadPool(2);
        worldRunnerService.scheduleAtFixedRate(worldRunner, 0, 2, TimeUnit.SECONDS);
        CharacterRunner characterRunner = CharacterRunner.instance(service, jedisPool, gameName);
        worldRunnerService.scheduleAtFixedRate(characterRunner, 0, 2, TimeUnit.SECONDS);


        server.start();

        UUID gameRequest = null;
        try{
            gameRequest = gameRequestDAO.getGameRequest(gameName);
            log.info("starting game request:"+gameRequest+" for game:"+gameName);
        }catch(Exception e){
            log.info("cant find game request for :"+gameName);
        }

        if(gameRequest!=null){
            if(!existing){
                gameRequestDAO.updateGameStatus(gameRequest, this.instance, gameName, port, characterIndexName, houseIndexName, equipmentIndexName);
            }
        }
        log.info("Listening on "+port);
        System.out.println("Server started......");


        server.awaitTermination();

    }

    private boolean createIndices(IndicesAdminClient adminClient, LostVictoryService service, HouseDAO housesDAO, TreeDAO treeDAO) throws IOException {
        deleteIndex(adminClient, houseIndexName);
        deleteIndex(adminClient, equipmentIndexName);
        deleteIndex(adminClient, treeIndexName);


        final CreateIndexRequestBuilder houseIndexRequestBuilder = adminClient.prepareCreate(houseIndexName);
        XContentBuilder builder = XContentFactory.jsonBuilder().startObject().startObject("houseStatus").startObject("properties");
        builder.startObject("location")
                .field("type", "geo_point")
                .field("store", "yes")
                .endObject();
        houseIndexRequestBuilder.addMapping("houseStatus", builder);
        houseIndexRequestBuilder.execute().actionGet();

        final CreateIndexRequestBuilder treeIndexRequestBuilder = adminClient.prepareCreate(treeIndexName);
        builder = XContentFactory.jsonBuilder().startObject().startObject("treeStatus").startObject("properties");
        builder.startObject("location")
                .field("type", "geo_point")
                .field("store", "yes")
                .endObject();
        treeIndexRequestBuilder.addMapping("treeStatus", builder);
        treeIndexRequestBuilder.execute().actionGet();

        final CreateIndexRequestBuilder equipmentIndexRequestBuilder = adminClient.prepareCreate(equipmentIndexName);
        builder = XContentFactory.jsonBuilder().startObject().startObject("equipmentStatus").startObject("properties");
        builder.startObject("location")
                .field("type", "geo_point")
                .field("store", "yes")
                .endObject();

        equipmentIndexRequestBuilder.addMapping("equipmentStatus", builder);
        equipmentIndexRequestBuilder.execute().actionGet();

        service.loadScene();
        return false;
    }

    private void deleteIndex(IndicesAdminClient adminClient, String indexName) {
        final IndicesExistsResponse house = adminClient.prepareExists(indexName).execute().actionGet();
        if (house.isExists()) {
            log.info("index:"+indexName+" already exisits so deleting");
            adminClient.delete(new DeleteIndexRequest(indexName)).actionGet();
        }
    }

    private Client getESClient() {
        Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", "lostVictories").build();
        TransportClient transportClient = new TransportClient(settings);
        transportClient = transportClient.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
        return (Client) transportClient;


    }


    public static void main (String[] args) throws IOException, InterruptedException {
        if(args.length==0){
            new LostVictoriesServerGRPC("test_lost_victories1", 5055).run();
        }else{
            new LostVictoriesServerGRPC(args[0], Integer.parseInt(args[1])).run();
        }

    }
}
