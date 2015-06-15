package lostVictories.messageHanders;

import java.util.Set;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CheckoutScreenRequest;
import com.jme3.lostVictories.network.messages.CheckoutScreenResponse;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;

import lostVictories.CharacterDAO;

public class CheckoutScreenMessageHandler{
	
	public static Long CLIENT_RANGE = 400l;

	private CharacterDAO characterDAO;

	public CheckoutScreenMessageHandler(CharacterDAO characterDAO) {
		this.characterDAO = characterDAO;
	}

	public LostVictoryMessage handle(CheckoutScreenRequest m) {
		Set<CharacterMessage> allCharacters = characterDAO.getAllCharacters(m.x, m.y, m.z, CLIENT_RANGE);
		return new CheckoutScreenResponse(allCharacters);
	}

}
