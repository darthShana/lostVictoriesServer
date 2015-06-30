package lostVictories.messageHanders;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import lostVictories.CharacterDAO;
import lostVictories.HouseDAO;

import org.junit.Before;
import org.junit.Test;

import com.jme3.lostVictories.network.messages.Action;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.UpdateCharactersRequest;
import com.jme3.lostVictories.network.messages.UpdateCharactersResponse;
import com.jme3.lostVictories.network.messages.Vector;


public class UpdateCharactersMessageHandlerTest {

	private CharacterDAO characterDAO;
	private UpdateCharactersMessageHandler handler;

	@Before
	public void setUp(){
		characterDAO = mock(CharacterDAO.class);
		handler = new UpdateCharactersMessageHandler(characterDAO, mock(HouseDAO.class));
	}
	
	@Test
	public void testUpdateAllVacantCharacter() {
		UUID clientID = UUID.randomUUID();
		
		HashMap<UUID, CharacterMessage> storedValues = new HashMap<UUID, CharacterMessage>();
		HashSet<CharacterMessage> inRange = new HashSet<>();
		CharacterMessage c1 = putCharacter(storedValues, inRange, new Vector(2, 2, 2), new Vector(1, 0, 0), Action.IDLE);
		CharacterMessage c2 = putCharacter(storedValues, inRange, new Vector(3, 3, 3), new Vector(1, 0, 0), Action.IDLE);
		when(characterDAO.getAllCharacters(anySet())).thenReturn(storedValues);
		when(characterDAO.getAllCharacters(2.1f, 2, 2, CheckoutScreenMessageHandler.CLIENT_RANGE)).thenReturn(inRange);
		
		HashSet<CharacterMessage> characters = new HashSet<CharacterMessage>();
		CharacterMessage cc3 = getCharacterSource(c1.getId(), new Vector(2.1f, 2, 2), new Vector(0, 0, 1), Action.MOVE);
		characters.add(cc3);
		characters.add(getCharacterSource(c2.getId(), new Vector(3.1f, 3, 3), new Vector(0, 0, 1), Action.MOVE));
		UpdateCharactersResponse handle = (UpdateCharactersResponse) handler.handle(new UpdateCharactersRequest(clientID, characters, cc3));
		
		assertEquals(2, handle.getCharacters().size());
		Map<UUID, CharacterMessage> ret = handle.getCharacters().stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
		CharacterMessage first = ret.get(c1.getId());
		assertEquals(new Vector(2.1f, 2, 2), first.getLocation());
		assertEquals(new Vector(0, 0, 1), first.getOrientation());
		assertEquals(Action.MOVE, first.getAction());
		assertEquals(clientID, first.getCheckoutClient());
	}

	private CharacterMessage putCharacter(HashMap<UUID, CharacterMessage> storedValues, HashSet<CharacterMessage> inRange, Vector location, Vector orientation, Action action) {
		UUID c1 = UUID.randomUUID();
		CharacterMessage cc1 = getCharacterSource(c1, location, orientation, action);
		storedValues.put(c1, cc1);
		inRange.add(cc1);
		return cc1;
	}

	private CharacterMessage getCharacterSource(UUID id, Vector location, Vector orientation, Action action) {
		CharacterMessage c = new CharacterMessage(id, null, location, null, null, null, null, false);
		c.setOrientation(orientation);
		c.setAction(action);
		return c;
	}
	
	@Test
	public void testDoesNotOverideCheckedOutCharacters(){
		UUID clientID = UUID.randomUUID();
		UUID clientID2 = UUID.randomUUID();
		
		HashMap<UUID, CharacterMessage> storedValues = new HashMap<UUID, CharacterMessage>();
		HashSet<CharacterMessage> inRange = new HashSet<>();
		
		CharacterMessage d1 = putCharacter(storedValues, inRange, new Vector(2, 2, 2), new Vector(1, 0, 0), Action.IDLE);
		CharacterMessage d2 = putCharacter(storedValues, inRange, new Vector(3, 3, 3), new Vector(1, 0, 0), Action.IDLE);
		d2.setCheckoutClient(clientID2);
		d2.setCheckoutTime(System.currentTimeMillis());
		when(characterDAO.getAllCharacters(anySet())).thenReturn(storedValues);
		when(characterDAO.getAllCharacters(2.2f, 2, 2, CheckoutScreenMessageHandler.CLIENT_RANGE)).thenReturn(inRange);
		
		HashSet<CharacterMessage> characters = new HashSet<CharacterMessage>();
		characters.add(getCharacterSource(d1.getId(), new Vector(2.2f, 2, 2), new Vector(0, 0, 1), Action.MOVE));
		characters.add(getCharacterSource(d2.getId(), new Vector(3.1f, 3, 3), new Vector(0, 0, 1), Action.MOVE));
		UpdateCharactersResponse handle = (UpdateCharactersResponse) handler.handle(new UpdateCharactersRequest(clientID, characters, d1));
		
		assertEquals(2, handle.getCharacters().size());
		Map<UUID, CharacterMessage> ret = handle.getCharacters().stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
		CharacterMessage first = ret.get(d1.getId());
		assertEquals(new Vector(2.2f, 2, 2), first.getLocation());
		assertEquals(new Vector(0, 0, 1), first.getOrientation());
		assertEquals(Action.MOVE, first.getAction());
		assertEquals(clientID, first.getCheckoutClient());
		
		CharacterMessage second = ret.get(d2.getId());
		assertEquals(new Vector(3, 3, 3), second.getLocation());
		assertEquals(new Vector(1, 0, 0), second.getOrientation());
		assertEquals(Action.IDLE, second.getAction());
		assertEquals(clientID2, second.getCheckoutClient());
	}
	
