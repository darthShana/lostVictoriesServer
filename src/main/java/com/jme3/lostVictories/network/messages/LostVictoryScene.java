package com.jme3.lostVictories.network.messages;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;
import lostVictories.dao.TreeDAO;


import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jme3.lostVictories.objectives.CaptureTown;
import com.jme3.lostVictories.objectives.FollowCommander;
import com.jme3.math.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LostVictoryScene {
	
	public static int SCENE_WIDTH = 512;
	public static int SCENE_HEIGHT = 512;
	public static float SCENE_SCALE = .25f;
	public static Vector germanVehicleSpawnPoint = new Vector(257, 96, 31);
    public static Vector germanBase = new Vector(150, 100, 20);
	public static Vector americanVehicleSpawnPoint = new Vector(50, 101, -366);
    public static Vector americanBase = new Vector(90, 100, -380);
	
	private static Logger log = LoggerFactory.getLogger(LostVictoryScene.class);
	
	public void loadScene(CharacterDAO characterDAO, HouseDAO housesDAO, TreeDAO treeDAO) throws JsonGenerationException, JsonMappingException, IOException {
		log.debug("Loading Scene");
		
		Map<UUID, CharacterMessage> characters = new HashMap<>();
		
		
		CharacterMessage a = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase, Country.GERMAN, Weapon.RIFLE, RankMessage.COLONEL, null);
		a.addObjective(UUID.randomUUID(), new CaptureTown());
		CharacterMessage b = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase, Country.AMERICAN, Weapon.RIFLE, RankMessage.COLONEL, null);
		b.addObjective(UUID.randomUUID(), new CaptureTown());
		
		CharacterMessage gl1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(5, 0, 5), Country.GERMAN, Weapon.RIFLE, RankMessage.LIEUTENANT, a.getId());
		CharacterMessage gl2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(10, 0, 5), Country.GERMAN, Weapon.RIFLE, RankMessage.LIEUTENANT, a.getId());
		CharacterMessage gl3 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(15, 0, 5), Country.GERMAN, Weapon.RIFLE, RankMessage.LIEUTENANT, a.getId());
		CharacterMessage gl4 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(20, 0, 5), Country.GERMAN, Weapon.RIFLE, RankMessage.LIEUTENANT, a.getId());
		CharacterMessage gl5 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(20, 0, 5), Country.GERMAN, Weapon.RIFLE, RankMessage.LIEUTENANT, a.getId());

		CharacterMessage al1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(5, 0, 5), Country.AMERICAN, Weapon.RIFLE, RankMessage.LIEUTENANT, b.getId());
		CharacterMessage al2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(-18, 0, 100), Country.AMERICAN, Weapon.RIFLE, RankMessage.LIEUTENANT, b.getId());
		CharacterMessage al3 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(-57, 0, 44), Country.AMERICAN, Weapon.RIFLE, RankMessage.LIEUTENANT, b.getId());
		CharacterMessage al4 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(-5, 0, 60), Country.AMERICAN, Weapon.RIFLE, RankMessage.LIEUTENANT, b.getId());
		CharacterMessage al5 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(-5, 0, 60), Country.AMERICAN, Weapon.RIFLE, RankMessage.LIEUTENANT, b.getId());

		characters.put(a.getId(), a);
		characters.put(b.getId(), b);
		characters.put(gl1.getId(), gl1);
		characters.put(gl2.getId(), gl2);
		characters.put(gl3.getId(), gl3);
		characters.put(gl4.getId(), gl4);
		characters.put(gl5.getId(), gl5);

		characters.put(al1.getId(), al1);
		characters.put(al2.getId(), al2);
		characters.put(al3.getId(), al3);
		characters.put(al4.getId(), al4);
		characters.put(al5.getId(), al5);

		CharacterMessage a1 = new CharacterMessage(UUID.fromString("2fbe421f-f701-49c9-a0d4-abb0fa904204"), CharacterType.AVATAR, germanBase.add(22, 0, 13), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl1.getId());
		a1.userID = UUID.fromString("2fbe421f-f701-49c9-a0d4-abb0fa904204");
		loadSquad(characters, a1, germanBase.add(-10, 0, 15), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE   );
