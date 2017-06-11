package lostVictories.messageHanders;

import static com.lostVictories.service.LostVictoriesService.bytes;
import static com.lostVictories.service.LostVictoriesService.uuid;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.jme3.lostVictories.network.messages.*;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.wrapper.RelatedCharacterStatusResponse;
import com.lostVictories.api.CharacterStatusResponse;
import com.lostVictories.api.LostVictoryMessage;
import com.lostVictories.api.UpdateCharactersRequest;
import com.lostVictories.service.CheckoutScreenMessageHandler;
import com.lostVictories.service.MessageMapper;
import com.lostVictories.service.SafeStreamObserver;
import com.lostVictories.service.UpdateCharactersMessageHandler;
import lostVictories.WorldRunner;
import lostVictories.dao.CharacterDAO;
import lostVictories.dao.EquipmentDAO;
import lostVictories.dao.HouseDAO;

import org.elasticsearch.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;

import com.jme3.lostVictories.network.messages.actions.Action;


public class UpdateCharactersMessageHandlerTest {

	private CharacterDAO characterDAO;
	private UpdateCharactersMessageHandler handler;
	private MessageMapper mm = new MessageMapper();

	@Before
	public void setUp(){
		characterDAO = mock(CharacterDAO.class);
		handler = new UpdateCharactersMessageHandler(characterDAO, mock(HouseDAO.class), mock(EquipmentDAO.class), mock(WorldRunner.class), new MessageRepository());
	}

	@Test
	public void testSimpleUpdateCharacter() throws IOException {
		UUID clientID = UUID.randomUUID();

		HashSet<CharacterMessage> inRange = new HashSet<>();
		CharacterMessage c1 = putCharacter(inRange, new Vector(2, 2, 2), new Vector(1, 0, 0), Action.idle(), null);
		when(characterDAO.getCharacter(eq(clientID))).thenReturn(getCharacterSource(clientID, new Vector(3,3, 3), new Vector(1, 0,0 ), Action.idle(), null));
		when(characterDAO.getAllCharacters(3, 3, 3, CheckoutScreenMessageHandler.CLIENT_RANGE)).thenReturn(inRange);

		CharacterMessage updatedCharacter = getCharacterSource(c1.getId(), new Vector(2.1f, 2, 2), new Vector(0, 0, 1), Action.idle(), null);
		updatedCharacter.setVersion(5);
		when(characterDAO.updateCharacterState(c1)).thenReturn(updatedCharacter);

		CharacterMessage cc3 = getCharacterSource(c1.getId(), new Vector(2.1f, 2, 2), new Vector(0, 0, 1), Action.move(), null);
        ArgumentCaptor<LostVictoryMessage> argument = ArgumentCaptor.forClass(LostVictoryMessage.class);
        SafeStreamObserver mock = mock(SafeStreamObserver.class);

        handler.handle(convertToCharacterMessage(cc3, clientID, 5000), mock, new HashSet<>());

        verify(mock).onNext(argument.capture());
		verify(characterDAO, times(1)).updateCharacterState(anyObject());

        CharacterStatusResponse next = argument.getValue().getCharacterStatusResponse();
		com.lostVictories.api.CharacterMessage next1 = next.getUnit();
		assertEquals(next1.getLocation(), com.lostVictories.api.Vector.newBuilder().setX(2.1f).setY(2).setZ(2).build());
		assertEquals(next1.getVersion(), 5);
	}