	@Test 
	public void testOveridesStaleCheckedoutCharacters(){
		UUID clientID = UUID.randomUUID();
		UUID clientID2 = UUID.randomUUID();
		
		HashMap<UUID, CharacterMessage> storedValues = new HashMap<UUID, CharacterMessage>();
		HashSet<CharacterMessage> inRange = new HashSet<>();
		CharacterMessage cc1 = putCharacter(storedValues, inRange, new Vector(2, 2, 2), new Vector(1, 0, 0), Action.IDLE);
		CharacterMessage cc2 = putCharacter(storedValues, inRange, new Vector(4, 4, 4), new Vector(1, 0, 0), Action.IDLE);
		cc2.setCheckoutClient(clientID2);
		cc2.setCheckoutTime(System.currentTimeMillis()-(CharacterMessage.CHECKOUT_TIMEOUT*2));
		when(characterDAO.getAllCharacters(anySet())).thenReturn(storedValues);
		when(characterDAO.getAllCharacters(2.2f, 2, 2, CheckoutScreenMessageHandler.CLIENT_RANGE)).thenReturn(inRange);
		
		HashSet<CharacterMessage> characters = new HashSet<CharacterMessage>();
		characters.add(getCharacterSource(cc1.getId(), new Vector(2.2f, 2, 2), new Vector(0, 0, 1), Action.MOVE));
		characters.add(getCharacterSource(cc2.getId(), new Vector(4.1f, 4, 4), new Vector(0, 0, 1), Action.MOVE));
		UpdateCharactersResponse handle = (UpdateCharactersResponse) handler.handle(new UpdateCharactersRequest(clientID, characters, cc1));
		
		assertEquals(2, handle.getCharacters().size());
		Map<UUID, CharacterMessage> ret = handle.getCharacters().stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
		CharacterMessage first = ret.get(cc1.getId());
		assertEquals(new Vector(2.2f, 2, 2), first.getLocation());
		assertEquals(new Vector(0, 0, 1), first.getOrientation());
		assertEquals(Action.MOVE, first.getAction());
		assertEquals(clientID, first.getCheckoutClient());
		
		CharacterMessage second = ret.get(cc2.getId());
		assertEquals(new Vector(4.1f, 4, 4), second.getLocation());
		assertEquals(new Vector(0, 0, 1), second.getOrientation());
		assertEquals(Action.MOVE, second.getAction());
		assertEquals(clientID, second.getCheckoutClient());
	}
	
