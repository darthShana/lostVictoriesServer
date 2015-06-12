package lostVictories;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CheckoutScreenRequest;
import com.jme3.lostVictories.network.messages.CheckoutScreenResponse;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;
import com.jme3.lostVictories.network.messages.UpdateCharactersRequest;


public class MessageHandler extends SimpleChannelHandler {

	private static Logger log = Logger.getLogger(MessageHandler.class);
	private static Long CLIENT_RANGE = 400l;
	
	private CharacterDAO characterDAO;

	public MessageHandler(CharacterDAO characterDAO) {
		this.characterDAO = characterDAO;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		
		LostVictoryMessage msg = (LostVictoryMessage)e.getMessage();
		LostVictoryMessage lostVictoryMessage;
		
		if(msg instanceof CheckoutScreenRequest){
			CheckoutScreenRequest m = (CheckoutScreenRequest) msg;
			Set<CharacterMessage> allCharacters = characterDAO.getAllCharacters(m.x, m.y, m.z, CLIENT_RANGE);
			lostVictoryMessage = new CheckoutScreenResponse(allCharacters);
			log.info("returning scene");
		}else if(msg instanceof UpdateCharactersRequest){
			Set<CharacterMessage> allCharacters = ((UpdateCharactersRequest) msg).getCharacters();
			HashMap<UUID, CharacterMessage> existing = characterDAO.getAllCharacters(allCharacters.stream().map(c->c.getId()).collect(Collectors.toSet()));
			allCharacters = allCharacters.stream().filter(c->c.hasChanged(existing.get(c.getId()))).collect(Collectors.toSet());
			HashMap<UUID, CharacterMessage> sent = (HashMap<UUID, CharacterMessage>) allCharacters.stream().filter(c->c.isCheckedOutBy(msg.getClientID(), System.currentTimeMillis()) || c.isNotCheckedOut()).collect(Collectors.toMap(c->c.getId(), c->c));
			existing.values().stream().forEach(c->c.updateState(sent.get(c.getId()), msg.getClientID(), System.currentTimeMillis()));
			characterDAO.save(existing.values());
			lostVictoryMessage = new CheckoutScreenResponse(new HashSet<CharacterMessage>(existing.values()));
		} else{
			String newMessage = msg.getClientID() + " test 78";
			lostVictoryMessage = new LostVictoryMessage(newMessage);
			System.out.println("Hey Guys !  I got a date ! [" + msg.getClientID() + "] and I modified it to [" + newMessage + "]");
		}
		
		Channel channel = e.getChannel();
		ChannelFuture channelFuture = Channels.future(e.getChannel());
		ChannelEvent responseEvent = new DownstreamMessageEvent(channel, channelFuture, lostVictoryMessage, channel.getRemoteAddress());
		ctx.sendDownstream(responseEvent);
		
		super.messageReceived(ctx, e);
	}


	
}
