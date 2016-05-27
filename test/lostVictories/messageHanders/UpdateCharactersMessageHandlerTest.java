package lostVictories.messageHanders;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import lostVictories.WorldRunner;
import lostVictories.dao.CharacterDAO;
import lostVictories.dao.EquipmentDAO;
import lostVictories.dao.GameStatusDAO;
import lostVictories.dao.HouseDAO;

import org.elasticsearch.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.network.messages.UpdateCharactersRequest;
import com.jme3.lostVictories.network.messages.UpdateCharactersResponse;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.actions.Action;


public class UpdateCharactersMessageHandlerTest {

	private CharacterDAO characterDAO;
	private UpdateCharactersMessageHandler handler;

	@Before
	public void setUp(){
		characterDAO = mock(CharacterDAO.class);
		handler = new UpdateCharactersMessageHandler(characterDAO, mock(HouseDAO.class), mock(EquipmentDAO.class), mock(WorldRunner.class));
	}
	
	class IsSetOfElements extends ArgumentMatcher<Set> {
		int size;
		public IsSetOfElements(int s) {
			size = s;
		}
	     public boolean matches(Object set) {
	         return ((Set) set).size() == size;
	     }
	 }
	
	@Test
	public void testUpdateAllVacantCharacter() throws IOException {
		UUID clientID = UUID.randomUUID();
		
		HashMap<UUID, CharacterMessage> storedValues = new HashMap<UUID, CharacterMessage>();
		HashSet<CharacterMessage> inRange = new HashSet<>();
		CharacterMessage c1 = putCharacter(storedValues, inRange, new Vector(2, 2, 2), new Vector(1, 0, 0), Action.idle());
		CharacterMessage c2 = putCharacter(storedValues, inRange, new Vector(3, 3, 3), new Vector(1, 0, 0), Action.idle());
		when(characterDAO.getCharacter(c1.getId())).thenReturn(c1);
		when(characterDAO.getAllCharacters(argThat(new IsSetOfElements(2)))).thenReturn(storedValues);
		when(characterDAO.getAllCharacters(2.1f, 2, 2, CheckoutScreenMessageHandler.CLIENT_RANGE)).thenReturn(inRange);
		
		HashSet<CharacterMessage> characters = new HashSet<CharacterMessage>();
		CharacterMessage cc3 = getCharacterSource(c1.getId(), new Vector(2.1f, 2, 2), new Vector(0, 0, 1), Action.move());
		characters.add(cc3);
		characters.add(getCharacterSource(c2.getId(), new Vector(3.1f, 3, 3), new Vector(0, 0, 1), Action.move()));
		UpdateCharactersResponse handle = (UpdateCharactersResponse) handler.handle(new UpdateCharactersRequest(clientID, characters, cc3));
		
		assertEquals(2, handle.getCharacters().size());
		Map<UUID, CharacterMessage> ret = handle.getCharacters().stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
		CharacterMessage first = ret.get(c1.getId());
		assertEquals(new Vector(2.1f, 2, 2), first.getLocation());
		assertEquals(new Vector(0, 0, 1), first.getOrientation());
		assertEquals(Action.move(), first.getActions().iterator().next());
		assertEquals(clientID, first.getCheckoutClient());
	}

	private CharacterMessage putCharacter(HashMap<UUID, CharacterMessage> storedValues, HashSet<CharacterMessage> inRange, Vector location, Vector orientation, Action action) {
		UUID c1 = UUID.randomUUID();
		CharacterMessage cc1 = getCharacterSource(c1, location, orientation, action);
		storedValues.put(c1, cc1);
		inRange.add(cc1);
		return cc1;
	}

	private CharacterMessage putCharacter(UUID id, HashMap<UUID, CharacterMessage> storedValues, HashSet<CharacterMessage> inRange, Vector location, Vector orientation, Action action) {
		CharacterMessage cc1 = getCharacterSource(id, location, orientation, action);
		storedValues.put(id, cc1);
		inRange.add(cc1);
		return cc1;
	}

	private CharacterMessage getCharacterSource(UUID id, Vector location, Vector orientation, Action action) {
		CharacterMessage c = new CharacterMessage(id, null, location, null, null, RankMessage.CADET_CORPORAL, null, false);
		c.setOrientation(orientation);
		c.setActions(ImmutableSet.of(action));
		return c;
	}
	
