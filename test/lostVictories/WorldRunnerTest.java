/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lostVictories;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.elasticsearch.common.collect.ImmutableSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.Country;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.Quaternion;
import com.jme3.lostVictories.network.messages.Vector;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author dharshanar
 */
public class WorldRunnerTest {
    
    WorldRunner instance ;
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        HouseMessage s1 = new HouseMessage("atype", new Vector(0, 0, 0), new Quaternion(0, 0, 0, 0));
        HouseMessage s2 = new HouseMessage("atype", new Vector(0, 0, 0), new Quaternion(0, 0, 0, 0));
        HouseMessage s3 = new HouseMessage("atype", new Vector(0, 0, 0), new Quaternion(0, 0, 0, 0));
        HouseMessage s4 = new HouseMessage("atype", new Vector(0, 0, 0), new Quaternion(0, 0, 0, 0));
        HouseMessage s5 = new HouseMessage("atype", new Vector(0, 0, 0), new Quaternion(0, 0, 0, 0));
        s1.contestOwnership(Country.GERMAN);
        s2.contestOwnership(Country.GERMAN);
        s3.contestOwnership(Country.AMERICAN);
        s4.contestOwnership(Country.AMERICAN);
        s5.contestOwnership(Country.AMERICAN);
        s1.changeOwnership();
        s2.changeOwnership();
        s3.changeOwnership();
        s4.changeOwnership();
        s5.changeOwnership();
        HouseDAO houseDAO = mock(HouseDAO.class);
        CharacterDAO characterDAO = mock(CharacterDAO.class);
        when(houseDAO.getAllHouses()).thenReturn(ImmutableSet.of(s1, s2, s3, s4, s5));
        when(characterDAO.getAllCharacters()).thenReturn(new HashSet<CharacterMessage>());
        instance = WorldRunner.instance(characterDAO, houseDAO);
        
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of run method, of class WorldRunner.
     */
    @Test
    public void testRun() {
        instance.run();
        
        assertEquals(instance.manPower.get(Country.GERMAN), (Long)2l);
        assertEquals(instance.manPower.get(Country.AMERICAN), (Long)3l);
        
        instance.manPower.put(Country.GERMAN, 199l);
        instance.manPower.put(Country.AMERICAN, 199l);
        
        instance.run();
        assertEquals(instance.manPower.get(Country.GERMAN), (Long)200l);
        assertEquals(instance.manPower.get(Country.AMERICAN), (Long)202l);
    }

    
}