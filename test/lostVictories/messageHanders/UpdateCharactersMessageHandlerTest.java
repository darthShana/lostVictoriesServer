package lostVictories.messageHanders;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import lostVictories.CharacterDAO;

import org.junit.Before;
import org.junit.Test;

import com.jme3.lostVictories.network.messages.Action;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CheckoutScreenRequest;
import com.jme3.lostVictories.network.messages.CheckoutScreenResponse;
import com.jme3.lostVictories.network.messages.UpdateCharactersRequest;
import com.jme3.lostVictories.network.messages.Vector;


public class UpdateCharactersMessageHandlerTest {

	private CharacterDAO characterDAO;
	private UpdateCharactersMessageHandler handler;

	@Before
	public void setUp(){
		characterDAO = mock(CharacterDAO.class);
		handler = new UpdateCharactersMessageHandler(characterDAO);
	}
	
	@Test
	public void testUpdateAllVacantCharacter() {
		UUID clientID = UUID.randomUUID();
		
		UUID c1 = UUID.randomUUID();
		UUID c2 = UUID.randomUUID();
		HashMap<UUID, CharacterMessage> storedValues = new HashMap<UUID, CharacterMessage>();
		storedValues.put(c1, getCharacterSource(c1, new Vector(2, 2, 2), Math.PI/2, Action.IDLE));
		storedValues.put(c2, getCharacterSource(c2, new Vector(3, 3, 3), Math.PI/2, Action.IDLE));
		when(characterDAO.getAllCharacters(anySet())).thenReturn(storedValues);
		
		HashSet<CharacterMessage> characters = new HashSet<CharacterMessage>();
		CharacterMessage cc2 = getCharacterSource(c1, new Vector(2.1, 2, 2), Math.PI, Action.WALK);
		characters.add(cc2);
		characters.add(getCharacterSource(c2, new Vector(3.1, 3, 3), Math.PI, Action.WALK));
		CheckoutScreenResponse handle = (CheckoutScreenResponse) handler.handle(new UpdateCharactersRequest(clientID, characters, cc2));
		
		assertEquals(2, handle.getCharacters().size());
		Map<UUID, CharacterMessage> ret = handle.getCharacters().stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
		CharacterMessage first = ret.get(c1);
		assertEquals(new Vector(2.1, 2, 2), first.getLocation());
		assertTrue(Math.PI-first.getOrientation()<0.001);
		assertEquals(Action.WALK, first.getAction());
		assertEquals(clientID, first.getCheckoutClient());
	}

	private CharacterMessage getCharacterSource(UUID id, Vector location, double orientation, Action action) {
		CharacterMessage c = new CharacterMessage(id, null, location, null, null, null, null, false);
		c.setOrientation(orientation);
		c.setAction(action);
		return c;
	}
	
	@Test
	public void testDoesNotOverideCheckedOutCharacters(){
		UUID clientID = UUID.randomUUID();
		UUID clientID2 = UUID.randomUUID();
		
		UUID c1 = UUID.randomUUID();
		UUID c2 = UUID.randomUUID();
		HashMap<UUID, CharacterMessage> storedValues = new HashMap<UUID, CharacterMessage>();
		CharacterMessage cc1 = getCharacterSource(c1, new Vector(2, 2, 2), Math.PI/2, Action.IDLE);
		CharacterMessage cc2 = getCharacterSource(c2, new Vector(3, 3, 3), Math.PI/2, Action.IDLE);
		cc2.setCheckoutClient(clientID2);
		cc2.setCheckoutTime(System.currentTimeMillis());
		storedValues.put(c1, cc1);
		storedValues.put(c2, cc2);
		when(characterDAO.getAllCharacters(anySet())).thenReturn(storedValues);
		
		HashSet<CharacterMessage> characters = new HashSet<CharacterMessage>();
		characters.add(getCharacterSource(c1, new Vector(2.2, 2, 2), Math.PI, Action.SHOOT));
		characters.add(getCharacterSource(c2, new Vector(3.1, 3, 3), Math.PI, Action.SHOOT));
		CheckoutScreenResponse handle = (CheckoutScreenResponse) handler.handle(new UpdateCharactersRequest(clientID, characters, cc1));
		
		assertEquals(2, handle.getCharacters().size());
		Map<UUID, CharacterMessage> ret = handle.getCharacters().stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
		CharacterMessage first = ret.get(c1);
		assertEquals(new Vector(2.2, 2, 2), first.getLocation());
		assertTrue(Math.PI-first.getOrientation()<0.001);
		assertEquals(Action.SHOOT, first.getAction());
		assertEquals(clientID, first.getCheckoutClient());
		
		CharacterMessage second = ret.get(c2);
		assertEquals(new Vector(3, 3, 3), second.getLocation());
		assertTrue(Math.PI/2-second.getOrientation()<0.001);
		assertEquals(Action.IDLE, second.getAction());
		assertEquals(clientID2, second.getCheckoutClient());
	}
	
