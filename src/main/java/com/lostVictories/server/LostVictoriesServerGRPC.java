package com.lostVictories.server;

import com.lostVictories.service.LostVictoriesServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lostVictories.CharacterRunner;
import lostVictories.WorldRunner;
import lostVictories.dao.*;
import lostVictories.messageHanders.MessageRepository;
import lostVictories.service.LostVictoryService;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.net.InetAddress;
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

        Client esClient = getESClient();
        IndicesAdminClient adminClient = esClient.admin().indices();
        TreeDAO treeDAO = new TreeDAO(esClient, treeIndexName);
        EquipmentDAO equipmentDAO = new EquipmentDAO(esClient, equipmentIndexName);
        GameRequestDAO gameRequestDAO = new GameRequestDAO(esClient);
        PlayerUsageDAO playerUsageDAO = new PlayerUsageDAO(esClient, gameName);
        MessageRepository messageRepository = new MessageRepository();
        WorldRunner worldRunner = WorldRunner.instance(gameName);

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(100);
        jedisPoolConfig.setMinIdle(100);
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, "localhost");
        service = new LostVictoryService(jedisPool, instance, treeDAO, equipmentDAO, gameRequestDAO, playerUsageDAO, messageRepository, worldRunner);


        boolean existing = createIndices(adminClient, service, treeDAO);

        LostVictoriesServiceImpl grpcService = new LostVictoriesServiceImpl(jedisPool, instance, treeDAO, equipmentDAO, gameRequestDAO, playerUsageDAO, messageRepository, worldRunner);
        Server server = ServerBuilder.forPort(port)
                .addService(grpcService)
                .build();

        log.info("starting server services.");
        worldRunner.setLostVictoryService(grpcService);
        ScheduledExecutorService worldRunnerService = Executors.newSingleThreadScheduledExecutor();
        worldRunnerService.scheduleAtFixedRate(worldRunner, 0, 2, TimeUnit.SECONDS);
        CharacterRunner characterRunner = CharacterRunner.instance(service, jedisPool, instance);
        worldRunnerService.scheduleAtFixedRate(characterRunner, 1, 2, TimeUnit.SECONDS);


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
                gameRequestDAO.updateGameStatus(gameRequest, this.instance, gameName, port, instance);
            }
        }
        log.info("Listening on "+port);
        System.out.println("Server started......");


        server.awaitTermination();

    }

    private boolean createIndices(IndicesAdminClient adminClient, LostVictoryService service, TreeDAO treeDAO) throws IOException {
        deleteIndex(adminClient, equipmentIndexName);
        deleteIndex(adminClient, treeIndexName);

        final CreateIndexRequestBuilder treeIndexRequestBuilder = adminClient.prepareCreate(treeIndexName);
        XContentBuilder builder = XContentFactory.jsonBuilder().startObject().startObject("treeStatus").startObject("properties");
        builder.startObject("location")
                .field("type", "geo_point")
                .field("store", "yes")
                .endObject();
        builder.endObject().endObject().endObject();

        treeIndexRequestBuilder.addMapping("treeStatus", builder);
        treeIndexRequestBuilder.execute().actionGet();

        final CreateIndexRequestBuilder equipmentIndexRequestBuilder = adminClient.prepareCreate(equipmentIndexName);
        builder = XContentFactory.jsonBuilder().startObject().startObject("equipmentStatus").startObject("properties");
        builder.startObject("location")
                .field("type", "geo_point")
                .field("store", "yes")
                .endObject();
        builder.endObject().endObject().endObject();

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

    private Client getESClient() throws IOException {
        isElasticHealthy();
        TransportClient transportClient = new PreBuiltTransportClient(Settings.EMPTY)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
        return transportClient;


    }

    private boolean isElasticHealthy() throws IOException {
        CloseableHttpClient httpclient = HttpClientBuilder.create().setRetryHandler((exception, executionCount, context) -> {
            if (executionCount > 3) {
                return false;
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) { }
            return true;
        }).build();

        try {
            HttpGet httpget = new HttpGet("http://localhost:9200/_cluster/health");
            System.out.println("Executing request " + httpget.getRequestLine());
            httpclient.execute(httpget);
            System.out.println("----------------------------------------");
            return true;
        } finally {
            httpclient.close();
        }
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