	@Test
	public void testPlayerWondersIntoViewPort(){
		UUID clientID = UUID.randomUUID();
		UUID clientID2 = UUID.randomUUID();
		
		UUID c1 = UUID.randomUUID();
		UUID c2 = UUID.randomUUID();
		UUID c3 = UUID.randomUUID();
		HashMap<UUID, CharacterMessage> storedValues = new HashMap<UUID, CharacterMessage>();
		CharacterMessage cc1 = getCharacterSource(c1, new Vector(2, 2, 2), new Vector(1, 0, 0), Action.IDLE);
		CharacterMessage cc2 = getCharacterSource(c2, new Vector(4, 4, 4), new Vector(1, 0, 0), Action.IDLE);
		cc2.setCheckoutClient(clientID2);
		cc2.setCheckoutTime(System.currentTimeMillis()-(CharacterMessage.CHECKOUT_TIMEOUT*2));
		CharacterMessage cc3 = getCharacterSource(c3, new Vector(6, 6, 6), new Vector(0, 0, 1), Action.MOVE);
		cc3.setCheckoutClient(clientID2);
		storedValues.put(c1, cc1);
		storedValues.put(c2, cc2);
		when(characterDAO.getAllCharacters(anySet())).thenReturn(storedValues);
		HashSet<CharacterMessage> inRange = new HashSet<>();
		inRange.add(cc1);
		inRange.add(cc2);
		inRange.add(cc3);
		when(characterDAO.getAllCharacters(2.2f, 2, 2, CheckoutScreenMessageHandler.CLIENT_RANGE)).thenReturn(inRange);
		
		HashSet<CharacterMessage> characters = new HashSet<CharacterMessage>();
		characters.add(getCharacterSource(c1, new Vector(2.2f, 2, 2), new Vector(0, 0, 1), Action.MOVE));
		characters.add(getCharacterSource(c2, new Vector(4.1f, 4, 4), new Vector(0, 0, 1), Action.MOVE));
		UpdateCharactersResponse handle = (UpdateCharactersResponse) handler.handle(new UpdateCharactersRequest(clientID, characters, cc1));
		
		assertEquals(3, handle.getCharacters().size());
		Map<UUID, CharacterMessage> ret = handle.getCharacters().stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
		CharacterMessage first = ret.get(c1);
		assertEquals(new Vector(2.2f, 2, 2), first.getLocation());
		assertEquals(new Vector(0, 0, 1), first.getOrientation());
		assertEquals(Action.MOVE, first.getAction());
		assertEquals(clientID, first.getCheckoutClient());
		
		CharacterMessage second = ret.get(c2);
		assertEquals(new Vector(4.1f, 4, 4), second.getLocation());
		assertEquals(new Vector(0, 0, 1), second.getOrientation());
		assertEquals(Action.MOVE, second.getAction());
		assertEquals(clientID, second.getCheckoutClient());

		CharacterMessage third = ret.get(c3);
		assertEquals(new Vector(6, 6, 6), third.getLocation());
		assertEquals(new Vector(0, 0, 1), third.getOrientation());
		assertEquals(Action.MOVE, third.getAction());
		assertEquals(clientID2, third.getCheckoutClient());
	}
	
	@Test
	public void testPlayerWondersOutOfViewPort(){
		UUID clientID = UUID.randomUUID();
		UUID clientID2 = UUID.randomUUID();
		
		UUID c1 = UUID.randomUUID();
		UUID c2 = UUID.randomUUID();
		UUID c3 = UUID.randomUUID();
		HashMap<UUID, CharacterMessage> storedValues = new HashMap<UUID, CharacterMessage>();
		CharacterMessage cc1 = getCharacterSource(c1, new Vector(2, 2, 2), new Vector(1, 0, 0), Action.IDLE);
		CharacterMessage cc2 = getCharacterSource(c2, new Vector(4, 4, 4), new Vector(1, 0, 0), Action.IDLE);
		CharacterMessage cc3 = getCharacterSource(c3, new Vector(6, 6, 6), new Vector(0, 0, 1), Action.MOVE);
		
		storedValues.put(c1, cc1);
		storedValues.put(c2, cc2);
		storedValues.put(c3, cc3);
		when(characterDAO.getAllCharacters(anySet())).thenReturn(storedValues);
		HashSet<CharacterMessage> inRange = new HashSet<>();
		inRange.add(cc1);
		inRange.add(cc2);
		when(characterDAO.getAllCharacters(2.2f, 2, 2, CheckoutScreenMessageHandler.CLIENT_RANGE)).thenReturn(inRange);
		
		HashSet<CharacterMessage> characters = new HashSet<CharacterMessage>();
		characters.add(getCharacterSource(c1, new Vector(2.2f, 2, 2), new Vector(0, 0, 1), Action.MOVE));
		characters.add(getCharacterSource(c2, new Vector(4.1f, 4, 4), new Vector(0, 0, 1), Action.MOVE));
		characters.add(getCharacterSource(c3, new Vector(6.1f, 6, 6), new Vector(0, 0, 1), Action.MOVE));
		UpdateCharactersResponse handle = (UpdateCharactersResponse) handler.handle(new UpdateCharactersRequest(clientID, characters, cc1));
		
		assertEquals(2, handle.getCharacters().size());
		Map<UUID, CharacterMessage> ret = handle.getCharacters().stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
		CharacterMessage first = ret.get(c1);
		assertEquals(new Vector(2.2f, 2, 2), first.getLocation());
		assertEquals(new Vector(0, 0, 1), first.getOrientation());
		assertEquals(Action.MOVE, first.getAction());
		assertEquals(clientID, first.getCheckoutClient());
		
		CharacterMessage second = ret.get(c2);
		assertEquals(new Vector(4.1f, 4, 4), second.getLocation());
		assertEquals(new Vector(0, 0, 1), second.getOrientation());
		assertEquals(Action.MOVE, second.getAction());
		assertEquals(clientID, second.getCheckoutClient());

	}
	
	

}
