package lostVictories.messageHanders;

import java.util.Set;

import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CheckoutScreenRequest;
import com.jme3.lostVictories.network.messages.CheckoutScreenResponse;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;
import com.jme3.lostVictories.network.messages.Vector;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

public class CheckoutScreenMessageHandler{
	
	private static Logger log = Logger.getLogger(CheckoutScreenMessageHandler.class); 
	public static Long CLIENT_RANGE = 250l;
	private CharacterDAO characterDAO;
	private HouseDAO houseDAO;

	public CheckoutScreenMessageHandler(CharacterDAO characterDAO, HouseDAO houseDAO) {
		this.characterDAO = characterDAO;
		this.houseDAO = houseDAO;
	}

	public LostVictoryMessage handle(CheckoutScreenRequest m) {
		log.info("checking out scene for avatar:"+m.avatar);
		CharacterMessage avatar = characterDAO.getCharacter(m.avatar);
		if(avatar!=null){
			Vector l = avatar.getLocation();
	       	Set<CharacterMessage> allCharacters = characterDAO.getAllCharacters(l.x, l.y, l.z, CLIENT_RANGE);
			Set<HouseMessage> allHouses = houseDAO.getAllHouses();
			return new CheckoutScreenResponse(allCharacters, allHouses);
		}
		return new LostVictoryMessage(m.getClientID());
	}

}
