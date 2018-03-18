package lostVictories.messageHanders;

import static com.lostVictories.service.LostVictoriesService.bytes;
import static com.lostVictories.service.LostVictoriesService.uuid;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
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
        WorldRunner worldRunner = mock(WorldRunner.class);
        GameStatistics t = new GameStatistics();
        t.setHousesCaptured(5, 6);
        t.setVictorypoints(500, 600);
        handler = new UpdateCharactersMessageHandler(characterDAO, mock(HouseDAO.class), mock(EquipmentDAO.class), worldRunner, new MessageRepository());
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

        handler.handle(convertToCharacterMessage(cc3, clientID, 5000), mock, new HashMap<>());

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

        handler.handle(convertToCharacterMessage(cc3, clientID, 5000), mock, new HashMap<>());

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
        when(characterDAO.getCharacter(eq(clientID), eq(true))).thenReturn(avatar);
        when(characterDAO.getCharacter(eq(clientID))).thenReturn(avatar);

		c2.setCheckoutClient(UUID.randomUUID());
		c2.setCheckoutTime(System.currentTimeMillis()-5001);

        when(characterDAO.updateCharacterState(eq(c1))).thenReturn(c1);
        when(characterDAO.getAllCharacters(3, 3, 3, CheckoutScreenMessageHandler.CLIENT_RANGE)).thenReturn(inRange);

		CharacterMessage cc3 = getCharacterSource(c1.getId(), new Vector(2.1f, 2, 2), new Vector(0, 0, 1), Action.move(), null);

        ArgumentCaptor<LostVictoryMessage> argument1 = ArgumentCaptor.forClass(LostVictoryMessage.class);
        SafeStreamObserver mock1 = mock(SafeStreamObserver.class);

        handler.handle(convertToCharacterMessage(cc3, clientID, 5000), mock1, new HashMap<>());

        verify(mock1).onNext(argument1.capture());
        assertEquals(1, argument1.getAllValues().stream().filter(m->m.hasCharacterStatusResponse()).collect(Collectors.toSet()).size());

        ArgumentCaptor<LostVictoryMessage> argument2 = ArgumentCaptor.forClass(LostVictoryMessage.class);
        SafeStreamObserver mock2 = mock(SafeStreamObserver.class);

        handler.handle(convertToCharacterMessage(avatar, clientID, 5001), mock2, new HashMap<>());

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

        handler.handle(convertToCharacterMessage(cc3, clientID, 5001), mock, new HashMap<>());
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
        handler.handle(convertToCharacterMessage(cc3, avatar.getId(), 5000), mock, new HashMap<>());

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



	private CharacterMessage putCharacter(HashSet<CharacterMessage> inRange, Vector location, Vector orientation, Action action, UUID commandingOfficer) {
		UUID c1 = UUID.randomUUID();
		CharacterMessage cc1 = getCharacterSource(c1, location, orientation, action, commandingOfficer);

		if(inRange!=null) {
			inRange.add(cc1);
		}
		when(characterDAO.getCharacter(eq(cc1.getId()), eq(true))).thenReturn(cc1);
		return cc1;
	}


	private CharacterMessage getCharacterSource(UUID id, Vector location, Vector orientation, Action action, UUID commandingOfficer) {
		CharacterMessage c = new CharacterMessage(id, CharacterType.SOLDIER, location, Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, commandingOfficer);
		c.setOrientation(orientation);
		c.setActions(ImmutableSet.of(action));

		return c;
	}
	
	

}
