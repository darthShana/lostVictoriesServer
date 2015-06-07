package lostVictories;


import java.util.Set;

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
import com.jme3.lostVictories.network.messages.Character;
import com.jme3.lostVictories.network.messages.CheckoutScreenRequest;
import com.jme3.lostVictories.network.messages.CheckoutScreenResponse;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;


public class MessageHandler extends SimpleChannelHandler {

	private static Logger log = Logger.getLogger(MessageHandler.class);
	
	private CharacterDAO characterDAO;

	public MessageHandler(CharacterDAO characterDAO) {
		this.characterDAO = characterDAO;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		
		LostVictoryMessage msg = (LostVictoryMessage)e.getMessage();
		LostVictoryMessage lostVictoryMessage;
		
		if(msg instanceof CheckoutScreenRequest){
			Set<Character> allCharacters = characterDAO.getAllCharacters();
			lostVictoryMessage = new CheckoutScreenResponse(allCharacters);
			log.info("returning scene");
		}else{
			String newMessage = msg.getMessage() + " test 78";
			lostVictoryMessage = new LostVictoryMessage(newMessage);
			System.out.println("Hey Guys !  I got a date ! [" + msg.getMessage() + "] and I modified it to [" + newMessage + "]");
		}
		
		Channel channel = e.getChannel();
		ChannelFuture channelFuture = Channels.future(e.getChannel());
		ChannelEvent responseEvent = new DownstreamMessageEvent(channel, channelFuture, lostVictoryMessage, channel.getRemoteAddress());
		ctx.sendDownstream(responseEvent);
		
		super.messageReceived(ctx, e);
	}


	
}
