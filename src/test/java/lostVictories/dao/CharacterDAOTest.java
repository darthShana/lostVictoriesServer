package lostVictories.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jme3.lostVictories.network.messages.*;
import com.jme3.lostVictories.network.messages.Vector;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.JedisPool;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;


import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by dharshana on 19/04/17.
 */
public class CharacterDAOTest {

    private CharacterDAO dao;

    @Before
    public void setUp(){
        dao = new CharacterDAO(new JedisPool("localhost"));
        dao.deleteAllCharacters();
    }

    @Test
    public void testPutAndGet() throws JsonProcessingException {

        CharacterMessage s1 = CharacterMessageTest.createCharacter(UUID.randomUUID(), UUID.randomUUID(), new Vector(100, 90.5f, 50), RankMessage.CADET_CORPORAL, true);
        dao.putCharacter(s1.getId(), s1);
        CharacterMessage s2 = dao.getCharacter(s1.getId());

        assertEquals(s1.getCharacterType(), s2.getCharacterType());
        assertEquals(s1.getId(), s2.getId());
        assertEquals(s1.getUserID(), s2.getUserID());
        assertEquals(s1.getCountry(), s2.getCountry());
        assertEquals(s1.getWeapon(), s2.getWeapon());
        assertEquals(s1.getRank(), s2.getRank());
        assertEquals(s1.getCommandingOfficer(), s2.getCommandingOfficer());
        assertEquals(s1.getBoardedVehicle(), s2.getBoardedVehicle());
        assertEquals(s1.getUnitsUnderCommand(), s2.getUnitsUnderCommand());
        assertEquals(s1.getPassengers(), s2.getPassengers());
        assertEquals(s1.getCheckoutClient(), s2.getCheckoutClient());
        assertEquals(s1.getCheckoutTime(), s2.getCheckoutTime());
        assertEquals(s1.getOrientation(), s2.getOrientation());
        assertEquals(s1.getActions(), s2.getActions());
        assertEquals(s1.getObjectives(), s2.getObjectives());
        assertEquals(s1.isDead(), s2.isDead());
        assertEquals(s1.hasEngineDamage(), s2.hasEngineDamage());
        assertEquals(s1.getTimeOfDeath(), s2.getTimeOfDeath());
        assertEquals(s1.getVersion(), s2.getVersion());
        assertEquals(s1.getKills(), s2.getKills());

        assertEquals(s1.getLocation(), s2.getLocation());

    }

    @Test
    public void testPutOveridesOldProperties() throws JsonProcessingException {
        UUID randomID = UUID.randomUUID();
        CharacterMessage s1 = CharacterMessageTest.createCharacter(randomID, UUID.randomUUID(), new Vector(100, 90.5f, 50), RankMessage.CADET_CORPORAL, true);
        dao.putCharacter(s1.getId(), s1);
        CharacterMessage s2 = CharacterMessageTest.createCharacter(randomID, null, new Vector(100, 90.5f, 50), RankMessage.CADET_CORPORAL, true);
        dao.putCharacter(s2.getId(), s2);

        CharacterMessage s3 = dao.getCharacter(randomID);
        assertNull(s3.getCommandingOfficer());
    }

    @Test
    public void testGetAllCharactersInRange() throws JsonProcessingException {
        CharacterMessage s1 = CharacterMessageTest.createCharacter(UUID.randomUUID(), UUID.randomUUID(), new Vector(100, 90.5f, 50), RankMessage.CADET_CORPORAL, true);
        dao.putCharacter(s1.getId(), s1);
        CharacterMessage s2 = CharacterMessageTest.createCharacter(UUID.randomUUID(), UUID.randomUUID(), new Vector(110, 90.5f, 50), RankMessage.CADET_CORPORAL, true);
        dao.putCharacter(s2.getId(), s2);
        CharacterMessage s3 = CharacterMessageTest.createCharacter(UUID.randomUUID(), UUID.randomUUID(), new Vector(300, 90.5f, 50), RankMessage.CADET_CORPORAL, true);
        dao.putCharacter(s3.getId(), s3);
        CharacterMessage s4 = CharacterMessageTest.createCharacter(UUID.randomUUID(), UUID.randomUUID(), new Vector(351, 90.5f, 50), RankMessage.CADET_CORPORAL, true);
        dao.putCharacter(s4.getId(), s4);
        CharacterMessage s5 = CharacterMessageTest.createCharacter(UUID.randomUUID(), UUID.randomUUID(), new Vector(-400, 90.5f, -400), RankMessage.CADET_CORPORAL, true);
        dao.putCharacter(s5.getId(), s5);


        Set<UUID> inProximity = dao.getAllCharacters(100, 90.5f, 50, 250).stream().map(m->m.getId()).collect(Collectors.toSet());
        assertEquals(3, inProximity.size());
        assertTrue(inProximity.contains(s1.getId()));
        assertTrue(inProximity.contains(s2.getId()));
        assertTrue(inProximity.contains(s3.getId()));

        dao.delete(s2);
        inProximity = dao.getAllCharacters(100, 90.5f, 50, 250).stream().map(m->m.getId()).collect(Collectors.toSet());
        assertEquals(2, inProximity.size());
        assertTrue(inProximity.contains(s1.getId()));
        assertTrue(inProximity.contains(s3.getId()));
    }

