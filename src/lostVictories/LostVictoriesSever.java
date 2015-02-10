package lostVictories;

import java.io.IOException;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class LostVictoriesSever {
	
	
	private int port;
	private String indexName;

	public LostVictoriesSever(String instance, int port) {
		indexName = instance+"_unit_status";
		this.port = port;
		
	}
	
	private void run() throws InterruptedException, IOException {
		
		Client esClient = getESClient();
		IndicesAdminClient adminClient = esClient.admin().indices();
		try{
			createIndex(adminClient);
		}catch(IndexAlreadyExistsException e){}
		
		
		EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class) // (3)
             .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                	 ch.pipeline().addLast("framer", new DelimiterBasedFrameDecoder(8192, true, Delimiters.lineDelimiter())); 
                	 ch.pipeline().addLast("decoder", new StringDecoder());
                	 ch.pipeline().addLast("encoder", new StringEncoder());
                     ch.pipeline().addLast("handler", new UnitStateHandler(esClient, indexName));
                 }
             })
             .option(ChannelOption.SO_BACKLOG, 128)          // (5)
             .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)
            f.channel().closeFuture().sync();
            
        } finally {
        	System.out.println("closign groupr");
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
		
	}

	private void createIndex(IndicesAdminClient adminClient) throws IOException {
		CreateIndexRequest request = new CreateIndexRequest(indexName);
	    CreateIndexResponse response = adminClient.create(request).actionGet();
	    if (!response.isAcknowledged()) {
	        throw new RuntimeException("Failed to delete index " + indexName);
	    }
	    System.out.println("created index:"+indexName);
	    
	    XContentBuilder builder = XContentFactory.jsonBuilder().startObject().startObject("unitStatus").startObject("properties");
	    builder.startObject("location")
        	.field("type", "geo_point")
        	.field("store", "yes")
        	.endObject();
	}

	private Client getESClient() {
		Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", "elasticsearch").build();
		TransportClient transportClient = new TransportClient(settings);
		transportClient = transportClient.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
		return (Client) transportClient;

		
	}
	
	class ExistsException extends RuntimeException{}

	public static void main(String[] args) throws Exception {
		
		new LostVictoriesSever("test_lost_victories1", 5055).run();
	}


}