//		CharacterMessage panzer4 = loadPanzer4(UUID.fromString("ce0e6166-7299-4222-9f1a-938cdc9b24cb"), germanBase.add(-12, 0, 15), Country.GERMAN, a1, characters);
//        characters.put(panzer4.getId(), panzer4);
//		characters.add(loadAntiTankGun1(UUID.fromString("2d420131-2f1f-4901-b61a-248c2243848c"), germanBase.add(25, 0, 15), Country.GERMAN, a1));
//		CharacterMessage hf2 = loadHalfTrack1(UUID.fromString("9740bc8a-835d-4fa2-ab2b-6ed8d914e6ef"), germanBase.add(25, 0, 15), Country.GERMAN, a1, characters);
//		characters.put(hf2.getId(), hf2);

//		a1.addObjective(UUID.randomUUID(), createBootCampObjective(new Vector(246.29144f, 96.77546f, 55.412266f)));
//		a1.incrementKills(UUID.randomUUID());
//		a1.incrementKills(UUID.randomUUID());
//		a1.incrementKills(UUID.randomUUID());	
//		a1.incrementKills(UUID.randomUUID());
//		a1.incrementKills(UUID.randomUUID());
		
		CharacterMessage a2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(30, 0, 15), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl1.getId());
        loadSquad(characters, a2, germanBase.add(30, 0, 15), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.MG42);
        
        CharacterMessage a3 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(35, 0, 15), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl2.getId());
        loadSquad(characters, a3, germanBase.add(35, 0, 15), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        
        CharacterMessage a4 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(40, 0, 15), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl2.getId());
        loadSquad(characters, a4, germanBase.add(40, 0, 15), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.MORTAR);
        
        CharacterMessage a5 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(35, 0, 15), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl2.getId());
        loadSquad(characters, a5, germanBase.add(35, 0, 15), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        
        CharacterMessage gv1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(20, 0, 16), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl3.getId());
        loadSquad(characters, gv1, germanBase.add(21, 0, 17), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        CharacterMessage loadAntiTankGun1 = loadAntiTankGun1(UUID.fromString("f47db5e3-f07c-4bbc-8cb1-52263131a7a2"), germanBase.add(22, 0, 18), Country.GERMAN, gv1, characters);
		characters.put(loadAntiTankGun1.getId(), loadAntiTankGun1);
        
        CharacterMessage gv2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(81, 0, -10), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl3.getId());
        loadSquad(characters, gv2, germanBase.add(82, 0, -11), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.MG42);
        CharacterMessage loadHalfTrack = loadHalfTrack(germanBase.add(83, 0, -14), Country.GERMAN, gv2, characters);
		characters.put(loadHalfTrack.getId(), loadHalfTrack);
        
        CharacterMessage gv3 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(30, 0, 10), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl4.getId());
        loadSquad(characters, gv3, germanBase.add(25, 0, 15), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        
        CharacterMessage gv4 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(35, 0, 10), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl4.getId());
        loadSquad(characters, gv4, germanBase.add(25, 0, 15), Country.GERMAN, true, Weapon.RIFLE, Weapon.BAZOOKA, Weapon.MORTAR);

        CharacterMessage gv5 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(22, 0, 10), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl5.getId());
        loadSquad(characters, gv5, germanBase.add(20, 0, 15), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);

        CharacterMessage gv6 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(26, 0, 10), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl5.getId());
        loadSquad(characters, gv6, germanBase.add(25, 0, 15), Country.GERMAN, true, Weapon.RIFLE, Weapon.MORTAR, Weapon.BAZOOKA);

        
        
        CharacterMessage b1 = new CharacterMessage(UUID.fromString("d993932f-a185-4a6f-8d86-4ef6e2c5ff95"), CharacterType.AVATAR, americanBase.add(-10, 0, 10), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al1.getId());
        b1.userID = UUID.fromString("d993932f-a185-4a6f-8d86-4ef6e2c5ff95");
        loadSquad(characters, b1, americanBase.add(-10, 0, 15), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.MG42);