    @Test
    public void testFindClosestCharacter() throws JsonProcessingException {

        CharacterMessage s1 = CharacterMessageTest.createCharacter(UUID.randomUUID(), UUID.randomUUID(), new Vector(100, 90.5f, 50), RankMessage.CADET_CORPORAL, true);
        dao.putCharacter(s1.getId(), s1);
        CharacterMessage s2 = CharacterMessageTest.createCharacter(UUID.randomUUID(), UUID.randomUUID(), new Vector(110, 90.5f, 50), RankMessage.LIEUTENANT, true);
        dao.putCharacter(s2.getId(), s2);
        CharacterMessage s3 = CharacterMessageTest.createCharacter(UUID.randomUUID(), UUID.randomUUID(), new Vector(120, 90.5f, 50), RankMessage.CADET_CORPORAL, true);
        dao.putCharacter(s3.getId(), s3);
        CharacterMessage s4 = CharacterMessageTest.createCharacter(UUID.randomUUID(), UUID.randomUUID(), new Vector(130, 90.5f, 50), RankMessage.CADET_CORPORAL, true);
        dao.putCharacter(s4.getId(), s4);

        CharacterMessage closestCharacter = dao.findClosestCharacter(s1, RankMessage.CADET_CORPORAL);
        assertEquals(s3.getId(), closestCharacter.getId());

    }

    @Test
    public void testGetAllCharactersInIdSet() throws JsonProcessingException {
        CharacterMessage s1 = CharacterMessageTest.createCharacter(UUID.randomUUID(), UUID.randomUUID(), new Vector(100, 90.5f, 50), RankMessage.CADET_CORPORAL, true);
        dao.putCharacter(s1.getId(), s1);
        CharacterMessage s2 = CharacterMessageTest.createCharacter(UUID.randomUUID(), UUID.randomUUID(), new Vector(110, 90.5f, 50), RankMessage.LIEUTENANT, true);
        dao.putCharacter(s2.getId(), s2);
        CharacterMessage s3 = CharacterMessageTest.createCharacter(UUID.randomUUID(), UUID.randomUUID(), new Vector(120, 90.5f, 50), RankMessage.CADET_CORPORAL, true);
        dao.putCharacter(s3.getId(), s3);
        CharacterMessage s4 = CharacterMessageTest.createCharacter(UUID.randomUUID(), UUID.randomUUID(), new Vector(130, 90.5f, 50), RankMessage.CADET_CORPORAL, true);
        dao.putCharacter(s4.getId(), s4);

        Set<UUID> ids = new HashSet<>();
        ids.add(s1.getId());
        ids.add(s2.getId());
        Map<UUID, CharacterMessage> allCharacters = dao.getAllCharacters(ids);
        assertEquals(2, allCharacters.size());
        assertEquals(s1.getId(), allCharacters.get(s1.getId()).getId());
        assertEquals(s2.getId(), allCharacters.get(s2.getId()).getId());

    }

    @Test
    public void testSave() throws IOException {
        CharacterMessage s1 = CharacterMessageTest.createCharacter(UUID.randomUUID(), UUID.randomUUID(), new Vector(100, 90.5f, 50), RankMessage.CADET_CORPORAL, true);
        dao.putCharacter(s1.getId(), s1);
        CharacterMessage ss1 = dao.getCharacter(s1.getId());
        ss1.setOrientation(new Vector(1, 2, 5));
        HashSet<CharacterMessage> values = new HashSet<>();
        values.add(ss1);
        dao.save(values);

        CharacterMessage sss1 = dao.getCharacter(s1.getId());
        assertEquals(new Vector(1, 2, 5), sss1.getOrientation());
        assertEquals(ss1.getVersion()+1, sss1.getVersion());
    }

    @Test
    public void testUpdateCharacterState() throws IOException {
        CharacterMessage s1 = CharacterMessageTest.createCharacter(UUID.randomUUID(), UUID.randomUUID(), new Vector(100, 90.5f, 50), RankMessage.CADET_CORPORAL, true);
        dao.putCharacter(s1.getId(), s1);
        s1.setLocation(new Vector(34, 35, 36));
        s1.setOrientation(new Vector(37, 38, 39));
        s1.setCheckoutClient(null);

        HashMap<UUID, CharacterMessage> map = new HashMap<>();
        map.put(s1.getId(), s1);
        dao.updateCharacterState(map);

        CharacterMessage s2 = dao.getCharacter(s1.getId());
        assertEquals(new Vector(34, 35, 36), s2.getLocation());
        assertEquals(new Vector(37, 38, 39), s2.getOrientation());
        assertNull(s2.getCheckoutClient());
        assertEquals(s1.getVersion()+1, s2.getVersion());

    }