	@Test 
	public void testOveridesStaleCheckedoutCharacters(){
		UUID clientID = UUID.randomUUID();
		UUID clientID2 = UUID.randomUUID();
		
		UUID c1 = UUID.randomUUID();
		UUID c2 = UUID.randomUUID();
		HashMap<UUID, CharacterMessage> storedValues = new HashMap<UUID, CharacterMessage>();
		CharacterMessage cc1 = getCharacterSource(c1, new Vector(2, 2, 2), Math.PI/2, Action.IDLE);
		CharacterMessage cc2 = getCharacterSource(c2, new Vector(4, 4, 4), Math.PI/2, Action.IDLE);
		cc2.setCheckoutClient(clientID2);
		cc2.setCheckoutTime(System.currentTimeMillis()-(CharacterMessage.CHECKOUT_TIMEOUT*2));
		storedValues.put(c1, cc1);
		storedValues.put(c2, cc2);
		when(characterDAO.getAllCharacters(anySet())).thenReturn(storedValues);
		
		HashSet<CharacterMessage> characters = new HashSet<CharacterMessage>();
		characters.add(getCharacterSource(c1, new Vector(2.2, 2, 2), Math.PI, Action.SHOOT));
		characters.add(getCharacterSource(c2, new Vector(4.1, 4, 4), Math.PI, Action.SHOOT));
		CheckoutScreenResponse handle = (CheckoutScreenResponse) handler.handle(new UpdateCharactersRequest(clientID, characters, cc1));
		
		assertEquals(2, handle.getCharacters().size());
		Map<UUID, CharacterMessage> ret = handle.getCharacters().stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
		CharacterMessage first = ret.get(c1);
		assertEquals(new Vector(2.2, 2, 2), first.getLocation());
		assertTrue(Math.PI-first.getOrientation()<0.001);
		assertEquals(Action.SHOOT, first.getAction());
		assertEquals(clientID, first.getCheckoutClient());
		
		CharacterMessage second = ret.get(c2);
		assertEquals(new Vector(4.1, 4, 4), second.getLocation());
		assertTrue(Math.PI-second.getOrientation()<0.001);
		assertEquals(Action.SHOOT, second.getAction());
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
		CharacterMessage cc1 = getCharacterSource(c1, new Vector(2, 2, 2), Math.PI/2, Action.IDLE);
		CharacterMessage cc2 = getCharacterSource(c2, new Vector(4, 4, 4), Math.PI/2, Action.IDLE);
		cc2.setCheckoutClient(clientID2);
		cc2.setCheckoutTime(System.currentTimeMillis()-(CharacterMessage.CHECKOUT_TIMEOUT*2));
		CharacterMessage cc3 = getCharacterSource(c3, new Vector(6, 6, 6), Math.PI, Action.WALK);
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
		characters.add(getCharacterSource(c1, new Vector(2.2, 2, 2), Math.PI, Action.SHOOT));
		characters.add(getCharacterSource(c2, new Vector(4.1, 4, 4), Math.PI, Action.SHOOT));
		CheckoutScreenResponse handle = (CheckoutScreenResponse) handler.handle(new UpdateCharactersRequest(clientID, characters, cc1));
		
		assertEquals(3, handle.getCharacters().size());
		Map<UUID, CharacterMessage> ret = handle.getCharacters().stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
		CharacterMessage first = ret.get(c1);
		assertEquals(new Vector(2.2, 2, 2), first.getLocation());
		assertTrue(Math.PI-first.getOrientation()<0.001);
		assertEquals(Action.SHOOT, first.getAction());
		assertEquals(clientID, first.getCheckoutClient());
		
		CharacterMessage second = ret.get(c2);
		assertEquals(new Vector(4.1, 4, 4), second.getLocation());
		assertTrue(Math.PI-second.getOrientation()<0.001);
		assertEquals(Action.SHOOT, second.getAction());
		assertEquals(clientID, second.getCheckoutClient());

		CharacterMessage third = ret.get(c3);
		assertEquals(new Vector(6, 6, 6), third.getLocation());
		assertTrue(Math.PI-second.getOrientation()<0.001);
		assertEquals(Action.WALK, third.getAction());
		assertEquals(clientID2, third.getCheckoutClient());
	}
	
	

}