//        CharacterMessage loadAntiTankGun = loadAntiTankGun(americanBase.add(15, 0, 15), Country.AMERICAN, b1, characters);
//		characters.put(loadAntiTankGun.getId(), loadAntiTankGun);
//        CharacterMessage m4a2 = loadM4A2Sherman(UUID.fromString("16a67c6b-263b-4d74-a7a8-b6d28f014d28"), americanBase.add(-12, 0, 15), Country.AMERICAN, b1, characters);
//        characters.put(m4a2.getId(), m4a2);

//        CharacterMessage loadAmoredCar2 = loadAmoredCar(americanBase.add(10, 0, 15), Country.AMERICAN, b1, characters);
//		characters.put(loadAmoredCar2.getId(), loadAmoredCar2);
//        b1.addObjective(UUID.randomUUID(), createBootCampObjective(new Vector(-57.21826f, 96.380104f, -203.38945f)));
//        b1.incrementKills(UUID.randomUUID());
//        b1.incrementKills(UUID.randomUUID());
//        b1.incrementKills(UUID.randomUUID());
//        b1.incrementKills(UUID.randomUUID());
//        b1.incrementKills(UUID.randomUUID());
        
        CharacterMessage b2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(20, 0, -25), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al1.getId());
        loadSquad(characters,  b2, americanBase.add(25, 0, -25), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.MG42);
        
        CharacterMessage b3 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(-18, 0, 95), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al2.getId());
        loadSquad(characters, b3, americanBase.add(-15, 0, 95), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        
        CharacterMessage b4 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(-18, 0, 105), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al2.getId());
        loadSquad(characters, b4, americanBase.add(-15, 0, 105), Country.AMERICAN, true, Weapon.RIFLE, Weapon.BAZOOKA, Weapon.MORTAR);
        
        CharacterMessage c1 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(-50, 0, 40), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al3.getId());
        loadSquad(characters, c1, americanBase.add(6, 5, -9), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        CharacterMessage loadAntiTankGun12 = loadAntiTankGun1(UUID.fromString("00b5ceda-4a0a-490d-af71-bcaf81ede6eb"), americanBase.add(-55, 0, 45), Country.AMERICAN, c1, characters);
		characters.put(loadAntiTankGun12.getId(), loadAntiTankGun12);
        
        CharacterMessage c2 = new CharacterMessage(UUID.fromString("844fd93d-e65a-438a-82c5-dab9ad58e854"), CharacterType.SOLDIER, americanBase.add(40, 5, -40), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al3.getId());
        c2.id = UUID.fromString("844fd93d-e65a-438a-82c5-dab9ad58e854");

        loadSquad(characters, c2, americanBase.add(-55, 0, 45), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        CharacterMessage loadAmoredCar = loadAmoredCar(americanBase.add(-58, 0, 40), Country.AMERICAN, c2, characters);
		characters.put(loadAmoredCar.getId(), loadAmoredCar);
        
        CharacterMessage c3 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(19, 5, -35), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al4.getId());
        loadSquad(characters, c3, americanBase.add(25, 5, -30), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        
        CharacterMessage c4 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(20, 5, -35), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al4.getId());
        loadSquad(characters,  c4, americanBase.add(20, 5, -30), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        
        CharacterMessage c5 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(15, 5, -30), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al4.getId());
        loadSquad(characters,  c5, americanBase.add(15, 5, -25), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);

        CharacterMessage c6 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(22, 5, -35), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al5.getId());
        loadSquad(characters,  c6, americanBase.add(22, 5, -30), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);

        CharacterMessage c7 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(17, 5, -30), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al5.getId());
        loadSquad(characters,  c7, americanBase.add(17, 5, -25), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.BAZOOKA);

        gl1.addCharactersUnderCommand(a1, a2);
        gl2.addCharactersUnderCommand(a3, a4, a5);
        gl3.addCharactersUnderCommand(gv1, gv2);
        gl4.addCharactersUnderCommand(gv3, gv4);
        al1.addCharactersUnderCommand(b1, b2);
        al2.addCharactersUnderCommand(b3, b4);
        al3.addCharactersUnderCommand(c1, c2);
        al4.addCharactersUnderCommand(c3, c4, c5);
        
        a.addCharactersUnderCommand(gl1, gl2, gl3, gl4, gl5);
        b.addCharactersUnderCommand(al1, al2, al3, al4, al5);
        
        characters.values().stream().forEach(c -> characterDAO.putCharacter(c.getId(), c));

        
        
		Set<HouseMessage> houses = new HashSet<>();

        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(85.84874f, 97.170715f, 33.869347f), new Quaternion(0.0f, -0.34439534f, 0.0f, -0.93882906f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(103.148895f, 96.96666f, 40.908333f), new Quaternion(0.0f, 0.029995233f, 0.0f, 0.9995505f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(102.68075f, 97.14429f, 32.157806f), new Quaternion(0.0f, 0.029995233f, 0.0f, 0.9995505f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(95.7744f, 97.14428f, 24.304264f), new Quaternion(0.0f, 0.6925375f, 0.0f, 0.72138315f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(135.48383f, 97.125145f, -3.50099f), new Quaternion(0.0f, -0.62489754f, 0.0f, 0.78070724f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(81.365456f, 97.181625f, 86.72022f), new Quaternion(0.0f, -0.91136926f, 0.0f, -0.41159576f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(216.74788f, 94.7688f, 16.502869f), new Quaternion(0.0f, -0.69253737f, 0.0f, 0.721383f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(132.6822f, 98.17972f, -11.386422f), new Quaternion(0.0f, -0.96871597f, 0.0f, 0.24817565f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(86.982994f, 97.87142f, 77.44998f), new Quaternion(0.0f, -0.9373703f, 0.0f, 0.34833753f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(147.14592f, 96.03496f, 64.1337f), new Quaternion(0.0f, 0.7032796f, 0.0f, 0.71091384f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(154.63408f, 96.4922f, 41.82982f), new Quaternion(0.0f, 0.9802265f, 0.0f, -0.19788925f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(173.82977f, 98.228294f, -22.97573f), new Quaternion(0.0f, 0.19376664f, 0.0f, 0.9810479f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(154.38177f, 96.11536f, 57.028408f), new Quaternion(0.0f, -0.810498f, 0.0f, 0.5857435f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(139.58846f, 94.85659f, 63.706833f), new Quaternion(0.0f, 0.994925f, 0.0f, 0.100626245f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(212.9072f, 94.03517f, 30.799404f), new Quaternion(0.0f, 0.988136f, 0.0f, -0.15359321f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(82.85497f, 98.08014f, 70.971756f), new Quaternion(0.0f, 0.93441f, 0.0f, -0.35620642f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(121.98426f, 97.21556f, -10.843002f), new Quaternion(0.0f, -0.58233356f, 0.0f, -0.8129547f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(115.24847f, 96.95665f, -11.7784395f), new Quaternion(0.0f, -0.58233356f, 0.0f, -0.8129547f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(162.03284f, 97.09564f, -18.269882f), new Quaternion(0.0f, -0.22953017f, 0.0f, -0.97330856f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(171.95143f, 97.59071f, 2.871109f), new Quaternion(0.0f, 0.8191927f, 0.0f, 0.57352084f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(177.42531f, 97.59072f, 0.11059642f), new Quaternion(0.0f, 0.8441628f, 0.0f, 0.5360889f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(183.46837f, 98.10595f, -3.0439007f), new Quaternion(0.0f, 0.8191942f, 0.0f, 0.57352144f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(223.25099f, 93.86209f, 23.9045f), new Quaternion(0.0f, -0.6925382f, 0.0f, 0.72138405f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(200.25636f, 94.17422f, 35.49746f), new Quaternion(0.0f, 0.988136f, 0.0f, -0.15359321f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(209.42502f, 94.7169f, 16.131506f), new Quaternion(0.0f, -0.69253737f, 0.0f, 0.721383f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(74.90507f, 97.31726f, 79.37102f), new Quaternion(0.0f, 0.91136694f, 0.0f, 0.41159478f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(86.48432f, 97.170715f, 42.465282f), new Quaternion(0.0f, -0.34439534f, 0.0f, -0.93882906f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(-74.956856f, 97.71145f, 250.75462f), new Quaternion(0.0f, -0.7780751f, 0.0f, -0.628175f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(-61.854595f, 96.94649f, 238.05736f), new Quaternion(0.0f, -0.8819598f, 0.0f, 0.47133f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(-80.67092f, 97.02508f, 245.43098f), new Quaternion(0.0f, -0.91136926f, 0.0f, -0.41159576f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(-78.9443f, 96.92278f, 236.21306f), new Quaternion(0.0f, -0.51978105f, 0.0f, -0.8543024f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(160.4662f, 95.67944f, 52.367104f), new Quaternion(0.0f, -0.9884112f, 0.0f, 0.15180023f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(-67.67609f, 97.84121f, 248.27179f), new Quaternion(0.0f, 0.9258184f, 0.0f, 0.37797937f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(62.595802f, 98.19941f, 70.25858f), new Quaternion(0.0f, 0.38795218f, 0.0f, -0.9216832f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(334.66644f, 96.93109f, 131.02231f), new Quaternion(0.0f, 0.73900586f, 0.0f, 0.6736998f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(327.71066f, 95.96161f, 120.342834f), new Quaternion(0.0f, 0.988136f, 0.0f, -0.15359321f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(329.68738f, 95.94789f, 110.10412f), new Quaternion(0.0f, 0.988136f, 0.0f, -0.15359321f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(330.8568f, 95.930984f, 100.39261f), new Quaternion(0.0f, 0.988136f, 0.0f, -0.15359321f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(343.5364f, 97.095634f, 113.355774f), new Quaternion(0.0f, 0.7402314f, 0.0f, -0.67236257f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(123.548294f, 101.01445f, -286.72476f), new Quaternion(0.0f, 0.9707699f, 0.0f, -0.24002326f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(106.760475f, 100.95137f, -347.2597f), new Quaternion(0.0f, 0.25861996f, 0.0f, -0.96598136f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(116.51121f, 100.05997f, -336.4931f), new Quaternion(0.0f, 0.86321133f, 0.0f, -0.5048469f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(-235.40268f, 97.1148f, -193.67079f), new Quaternion(0.0f, 0.99909425f, 0.0f, 0.042597845f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(118.80721f, 100.33241f, -261.75403f), new Quaternion(0.0f, 0.8889288f, 0.0f, 0.458047f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(115.681656f, 100.06957f, -362.59695f), new Quaternion(0.0f, -0.081503056f, 0.0f, -0.9966777f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(-248.21228f, 96.112724f, -177.75186f), new Quaternion(-9.313226E-10f, -0.9474002f, 0.0f, 0.32006508f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(-256.1153f, 97.590614f, -170.75073f), new Quaternion(0.0f, -0.8555407f, 0.0f, -0.51773745f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(-291.6729f, 96.23219f, -242.44035f), new Quaternion(0.0f, 0.5944603f, 0.0f, -0.80412745f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(112.42636f, 100.33241f, -269.3277f), new Quaternion(0.0f, 0.8889288f, 0.0f, 0.458047f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(106.51669f, 100.33241f, -277.8734f), new Quaternion(0.0f, 0.8889288f, 0.0f, 0.458047f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(130.08974f, 101.14649f, -278.54166f), new Quaternion(0.0f, 0.9452684f, 0.0f, -0.32630324f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(110.09275f, 100.951355f, -355.61517f), new Quaternion(0.0f, 0.25861996f, 0.0f, -0.96598136f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(121.79952f, 100.50152f, -346.0753f), new Quaternion(0.0f, 0.86321133f, 0.0f, -0.5048469f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(74.035934f, 100.06957f, -361.57007f), new Quaternion(0.0f, 0.5084724f, 0.0f, -0.8610839f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(79.44162f, 100.06957f, -352.27124f), new Quaternion(0.0f, 0.5084724f, 0.0f, -0.8610839f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(67.942795f, 100.13351f, -344.39246f), new Quaternion(0.0f, 0.84416354f, 0.0f, 0.5360895f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(63.96949f, 100.13351f, -353.38663f), new Quaternion(0.0f, 0.84416354f, 0.0f, 0.5360895f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(67.63751f, 101.17485f, -365.89313f), new Quaternion(0.0f, -0.54669076f, 0.0f, 0.8373346f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(44.139374f, 100.95136f, -328.6428f), new Quaternion(0.0f, 0.66432834f, 0.0f, -0.74744415f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(50.925816f, 100.951355f, -328.6524f), new Quaternion(0.0f, 0.66432834f, 0.0f, -0.74744415f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(57.769135f, 100.95135f, -328.12537f), new Quaternion(0.0f, 0.66432834f, 0.0f, -0.74744415f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(37.655777f, 100.15298f, -316.66983f), new Quaternion(0.0f, 0.8889288f, 0.0f, 0.458047f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(47.208714f, 101.11962f, -314.56528f), new Quaternion(0.0f, -0.67546517f, 0.0f, -0.73739547f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(-241.23492f, 96.11273f, -186.65125f), new Quaternion(-9.186741E-10f, -0.88196206f, -1.5297202E-10f, 0.4713309f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(-261.9796f, 97.590614f, -167.89899f), new Quaternion(0.0f, -0.8555407f, 0.0f, -0.51773745f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(-294.70306f, 96.21984f, -252.12906f), new Quaternion(0.0f, 0.5944603f, 0.0f, -0.80412745f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(-357.4186f, 96.49775f, -210.37169f), new Quaternion(0.0f, 0.9520927f, 0.0f, 0.30581787f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(-310.2513f, 97.16935f, -232.86774f), new Quaternion(0.0f, -0.155954f, 0.0f, -0.98776776f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(-314.04636f, 97.590614f, -240.68694f), new Quaternion(0.0f, -0.12625504f, 0.0f, -0.9919999f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(-317.69565f, 97.59062f, -247.95282f), new Quaternion(0.0f, -0.12625504f, 0.0f, -0.9919999f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(-364.74133f, 96.49776f, -221.56511f), new Quaternion(0.0f, 0.6365387f, 0.0f, 0.77124834f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(-356.24576f, 96.11275f, -228.16621f), new Quaternion(-4.5122994E-10f, -0.17903028f, -8.1471113E-10f, 0.9838485f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(-346.6033f, 96.339905f, -224.18909f), new Quaternion(-4.5122994E-10f, -0.17903028f, -8.1471113E-10f, 0.9838485f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(-336.2033f, 96.112755f, -219.12177f), new Quaternion(-4.5122994E-10f, -0.17903028f, -8.1471113E-10f, 0.9838485f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(-330.44193f, 97.227974f, -167.19185f), new Quaternion(0.0f, -0.036584105f, 0.0f, -0.9993345f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(-331.11667f, 97.06876f, -176.90742f), new Quaternion(0.0f, -0.03658466f, 0.0f, -0.99933356f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(-331.56793f, 97.07747f, -156.47841f), new Quaternion(0.0f, 0.008407291f, 0.0f, -0.9999668f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(-330.9898f, 96.112755f, -146.50629f), new Quaternion(4.086152E-10f, 0.7032834f, -8.368974E-10f, 0.7109173f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(-340.83585f, 97.07747f, -156.5674f), new Quaternion(0.0f, 0.008407291f, 0.0f, -0.9999668f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(-315.2739f, 96.86195f, 16.324844f), new Quaternion(0.0f, 0.65463406f, 0.0f, 0.75594586f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(-303.69046f, 94.940025f, 19.279285f), new Quaternion(0.0f, 0.9995347f, 0.0f, 0.030498976f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(-291.807f, 96.11444f, 19.737808f), new Quaternion(0.0f, 0.9995177f, 0.0f, 0.031054825f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(-327.18765f, 96.25331f, 17.42927f), new Quaternion(4.15916E-7f, 0.65745425f, -5.627828E-7f, 0.75349444f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(-334.87363f, 95.96025f, 0.9493122f), new Quaternion(0.0f, 0.6960731f, 0.0f, 0.7179709f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(-332.2797f, 96.19855f, -17.737953f), new Quaternion(0.0f, 0.0f, 0.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(-332.37317f, 96.1994f, -27.538048f), new Quaternion(0.0f, 0.0f, 0.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(-331.9042f, 96.82576f, -40.902504f), new Quaternion(0.0f, 0.65357065f, 0.0f, 0.75686544f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(-332.10406f, 96.30746f, -54.241375f), new Quaternion(-9.880811E-7f, 0.6900288f, -1.2531933E-6f, 0.72378194f)));
        houses.forEach(h->housesDAO.putHouse(h));
        
        Set<TreeGroupMessage> trees = new HashSet<>();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("treeMap.json");
        JsonNode readTree = CharacterDAO.MAPPER.readTree(is);
		JsonNode jsonNode = readTree.get("trees");
		jsonNode.spliterator().forEachRemaining(treeGroup->trees.add(toObjectFromSource(treeGroup)));
        trees.forEach(t->treeDAO.putTree(t.getId(), t));
        	
        log.debug("scene loaded....");
	}
	


	private TreeGroupMessage toObjectFromSource(JsonNode treeGroup) {
		try {
			TreeGroupMessage treeToValue = CharacterDAO.MAPPER.treeToValue(treeGroup, TreeGroupMessage.class);
			treeToValue.setId(UUID.randomUUID());
			treeToValue.getTrees().stream().forEach(t->t.setId(UUID.randomUUID()));
			return treeToValue;
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}



	private String createBootCampObjective(final Vector vector)  {
		ObjectNode node = CharacterDAO.MAPPER.createObjectNode();
		try {
			node.put("class", "com.jme3.lostVictories.objectives.CompleteBootCamp");
			JsonNode valueToTree = CharacterDAO.MAPPER.valueToTree(vector);
			node.set("location", valueToTree);
			return CharacterDAO.MAPPER.writeValueAsString(node);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private CharacterMessage loadAmoredCar(Vector vector, Country country, CharacterMessage c2, Map<UUID, CharacterMessage> characters) {
		CharacterMessage armoredCar = new CharacterMessage(UUID.randomUUID(), CharacterType.ARMORED_CAR, vector, country, CharacterType.ARMORED_CAR.getDefaultWeapon(), RankMessage.PRIVATE, c2.getId());
		Optional<CharacterMessage> findFirst = c2.getUnitsUnderCommand().stream().map(i->characters.get(i))
				.filter(c->c.getCharacterType()==CharacterType.SOLDIER)
				.filter(c->c.boardedVehicle==null)
				.findFirst();
		c2.addCharactersUnderCommand(armoredCar);
		if(findFirst.isPresent()){
			armoredCar.passengers.add(findFirst.get().getId());
			findFirst.get().boardedVehicle = armoredCar.getId();
			findFirst.get().setLocation(vector);
		}
		return armoredCar;
	}

	private CharacterMessage loadHalfTrack(Vector vector, Country country, CharacterMessage gv2, Map<UUID, CharacterMessage> characters) {
		return loadHalfTrack1(UUID.randomUUID(), vector, country, gv2, characters);
	}



	private CharacterMessage loadHalfTrack1(UUID randomUUID, Vector vector, Country country, CharacterMessage gv2, Map<UUID, CharacterMessage> characters) {
		CharacterMessage halfTrack = new CharacterMessage(randomUUID, CharacterType.HALF_TRACK, vector, country, CharacterType.HALF_TRACK.getDefaultWeapon(), RankMessage.PRIVATE, gv2.getId());
		Optional<CharacterMessage> findFirst = gv2.getUnitsUnderCommand().stream().map(i->characters.get(i))
				.filter(c->c.getCharacterType()==CharacterType.SOLDIER)
				.filter(c->c.boardedVehicle==null)
				.findFirst();
		gv2.addCharactersUnderCommand(halfTrack);
		if(findFirst.isPresent()){
			halfTrack.passengers.add(findFirst.get().getId());
			findFirst.get().boardedVehicle = halfTrack.getId();
		}
		return halfTrack;
	}

    private CharacterMessage loadPanzer4(UUID randomUUID, Vector vector, Country country, CharacterMessage gv2, Map<UUID, CharacterMessage> characters) {
        CharacterMessage tank = new CharacterMessage(randomUUID, CharacterType.PANZER4, vector, country, CharacterType.PANZER4.getDefaultWeapon(), RankMessage.PRIVATE, gv2.getId());
        Optional<CharacterMessage> findFirst = gv2.getUnitsUnderCommand().stream().map(i->characters.get(i))
                .filter(c->c.getCharacterType()==CharacterType.SOLDIER)
                .filter(c->c.boardedVehicle==null)
                .findFirst();
        gv2.addCharactersUnderCommand(tank);
        if(findFirst.isPresent()){
            tank.passengers.add(findFirst.get().getId());
            findFirst.get().boardedVehicle = tank.getId();
        }
        return tank;
    }

    private CharacterMessage loadM4A2Sherman(UUID randomUUID, Vector vector, Country country, CharacterMessage gv2, Map<UUID, CharacterMessage> characters) {
        CharacterMessage tank = new CharacterMessage(randomUUID, CharacterType.M4SHERMAN, vector, country, CharacterType.PANZER4.getDefaultWeapon(), RankMessage.PRIVATE, gv2.getId());
        Optional<CharacterMessage> findFirst = gv2.getUnitsUnderCommand().stream().map(i->characters.get(i))
                .filter(c->c.getCharacterType()==CharacterType.SOLDIER)
                .filter(c->c.boardedVehicle==null)
                .findFirst();
        gv2.addCharactersUnderCommand(tank);
        if(findFirst.isPresent()){
            tank.passengers.add(findFirst.get().getId());
            findFirst.get().boardedVehicle = tank.getId();
        }
        return tank;
    }

	private CharacterMessage loadAntiTankGun(Vector vector, Country country, CharacterMessage gv1, Map<UUID, CharacterMessage> characters) {
		return loadAntiTankGun1(UUID.randomUUID(), vector, country, gv1, characters);
	}



	private CharacterMessage loadAntiTankGun1(UUID randomUUID, Vector vector, Country country, CharacterMessage gv1, Map<UUID, CharacterMessage> characters) {
		CharacterMessage atg = new CharacterMessage(randomUUID, CharacterType.ANTI_TANK_GUN, vector, country, CharacterType.ANTI_TANK_GUN.getDefaultWeapon(), RankMessage.PRIVATE, gv1.getId());
		Optional<CharacterMessage> findFirst = gv1.getUnitsUnderCommand().stream().map(i->characters.get(i))
				.filter(c->c.getCharacterType()==CharacterType.SOLDIER)
				.filter(c->c.boardedVehicle==null)
					.findFirst();
		gv1.addCharactersUnderCommand(atg);
		if(findFirst.isPresent()){
			atg.passengers.add(findFirst.get().getId());
			findFirst.get().boardedVehicle = atg.getId();
		}
		return atg;
	}

	public void loadSquad(Map<UUID, CharacterMessage> characters, CharacterMessage a1, Vector vector3f, Country country, boolean folllowCommander, Weapon...weapons) throws JsonGenerationException, JsonMappingException, IOException {
        characters.put(a1.getId(), a1);
		Set<CharacterMessage> cc = new HashSet<CharacterMessage>();
        int i = 0;
        Vector3f offSet = new Vector3f(2, 0, 2);
        for(Weapon w:weapons){
            CharacterMessage e = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, vector3f.add(i*3, 0, 0), country, w, RankMessage.PRIVATE, a1.getId());
            if(folllowCommander){
            	e.addObjective(UUID.randomUUID(), new FollowCommander(new Vector(offSet), 10));
            	offSet = offSet.add(2, 0, 2);
            }
			cc.add(e);
            i++;
        }
        a1.addCharactersUnderCommand(cc);
        cc.forEach(c->characters.put(c.getId(), c));
    }

}
