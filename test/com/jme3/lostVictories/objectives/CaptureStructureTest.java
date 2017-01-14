package com.jme3.lostVictories.objectives;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Test;

public class CaptureStructureTest {

	@Test
	public void testClashesWith() {
		CaptureStructure objective = new CaptureStructure(UUID.randomUUID().toString());
		
		assertTrue(objective.clashesWith(TravelObjective.class));
		assertTrue(objective.clashesWith(TransportSquad.class));
	}

}