	@Test
	public void testDoesNotOverideCheckedOutCharacters() throws IOException{
		UUID clientID = UUID.randomUUID();
		UUID clientID2 = UUID.randomUUID();
		
		HashMap<UUID, CharacterMessage> storedValues = new HashMap<UUID, CharacterMessage>();
		HashSet<CharacterMessage> inRange = new HashSet<>();
		
		CharacterMessage d1 = putCharacter(storedValues, inRange, new Vector(2, 2, 2), new Vector(1, 0, 0), Action.idle());
		CharacterMessage d2 = putCharacter(storedValues, inRange, new Vector(3, 3, 3), new Vector(1, 0, 0), Action.idle());
		d2.setCheckoutClient(clientID2);
		d2.setCheckoutTime(System.currentTimeMillis());
		when(characterDAO.getCharacter(d1.getId())).thenReturn(d1);
		when(characterDAO.getAllCharacters(argThat(new IsSetOfElements(2)))).thenReturn(storedValues);
		when(characterDAO.getAllCharacters(2.2f, 2, 2, CheckoutScreenMessageHandler.CLIENT_RANGE)).thenReturn(inRange);
		
		HashSet<CharacterMessage> characters = new HashSet<CharacterMessage>();
		characters.add(getCharacterSource(d1.getId(), new Vector(2.2f, 2, 2), new Vector(0, 0, 1), Action.move()));
		characters.add(getCharacterSource(d2.getId(), new Vector(3.1f, 3, 3), new Vector(0, 0, 1), Action.move()));
		UpdateCharactersResponse handle = (UpdateCharactersResponse) handler.handle(new UpdateCharactersRequest(clientID, characters, d1));
		
		assertEquals(2, handle.getCharacters().size());
		Map<UUID, CharacterMessage> ret = handle.getCharacters().stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
		CharacterMessage first = ret.get(d1.getId());
		assertEquals(new Vector(2.2f, 2, 2), first.getLocation());
		assertEquals(new Vector(0, 0, 1), first.getOrientation());
		assertEquals(Action.move(), first.getActions().iterator().next());
		assertEquals(clientID, first.getCheckoutClient());
		
		CharacterMessage second = ret.get(d2.getId());
		assertEquals(new Vector(3, 3, 3), second.getLocation());
		assertEquals(new Vector(1, 0, 0), second.getOrientation());
		assertEquals(Action.idle(), second.getActions().iterator().next());
		assertEquals(clientID2, second.getCheckoutClient());
	}
	
	@Test 
	public void testOveridesStaleCheckedoutCharacters() throws IOException{
		UUID clientID = UUID.randomUUID();
		UUID clientID2 = UUID.randomUUID();
		
		HashMap<UUID, CharacterMessage> storedValues = new HashMap<UUID, CharacterMessage>();
		HashSet<CharacterMessage> inRange = new HashSet<>();
		CharacterMessage cc1 = putCharacter(storedValues, inRange, new Vector(2, 2, 2), new Vector(1, 0, 0), Action.idle());
		CharacterMessage cc2 = putCharacter(storedValues, inRange, new Vector(4, 4, 4), new Vector(1, 0, 0), Action.idle());
		cc2.setCheckoutClient(clientID2);
		cc2.setCheckoutTime(System.currentTimeMillis()-(CharacterMessage.CHECKOUT_TIMEOUT*2));
		
		when(characterDAO.getCharacter(cc1.getId())).thenReturn(cc1);
		when(characterDAO.getAllCharacters(argThat(new IsSetOfElements(2)))).thenReturn(storedValues);
		when(characterDAO.getAllCharacters(2.2f, 2, 2, CheckoutScreenMessageHandler.CLIENT_RANGE)).thenReturn(inRange);
		
		HashSet<CharacterMessage> characters = new HashSet<CharacterMessage>();
		characters.add(getCharacterSource(cc1.getId(), new Vector(2.2f, 2, 2), new Vector(0, 0, 1), Action.move()));
		characters.add(getCharacterSource(cc2.getId(), new Vector(4.1f, 4, 4), new Vector(0, 0, 1), Action.move()));
		UpdateCharactersResponse handle = (UpdateCharactersResponse) handler.handle(new UpdateCharactersRequest(clientID, characters, cc1));
		
		assertEquals(2, handle.getCharacters().size());
		Map<UUID, CharacterMessage> ret = handle.getCharacters().stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
		CharacterMessage first = ret.get(cc1.getId());
		assertEquals(new Vector(2.2f, 2, 2), first.getLocation());
		assertEquals(new Vector(0, 0, 1), first.getOrientation());
		assertEquals(Action.move(), first.getActions().iterator().next());
		assertEquals(clientID, first.getCheckoutClient());
		
		CharacterMessage second = ret.get(cc2.getId());
		assertEquals(new Vector(4.1f, 4, 4), second.getLocation());
		assertEquals(new Vector(0, 0, 1), second.getOrientation());
		assertEquals(Action.move(), second.getActions().iterator().next());
		assertEquals(clientID, second.getCheckoutClient());
	}
	
