package lostVictories.messageHanders;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lostVictories.CharacterDAO;
import lostVictories.HouseDAO;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.google.gson.Gson;
import com.jme3.lostVictories.network.messages.AddObjectiveRequest;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CheckoutScreenRequest;
import com.jme3.lostVictories.network.messages.CheckoutScreenResponse;
import com.jme3.lostVictories.network.messages.DeathNotificationRequest;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;
import com.jme3.lostVictories.network.messages.UpdateCharactersRequest;


public class MessageHandler extends SimpleChannelHandler {

	private static Logger log = Logger.getLogger(MessageHandler.class);
	
	private UpdateCharactersMessageHandler updateCharactersMessageHandler;
	private CheckoutScreenMessageHandler checkoutScreenMessageHandler;
	private DeathNotificationMessageHandler deathNotificationMessageHandler;
	private AddObjectiveMessageHandler addObjectiveMessageHandler;

	public MessageHandler(CharacterDAO characterDAO, HouseDAO houseDAO) {
		updateCharactersMessageHandler = new UpdateCharactersMessageHandler(characterDAO, houseDAO);
		checkoutScreenMessageHandler = new CheckoutScreenMessageHandler(characterDAO, houseDAO);
		deathNotificationMessageHandler = new DeathNotificationMessageHandler(characterDAO);
		addObjectiveMessageHandler = new AddObjectiveMessageHandler(characterDAO);
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