    @Test
    public void testUpdateCharacterStateNoCheckout() throws IOException {
        CharacterMessage s1 = CharacterMessageTest.createCharacter(UUID.randomUUID(), UUID.randomUUID(), new Vector(100, 90.5f, 50), RankMessage.CADET_CORPORAL, true);
        UUID originalCheckoutClient = s1.getCheckoutClient();
        dao.putCharacter(s1.getId(), s1);
        s1.setLocation(new Vector(34, 35, 36));
        s1.setOrientation(new Vector(37, 38, 39));
        s1.setCheckoutClient(null);

        HashMap<UUID, CharacterMessage> map = new HashMap<>();
        map.put(s1.getId(), s1);
        dao.updateCharacterStateNoCheckout(map);

        CharacterMessage s2 = dao.getCharacter(s1.getId());
        assertEquals(new Vector(34, 35, 36), s2.getLocation());
        assertEquals(new Vector(37, 38, 39), s2.getOrientation());
        assertEquals(originalCheckoutClient, s2.getCheckoutClient());
        assertEquals(s1.getVersion()+1, s2.getVersion());
    }

    @Test
    public void testSaveCommandStructure() throws IOException {
        CharacterMessage s1 = CharacterMessageTest.createCharacter(UUID.randomUUID(), UUID.randomUUID(), new Vector(100, 90.5f, 50), RankMessage.CADET_CORPORAL, true);
        dao.putCharacter(s1.getId(), s1);
        CharacterMessage s2 = CharacterMessageTest.createCharacter(UUID.randomUUID(), UUID.randomUUID(), new Vector(110, 90.5f, 50), RankMessage.LIEUTENANT, true);
        dao.putCharacter(s2.getId(), s2);

        s1.addCharactersUnderCommand(s2);
        HashMap<UUID, CharacterMessage> map = new HashMap<>();
        map.put(s1.getId(), s1);
        dao.saveCommandStructure(map);

        CharacterMessage ss1 = dao.getCharacter(s1.getId());
        assertTrue(ss1.getUnitsUnderCommand().contains(s2.getId()));
    }

    @Test
    public void testUpdateCharactersUnderCommand() throws IOException {
        CharacterMessage s1 = CharacterMessageTest.createCharacter(UUID.randomUUID(), UUID.randomUUID(), new Vector(100, 90.5f, 50), RankMessage.CADET_CORPORAL, true);
        dao.putCharacter(s1.getId(), s1);
        CharacterMessage s2 = CharacterMessageTest.createCharacter(UUID.randomUUID(), UUID.randomUUID(), new Vector(110, 90.5f, 50), RankMessage.LIEUTENANT, true);
        dao.putCharacter(s2.getId(), s2);

        s1.addCharactersUnderCommand(s2);
        dao.updateCharactersUnderCommand(s1);

        CharacterMessage ss1 = dao.getCharacter(s1.getId());
        assertTrue(ss1.getUnitsUnderCommand().contains(s2.getId()));
    }

    @Test
    public void testVersionControleOnUpdate() throws IOException {
        UUID identity = UUID.randomUUID();
        CharacterMessage s1 = CharacterMessageTest.createCharacter(identity, UUID.randomUUID(), new Vector(100, 90.5f, 50), RankMessage.CADET_CORPORAL, false);
        dao.putCharacter(s1.getId(), s1);

        CharacterDAO dao1 = new CharacterDAO(new JedisPool("localhost"));
        HashSet<UUID> ids = new HashSet<>();
        ids.add(s1.getId());
        Map<UUID, CharacterMessage> allCharacters = dao1.getAllCharacters(ids);
        assertFalse(allCharacters.get(identity).isDead());

        CharacterDAO dao2 = new CharacterDAO(new JedisPool("localhost"));
        HashMap<UUID, CharacterMessage> toSave = new HashMap<>();
        s1.kill();
        toSave.put(s1.getId(), s1);
        dao2.saveCommandStructure(toSave);

        //this should fail as the key has been updated
        CharacterMessage newS1 = CharacterMessageTest.createCharacter(identity, UUID.randomUUID(), new Vector(3, 4f, 5), RankMessage.CADET_CORPORAL, false);
        Map<UUID, CharacterMessage> toSave2 = new HashMap<>();
        toSave2.put(newS1.getId(), newS1);
        dao1.updateCharacterState(toSave2);

        CharacterMessage finalS1 = dao.getCharacter(identity);
        assertTrue(finalS1.isDead());
        assertEquals(new Vector(100, 90.5f, 50), finalS1.getLocation());

    }

}
