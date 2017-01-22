package com.jme3.lostVictories.network.messages.actions;

import static lostVictories.dao.CharacterDAO.MAPPER;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import lostVictories.dao.CharacterDAO;

import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.objectives.Objective;
import com.jme3.lostVictories.objectives.TravelObjective;

public class ActionTest {

	@Test
	public void testDeserialise() throws IOException{
		Set<Action> mySet = new HashSet<>();
		Idle action = new Idle();
		mySet.add(action);
		
		

		System.out.println("action as json:"+MAPPER.writeValueAsString(action));
		System.out.println("action set as json:"+MAPPER.writeValueAsString(MAPPER.writerFor(new TypeReference<Set<Action>>() {}).writeValueAsString(mySet)));
		
		Set<Objective> mySet2 = new HashSet<>();
		TravelObjective t = new TravelObjective(new Vector(0, 0, 0), null);
		mySet2.add(t);
		System.out.println("obj as json:"+MAPPER.writeValueAsString(t));
		System.out.println("obj set as json:"+MAPPER.writeValueAsString(mySet2));
		

	}

}
