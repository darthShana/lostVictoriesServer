package lostVictories.messageHanders;


import java.util.UUID;

import lostVictories.WorldRunner;
import lostVictories.dao.CharacterDAO;
import lostVictories.dao.EquipmentDAO;
import lostVictories.dao.HouseDAO;
import lostVictories.dao.PlayerUsageDAO;
import lostVictories.dao.TreeDAO;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.jme3.lostVictories.network.messages.AddObjectiveRequest;
import com.jme3.lostVictories.network.messages.BoardVehicleRequest;
import com.jme3.lostVictories.network.messages.CheckoutScreenRequest;
import com.jme3.lostVictories.network.messages.DeathNotificationRequest;
import com.jme3.lostVictories.network.messages.DisembarkPassengersRequest;
import com.jme3.lostVictories.network.messages.EquipmentCollectionRequest;
import com.jme3.lostVictories.network.messages.PassengerDeathNotificationRequest;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;
import com.jme3.lostVictories.network.messages.UpdateCharactersRequest;


public class MessageHandler extends SimpleChannelHandler {

	private static Logger log = Logger.getLogger(MessageHandler.class);
	
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
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		
		LostVictoryMessage msg = (LostVictoryMessage)e.getMessage();
		LostVictoryMessage lostVictoryMessage;
		
		if(msg instanceof CheckoutScreenRequest){
			lostVictoryMessage = checkoutScreenMessageHandler.handle((CheckoutScreenRequest) msg);
			log.info("returning scene");
		} else if(msg instanceof UpdateCharactersRequest){
			lostVictoryMessage = updateCharactersMessageHandler.handle((UpdateCharactersRequest)msg);
		} else if(msg instanceof DeathNotificationRequest) {
			lostVictoryMessage = deathNotificationMessageHandler.handle((DeathNotificationRequest)msg);
		} else if(msg instanceof PassengerDeathNotificationRequest) {
			lostVictoryMessage = gunnerDeathNotificationMessageHandler.handle((PassengerDeathNotificationRequest)msg);
		}else if(msg instanceof EquipmentCollectionRequest) {
			lostVictoryMessage = collectEquipmentMessageHandler.handle((EquipmentCollectionRequest)msg);
		} else if(msg instanceof BoardVehicleRequest){
			lostVictoryMessage = boardingVehicleMessageHandler.handle((BoardVehicleRequest)msg);
		} else if(msg instanceof DisembarkPassengersRequest){
			lostVictoryMessage = disembarkPassengersMessageHandler.handle((DisembarkPassengersRequest)msg);
		} else if(msg instanceof AddObjectiveRequest){
			lostVictoryMessage = addObjectiveMessageHandler.handle((AddObjectiveRequest)msg);
		} 
		else{
			lostVictoryMessage = new LostVictoryMessage(UUID.randomUUID());
			System.out.println("Hey Guys !  I got a date ! [" + msg.getClientID() + "] and I modified it to []");
		}
		
		Channel channel = e.getChannel();
		ChannelFuture channelFuture = Channels.future(e.getChannel());
		ChannelEvent responseEvent = new DownstreamMessageEvent(channel, channelFuture, lostVictoryMessage, channel.getRemoteAddress());
		ctx.sendDownstream(responseEvent);
		
		super.messageReceived(ctx, e);
	}


	
}
