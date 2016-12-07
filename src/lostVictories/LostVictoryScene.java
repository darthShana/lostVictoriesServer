package lostVictories;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ObjectNode;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.Country;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.Quaternion;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.Weapon;
import com.jme3.lostVictories.objectives.CaptureTown;
import com.jme3.lostVictories.objectives.FollowUnit;
import com.jme3.math.Vector3f;

public class LostVictoryScene {
	
	public static int SCENE_WIDTH = 512;
	public static int SCENE_HEIGHT = 512;
	public static float SCENE_SCALE = .25f;
	
	private static Logger log = Logger.getLogger(LostVictoryScene.class); 
	
	public void loadScene(CharacterDAO characterDAO, HouseDAO housesDAO) throws JsonGenerationException, JsonMappingException, IOException {
		log.debug("Loading Scene");
		
		Set<CharacterMessage> characters = new HashSet<CharacterMessage>();
		
		Vector germanBase = new Vector(150, 100, 20);
		Vector americanBase = new Vector(90, 100, -380);
		
		CharacterMessage a = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase, Country.GERMAN, Weapon.RIFLE, RankMessage.COLONEL, null, false);
		a.addObjective(UUID.randomUUID(), new CaptureTown(System.currentTimeMillis()).asJSON());
		CharacterMessage b = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase, Country.AMERICAN, Weapon.RIFLE, RankMessage.COLONEL, null, false);
		b.addObjective(UUID.randomUUID(), new CaptureTown(System.currentTimeMillis()).asJSON());
		
		CharacterMessage gl1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(5, 0, 5), Country.GERMAN, Weapon.RIFLE, RankMessage.LIEUTENANT, a.getId(), false);
		CharacterMessage gl2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(10, 0, 5), Country.GERMAN, Weapon.RIFLE, RankMessage.LIEUTENANT, a.getId(), false);
		
		CharacterMessage al1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(-5, 0, -5), Country.AMERICAN, Weapon.RIFLE, RankMessage.LIEUTENANT, b.getId(), false);
		CharacterMessage al2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(-5, 0, -10), Country.AMERICAN, Weapon.RIFLE, RankMessage.LIEUTENANT, b.getId(), false );
		
		characters.add(a);
		characters.add(b);
		characters.add(gl1);
		characters.add(gl2);
		characters.add(al1);
		characters.add(al2);
		CharacterMessage a1 = new CharacterMessage(UUID.fromString("2fbe421f-f701-49c9-a0d4-abb0fa904204"), CharacterType.AVATAR, germanBase.add(22, 0, 13), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl1.getId(), false);
		loadSquad(characters, a1, germanBase.add(-10, 0, 15), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE   );
		characters.add(loadAntiTankGun1(UUID.fromString("2d420131-2f1f-4901-b61a-248c2243848c"), germanBase.add(25, 0, 15), Country.GERMAN, a1));
//		characters.add(loadHalfTrack(germanBase.add(25, 0, 15), Country.GERMAN, a1));
//		a1.addObjective(UUID.randomUUID(), createBootCampObjective(new Vector(246.29144f, 96.77546f, 55.412266f)));
//		a1.incrementKills(UUID.randomUUID());
//		a1.incrementKills(UUID.randomUUID());
//		a1.incrementKills(UUID.randomUUID());	
//		a1.incrementKills(UUID.randomUUID());
//		a1.incrementKills(UUID.randomUUID());
		
		CharacterMessage a2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(30, 0, 15), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl1.getId(), false);
        loadSquad(characters, a2, germanBase.add(30, 0, 15), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.MG42);
        
        CharacterMessage a3 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(35, 0, 15), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl1.getId(), false);
        loadSquad(characters, a3, germanBase.add(35, 0, 15), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        
        CharacterMessage a4 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(40, 0, 15), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl1.getId(), false);
        loadSquad(characters, a4, germanBase.add(40, 0, 15), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.MORTAR);
        
        CharacterMessage gv1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(20, 0, 16), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl2.getId(), false);
        loadSquad(characters, gv1, germanBase.add(21, 0, 17), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE);
        characters.add(loadAntiTankGun1(UUID.fromString("f47db5e3-f07c-4bbc-8cb1-52263131a7a2"), germanBase.add(22, 0, 18), Country.GERMAN, gv1));
        
        CharacterMessage gv2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(81, 0, -10), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl2.getId(), false);
        loadSquad(characters, gv2, germanBase.add(82, 0, -11), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE);
        characters.add(loadHalfTrack(germanBase.add(83, 0, -14), Country.GERMAN, gv2));
        
        CharacterMessage gv3 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(30, 0, 10), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl2.getId(), false);
        loadSquad(characters, gv3, germanBase.add(25, 0, 15), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        
        CharacterMessage gv4 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(35, 0, 10), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl2.getId(), false);
        loadSquad(characters, gv4, germanBase.add(25, 0, 15), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE);
        
        CharacterMessage b1 = new CharacterMessage(UUID.fromString("d993932f-a185-4a6f-8d86-4ef6e2c5ff95"), CharacterType.AVATAR, americanBase.add(-10, 0, 10), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al1.getId(), false);
        loadSquad(characters, b1, americanBase.add(-10, 0, 15), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE);
        characters.add(loadAntiTankGun(americanBase.add(15, 0, 15), Country.AMERICAN, b1));
        //characters.add(loadAmoredCar(americanBase.add(10, 0, 15), Country.AMERICAN, b1));
        //characters.add(loadAntiTankGun1(UUID.randomUUID(), americanBase.add(20, 0, 20), Country.AMERICAN, b1));
