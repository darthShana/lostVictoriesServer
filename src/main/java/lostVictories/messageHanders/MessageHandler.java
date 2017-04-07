package lostVictories.messageHanders;


import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lostVictories.WorldRunner;
import lostVictories.dao.CharacterDAO;
import lostVictories.dao.EquipmentDAO;
import lostVictories.dao.HouseDAO;
import lostVictories.dao.PlayerUsageDAO;
import lostVictories.dao.TreeDAO;

import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.wrapper.AddObjectiveRequest;
import com.jme3.lostVictories.network.messages.wrapper.BoardVehicleRequest;
import com.jme3.lostVictories.network.messages.wrapper.CheckoutScreenRequest;
import com.jme3.lostVictories.network.messages.wrapper.DeathNotificationRequest;
import com.jme3.lostVictories.network.messages.wrapper.DisembarkPassengersRequest;
import com.jme3.lostVictories.network.messages.wrapper.EquipmentCollectionRequest;
import com.jme3.lostVictories.network.messages.wrapper.PassengerDeathNotificationRequest;
import com.jme3.lostVictories.network.messages.wrapper.LostVictoryMessage;
import com.jme3.lostVictories.network.messages.wrapper.UpdateCharactersRequest;


public class MessageHandler extends SimpleChannelInboundHandler<DatagramPacket> {

	private static Logger log = Logger.getLogger(MessageHandler.class);

	public static ObjectMapper objectMapper = new ObjectMapper();
	static {
		objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
		objectMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
		objectMapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);

		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}

	
	private UpdateCharactersMessageHandler updateCharactersMessageHandler;
	private CheckoutScreenMessageHandler checkoutScreenMessageHandler;
	private DeathNotificationMessageHandler deathNotificationMessageHandler;
	private PassengerDeathNotificationMessageHandler gunnerDeathNotificationMessageHandler;
	private AddObjectiveMessageHandler addObjectiveMessageHandler;
	private CollectEquipmentMessageHandler collectEquipmentMessageHandler;
	private BoardingVehicleMessageHandler boardingVehicleMessageHandler;
	private DisembarkPassengersMessageHandler disembarkPassengersMessageHandler;

	public MessageHandler(CharacterDAO characterDAO, HouseDAO houseDAO, EquipmentDAO equipmentDAO, PlayerUsageDAO playerUsageDAO, TreeDAO treeDAO, WorldRunner worldRunner, MessageRepository messageRepository) {
		updateCharactersMessageHandler = new UpdateCharactersMessageHandler(characterDAO, houseDAO, equipmentDAO, worldRunner, messageRepository);
		checkoutScreenMessageHandler = new CheckoutScreenMessageHandler(characterDAO, houseDAO, equipmentDAO, treeDAO, playerUsageDAO);
		deathNotificationMessageHandler = new DeathNotificationMessageHandler(characterDAO, equipmentDAO);
		gunnerDeathNotificationMessageHandler = new PassengerDeathNotificationMessageHandler(characterDAO);
		addObjectiveMessageHandler = new AddObjectiveMessageHandler(characterDAO);
		collectEquipmentMessageHandler = new CollectEquipmentMessageHandler(characterDAO, equipmentDAO, messageRepository);
		boardingVehicleMessageHandler = new BoardingVehicleMessageHandler(characterDAO, messageRepository);
		disembarkPassengersMessageHandler = new DisembarkPassengersMessageHandler(characterDAO);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {

		LostVictoryMessage msg = CharacterDAO.MAPPER.readValue(extractMessage(packet), LostVictoryMessage.class);
		Set<LostVictoryMessage> lostVictoryMessages = new HashSet<>();
		
		if(msg instanceof CheckoutScreenRequest){
			lostVictoryMessages.addAll(checkoutScreenMessageHandler.handle((CheckoutScreenRequest) msg));
			log.info("returning scene");
		} else if(msg instanceof UpdateCharactersRequest){
			lostVictoryMessages.addAll(updateCharactersMessageHandler.handle((UpdateCharactersRequest)msg));
		} else if(msg instanceof DeathNotificationRequest) {
			lostVictoryMessages.addAll(deathNotificationMessageHandler.handle((DeathNotificationRequest)msg));
		} else if(msg instanceof PassengerDeathNotificationRequest) {
			lostVictoryMessages.addAll(gunnerDeathNotificationMessageHandler.handle((PassengerDeathNotificationRequest)msg));
		}else if(msg instanceof EquipmentCollectionRequest) {
			lostVictoryMessages.addAll(collectEquipmentMessageHandler.handle((EquipmentCollectionRequest)msg));
		} else if(msg instanceof BoardVehicleRequest){
			lostVictoryMessages.addAll(boardingVehicleMessageHandler.handle((BoardVehicleRequest)msg));
		} else if(msg instanceof DisembarkPassengersRequest){
			lostVictoryMessages.addAll(disembarkPassengersMessageHandler.handle((DisembarkPassengersRequest)msg));
		} else if(msg instanceof AddObjectiveRequest){
			lostVictoryMessages.addAll(addObjectiveMessageHandler.handle((AddObjectiveRequest)msg));
		} else{
			throw new RuntimeException("unknown request:"+msg);
		}

		lostVictoryMessages.forEach(m->{
			try {
				byte[] compressed = packMessage(m);


//				Deflater deflater = new Deflater();
//				deflater.setLevel(Deflater.BEST_COMPRESSION);
//				deflater.setInput(data);
//				ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
//				deflater.finish();
//				byte[] buffer = new byte[2048];
//				while (!deflater.finished()) {
//					int count = deflater.deflate(buffer); // returns the generated code... index
//					outputStream.write(buffer, 0, count);
//				}
//
//				outputStream.close();
//				byte[] output = outputStream.toByteArray();
				ctx.write(new DatagramPacket(Unpooled.copiedBuffer(compressed), packet.sender()));
//				System.out.println("send back response:"+m.getClass()+" size"+data.length+"/"+compressed.length);


			} catch (IOException e) {
				e.printStackTrace();
			}
		});







	}

	private byte[] packMessage(LostVictoryMessage m) throws IOException {
		//				byte[] data = SerializationUtils.serialize(m);
		byte[] data = objectMapper.writeValueAsBytes(m);

		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
		GZIPOutputStream gzip = new GZIPOutputStream(bos);
		gzip.write(data);
		gzip.close();
		byte[] compressed = bos.toByteArray();
		bos.close();
		return compressed;
	}

	private String extractMessage(DatagramPacket packet) throws IOException {
		int i = 0;
		byte[] incomming = new byte[packet.content().capacity()];
		while (packet.content().isReadable()) {
			incomming[i++] = packet.content().readByte();
		}

		ByteArrayInputStream bis = new ByteArrayInputStream(incomming);
		GZIPInputStream gis = new GZIPInputStream(bis);
		BufferedReader br = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
		StringBuilder sb = new StringBuilder();
		String line;
		while((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();
		gis.close();
		bis.close();
		return sb.toString();
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}


	
}
