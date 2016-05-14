package lostVictories;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.EquipmentDAO;
import lostVictories.dao.GameRequestDAO;
import lostVictories.dao.GameStatusDAO;
import lostVictories.dao.HouseDAO;
import lostVictories.messageHanders.MessageHandler;

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
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;


public class LostVictoriesSever {
	
	private static Logger log = Logger.getLogger(LostVictoriesSever.class); 
	
	private int port;
	private String characterIndexName;
	private String houseIndexName;
	private String equipmentIndexName;

	private String instance;

	private String gameName;

	public LostVictoriesSever(String instance, int port) {
		this.gameName = instance;
		this.instance = instance.toLowerCase().replace(' ', '_');
		characterIndexName = this.instance+"_unit_status";
		houseIndexName = this.instance+"_house_status";
		equipmentIndexName = this.instance+"_equipment_status";
		this.port = port;
		
	}
	
	private void run() throws InterruptedException, IOException {
		Client esClient = getESClient();
		IndicesAdminClient adminClient = esClient.admin().indices();
		CharacterDAO characterDAO = new CharacterDAO(esClient, characterIndexName);
		HouseDAO houseDAO = new HouseDAO(esClient, houseIndexName);
		EquipmentDAO equipmentDAO = new EquipmentDAO(esClient, equipmentIndexName);
		GameStatusDAO gameStatusDAO = new GameStatusDAO(esClient, characterIndexName);
		GameRequestDAO gameRequestDAO = new GameRequestDAO(esClient);
		
		boolean existing = createIndices(adminClient, characterDAO, houseDAO);
		if(!existing){
			gameStatusDAO.createGameStatus(UUID.randomUUID(), gameName, port, characterIndexName, houseIndexName, equipmentIndexName);
		}
		
		ScheduledExecutorService worldRunnerService = Executors.newScheduledThreadPool(2);
		WorldRunner worldRunner = WorldRunner.instance(characterDAO, houseDAO, gameStatusDAO);
		worldRunnerService.scheduleAtFixedRate(worldRunner, 0, 2, TimeUnit.SECONDS);
		CharacterRunner characterRunner = CharacterRunner.instance(characterDAO, houseDAO);
		worldRunnerService.scheduleAtFixedRate(characterRunner, 0, 2, TimeUnit.SECONDS);
		
		ServerBootstrap bootstrap = new ServerBootstrap( new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
				 
		 // Set up the pipeline factory.
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(
					new ObjectDecoder(ClassResolvers.cacheDisabled(getClass().getClassLoader())),
					new ObjectEncoder(),
					new MessageHandler(characterDAO, houseDAO, equipmentDAO, gameStatusDAO)
				);
			 };
		 });
		 
		 // Bind and start to accept incoming connections.
		 bootstrap.bind(new InetSocketAddress("0.0.0.0", port));
		 UUID gameRequest = gameRequestDAO.getGameRequest(gameName);
		 log.info("starting game request:"+gameRequest+"for game:"+gameName);
		 if(gameRequest!=null){
			 gameRequestDAO.updateGameeRequest(gameRequest);
		 }
		 log.info("Listening on "+port);
		
	}



	private boolean createIndices(IndicesAdminClient adminClient, CharacterDAO characterDAO, HouseDAO housesDAO) throws IOException {
		final IndicesExistsResponse res = adminClient.prepareExists(characterIndexName).execute().actionGet();
        if (res.isExists()) {
        	log.info("index:"+characterIndexName+" already exisits");
            return true;
        }
        log.info("creating new index:"+characterIndexName);
	    
	    deleteIndex(adminClient, houseIndexName);
	    deleteIndex(adminClient, equipmentIndexName);
        
        final CreateIndexRequestBuilder createIndexRequestBuilder = adminClient.prepareCreate(characterIndexName);
        
	    XContentBuilder builder = XContentFactory.jsonBuilder().startObject().startObject("unitStatus").startObject("properties");
	    builder.startObject("location")
        	.field("type", "geo_point")
        	.endObject();
	    builder.startObject("type")
    		.field("type", "string")
    		.field("index", "not_analyzed")
    		.field("store", "yes")
    		.endObject();
	    builder.startObject("rank")
			.field("type", "string")
			.field("index", "not_analyzed")
			.field("store", "yes")
			.endObject();
	    builder.startObject("country")
			.field("type", "string")
			.field("index", "not_analyzed")
			.field("store", "yes")
			.endObject();
	    builder.startObject("userID")
		    .field("type", "string")
		    .field("index", "not_analyzed")
		    .field("store", "yes")
		    .endObject();
	    
	    createIndexRequestBuilder.addMapping("unitStatus", builder);
	    createIndexRequestBuilder.execute().actionGet();
	    
	    final CreateIndexRequestBuilder houseIndexRequestBuilder = adminClient.prepareCreate(houseIndexName);
	    builder = XContentFactory.jsonBuilder().startObject().startObject("houseStatus").startObject("properties");
	    builder.startObject("location")
        	.field("type", "geo_point")
        	.field("store", "yes")
        	.endObject();
	    
	    houseIndexRequestBuilder.addMapping("houseStatus", builder);
	    houseIndexRequestBuilder.execute().actionGet();
	    
	    final CreateIndexRequestBuilder equipmentIndexRequestBuilder = adminClient.prepareCreate(equipmentIndexName);
	    builder = XContentFactory.jsonBuilder().startObject().startObject("equipmentStatus").startObject("properties");
	    builder.startObject("location")
        	.field("type", "geo_point")
        	.field("store", "yes")
        	.endObject();
	    
	    equipmentIndexRequestBuilder.addMapping("equipmentStatus", builder);
	    equipmentIndexRequestBuilder.execute().actionGet();
	    
	    new LostVictoryScene().loadScene(characterDAO, housesDAO);
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
	

	public static void main(String[] args) throws Exception {
		if(args.length==0){
//			new LostVictoriesSever("test_lost_victories1", 5055).run();
			new LostVictoriesSever("Saar Offensive", 5055).run();
		}else{
			new LostVictoriesSever(args[0], Integer.parseInt(args[1])).run();
		}
	}


}