//        b1.addObjective(UUID.randomUUID(), createBootCampObjective(new Vector(-57.21826f, 96.380104f, -203.38945f)));
//        b1.incrementKills(UUID.randomUUID());
//        b1.incrementKills(UUID.randomUUID());
//        b1.incrementKills(UUID.randomUUID());
//        b1.incrementKills(UUID.randomUUID());
//        b1.incrementKills(UUID.randomUUID());
        
        CharacterMessage b2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(20, 0, -25), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al1.getId(), false);
        loadSquad(characters,  b2, americanBase.add(25, 0, -25), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.MG42);
        
        CharacterMessage b3 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(22, 5, -30), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al1.getId(), false);
        loadSquad(characters, b3, americanBase.add(21, 5, -39), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        
        CharacterMessage b4 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(24, 5, -38), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al1.getId(), false);
        loadSquad(characters, b4, americanBase.add(23, 5, -39), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.MORTAR);
        
        CharacterMessage c1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(9, 5, -9), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al2.getId(), false);
        loadSquad(characters, c1, americanBase.add(6, 5, -9), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE);
        characters.add(loadAntiTankGun1(UUID.fromString("00b5ceda-4a0a-490d-af71-bcaf81ede6eb"), americanBase.add(9, 5, -9), Country.AMERICAN, c1));
        
        CharacterMessage c2 = new CharacterMessage(UUID.fromString("844fd93d-e65a-438a-82c5-dab9ad58e854"), CharacterType.AVATAR, americanBase.add(40, 5, -40), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al2.getId(), false);
        loadSquad(characters, c2, americanBase.add(45, 5, -35), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE);
        characters.add(loadAmoredCar(americanBase.add(40, 5, -30), Country.AMERICAN, c2));
        
        CharacterMessage c3 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(19, 5, -35), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al2.getId(), false);
        loadSquad(characters, c3, americanBase.add(25, 5, -30), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        
        CharacterMessage c4 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(20, 5, -35), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al2.getId(), false);
        loadSquad(characters,  c4, americanBase.add(20, 5, -30), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        
        gl1.addCharactersUnderCommand(a1, a2, a3, a4);
        gl2.addCharactersUnderCommand(gv1, gv2, gv3, gv4);
        al1.addCharactersUnderCommand(b1, b2, b3, b4);
        al2.addCharactersUnderCommand(c1, c2, c3, c4);
        
        a.addCharactersUnderCommand(gl1, gl2);
        b.addCharactersUnderCommand(al1, al2);
        
        characters.stream().forEach(c -> characterDAO.putCharacter(c.getId(), c));

        
        
		Set<HouseMessage> houses = new HashSet<HouseMessage>();

        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(85.84874f, 97.170715f, 33.869347f), new Quaternion(0.0f, -0.34439534f, 0.0f, -0.93882906f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(103.148895f, 96.96666f, 40.908333f), new Quaternion(0.0f, 0.029995233f, 0.0f, 0.9995505f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(102.68075f, 97.14429f, 32.157806f), new Quaternion(0.0f, 0.029995233f, 0.0f, 0.9995505f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(95.7744f, 97.14428f, 24.304264f), new Quaternion(0.0f, 0.6925375f, 0.0f, 0.72138315f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(135.48383f, 97.125145f, -3.50099f), new Quaternion(0.0f, -0.62489754f, 0.0f, 0.78070724f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(81.365456f, 97.181625f, 86.72022f), new Quaternion(0.0f, -0.91136926f, 0.0f, -0.41159576f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(216.74788f, 94.7688f, 16.502869f), new Quaternion(0.0f, -0.69253737f, 0.0f, 0.721383f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(128.95618f, 97.579605f, -11.019497f), new Quaternion(0.0f, -0.96871597f, 0.0f, 0.24817565f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(86.982994f, 97.87142f, 77.44998f), new Quaternion(0.0f, -0.9373703f, 0.0f, 0.34833753f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(147.14592f, 96.03496f, 64.1337f), new Quaternion(0.0f, 0.7032796f, 0.0f, 0.71091384f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(155.8704f, 96.24277f, 51.39864f), new Quaternion(0.0f, 0.654415f, 0.0f, -0.75613797f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(154.63408f, 96.4922f, 41.82982f), new Quaternion(0.0f, 0.9802265f, 0.0f, -0.19788925f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(177.0471f, 97.52623f, -14.900481f), new Quaternion(0.0f, 0.19376664f, 0.0f, 0.9810479f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(154.38177f, 96.11536f, 57.028408f), new Quaternion(0.0f, -0.810498f, 0.0f, 0.5857435f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(139.58846f, 94.85659f, 63.706833f), new Quaternion(0.0f, 0.994925f, 0.0f, 0.100626245f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(217.47525f, 94.17421f, 30.423859f), new Quaternion(0.0f, 0.988136f, 0.0f, -0.15359321f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(82.85497f, 98.08014f, 70.971756f), new Quaternion(0.0f, 0.93441f, 0.0f, -0.35620642f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(121.98426f, 97.21556f, -10.843002f), new Quaternion(0.0f, -0.58233356f, 0.0f, -0.8129547f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(115.24847f, 96.95665f, -11.7784395f), new Quaternion(0.0f, -0.58233356f, 0.0f, -0.8129547f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(168.07709f, 97.09564f, -10.525066f), new Quaternion(0.0f, -0.22953017f, 0.0f, -0.97330856f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(171.95143f, 97.59071f, 2.871109f), new Quaternion(0.0f, 0.8191927f, 0.0f, 0.57352084f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(177.42531f, 97.59072f, 0.11059642f), new Quaternion(0.0f, 0.8441628f, 0.0f, 0.5360889f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(183.46837f, 98.10595f, -3.0439007f), new Quaternion(0.0f, 0.8191942f, 0.0f, 0.57352144f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(223.25099f, 93.86209f, 23.9045f), new Quaternion(0.0f, -0.6925382f, 0.0f, 0.72138405f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(208.8207f, 94.17421f, 33.08174f), new Quaternion(0.0f, 0.988136f, 0.0f, -0.15359321f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(209.42502f, 94.7169f, 16.131506f), new Quaternion(0.0f, -0.69253737f, 0.0f, 0.721383f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(74.69646f, 97.181625f, 79.55486f), new Quaternion(0.0f, -0.91136926f, 0.0f, -0.41159576f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(86.48432f, 97.170715f, 42.465282f), new Quaternion(0.0f, -0.34439534f, 0.0f, -0.93882906f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(86.48432f, 97.170715f, 42.465282f), new Quaternion(0.0f, -0.34439534f, 0.0f, -0.93882906f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(-75.20234f, 97.416245f, 250.80757f), new Quaternion(0.0f, -0.9949262f, 0.0f, 0.10062607f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(-61.854595f, 96.94649f, 238.05736f), new Quaternion(0.0f, -0.8819598f, 0.0f, 0.47133f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(-80.67092f, 97.02508f, 245.43098f), new Quaternion(0.0f, -0.91136926f, 0.0f, -0.41159576f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(-78.9443f, 96.92278f, 236.21306f), new Quaternion(0.0f, -0.51978105f, 0.0f, -0.8543024f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(155.8704f, 96.24277f, 51.39864f), new Quaternion(0.0f, 0.654415f, 0.0f, -0.75613797f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(-68.01026f, 97.39159f, 247.97997f), new Quaternion(0.0f, 0.905864f, 0.0f, -0.42357722f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(70.23832f, 97.67574f, 65.6307f), new Quaternion(0.0f, -0.4000708f, 0.0f, -0.91648775f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(334.65643f, 96.39504f, 130.91393f), new Quaternion(0.0f, 0.654415f, 0.0f, -0.75613797f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(327.71066f, 95.96161f, 120.342834f), new Quaternion(0.0f, 0.988136f, 0.0f, -0.15359321f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(329.68738f, 95.94789f, 110.10412f), new Quaternion(0.0f, 0.988136f, 0.0f, -0.15359321f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(330.8568f, 95.930984f, 100.39261f), new Quaternion(0.0f, 0.988136f, 0.0f, -0.15359321f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(343.5364f, 97.095634f, 113.355774f), new Quaternion(0.0f, 0.7402314f, 0.0f, -0.67236257f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(124.81777f, 100.93056f, -286.83356f), new Quaternion(0.0f, 0.44252115f, 0.0f, -0.89676046f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(106.760475f, 100.95137f, -347.2597f), new Quaternion(0.0f, 0.25861996f, 0.0f, -0.96598136f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(116.51121f, 100.05997f, -336.4931f), new Quaternion(0.0f, 0.86321133f, 0.0f, -0.5048469f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(-238.60928f, 96.69255f, -193.10406f), new Quaternion(0.0f, 0.654415f, 0.0f, -0.75613797f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(118.80721f, 100.33241f, -261.75403f), new Quaternion(0.0f, 0.8889288f, 0.0f, 0.458047f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(115.681656f, 100.06957f, -362.59695f), new Quaternion(0.0f, -0.081503056f, 0.0f, -0.9966777f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(-248.21228f, 96.112724f, -177.75186f), new Quaternion(-9.313226E-10f, -0.9474002f, 0.0f, 0.32006508f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(-256.1153f, 97.590614f, -170.75073f), new Quaternion(0.0f, -0.8555407f, 0.0f, -0.51773745f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(-291.69894f, 100.21826f, -242.43237f), new Quaternion(0.0f, 0.5944603f, 0.0f, -0.80412745f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(112.42636f, 100.33241f, -269.3277f), new Quaternion(0.0f, 0.8889288f, 0.0f, 0.458047f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(106.51669f, 100.33241f, -277.8734f), new Quaternion(0.0f, 0.8889288f, 0.0f, 0.458047f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(130.1249f, 100.93056f, -280.80975f), new Quaternion(0.0f, 0.44252115f, 0.0f, -0.89676046f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(110.09275f, 100.951355f, -355.61517f), new Quaternion(0.0f, 0.25861996f, 0.0f, -0.96598136f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(121.719955f, 100.05996f, -346.12006f), new Quaternion(0.0f, 0.86321133f, 0.0f, -0.5048469f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(74.035934f, 100.06957f, -361.57007f), new Quaternion(0.0f, 0.5084724f, 0.0f, -0.8610839f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(79.44162f, 100.06957f, -352.27124f), new Quaternion(0.0f, 0.5084724f, 0.0f, -0.8610839f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(67.942795f, 100.13351f, -344.39246f), new Quaternion(0.0f, 0.84416354f, 0.0f, 0.5360895f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(63.96949f, 100.13351f, -353.38663f), new Quaternion(0.0f, 0.84416354f, 0.0f, 0.5360895f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(67.51045f, 100.793396f, -365.83734f), new Quaternion(0.0f, -0.3018129f, 0.0f, -0.95336974f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(44.139374f, 100.95136f, -328.6428f), new Quaternion(0.0f, 0.66432834f, 0.0f, -0.74744415f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(50.925816f, 100.951355f, -328.6524f), new Quaternion(0.0f, 0.66432834f, 0.0f, -0.74744415f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(57.769135f, 100.95135f, -328.12537f), new Quaternion(0.0f, 0.66432834f, 0.0f, -0.74744415f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(37.655777f, 100.15298f, -316.66983f), new Quaternion(0.0f, 0.8889288f, 0.0f, 0.458047f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(50.668335f, 100.79339f, -316.29382f), new Quaternion(0.0f, -0.67546517f, 0.0f, -0.73739547f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(-241.23492f, 96.11273f, -186.65125f), new Quaternion(-9.186741E-10f, -0.88196206f, -1.5297202E-10f, 0.4713309f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(-261.9796f, 97.590614f, -167.89899f), new Quaternion(0.0f, -0.8555407f, 0.0f, -0.51773745f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(-295.37708f, 100.21827f, -251.92232f), new Quaternion(0.0f, 0.5944603f, 0.0f, -0.80412745f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(-357.4186f, 96.49775f, -210.37169f), new Quaternion(0.0f, 0.9520927f, 0.0f, 0.30581787f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(-310.41464f, 96.692535f, -236.13068f), new Quaternion(0.0f, -0.74902904f, 0.0f, -0.66254085f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(-314.04636f, 97.590614f, -240.68694f), new Quaternion(0.0f, -0.12625504f, 0.0f, -0.9919999f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(-317.69565f, 97.59062f, -247.95282f), new Quaternion(0.0f, -0.12625504f, 0.0f, -0.9919999f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(-364.74133f, 96.49776f, -221.56511f), new Quaternion(0.0f, 0.6365387f, 0.0f, 0.77124834f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(-356.24576f, 96.11275f, -228.16621f), new Quaternion(-4.5122994E-10f, -0.17903028f, -8.1471113E-10f, 0.9838485f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(-346.6033f, 96.339905f, -224.18909f), new Quaternion(-4.5122994E-10f, -0.17903028f, -8.1471113E-10f, 0.9838485f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(-336.2033f, 96.112755f, -219.12177f), new Quaternion(-4.5122994E-10f, -0.17903028f, -8.1471113E-10f, 0.9838485f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(-330.469f, 96.59102f, -167.56113f), new Quaternion(0.0f, -0.74902904f, 0.0f, -0.66254085f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(-328.93063f, 96.59101f, -175.9047f), new Quaternion(0.0f, -0.74902904f, 0.0f, -0.66254085f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(-331.56793f, 97.07747f, -156.47841f), new Quaternion(0.0f, 0.008407291f, 0.0f, -0.9999668f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(-330.9898f, 96.112755f, -146.50629f), new Quaternion(4.086152E-10f, 0.7032834f, -8.368974E-10f, 0.7109173f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(-340.83585f, 97.07747f, -156.5674f), new Quaternion(0.0f, 0.008407291f, 0.0f, -0.9999668f)));
       
        
        houses.forEach(h->housesDAO.putHouse(h.getId(), h));
        
        //Set<TreeMessage> trees = new HashSet<TreeMessage>();
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
	
	private CharacterMessage loadAmoredCar(Vector vector, Country country, CharacterMessage c2) {
		CharacterMessage armoredCar = new CharacterMessage(UUID.randomUUID(), CharacterType.ARMORED_CAR, vector, country, CharacterType.ARMORED_CAR.getDefaultWeapon(), RankMessage.PRIVATE, c2.getId(), false);
		c2.addCharactersUnderCommand(armoredCar);
		return armoredCar;
	}

	private CharacterMessage loadHalfTrack(Vector vector, Country country, CharacterMessage gv2) {
		return loadHalfTrack1(UUID.randomUUID(), vector, country, gv2);
	}



	private CharacterMessage loadHalfTrack1(UUID randomUUID, Vector vector, Country country, CharacterMessage gv2) {
		CharacterMessage halfTrack = new CharacterMessage(randomUUID, CharacterType.HALF_TRACK, vector, country, CharacterType.HALF_TRACK.getDefaultWeapon(), RankMessage.PRIVATE, gv2.getId(), false);
		gv2.addCharactersUnderCommand(halfTrack);
		return halfTrack;
	}

	private CharacterMessage loadAntiTankGun(Vector vector, Country country, CharacterMessage gv1) {
		return loadAntiTankGun1(UUID.randomUUID(), vector, country, gv1);
	}



	private CharacterMessage loadAntiTankGun1(UUID randomUUID, Vector vector, Country country, CharacterMessage gv1) {
		CharacterMessage atg = new CharacterMessage(randomUUID, CharacterType.ANTI_TANK_GUN, vector, country, CharacterType.ANTI_TANK_GUN.getDefaultWeapon(), RankMessage.PRIVATE, gv1.getId(), false);
		gv1.addCharactersUnderCommand(atg);
		return atg;
	}

	public void loadSquad(Set<CharacterMessage> characters, CharacterMessage a1, Vector vector3f, Country country, boolean folllowCommander, Weapon...weapons) throws JsonGenerationException, JsonMappingException, IOException {
        characters.add(a1);
		Set<CharacterMessage> cc = new HashSet<CharacterMessage>();
        int i = 0;
        Vector3f offSet = new Vector3f(2, 0, 2);
        for(Weapon w:weapons){
            CharacterMessage e = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, vector3f.add(i*3, 0, 0), country, w, RankMessage.PRIVATE, a1.getId(), false);
            if(folllowCommander){
            	e.addObjective(UUID.randomUUID(), new FollowUnit(a1.getId(), new Vector(offSet), 10).asJSON());
            	offSet = offSet.add(2, 0, 2);
            }
			cc.add(e);
            i++;
        }
        a1.addCharactersUnderCommand(cc);
        characters.addAll(cc);
    }

}
