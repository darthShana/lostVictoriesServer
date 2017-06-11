package lostVictories;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.Country;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.Weapon;

public class VehicleFactoryTest {

	private VehicleFactory factory;

	@Before
	public void setupTests(){
		factory = new VehicleFactory(Country.GERMAN);
		
		CharacterMessage c1 = new CharacterMessage(UUID.randomUUID(), CharacterType.HALF_TRACK, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.PRIVATE, UUID.randomUUID());
		CharacterMessage c2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.PRIVATE, UUID.randomUUID());
		CharacterMessage c3 = new CharacterMessage(UUID.randomUUID(), CharacterType.HALF_TRACK, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.PRIVATE, UUID.randomUUID());
		CharacterMessage c4 = new CharacterMessage(UUID.randomUUID(), CharacterType.ANTI_TANK_GUN, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.PRIVATE, UUID.randomUUID());
		CharacterMessage c5 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.PRIVATE, UUID.randomUUID());
		CharacterMessage c6 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.AMERICAN, Weapon.RIFLE, RankMessage.PRIVATE, UUID.randomUUID());
		CharacterMessage c7 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.AMERICAN, Weapon.RIFLE, RankMessage.PRIVATE, UUID.randomUUID());
		CharacterMessage c8 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(0, 0, 0), Country.GERMAN, Weapon.RIFLE, RankMessage.PRIVATE, UUID.randomUUID());
	
		Set<CharacterMessage> all = new HashSet<CharacterMessage>();
		all.add(c1);all.add(c2);all.add(c3);all.add(c4);all.add(c5);all.add(c6);all.add(c7);all.add(c8);
		
		factory.updateSenses(all);
	}
	
	@Test
	public void testUpdateSenses() {
		
		assertEquals(1, factory.senses.get(CharacterType.ANTI_TANK_GUN).intValue());
		assertEquals(2, factory.senses.get(CharacterType.HALF_TRACK).intValue());
		assertEquals(3, factory.senses.get(CharacterType.SOLDIER).intValue());

	}
	
	@Test
	public void testGetVehicle(){
		CharacterType vehicle = factory.getVehicle(RankMessage.PRIVATE);
		assertEquals(CharacterType.ANTI_TANK_GUN, vehicle);
		
		vehicle = factory.getVehicle(RankMessage.PRIVATE);
		assertEquals(CharacterType.HALF_TRACK, vehicle);
		
		vehicle = factory.getVehicle(RankMessage.PRIVATE);
		assertEquals(CharacterType.PANZER4, vehicle);

		vehicle = factory.getVehicle(RankMessage.PRIVATE);
		assertNull(vehicle);
	}
	
	@Test
	public void testGetVehicleForCommandingOfficer(){
		CharacterType vehicle = factory.getVehicle(RankMessage.CADET_CORPORAL);
		assertNull(vehicle);
	}

}
