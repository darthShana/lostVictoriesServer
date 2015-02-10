package lostVictories;

import static org.elasticsearch.common.xcontent.XContentFactory.*;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.geo.GeoPoint;
import com.google.gson.Gson;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class UnitStateHandler extends SimpleChannelInboundHandler<String> {

	private Client esClient;
	private String indexName;

	public UnitStateHandler(Client esClient, String indexName) {
		this.esClient = esClient;
		this.indexName = indexName;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext arg0, String arg1) throws Exception {
		if(arg1.indexOf('{')!=-1 && arg1.lastIndexOf('}')!=-1){
			String s = arg1.substring(arg1.indexOf('{'), arg1.lastIndexOf('}')+1);
			UnitState us = new Gson().fromJson(s, UnitState.class);
			//System.out.println("arg1:"+us);
			
			us.getUnitPositions().forEach((uuid, vector) -> storeUnitState(uuid, vector));
		}
		
	}

	private void storeUnitState(UUID uuid, Vector vector) {
		try {
			IndexResponse response = esClient.prepareIndex(indexName, "unitStatus", uuid.toString())
			        .setSource(jsonBuilder()
			                    .startObject()
			                        .field("date", new Date())
			                        .field("location", new GeoPoint(vector.x, vector.z))
			                    .endObject()
			                  )
			        .execute()
			        .actionGet();

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}





	


}
