package lostVictories.messageHanders;

import java.util.Set;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CheckoutScreenRequest;
import com.jme3.lostVictories.network.messages.CheckoutScreenResponse;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;
import com.jme3.lostVictories.network.messages.Vector;

import lostVictories.CharacterDAO;
import lostVictories.HouseDAO;

public class CheckoutScreenMessageHandler{
	
	public static Long CLIENT_RANGE = 800l;

	private CharacterDAO characterDAO;

	private HouseDAO houseDAO;

	public CheckoutScreenMessageHandler(CharacterDAO characterDAO, HouseDAO houseDAO) {
		this.characterDAO = characterDAO;
		this.houseDAO = houseDAO;
	}

	public LostVictoryMessage handle(CheckoutScreenRequest m) {
		CharacterMessage avatar = characterDAO.getCharacter(m.avatar);
		Vector l = avatar.getLocation();
		Set<CharacterMessage> allCharacters = characterDAO.getAllCharacters(l.x, l.y, l.z, CLIENT_RANGE);
		Set<HouseMessage> allHouses = houseDAO.getAllHouses();
		return new CheckoutScreenResponse(allCharacters, allHouses);
	}

}
