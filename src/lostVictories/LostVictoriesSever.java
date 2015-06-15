package lostVictories;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import lostVictories.messageHanders.MessageHandler;

import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.indices.IndexAlreadyExistsException;
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
	private String indexName;

	public LostVictoriesSever(String instance, int port) {
		indexName = instance+"_unit_status";
		this.port = port;
		
	}
	
	private void run() throws InterruptedException, IOException {
		Client esClient = getESClient();
		IndicesAdminClient adminClient = esClient.admin().indices();
		CharacterDAO characterDAO = new CharacterDAO(esClient, indexName);
		
		createIndex(adminClient, characterDAO);
		
		ServerBootstrap bootstrap = new ServerBootstrap( new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
				 
		 // Set up the pipeline factory.
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(
					new ObjectDecoder(ClassResolvers.cacheDisabled(getClass().getClassLoader())),
					new ObjectEncoder(),
					new MessageHandler(characterDAO)
				);
			 };
		 });
		 
		 // Bind and start to accept incoming connections.
		 bootstrap.bind(new InetSocketAddress("0.0.0.0", port));
		 log.info("Listening on "+port);
		
		
	}

	private void createIndex(IndicesAdminClient adminClient, CharacterDAO characterDAO) throws IOException {
		final IndicesExistsResponse res = adminClient.prepareExists(indexName).execute().actionGet();
        if (res.isExists()) {
        	log.info("index:"+indexName+" already exisits");
            return;
        }
        log.info("creating new index:"+indexName);

        final CreateIndexRequestBuilder createIndexRequestBuilder = adminClient.prepareCreate(indexName);
		
	    XContentBuilder builder = XContentFactory.jsonBuilder().startObject().startObject("unitStatus").startObject("properties");
	    builder.startObject("location")
        	.field("type", "geo_point")
        	.field("store", "yes")
        	.endObject();
	    
	    createIndexRequestBuilder.addMapping("unitStatus", builder);
	    createIndexRequestBuilder.execute().actionGet();
	    
	    new LostVictoryScene().loadScene(characterDAO);
	}

	private Client getESClient() {
		Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", "elasticsearch").build();
		TransportClient transportClient = new TransportClient(settings);
		transportClient = transportClient.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
		return (Client) transportClient;

		
	}
	

	public static void main(String[] args) throws Exception {
		
		new LostVictoriesSever("test_lost_victories1", 5055).run();
	}


}