	@Test
	public void testDoesNotUpdateOODCharacterButReturnsNewVersion() throws IOException {
		UUID clientID = UUID.randomUUID();

		HashSet<CharacterMessage> inRange = new HashSet<>();
		CharacterMessage c1 = putCharacter(inRange, new Vector(2, 2, 2), new Vector(1, 0, 0), Action.idle(), null);
		c1.setVersion(7);
        when(characterDAO.getCharacter(eq(clientID))).thenReturn(getCharacterSource(clientID, new Vector(3,3, 3), new Vector(1, 0,0 ), Action.idle(), null));
		when(characterDAO.getAllCharacters(3, 3, 3, CheckoutScreenMessageHandler.CLIENT_RANGE)).thenReturn(inRange);

		CharacterMessage cc3 = getCharacterSource(c1.getId(), new Vector(2.1f, 2, 2), new Vector(0, 0, 1), Action.move(), null);
		cc3.setVersion(5);

        ArgumentCaptor<LostVictoryMessage> argument = ArgumentCaptor.forClass(LostVictoryMessage.class);
        SafeStreamObserver mock = mock(SafeStreamObserver.class);

        handler.handle(convertToCharacterMessage(cc3, clientID, 5000), mock, new HashSet<>());

        verify(mock).onNext(argument.capture());
        verify(characterDAO, times(0)).updateCharacterState(anyObject());
		CharacterStatusResponse next = argument.getAllValues().stream().filter(m->m.hasCharacterStatusResponse()).map(r->r.getCharacterStatusResponse()).findFirst().get();
		assertEquals(uuid(next.getUnit().getId()), c1.getId());
		assertEquals(c1.getLocation(), new Vector(next.getUnit().getLocation()));
	}

	@Test
	public void testReturnsOtherCharactersNearbyIfNotCheckedOut() throws IOException {
		UUID clientID = UUID.randomUUID();

		HashSet<CharacterMessage> inRange = new HashSet<>();
		CharacterMessage c1 = putCharacter(inRange, new Vector(2, 2, 2), new Vector(1, 0, 0), Action.idle(), null);
		CharacterMessage c2 = putCharacter(inRange, new Vector(2, 2, 2), new Vector(1, 0, 0), Action.idle(), null);

        CharacterMessage avatar = getCharacterSource(clientID, new Vector(3, 3, 3), new Vector(1, 0, 0), Action.idle(), null);
        when(characterDAO.getCharacter(eq(clientID))).thenReturn(avatar);

		c2.setCheckoutClient(UUID.randomUUID());
		c2.setCheckoutTime(System.currentTimeMillis()-5001);

        when(characterDAO.updateCharacterState(eq(c1))).thenReturn(c1);
        when(characterDAO.getAllCharacters(3, 3, 3, CheckoutScreenMessageHandler.CLIENT_RANGE)).thenReturn(inRange);

		CharacterMessage cc3 = getCharacterSource(c1.getId(), new Vector(2.1f, 2, 2), new Vector(0, 0, 1), Action.move(), null);

        ArgumentCaptor<LostVictoryMessage> argument1 = ArgumentCaptor.forClass(LostVictoryMessage.class);
        SafeStreamObserver mock1 = mock(SafeStreamObserver.class);

        handler.handle(convertToCharacterMessage(cc3, clientID, 5000), mock1, new HashSet<>());

        verify(mock1).onNext(argument1.capture());
        assertEquals(1, argument1.getAllValues().stream().filter(m->m.hasCharacterStatusResponse()).collect(Collectors.toSet()).size());

        ArgumentCaptor<LostVictoryMessage> argument2 = ArgumentCaptor.forClass(LostVictoryMessage.class);
        SafeStreamObserver mock2 = mock(SafeStreamObserver.class);

        handler.handle(convertToCharacterMessage(cc3, clientID, 5001), mock2, new HashSet<>());

        verify(mock2, times(2)).onNext(argument2.capture());

	}

