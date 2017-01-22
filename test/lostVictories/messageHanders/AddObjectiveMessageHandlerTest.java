package lostVictories.messageHanders;

import static lostVictories.dao.CharacterDAO.MAPPER;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.UUID;

import lostVictories.dao.CharacterDAO;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.jme3.lostVictories.network.messages.AddObjectiveRequest;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.Country;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.Weapon;
import com.jme3.lostVictories.objectives.TravelObjective;

public class AddObjectiveMessageHandlerTest {

	@Test
	public void testHandle() throws JsonGenerationException, JsonMappingException, IOException {
		UUID characterID = UUID.randomUUID();
		UUID captureStructureID = UUID.randomUUID();
		CharacterMessage characterMessage = new CharacterMessage(characterID, CharacterType.SOLDIER, new Vector(0, 0, 0), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, UUID.randomUUID());
		characterMessage.addObjective(UUID.randomUUID(), "{\"class\":\"com.jme3.lostVictories.objectives.RemanVehicle\"}");
		characterMessage.addObjective(captureStructureID, "{\"structure\":\"fa076669-02b4-4cc5-b7fa-d79524471d17\",\"class\":\"com.jme3.lostVictories.objectives.CaptureStructure\"}");

		CharacterDAO characterDAO = mock(CharacterDAO.class);
		when(characterDAO.getCharacter(characterID)).thenReturn(characterMessage);
		
		AddObjectiveMessageHandler handler = new AddObjectiveMessageHandler(characterDAO);
		
		TravelObjective travel = new TravelObjective(new Vector(100, 0 , 100),  new Vector(0, 0, 0));
		UUID travelId = UUID.randomUUID();
		
		handler.handle(new AddObjectiveRequest(characterID, characterID, travelId, MAPPER.writeValueAsString(travel)));
		
		assertEquals(2, characterMessage.getObjectives().size());
		assertTrue(characterMessage.getObjectives().containsKey(travelId.toString()));
		assertFalse(characterMessage.getObjectives().containsKey(captureStructureID.toString()));
	}

}