	@Test
	public void testPlayerWondersIntoViewPort() throws IOException{
		UUID clientID = UUID.randomUUID();
		UUID clientID2 = UUID.randomUUID();
		
		UUID c1 = UUID.randomUUID();
		UUID c2 = UUID.randomUUID();
		UUID c3 = UUID.randomUUID();
		HashMap<UUID, CharacterMessage> storedValues = new HashMap<UUID, CharacterMessage>();
		CharacterMessage cc1 = getCharacterSource(c1, new Vector(2, 2, 2), new Vector(1, 0, 0), Action.idle());
		CharacterMessage cc2 = getCharacterSource(c2, new Vector(4, 4, 4), new Vector(1, 0, 0), Action.idle());
		cc2.setCheckoutClient(clientID2);
		cc2.setCheckoutTime(System.currentTimeMillis()-(CharacterMessage.CHECKOUT_TIMEOUT*2));
		CharacterMessage cc3 = getCharacterSource(c3, new Vector(6, 6, 6), new Vector(0, 0, 1), Action.move());
		cc3.setCheckoutClient(clientID2);
		storedValues.put(c1, cc1);
		storedValues.put(c2, cc2);
		
		when(characterDAO.getCharacter(cc1.getId())).thenReturn(cc1);
		when(characterDAO.getAllCharacters(argThat(new IsSetOfElements(2)))).thenReturn(storedValues);
		HashSet<CharacterMessage> inRange = new HashSet<>();
		inRange.add(cc1);
		inRange.add(cc2);
		inRange.add(cc3);
		when(characterDAO.getAllCharacters(2.2f, 2, 2, CheckoutScreenMessageHandler.CLIENT_RANGE)).thenReturn(inRange);
		
		HashSet<CharacterMessage> characters = new HashSet<CharacterMessage>();
		characters.add(getCharacterSource(c1, new Vector(2.2f, 2, 2), new Vector(0, 0, 1), Action.move()));
		characters.add(getCharacterSource(c2, new Vector(4.1f, 4, 4), new Vector(0, 0, 1), Action.move()));
		UpdateCharactersResponse handle = (UpdateCharactersResponse) handler.handle(new UpdateCharactersRequest(clientID, characters, cc1));
		
		assertEquals(3, handle.getCharacters().size());
		Map<UUID, CharacterMessage> ret = handle.getCharacters().stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
		CharacterMessage first = ret.get(c1);
		assertEquals(new Vector(2.2f, 2, 2), first.getLocation());
		assertEquals(new Vector(0, 0, 1), first.getOrientation());
		assertEquals(Action.move(), first.getActions().iterator().next());
		assertEquals(clientID, first.getCheckoutClient());
		
		CharacterMessage second = ret.get(c2);
		assertEquals(new Vector(4.1f, 4, 4), second.getLocation());
		assertEquals(new Vector(0, 0, 1), second.getOrientation());
		assertEquals(Action.move(), second.getActions().iterator().next());
		assertEquals(clientID, second.getCheckoutClient());

		CharacterMessage third = ret.get(c3);
		assertEquals(new Vector(6, 6, 6), third.getLocation());
		assertEquals(new Vector(0, 0, 1), third.getOrientation());
		assertEquals(Action.move(), third.getActions().iterator().next());
		assertEquals(clientID2, third.getCheckoutClient());
	}
	