	@Test
	public void testReturnsRelatedCharactersNotInAvatarsCheckout() throws IOException {
		UUID clientID = UUID.randomUUID();

		HashSet<CharacterMessage> inRange = new HashSet<>();
        CharacterMessage c3 = putCharacter(null, new Vector(2, 2, 2), new Vector(1, 0, 0), Action.idle(), null);
		CharacterMessage c1 = putCharacter(inRange, new Vector(2, 2, 2), new Vector(1, 0, 0), Action.idle(), c3.getId());
		CharacterMessage c4 = putCharacter(null, new Vector(2, 2, 2), new Vector(1, 0, 0), Action.idle(), null);
        CharacterMessage avatar = getCharacterSource(clientID, new Vector(3, 3, 3), new Vector(1, 0, 0), Action.idle(), null);

		c1.addCharactersUnderCommand(c4);

		when(characterDAO.getCharacter(eq(c3.getId()))).thenReturn(c3);
        when(characterDAO.getAllCharacters(eq(setOf(c4.getId())))).thenReturn(mapOf(c4));
		when(characterDAO.getAllCharacters(3, 3, 3, CheckoutScreenMessageHandler.CLIENT_RANGE)).thenReturn(inRange);

		CharacterMessage cc3 = getCharacterSource(c1.getId(), new Vector(2.1f, 2, 2), new Vector(0, 0, 1), Action.move(), null);
		when(characterDAO.getCharacter(eq(clientID))).thenReturn(avatar);
		when(characterDAO.updateCharacterState(eq(c1))).thenReturn(c1);

        ArgumentCaptor<LostVictoryMessage> argument = ArgumentCaptor.forClass(LostVictoryMessage.class);
        SafeStreamObserver mock = mock(SafeStreamObserver.class);

        handler.handle(convertToCharacterMessage(cc3, clientID, 5001), mock, new HashSet<>());
        verify(mock, times(3)).onNext(argument.capture());
        Set<UUID> collect = argument.getAllValues().stream().filter(m -> m.hasCharacterStatusResponse()).map(msg->uuid(msg.getCharacterStatusResponse().getUnit().getId())).collect(Collectors.toSet());
		assertEquals(1, collect.size());
		assertTrue(collect.contains(c1.getId()));

		Set<UUID> m2 = argument.getAllValues().stream().filter(m->m.hasRelatedCharacterStatusResponse()).map(m->uuid(m.getRelatedCharacterStatusResponse().getUnit().getId())).collect(Collectors.toSet());
		assertEquals(2, m2.size());
		assertTrue(m2.contains(c3.getId()));
		assertTrue(m2.contains(c4.getId()));

	}

	@Test
	public void testDoesNotReturnResponseIfCharacterIsOutOfRangeOfAvatar() throws IOException {
		UUID clientID = UUID.randomUUID();

		HashSet<CharacterMessage> inRange = new HashSet<>();
		CharacterMessage c1 = putCharacter(null, new Vector(600, 2, 2), new Vector(1, 0, 0), Action.idle(), null);
		CharacterMessage avatar = putCharacter(inRange, new Vector(2, 2, 2), new Vector(1, 0, 0), Action.idle(), null);

        when(characterDAO.getCharacter(eq(avatar.getId()))).thenReturn(avatar);
        when(characterDAO.getAllCharacters(eq(setOf(c1.getId())))).thenReturn(mapOf(c1));
		when(characterDAO.getAllCharacters(2, 2, 2, CheckoutScreenMessageHandler.CLIENT_RANGE)).thenReturn(inRange);
		CharacterMessage updatedCharacter = getCharacterSource(c1.getId(), new Vector(2.1f, 2, 2), new Vector(0, 0, 1), Action.idle(), null);
		updatedCharacter.setVersion(5);
		when(characterDAO.updateCharacterState(c1)).thenReturn(updatedCharacter);

		CharacterMessage cc3 = getCharacterSource(c1.getId(), new Vector(600.1f, 2, 2), new Vector(0, 0, 1), Action.move(), null);
        SafeStreamObserver mock = mock(SafeStreamObserver.class);
        handler.handle(convertToCharacterMessage(cc3, avatar.getId(), 5000), mock, new HashSet<>());

		verify(characterDAO, times(1)).updateCharacterState(anyObject());
		verify(mock, never()).onNext(anyObject());
	}

    private <T> Set<T> setOf(T... c1) {
	    Set<T> ret = new HashSet<T>();
	    for(T t:c1){
	        ret.add(t);
        }
        return ret;
    }

    private Map<UUID, CharacterMessage> mapOf(CharacterMessage... c1) {
        Map<UUID, CharacterMessage> ret = new HashMap<UUID, CharacterMessage>();
        for(CharacterMessage t:c1){
            ret.put(t.getId(), t);
        }
        return ret;
    }

