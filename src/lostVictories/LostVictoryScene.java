package lostVictories;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.Country;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.Quaternion;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.Weapon;

public class LostVictoryScene {
	
	public static int SCENE_WIDTH = 512;
	public static int SCENE_HEIGHT = 512;
	
	private static Logger log = Logger.getLogger(LostVictoryScene.class); 
	
	public void loadScene(CharacterDAO characterDAO, HouseDAO housesDAO) {
		log.debug("Loading Scene");
		
		Set<CharacterMessage> characters = new HashSet<CharacterMessage>();
		
		CharacterMessage a = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(-125, 7, 370), Country.GERMAN, Weapon.RIFLE, RankMessage.COLONEL, null, false);
		a.addObjective(UUID.randomUUID(), createCaptureTownObjective());
		CharacterMessage b = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(195, 7, -375), Country.AMERICAN, Weapon.RIFLE, RankMessage.COLONEL, null, false);
		b.addObjective(UUID.randomUUID(), createCaptureTownObjective());
		
		CharacterMessage gl1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(-120, 7, 375), Country.GERMAN, Weapon.RIFLE, RankMessage.LIEUTENANT, a.getId(), false);
		CharacterMessage gl2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(-115, 7, 375), Country.GERMAN, Weapon.RIFLE, RankMessage.LIEUTENANT, a.getId(), false);
		
		CharacterMessage al1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(190, 7, -380), Country.AMERICAN, Weapon.RIFLE, RankMessage.LIEUTENANT, b.getId(), false);
		CharacterMessage al2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(190, 7, -385), Country.AMERICAN, Weapon.RIFLE, RankMessage.LIEUTENANT, b.getId(), false );
		
		characters.add(a);
		characters.add(b);
		characters.add(gl1);
		characters.add(gl2);
		characters.add(al1);
		characters.add(al2);
		
		CharacterMessage a1 = new CharacterMessage(UUID.fromString("2fbe421f-f701-49c9-a0d4-abb0fa904204"), CharacterType.AVATAR, new Vector(-210, 7, 380), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl1.getId(), false);
		loadSquad(characters, a1, new Vector(-215, 7, 385), Country.GERMAN, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
		a1.addObjective(UUID.randomUUID(), createBootCampObjective(new Vector(-225, 7, 305)));
		
		//UUID.fromString("d993932f-a185-4a6f-8d86-4ef6e2c5ff95")
		CharacterMessage a2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(-230, 7, 380), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl1.getId(), false);
        loadSquad(characters, a2, new Vector(-235, 7, 385), Country.GERMAN, Weapon.RIFLE, Weapon.RIFLE, Weapon.MG42);
        
        CharacterMessage a3 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(-250, 7, 380), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl1.getId(), false);
        loadSquad(characters, a3, new Vector(-255, 7, 385), Country.GERMAN, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        
        CharacterMessage a4 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(-270, 7, 380), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl1.getId(), false);
        loadSquad(characters, a4, new Vector(-275, 7, 385), Country.GERMAN, Weapon.RIFLE, Weapon.RIFLE, Weapon.MORTAR);
        
        CharacterMessage gv1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(-200, 7, 380), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl2.getId(), false);
        loadSquad(characters, gv1, new Vector(-195, 7, 380), Country.GERMAN, Weapon.RIFLE, Weapon.RIFLE);
        characters.add(loadAntiTankGun(new Vector(-295, 7, 400), Country.GERMAN, gv1));
        
        CharacterMessage gv2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(-295, 7, 380), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl2.getId(), false);
        loadSquad(characters, gv2, new Vector(-195, 7, 380), Country.GERMAN, Weapon.RIFLE, Weapon.RIFLE);
        characters.add(loadHalfTrack(new Vector(-195, 7, 400), Country.GERMAN, gv2));
        
        CharacterMessage gv3 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(-200, 7, 360), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl2.getId(), false);
        loadSquad(characters, gv3, new Vector(-205, 10, 365), Country.GERMAN, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        
        CharacterMessage gv4 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(-295, 7, 360), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl2.getId(), false);
        loadSquad(characters, gv4, new Vector(-300, 7, 365), Country.GERMAN, Weapon.RIFLE, Weapon.RIFLE, Weapon.MG42);
        
        CharacterMessage b1 = new CharacterMessage(UUID.fromString("d993932f-a185-4a6f-8d86-4ef6e2c5ff95"), CharacterType.AVATAR, new Vector(180, 5, -385), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al1.getId(), false);
        loadSquad(characters, b1, new Vector(175, 5, -390), Country.AMERICAN, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        b1.addObjective(UUID.randomUUID(), createBootCampObjective(new Vector(125, 0, -330)));
        b1.incrementKills(UUID.randomUUID());
        b1.incrementKills(UUID.randomUUID());
        b1.incrementKills(UUID.randomUUID());
        b1.incrementKills(UUID.randomUUID());
        
        CharacterMessage b2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(200, 5, -385), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al1.getId(), false);
        loadSquad(characters,  b2, new Vector(195, 5, -390), Country.AMERICAN, Weapon.RIFLE, Weapon.RIFLE, Weapon.MG42);
        
        CharacterMessage b3 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(220, 5, -385), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al1.getId(), false);
        loadSquad(characters, b3, new Vector(215, 5, -390), Country.AMERICAN, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        
        CharacterMessage b4 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(240, 5, -385), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al1.getId(), false);
        loadSquad(characters, b4, new Vector(235, 5, -390), Country.AMERICAN, Weapon.RIFLE, Weapon.RIFLE, Weapon.MORTAR);
        
        CharacterMessage c1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(195, 5, -395), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al2.getId(), false);
        loadSquad(characters, c1, new Vector(165, 5, -395), Country.AMERICAN, Weapon.RIFLE, Weapon.RIFLE);
        characters.add(loadAntiTankGun(new Vector(190, 5, -390), Country.AMERICAN, c1));
        
        CharacterMessage c2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(200, 5, -395), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al2.getId(), false);
        loadSquad(characters, c2, new Vector(145, 5, -395), Country.AMERICAN, Weapon.RIFLE, Weapon.RIFLE);
        characters.add(loadAmoredCar(new Vector(200, 5, -390), Country.AMERICAN, c2));
        
        CharacterMessage c3 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(190, 5, -375), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al2.getId(), false);
        loadSquad(characters, c3, new Vector(125, 5, -380), Country.AMERICAN, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        
        CharacterMessage c4 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(200, 5, -375), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al2.getId(), false);
        loadSquad(characters,  c4, new Vector(200, 5, -380), Country.AMERICAN, Weapon.RIFLE, Weapon.RIFLE, Weapon.MG42);
        
        gl1.addCharactersUnderCommand(a1, a2, a3, a4);
        gl2.addCharactersUnderCommand(gv1, gv2, gv3, gv4);
        al1.addCharactersUnderCommand(b1, b2, b3, b4);
        al2.addCharactersUnderCommand(c1, c2, c3, c4);
        
        a.addCharactersUnderCommand(gl1, gl2);
        b.addCharactersUnderCommand(al1, al2);
        
        characters.forEach(c -> characterDAO.putCharacter(c.getId(), c));
        
		Set<HouseMessage> houses = new HashSet<HouseMessage>();

        houses.add(new HouseMessage("ahouse", new Vector(-50.0f, 5.0f, -250.0f), new Quaternion(0.0f, 0.0f, 0.0f, 1.0f)));
		houses.add(new HouseMessage("ahouse", new Vector(160.0f, 5.0f, 50.0f), new Quaternion(0.0f, -0.70710677f, 0.0f, 0.70710677f)));
        houses.add(new HouseMessage("ahouse", new Vector(-80.0f, 5.0f, -250.0f), new Quaternion(0.0f, 0.0f, 0.0f, 1.0f)));
        houses.add(new HouseMessage("ahouse", new Vector(100.0f, 5.0f, -40.0f), new Quaternion(0.0f, 0.0f, 0.0f, 1.0f)));
        houses.add(new HouseMessage("ahouse", new Vector(100.0f, 5.0f, 50.0f), new Quaternion(0.0f, -0.70710677f, 0.0f, 0.70710677f)));
        houses.add(new HouseMessage("ahouse", new Vector(100.0f, 5.0f, -70.0f), new Quaternion(0.0f, 0.0f, 0.0f, 1.0f)));
        houses.add(new HouseMessage("ahouse", new Vector(120.0f, 5.0f, 50.0f), new Quaternion(0.0f, -0.70710677f, 0.0f, 0.70710677f)));
        houses.add(new HouseMessage("ahouse", new Vector(230.0f, 2.3837547f, -39.32052f), new Quaternion(0.0f, -0.70710677f, 0.0f, 0.70710677f)));
        houses.add(new HouseMessage("ahouse", new Vector(140.0f, 5.0f, 50.0f), new Quaternion(0.0f, -0.70710677f, 0.0f, 0.70710677f)));
        houses.add(new HouseMessage("ahouse", new Vector(230.0f, 2.2003498f, -70.56318f), new Quaternion(0.0f, -0.70710677f, 0.0f, 0.70710677f)));
        houses.add(new HouseMessage("ahouse", new Vector(130.0f, 5.0f, -40.0f), new Quaternion(0.0f, 0.0f, 0.0f, 1.0f)));
        houses.add(new HouseMessage("ahouse", new Vector(130.0f, 5.0f, -70.0f), new Quaternion(0.0f, 0.0f, 0.0f, 1.0f)));
        houses.add(new HouseMessage("bhouse", new Vector(55.0f, 5.0f, -130.0f), new Quaternion(0.0f, -0.70710677f, 0.0f, 0.70710677f)));
        houses.add(new HouseMessage("bhouse", new Vector(25.0f, 5.0f, -150.0f), new Quaternion(0.0f, 0.70710677f, 0.0f, 0.70710677f)));
        houses.add(new HouseMessage("bhouse", new Vector(-25.0f, 5.0f, -130.0f), new Quaternion(0.0f, 0.0f, 0.0f, 1.0f)));
        houses.add(new HouseMessage("bhouse", new Vector(-60.0f, 5.0f, -130.0f), new Quaternion(0.0f, 0.0f, 0.0f, 1.0f)));
        houses.add(new HouseMessage("bhouse", new Vector(100.0f, 5.0f, 0.0f), new Quaternion(0.0f, -0.70710677f, 0.0f, 0.70710677f)));
        houses.add(new HouseMessage("bhouse", new Vector(100.0f, 5.0f, 30.0f), new Quaternion(0.0f, -0.70710677f, 0.0f, 0.70710677f)));
        houses.add(new HouseMessage("bhouse", new Vector(-25.0f, 5.0f, -170.0f), new Quaternion(0.0f, 0.0f, 0.0f, 1.0f)));
        houses.add(new HouseMessage("bhouse", new Vector(-60.0f, 5.0f, -175.0f), new Quaternion(0.0f, 0.0f, 0.0f, 1.0f)));
        houses.add(new HouseMessage("chouse", new Vector(350.0f, 2.0f, 400.0f), new Quaternion(0.0f, -0.38268346f, 0.0f, 0.9238795f)));
        houses.add(new HouseMessage("chouse", new Vector(400.0f, 5.0f, 50.0f), new Quaternion(0.0f, 0.0f, 0.0f, 1.0f)));
        houses.add(new HouseMessage("chouse", new Vector(50.0f, 4.0f, 400.0f), new Quaternion(0.0f, -0.70710677f, 0.0f, 0.70710677f)));
        houses.add(new HouseMessage("chouse", new Vector(150.0f, 5.0f, 200.0f), new Quaternion(0.0f, -0.38268346f, 0.0f, 0.9238795f)));
        houses.add(new HouseMessage("chouse", new Vector(350.0f, 7.0f, -200.0f), new Quaternion(0.0f, 0.0f, 0.0f, 1.0f)));
        houses.add(new HouseMessage("chouse", new Vector(350.0f, 4.827713f, -399.61368f), new Quaternion(0.0f, 0.0f, 0.0f, 1.0f)));
        houses.add(new HouseMessage("chouse", new Vector(50.0f, 0.0f, -400.0f), new Quaternion(0.0f, 0.70710677f, 0.0f, 0.70710677f)));
        houses.add(new HouseMessage("chouse", new Vector(-200.0f, 7.0f, -400.0f), new Quaternion(0.0f, 0.70710677f, 0.0f, 0.70710677f)));
        houses.add(new HouseMessage("chouse", new Vector(-300.0f, 2.0f, -200.0f), new Quaternion(0.0f, 0.0f, 0.0f, 1.0f)));
        houses.add(new HouseMessage("chouse", new Vector(-400.0f, 1.0f, 0.0f), new Quaternion(0.0f, 0.0f, 0.0f, 1.0f)));
        houses.add(new HouseMessage("chouse", new Vector(-400.0f, 3.0f, 150.0f), new Quaternion(0.0f, 0.0f, 0.0f, 1.0f)));
        houses.add(new HouseMessage("chouse", new Vector(-400.0f, 8.0f, 350.45978f), new Quaternion(0.0f, 0.0f, 0.0f, 1.0f)));
        
        houses.forEach(h->housesDAO.putHouse(h.getId(), h));
	}
	
	private String createBootCampObjective(final Vector vector)  {
		ObjectNode node = CharacterDAO.MAPPER.createObjectNode();
		try {
			node.put("classType", "com.jme3.lostVictories.objectives.CompleteBootCamp");
			JsonNode valueToTree = CharacterDAO.MAPPER.valueToTree(vector);
			node.put("location", valueToTree);
			return CharacterDAO.MAPPER.writeValueAsString(node);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private String createCaptureTownObjective()  {
		ObjectNode node = CharacterDAO.MAPPER.createObjectNode();
		try {
			node.put("classType", "com.jme3.lostVictories.objectives.CaptureTown");
			return CharacterDAO.MAPPER.writeValueAsString(node);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private CharacterMessage loadAmoredCar(Vector vector, Country country, CharacterMessage c2) {
		CharacterMessage armoredCar = new CharacterMessage(UUID.randomUUID(), CharacterType.ARMORED_CAR, vector, country, Weapon.MG42, RankMessage.PRIVATE, c2.getId(), false);
		c2.addCharactersUnderCommand(armoredCar);
		return armoredCar;
	}

	private CharacterMessage loadHalfTrack(Vector vector, Country country, CharacterMessage gv2) {
		CharacterMessage halfTrack = new CharacterMessage(UUID.randomUUID(), CharacterType.HALF_TRACK, vector, country, Weapon.MG42, RankMessage.PRIVATE, gv2.getId(), false);
		gv2.addCharactersUnderCommand(halfTrack);
		return halfTrack;
	}

	private CharacterMessage loadAntiTankGun(Vector vector, Country country, CharacterMessage gv1) {
		CharacterMessage atg = new CharacterMessage(UUID.randomUUID(), CharacterType.ANTI_TANK_GUN, vector, country, Weapon.CANNON, RankMessage.PRIVATE, gv1.getId(), false);
		gv1.addCharactersUnderCommand(atg);
		return atg;
	}

	public void loadSquad(Set<CharacterMessage> characters, CharacterMessage a1, Vector vector3f, Country country, Weapon...weapons) {
        characters.add(a1);
		Set<CharacterMessage> cc = new HashSet<CharacterMessage>();
        int i = 0;
        for(Weapon w:weapons){
            cc.add(new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, vector3f.add(i*3, 5, 0), country, w, RankMessage.PRIVATE, a1.getId(), false));
            i++;
        }
        a1.addCharactersUnderCommand(cc);
        characters.addAll(cc);
    }

}