	@Test
	public void testPlayerWondersOutOfViewPort() throws IOException{
		UUID clientID = UUID.randomUUID();
		UUID clientID2 = UUID.randomUUID();
		
		UUID c1 = UUID.randomUUID();
		UUID c2 = UUID.randomUUID();
		UUID c3 = UUID.randomUUID();
		HashMap<UUID, CharacterMessage> storedValues = new HashMap<UUID, CharacterMessage>();
		CharacterMessage cc1 = getCharacterSource(c1, new Vector(2, 2, 2), new Vector(1, 0, 0), Action.idle());
		CharacterMessage cc2 = getCharacterSource(c2, new Vector(4, 4, 4), new Vector(1, 0, 0), Action.idle());
		CharacterMessage cc3 = getCharacterSource(c3, new Vector(6, 6, 6), new Vector(0, 0, 1), Action.move());
		
		storedValues.put(c1, cc1);
		storedValues.put(c2, cc2);
		storedValues.put(c3, cc3);
		when(characterDAO.getCharacter(cc1.getId())).thenReturn(cc1);
		when(characterDAO.getAllCharacters(argThat(new IsSetOfElements(3)))).thenReturn(storedValues);
		HashSet<CharacterMessage> inRange = new HashSet<>();
		inRange.add(cc1);
		inRange.add(cc2);
		when(characterDAO.getAllCharacters(2.2f, 2, 2, CheckoutScreenMessageHandler.CLIENT_RANGE)).thenReturn(inRange);
		
		HashSet<CharacterMessage> characters = new HashSet<CharacterMessage>();
		characters.add(getCharacterSource(c1, new Vector(2.2f, 2, 2), new Vector(0, 0, 1), Action.move()));
		characters.add(getCharacterSource(c2, new Vector(4.1f, 4, 4), new Vector(0, 0, 1), Action.move()));
		characters.add(getCharacterSource(c3, new Vector(6.1f, 6, 6), new Vector(0, 0, 1), Action.move()));
		UpdateCharactersResponse handle = (UpdateCharactersResponse) handler.handle(new UpdateCharactersRequest(clientID, characters, cc1));
		
		assertEquals(2, handle.getCharacters().size());
		Map<UUID, CharacterMessage> ret = handle.getCharacters().stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
		CharacterMessage first = ret.get(c1);
		assertEquals(new Vector(2.2f, 2, 2), first.getLocation());
		assertEquals(new Vector(0, 0, 1), first.getOrientation());
		assertEquals(Action.move(), first.getActions().iterator().next());
		assertEquals(clientID, first.getCheckoutClient());
		
		CharacterMessage second = ret.get(c2);
		assertEquals(new Vector(4.1f, 4, 4), second.getLocation());
		assertEquals(new Vector(0, 0, 1), second.getOrientation());
		assertEquals(Action.move(), second.getActions().iterator().next());
		assertEquals(clientID, second.getCheckoutClient());

	}
	
	@Test
	public void testAvatarOfClientIsOveriddenInLocalCheckout() throws IOException{
		UUID clientID1 = UUID.randomUUID();
		UUID clientID2 = UUID.randomUUID();
		
		HashMap<UUID, CharacterMessage> storedValues = new HashMap<UUID, CharacterMessage>();
		HashSet<CharacterMessage> inRange = new HashSet<>();
		CharacterMessage cc1 = putCharacter(clientID1, storedValues, inRange, new Vector(2, 2, 2), new Vector(1, 0, 0), Action.idle());
		CharacterMessage cc2 = putCharacter(clientID2, storedValues, inRange, new Vector(4, 4, 4), new Vector(1, 0, 0), Action.idle());
		cc1.setCheckoutClient(clientID1);
		cc2.setCheckoutClient(clientID1);
		cc1.setCheckoutTime(System.currentTimeMillis());
		cc2.setCheckoutTime(System.currentTimeMillis());
		when(characterDAO.getCharacter(cc1.getId())).thenReturn(cc1);
		when(characterDAO.getAllCharacters(argThat(new IsSetOfElements(2)))).thenReturn(storedValues);
		when(characterDAO.getAllCharacters(2.2f, 2, 2, CheckoutScreenMessageHandler.CLIENT_RANGE)).thenReturn(inRange);
		
		HashSet<CharacterMessage> characters1 = new HashSet<CharacterMessage>();
		characters1.add(getCharacterSource(cc1.getId(), new Vector(2.2f, 2, 2), new Vector(0, 0, 1), Action.move()));
		characters1.add(getCharacterSource(cc2.getId(), new Vector(4.1f, 4, 4), new Vector(0, 0, 1), Action.move()));
		UpdateCharactersResponse handle1 = (UpdateCharactersResponse) handler.handle(new UpdateCharactersRequest(clientID1, characters1, cc1));
		HashSet<CharacterMessage> characters2 = new HashSet<CharacterMessage>();
		characters2.add(getCharacterSource(cc1.getId(), new Vector(3.2f, 2, 2), new Vector(0, 0, 1), Action.idle()));
		characters2.add(getCharacterSource(cc2.getId(), new Vector(5.1f, 4, 4), new Vector(0, 0, 1), Action.idle()));
		UpdateCharactersResponse handle2 = (UpdateCharactersResponse) handler.handle(new UpdateCharactersRequest(clientID2, characters2, cc1));
		
		assertEquals(2, handle2.getCharacters().size());
		Map<UUID, CharacterMessage> ret = handle2.getCharacters().stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
		CharacterMessage first = ret.get(cc1.getId());
		assertEquals(new Vector(2.2f, 2, 2), first.getLocation());
		assertEquals(Action.move(), first.getActions().iterator().next());
		assertEquals(clientID1, first.getCheckoutClient());
		
		CharacterMessage second = ret.get(cc2.getId());
		assertEquals(new Vector(5.1f, 4, 4), second.getLocation());
		assertEquals(Action.idle(), second.getActions().iterator().next());
		assertEquals(clientID2, second.getCheckoutClient());
	}
	
	

}