    private UpdateCharactersRequest convertToCharacterMessage(com.jme3.lostVictories.network.messages.CharacterMessage cm, UUID avatar, long l) {

        com.lostVictories.api.CharacterMessage.Builder characterBuilder = com.lostVictories.api.CharacterMessage.newBuilder()
                .setId(bytes(cm.getId()))
                .setLocation(cm.getLocation().toMessage())
                .setOrientation(cm.getOrientation().toMessage())
                .putAllObjectives(cm.readObjectives()).addAllCompletedObjectives(cm.getAllCompletedObjectives().stream().map(oid-> bytes(UUID.fromString(oid))).collect(Collectors.toSet()))
                .setDead(cm.isDead())
                .setEngineDamaged(cm.hasEngineDamage())
                .setVersion(cm.getVersion());

        cm.getActions().forEach(a->{
            characterBuilder.addActions(a.toMessage());
        });

        UpdateCharactersRequest.Builder builder = UpdateCharactersRequest.newBuilder()
                .setClientID(bytes(avatar))
                .setAvatar(bytes(avatar))
                .setClientStartTime(l)
                .setCharacter(characterBuilder.build());
        return builder.build();
    }

//	@Test
//	public void testUpdateAllVacantCharacter() throws IOException {
//		UUID clientID = UUID.randomUUID();
//
//		HashMap<UUID, CharacterMessage> storedValues = new HashMap<UUID, CharacterMessage>();
//		HashSet<CharacterMessage> inRange = new HashSet<>();
//		CharacterMessage c1 = putCharacter(storedValues, inRange, new Vector(2, 2, 2), new Vector(1, 0, 0), Action.idle());
//		CharacterMessage c2 = putCharacter(storedValues, inRange, new Vector(3, 3, 3), new Vector(1, 0, 0), Action.idle());
//		when(characterDAO.getCharacter(c1.getId())).thenReturn(c1);
//		when(characterDAO.getAllCharacters(argThat(new IsSetOfElements(2)))).thenReturn(storedValues);
//		when(characterDAO.getAllCharacters(2.1f, 2, 2, CheckoutScreenMessageHandler.CLIENT_RANGE)).thenReturn(inRange);
//
//		HashSet<CharacterMessage> characters = new HashSet<CharacterMessage>();
//		CharacterMessage cc3 = getCharacterSource(c1.getId(), new Vector(2.1f, 2, 2), new Vector(0, 0, 1), Action.move());
//		characters.add(cc3);
//		characters.add(getCharacterSource(c2.getId(), new Vector(3.1f, 3, 3), new Vector(0, 0, 1), Action.move()));
//
//		Set<LostVictoryMessage> results = handler.handle(new UpdateCharactersRequest(clientID, characters, cc3.getId()));
//		CharacterStatusResponse handle = (CharacterStatusResponse) results.stream().filter(s->s instanceof  CharacterStatusResponse).findFirst().get();
//
//		assertEquals(2, handle.getCharacters().size());
//		Map<UUID, CharacterMessage> ret = handle.getCharacters().stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
//		CharacterMessage first = ret.get(c1.getId());
//		assertEquals(new Vector(2.1f, 2, 2), first.getLocation());
//		assertEquals(new Vector(0, 0, 1), first.getOrientation());
//		assertEquals(Action.move(), first.getActions().iterator().next());
//		assertEquals(clientID, first.getCheckoutClient());
//	}

	private CharacterMessage putCharacter(HashSet<CharacterMessage> inRange, Vector location, Vector orientation, Action action, UUID commandingOfficer) {
		UUID c1 = UUID.randomUUID();
		CharacterMessage cc1 = getCharacterSource(c1, location, orientation, action, commandingOfficer);

		if(inRange!=null) {
			inRange.add(cc1);
		}
		when(characterDAO.getCharacter(eq(cc1.getId()), eq(true))).thenReturn(cc1);
		return cc1;
	}

//	private CharacterMessage putCharacter(UUID id, HashMap<UUID, CharacterMessage> storedValues, HashSet<CharacterMessage> inRange, Vector location, Vector orientation, Action action) {
//		CharacterMessage cc1 = getCharacterSource(id, location, orientation, action);
//		storedValues.put(id, cc1);
//		inRange.add(cc1);
//		return cc1;
//	}

