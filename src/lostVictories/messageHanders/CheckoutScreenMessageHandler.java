package lostVictories.messageHanders;

import java.util.Set;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CheckoutScreenRequest;
import com.jme3.lostVictories.network.messages.CheckoutScreenResponse;
import com.jme3.lostVictories.network.messages.LostVictoryMessage;
import com.jme3.lostVictories.network.messages.Vector;

import lostVictories.CharacterDAO;

public class CheckoutScreenMessageHandler{
	
	public static Long CLIENT_RANGE = 800l;

	private CharacterDAO characterDAO;

	public CheckoutScreenMessageHandler(CharacterDAO characterDAO) {
		this.characterDAO = characterDAO;
	}

	public LostVictoryMessage handle(CheckoutScreenRequest m) {
		CharacterMessage avatar = characterDAO.getCharacter(m.avatar);
		Vector l = avatar.getLocation();
		Set<CharacterMessage> allCharacters = characterDAO.getAllCharacters(l.x, l.y, l.z, CLIENT_RANGE);
		return new CheckoutScreenResponse(allCharacters);
	}

}