	private CharacterMessage getCharacterSource(UUID id, Vector location, Vector orientation, Action action, UUID commandingOfficer) {
		CharacterMessage c = new CharacterMessage(id, CharacterType.SOLDIER, location, Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, commandingOfficer);
		c.setOrientation(orientation);
		c.setActions(ImmutableSet.of(action));

		return c;
	}
	
//	@Test
//	public void testDoesNotOverideCheckedOutCharacters() throws IOException{
//		UUID clientID = UUID.randomUUID();
//		UUID clientID2 = UUID.randomUUID();
//
//		HashMap<UUID, CharacterMessage> storedValues = new HashMap<UUID, CharacterMessage>();
//		HashSet<CharacterMessage> inRange = new HashSet<>();
//
//		CharacterMessage d1 = putCharacter(storedValues, inRange, new Vector(2, 2, 2), new Vector(1, 0, 0), Action.idle());
//		CharacterMessage d2 = putCharacter(storedValues, inRange, new Vector(3, 3, 3), new Vector(1, 0, 0), Action.idle());
//		d2.setCheckoutClient(clientID2);
//		d2.setCheckoutTime(System.currentTimeMillis());
//		when(characterDAO.getCharacter(d1.getId())).thenReturn(d1);
//		when(characterDAO.getAllCharacters(argThat(new IsSetOfElements(2)))).thenReturn(storedValues);
//		when(characterDAO.getAllCharacters(2.2f, 2, 2, CheckoutScreenMessageHandler.CLIENT_RANGE)).thenReturn(inRange);
//
//		HashSet<CharacterMessage> characters = new HashSet<CharacterMessage>();
//		characters.add(getCharacterSource(d1.getId(), new Vector(2.2f, 2, 2), new Vector(0, 0, 1), Action.move()));
//		characters.add(getCharacterSource(d2.getId(), new Vector(3.1f, 3, 3), new Vector(0, 0, 1), Action.move()));
//
//		Set<LostVictoryMessage> results = handler.handle(new UpdateCharactersRequest(clientID, characters, d1.getId()));
//		CharacterStatusResponse handle = (CharacterStatusResponse) results.stream().filter(s->s instanceof  CharacterStatusResponse).findFirst().get();
//
//		assertEquals(2, handle.getCharacters().size());
//		Map<UUID, CharacterMessage> ret = handle.getCharacters().stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
//		CharacterMessage first = ret.get(d1.getId());
//		assertEquals(new Vector(2.2f, 2, 2), first.getLocation());
//		assertEquals(new Vector(0, 0, 1), first.getOrientation());
//		assertEquals(Action.move(), first.getActions().iterator().next());
//		assertEquals(clientID, first.getCheckoutClient());
//
//		CharacterMessage second = ret.get(d2.getId());
//		assertEquals(new Vector(3, 3, 3), second.getLocation());
//		assertEquals(new Vector(1, 0, 0), second.getOrientation());
//		assertEquals(Action.idle(), second.getActions().iterator().next());
//		assertEquals(clientID2, second.getCheckoutClient());
//	}
//
//	@Test
//	public void testOveridesStaleCheckedoutCharacters() throws IOException{
//		UUID clientID = UUID.randomUUID();
//		UUID clientID2 = UUID.randomUUID();
//
//		HashMap<UUID, CharacterMessage> storedValues = new HashMap<UUID, CharacterMessage>();
//		HashSet<CharacterMessage> inRange = new HashSet<>();
//		CharacterMessage cc1 = putCharacter(storedValues, inRange, new Vector(2, 2, 2), new Vector(1, 0, 0), Action.idle());
//		CharacterMessage cc2 = putCharacter(storedValues, inRange, new Vector(4, 4, 4), new Vector(1, 0, 0), Action.idle());
//		cc2.setCheckoutClient(clientID2);
//		cc2.setCheckoutTime(System.currentTimeMillis()-(CharacterMessage.CHECKOUT_TIMEOUT*2));
//
//		when(characterDAO.getCharacter(cc1.getId())).thenReturn(cc1);
//		when(characterDAO.getAllCharacters(argThat(new IsSetOfElements(2)))).thenReturn(storedValues);
//		when(characterDAO.getAllCharacters(2.2f, 2, 2, CheckoutScreenMessageHandler.CLIENT_RANGE)).thenReturn(inRange);
//
//		HashSet<CharacterMessage> characters = new HashSet<CharacterMessage>();
//		characters.add(getCharacterSource(cc1.getId(), new Vector(2.2f, 2, 2), new Vector(0, 0, 1), Action.move()));
//		characters.add(getCharacterSource(cc2.getId(), new Vector(4.1f, 4, 4), new Vector(0, 0, 1), Action.move()));
//
//		Set<LostVictoryMessage> results = handler.handle(new UpdateCharactersRequest(clientID, characters, cc1.getId()));
//		CharacterStatusResponse handle = (CharacterStatusResponse) results.stream().filter(s->s instanceof  CharacterStatusResponse).findFirst().get();
//
//		assertEquals(2, handle.getCharacters().size());
//		Map<UUID, CharacterMessage> ret = handle.getCharacters().stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
//		CharacterMessage first = ret.get(cc1.getId());
//		assertEquals(new Vector(2.2f, 2, 2), first.getLocation());
//		assertEquals(new Vector(0, 0, 1), first.getOrientation());
//		assertEquals(Action.move(), first.getActions().iterator().next());
//		assertEquals(clientID, first.getCheckoutClient());
//
//		CharacterMessage second = ret.get(cc2.getId());
//		assertEquals(new Vector(4.1f, 4, 4), second.getLocation());
//		assertEquals(new Vector(0, 0, 1), second.getOrientation());
//		assertEquals(Action.move(), second.getActions().iterator().next());
//		assertEquals(clientID, second.getCheckoutClient());
//	}
//
//	@Test
//	public void testPlayerWondersIntoViewPort() throws IOException{
//		UUID clientID = UUID.randomUUID();
//		UUID clientID2 = UUID.randomUUID();
//
//		UUID c1 = UUID.randomUUID();
//		UUID c2 = UUID.randomUUID();
//		UUID c3 = UUID.randomUUID();
//		HashMap<UUID, CharacterMessage> storedValues = new HashMap<UUID, CharacterMessage>();
//		CharacterMessage cc1 = getCharacterSource(c1, new Vector(2, 2, 2), new Vector(1, 0, 0), Action.idle());
//		CharacterMessage cc2 = getCharacterSource(c2, new Vector(4, 4, 4), new Vector(1, 0, 0), Action.idle());
//		cc2.setCheckoutClient(clientID2);
//		cc2.setCheckoutTime(System.currentTimeMillis()-(CharacterMessage.CHECKOUT_TIMEOUT*2));
//		CharacterMessage cc3 = getCharacterSource(c3, new Vector(6, 6, 6), new Vector(0, 0, 1), Action.move());
//		cc3.setCheckoutClient(clientID2);
//		storedValues.put(c1, cc1);
//		storedValues.put(c2, cc2);
//
//		when(characterDAO.getCharacter(cc1.getId())).thenReturn(cc1);
//		when(characterDAO.getAllCharacters(argThat(new IsSetOfElements(2)))).thenReturn(storedValues);
//		HashSet<CharacterMessage> inRange = new HashSet<>();
//		inRange.add(cc1);
//		inRange.add(cc2);
//		inRange.add(cc3);
//		when(characterDAO.getAllCharacters(2.2f, 2, 2, CheckoutScreenMessageHandler.CLIENT_RANGE)).thenReturn(inRange);
//
//		HashSet<CharacterMessage> characters = new HashSet<CharacterMessage>();
//		characters.add(getCharacterSource(c1, new Vector(2.2f, 2, 2), new Vector(0, 0, 1), Action.move()));
//		characters.add(getCharacterSource(c2, new Vector(4.1f, 4, 4), new Vector(0, 0, 1), Action.move()));
//
//		Set<LostVictoryMessage> results = handler.handle(new UpdateCharactersRequest(clientID, characters, cc1.getId()));
//		CharacterStatusResponse handle = (CharacterStatusResponse) results.stream().filter(s->s instanceof  CharacterStatusResponse).findFirst().get();
//
//		assertEquals(3, handle.getCharacters().size());
//		Map<UUID, CharacterMessage> ret = handle.getCharacters().stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
//		CharacterMessage first = ret.get(c1);
//		assertEquals(new Vector(2.2f, 2, 2), first.getLocation());
//		assertEquals(new Vector(0, 0, 1), first.getOrientation());
//		assertEquals(Action.move(), first.getActions().iterator().next());
//		assertEquals(clientID, first.getCheckoutClient());
//
//		CharacterMessage second = ret.get(c2);
//		assertEquals(new Vector(4.1f, 4, 4), second.getLocation());
//		assertEquals(new Vector(0, 0, 1), second.getOrientation());
//		assertEquals(Action.move(), second.getActions().iterator().next());
//		assertEquals(clientID, second.getCheckoutClient());
//
//		CharacterMessage third = ret.get(c3);
//		assertEquals(new Vector(6, 6, 6), third.getLocation());
//		assertEquals(new Vector(0, 0, 1), third.getOrientation());
//		assertEquals(Action.move(), third.getActions().iterator().next());
//		assertEquals(clientID2, third.getCheckoutClient());
//	}
//
//	@Test
//	public void testPlayerWondersOutOfViewPort() throws IOException{
//		UUID clientID = UUID.randomUUID();
//		UUID clientID2 = UUID.randomUUID();
//
//		UUID c1 = UUID.randomUUID();
//		UUID c2 = UUID.randomUUID();
//		UUID c3 = UUID.randomUUID();
//		HashMap<UUID, CharacterMessage> storedValues = new HashMap<UUID, CharacterMessage>();
//		CharacterMessage cc1 = getCharacterSource(c1, new Vector(2, 2, 2), new Vector(1, 0, 0), Action.idle());
//		CharacterMessage cc2 = getCharacterSource(c2, new Vector(4, 4, 4), new Vector(1, 0, 0), Action.idle());
//		CharacterMessage cc3 = getCharacterSource(c3, new Vector(6, 6, 6), new Vector(0, 0, 1), Action.move());
//
//		storedValues.put(c1, cc1);
//		storedValues.put(c2, cc2);
//		storedValues.put(c3, cc3);
//		when(characterDAO.getCharacter(cc1.getId())).thenReturn(cc1);
//		when(characterDAO.getAllCharacters(argThat(new IsSetOfElements(3)))).thenReturn(storedValues);
//		HashSet<CharacterMessage> inRange = new HashSet<>();
//		inRange.add(cc1);
//		inRange.add(cc2);
//		when(characterDAO.getAllCharacters(2.2f, 2, 2, CheckoutScreenMessageHandler.CLIENT_RANGE)).thenReturn(inRange);
//
//		HashSet<CharacterMessage> characters = new HashSet<CharacterMessage>();
//		characters.add(getCharacterSource(c1, new Vector(2.2f, 2, 2), new Vector(0, 0, 1), Action.move()));
//		characters.add(getCharacterSource(c2, new Vector(4.1f, 4, 4), new Vector(0, 0, 1), Action.move()));
//		characters.add(getCharacterSource(c3, new Vector(6.1f, 6, 6), new Vector(0, 0, 1), Action.move()));
//
//		Set<LostVictoryMessage> results = handler.handle(new UpdateCharactersRequest(clientID, characters, cc1.getId()));
//		CharacterStatusResponse handle = (CharacterStatusResponse) results.stream().filter(s->s instanceof  CharacterStatusResponse).findFirst().get();
//
//		assertEquals(2, handle.getCharacters().size());
//		Map<UUID, CharacterMessage> ret = handle.getCharacters().stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
//		CharacterMessage first = ret.get(c1);
//		assertEquals(new Vector(2.2f, 2, 2), first.getLocation());
//		assertEquals(new Vector(0, 0, 1), first.getOrientation());
//		assertEquals(Action.move(), first.getActions().iterator().next());
//		assertEquals(clientID, first.getCheckoutClient());
//
//		CharacterMessage second = ret.get(c2);
//		assertEquals(new Vector(4.1f, 4, 4), second.getLocation());
//		assertEquals(new Vector(0, 0, 1), second.getOrientation());
//		assertEquals(Action.move(), second.getActions().iterator().next());
//		assertEquals(clientID, second.getCheckoutClient());
//
//	}
//
//	@Test
//	public void testAvatarOfClientIsOveriddenInLocalCheckout() throws IOException{
//		UUID clientID1 = UUID.randomUUID();
//		UUID clientID2 = UUID.randomUUID();
//
//		HashMap<UUID, CharacterMessage> storedValues = new HashMap<UUID, CharacterMessage>();
//		HashSet<CharacterMessage> inRange = new HashSet<>();
//		CharacterMessage cc1 = putCharacter(clientID1, storedValues, inRange, new Vector(2, 2, 2), new Vector(1, 0, 0), Action.idle());
//		CharacterMessage cc2 = putCharacter(clientID2, storedValues, inRange, new Vector(4, 4, 4), new Vector(1, 0, 0), Action.idle());
//		cc1.setCheckoutClient(clientID1);
//		cc2.setCheckoutClient(clientID1);
//		cc1.setCheckoutTime(System.currentTimeMillis());
//		cc2.setCheckoutTime(System.currentTimeMillis());
//		when(characterDAO.getCharacter(cc1.getId())).thenReturn(cc1);
//		when(characterDAO.getAllCharacters(argThat(new IsSetOfElements(2)))).thenReturn(storedValues);
//		when(characterDAO.getAllCharacters(2.2f, 2, 2, CheckoutScreenMessageHandler.CLIENT_RANGE)).thenReturn(inRange);
//
//		HashSet<CharacterMessage> characters1 = new HashSet<CharacterMessage>();
//		characters1.add(getCharacterSource(cc1.getId(), new Vector(2.2f, 2, 2), new Vector(0, 0, 1), Action.move()));
//		characters1.add(getCharacterSource(cc2.getId(), new Vector(4.1f, 4, 4), new Vector(0, 0, 1), Action.move()));
//
//		Set<LostVictoryMessage> results = handler.handle(new UpdateCharactersRequest(clientID1, characters1, cc1.getId()));
//		CharacterStatusResponse handle = (CharacterStatusResponse) results.stream().filter(s->s instanceof  CharacterStatusResponse).findFirst().get();
//
//		HashSet<CharacterMessage> characters2 = new HashSet<CharacterMessage>();
//		characters2.add(getCharacterSource(cc1.getId(), new Vector(3.2f, 2, 2), new Vector(0, 0, 1), Action.idle()));
//		characters2.add(getCharacterSource(cc2.getId(), new Vector(5.1f, 4, 4), new Vector(0, 0, 1), Action.idle()));
//
//		Set<LostVictoryMessage> results2 = handler.handle(new UpdateCharactersRequest(clientID2, characters2, cc1.getId()));
//		CharacterStatusResponse handle2 = (CharacterStatusResponse) results2.stream().filter(s->s instanceof  CharacterStatusResponse).findFirst().get();
//
//		assertEquals(2, handle2.getCharacters().size());
//		Map<UUID, CharacterMessage> ret = handle2.getCharacters().stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
//		CharacterMessage first = ret.get(cc1.getId());
//		assertEquals(new Vector(2.2f, 2, 2), first.getLocation());
//		assertEquals(Action.move(), first.getActions().iterator().next());
//		assertEquals(clientID1, first.getCheckoutClient());
//
//		CharacterMessage second = ret.get(cc2.getId());
//		assertEquals(new Vector(5.1f, 4, 4), second.getLocation());
//		assertEquals(Action.idle(), second.getActions().iterator().next());
//		assertEquals(clientID2, second.getCheckoutClient());
//	}
	
	

}
