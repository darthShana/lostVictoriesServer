package com.jme3.lostVictories.network.messages;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import lostVictories.NavMeshStore;
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
	public static Vector germanVehicleSpawnPoint = new Vector(349, 97, 183);
    public static Vector germanBase = new Vector(150, 100, 25);
	public static Vector americanVehicleSpawnPoint = new Vector(143, 101, -485);
    public static Vector americanBase = new Vector(90, 100, -380);
	
	private static Logger log = LoggerFactory.getLogger(LostVictoryScene.class);
	
	public void loadScene(CharacterDAO characterDAO, HouseDAO housesDAO, TreeDAO treeDAO, NavMeshStore pathfinder) throws JsonGenerationException, JsonMappingException, IOException {
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

		CharacterMessage gc1 = new CharacterMessage(UUID.fromString("2fbe421f-f701-49c9-a0d4-abb0fa904204"), CharacterType.AVATAR, germanBase.add(22, 0, 13), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl1.getId());
        gc1.userID = UUID.fromString("2fbe421f-f701-49c9-a0d4-abb0fa904204");
		loadSquad(characters, gc1, germanBase.add(-10, 0, 15), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE   );
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
		
		CharacterMessage gc2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(30, 0, 15), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl1.getId());
        loadSquad(characters, gc2, germanBase.add(30, 0, 15), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.MG42);
        
        CharacterMessage gc3 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(35, 0, 15), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl2.getId());
        loadSquad(characters, gc3, germanBase.add(35, 0, 15), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        
        CharacterMessage gc4 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(40, 0, 15), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl2.getId());
        loadSquad(characters, gc4, germanBase.add(40, 0, 15), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.MORTAR, Weapon.RIFLE);
        
        CharacterMessage gc5 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(35, 0, 15), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl2.getId());
        loadSquad(characters, gc5, germanBase.add(35, 0, 15), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        
        CharacterMessage gc6 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(20, 0, 16), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl3.getId());
        loadSquad(characters, gc6, germanBase.add(21, 0, 17), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        CharacterMessage loadAntiTankGun1 = loadAntiTankGun1(UUID.fromString("f47db5e3-f07c-4bbc-8cb1-52263131a7a2"), germanBase.add(22, 0, 18), Country.GERMAN, gc6, characters);
		characters.put(loadAntiTankGun1.getId(), loadAntiTankGun1);
        
        CharacterMessage gc7 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(81, 0, -10), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl3.getId());
        loadSquad(characters, gc7, germanBase.add(82, 0, -11), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.MG42);
        CharacterMessage loadHalfTrack = loadHalfTrack(germanBase.add(83, 0, -14), Country.GERMAN, gc7, characters);
		characters.put(loadHalfTrack.getId(), loadHalfTrack);
        
        CharacterMessage gc8 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(30, 0, 10), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl4.getId());
        loadSquad(characters, gc8, germanBase.add(25, 0, 15), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        
        CharacterMessage gc9 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(35, 0, 10), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl4.getId());
        loadSquad(characters, gc9, germanBase.add(25, 0, 15), Country.GERMAN, true, Weapon.RIFLE, Weapon.BAZOOKA, Weapon.MORTAR);

        CharacterMessage gc10 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(22, 0, 10), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl5.getId());
        loadSquad(characters, gc10, germanBase.add(20, 0, 15), Country.GERMAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);

        CharacterMessage gc11 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, germanBase.add(26, 0, 10), Country.GERMAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, gl5.getId());
        loadSquad(characters, gc11, germanBase.add(25, 0, 15), Country.GERMAN, true, Weapon.RIFLE, Weapon.MORTAR, Weapon.BAZOOKA);

        
        
        CharacterMessage ac1 = new CharacterMessage(UUID.fromString("d993932f-a185-4a6f-8d86-4ef6e2c5ff95"), CharacterType.AVATAR, americanBase.add(-10, 0, 10), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al1.getId());
        ac1.userID = UUID.fromString("d993932f-a185-4a6f-8d86-4ef6e2c5ff95");
        loadSquad(characters, ac1, americanBase.add(-10, 0, 15), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE, Weapon.MORTAR);
//        CharacterMessage loadAntiTankGun = loadAntiTankGun(americanBase.add(15, 0, 15), Country.AMERICAN, b1, characters);
//		characters.put(loadAntiTankGun.getId(), loadAntiTankGun);

//        CharacterMessage loadAmoredCar2 = loadAmoredCar(americanBase.add(10, 0, 15), Country.AMERICAN, b1, characters);
//		characters.put(loadAmoredCar2.getId(), loadAmoredCar2);
//        b1.addObjective(UUID.randomUUID(), createBootCampObjective(new Vector(-57.21826f, 96.380104f, -203.38945f)));
//        b1.incrementKills(UUID.randomUUID());
//        b1.incrementKills(UUID.randomUUID());
//        b1.incrementKills(UUID.randomUUID());
//        b1.incrementKills(UUID.randomUUID());
//        b1.incrementKills(UUID.randomUUID());
        
        CharacterMessage ac2 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(20, 0, -25), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al1.getId());
        loadSquad(characters,  ac2, americanBase.add(25, 0, -25), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.MORTAR, Weapon.MG42);
        CharacterMessage m4a2 = loadM4A2Sherman(UUID.fromString("16a67c6b-263b-4d74-a7a8-b6d28f014d28"), americanBase.add(-12, 0, 15), Country.AMERICAN, ac2, characters);
        characters.put(m4a2.getId(), m4a2);

        CharacterMessage ac3 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(-18, 0, 95), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al2.getId());
        loadSquad(characters, ac3, americanBase.add(-15, 0, 95), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        
        CharacterMessage ac4 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(-18, 0, 105), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al2.getId());
        loadSquad(characters, ac4, americanBase.add(-15, 0, 105), Country.AMERICAN, true, Weapon.RIFLE, Weapon.BAZOOKA, Weapon.MORTAR, Weapon.RIFLE);
        
        CharacterMessage ac5 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(-50, 0, 40), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al3.getId());
        loadSquad(characters, ac5, americanBase.add(6, 5, -9), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE, Weapon.MORTAR);
        CharacterMessage loadAntiTankGun12 = loadAntiTankGun1(UUID.fromString("00b5ceda-4a0a-490d-af71-bcaf81ede6eb"), americanBase.add(-55, 0, 45), Country.AMERICAN, ac5, characters);
		characters.put(loadAntiTankGun12.getId(), loadAntiTankGun12);
        
        CharacterMessage ac6 = new CharacterMessage(UUID.fromString("844fd93d-e65a-438a-82c5-dab9ad58e854"), CharacterType.SOLDIER, americanBase.add(40, 5, -40), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al3.getId());
        ac6.id = UUID.fromString("844fd93d-e65a-438a-82c5-dab9ad58e854");
        loadSquad(characters, ac6, americanBase.add(-55, 0, 45), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE, Weapon.MORTAR);
        CharacterMessage loadAmoredCar = loadAmoredCar(americanBase.add(-58, 0, 40), Country.AMERICAN, ac6, characters);
		characters.put(loadAmoredCar.getId(), loadAmoredCar);
        
        CharacterMessage ac7 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(19, 5, -35), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al4.getId());
        loadSquad(characters, ac7, americanBase.add(25, 5, -30), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE, Weapon.BAZOOKA);
        
        CharacterMessage ac8 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(20, 5, -35), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al4.getId());
        loadSquad(characters,  ac8, americanBase.add(20, 5, -30), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        
        CharacterMessage ac9 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(15, 5, -30), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al4.getId());
        loadSquad(characters,  ac9, americanBase.add(15, 5, -25), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);

        CharacterMessage ac10 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(22, 5, -35), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al5.getId());
        loadSquad(characters,  ac10, americanBase.add(22, 5, -30), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE, Weapon.MORTAR);

        CharacterMessage ac11 = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, americanBase.add(17, 5, -30), Country.AMERICAN, Weapon.RIFLE, RankMessage.CADET_CORPORAL, al5.getId());
        loadSquad(characters,  ac11, americanBase.add(17, 5, -25), Country.AMERICAN, true, Weapon.RIFLE, Weapon.RIFLE, Weapon.BAZOOKA);

        gl1.addCharactersUnderCommand(gc1, gc2, gc10);
        gl2.addCharactersUnderCommand(gc3, gc4, gc5);
        gl3.addCharactersUnderCommand(gc6, gc7, gc9);
        gl4.addCharactersUnderCommand(gc8);
        gl5.addCharactersUnderCommand(gc11);

        al1.addCharactersUnderCommand(ac1, ac2, ac10);
        al2.addCharactersUnderCommand(ac3, ac4, ac5);
        al3.addCharactersUnderCommand(ac6, ac7, ac9);
        al4.addCharactersUnderCommand(ac8);
        al5.addCharactersUnderCommand(ac11);

        a.addCharactersUnderCommand(gl1, gl2, gl3, gl4, gl5);
        b.addCharactersUnderCommand(al1, al2, al3, al4, al5);
        
        characters.values().stream().forEach(c -> {
            Vector3f position = pathfinder.warp(c, c.getLocation());
            c.setLocation(new Vector(position));
            characterDAO.putCharacter(c.getId(), c);
        });

        
        
		Set<HouseMessage> houses = new HashSet<>();

        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(85.84874f, 97.170715f, 33.869347f), new Quaternion(0.0f, -0.34439534f, 0.0f, -0.93882906f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(102.82642f, 96.96666f, 35.291584f), new Quaternion(0.0f, 0.029995233f, 0.0f, 0.9995505f), new Vector(0.7f, 0.7f, 0.7f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(102.42478f, 97.14429f, 26.78571f), new Quaternion(0.0f, 0.029995233f, 0.0f, 0.9995505f), new Vector(0.7f, 0.7f, 0.7f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(94.46798f, 97.14428f, 17.404636f), new Quaternion(0.0f, 0.6925375f, 0.0f, 0.72138315f), new Vector(0.7f, 0.7f, 0.7f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(140.05841f, 97.000755f, -5.2044954f), new Quaternion(-3.4552602E-6f, 0.7514165f, 4.930732E-6f, 0.6598287f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(86.278984f, 97.18162f, 91.780205f), new Quaternion(0.0f, -0.91136926f, 0.0f, -0.41159576f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(216.74788f, 94.7688f, 16.502869f), new Quaternion(0.0f, -0.69253737f, 0.0f, 0.721383f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(144.77039f, 97.206505f, -15.7767935f), new Quaternion(7.159173E-6f, -0.61153936f, 4.9384325E-6f, 0.7912152f), new Vector(1.1000012f, 1.1000012f, 1.1000012f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(86.982994f, 97.87142f, 77.44998f), new Quaternion(0.0f, -0.9373703f, 0.0f, 0.34833753f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(147.14592f, 96.03496f, 64.1337f), new Quaternion(0.0f, 0.7032796f, 0.0f, 0.71091384f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(154.63408f, 96.4922f, 41.82982f), new Quaternion(0.0f, 0.9802265f, 0.0f, -0.19788925f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(173.82977f, 98.228294f, -22.97573f), new Quaternion(0.0f, 0.19376664f, 0.0f, 0.9810479f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(154.38177f, 96.11536f, 57.028408f), new Quaternion(0.0f, -0.810498f, 0.0f, 0.5857435f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(139.58846f, 94.85659f, 63.706833f), new Quaternion(0.0f, 0.994925f, 0.0f, 0.100626245f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(212.9072f, 94.03517f, 30.799404f), new Quaternion(0.0f, 0.988136f, 0.0f, -0.15359321f), new Vector(0.7f, 0.7f, 0.7f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(82.85497f, 98.08014f, 70.971756f), new Quaternion(0.0f, 0.93441f, 0.0f, -0.35620642f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(130.25476f, 96.76075f, -12.060875f), new Quaternion(1.25896295E-5f, 0.5515648f, -5.350244E-6f, -0.8341367f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(120.984535f, 96.95665f, -14.982307f), new Quaternion(0.0f, -0.58233356f, 0.0f, -0.8129547f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(162.03284f, 97.09564f, -18.269882f), new Quaternion(0.0f, -0.22953017f, 0.0f, -0.97330856f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(171.95143f, 97.59071f, 2.871109f), new Quaternion(0.0f, 0.8191927f, 0.0f, 0.57352084f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(177.42531f, 97.59072f, 0.11059642f), new Quaternion(0.0f, 0.8441628f, 0.0f, 0.5360889f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(183.46837f, 98.10595f, -3.0439007f), new Quaternion(0.0f, 0.8191942f, 0.0f, 0.57352144f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(223.25099f, 93.86209f, 23.9045f), new Quaternion(0.0f, -0.6925382f, 0.0f, 0.72138405f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(200.25636f, 94.17422f, 35.49746f), new Quaternion(0.0f, 0.988136f, 0.0f, -0.15359321f), new Vector(0.7f, 0.7f, 0.7f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(209.42502f, 94.7169f, 16.131506f), new Quaternion(0.0f, -0.69253737f, 0.0f, 0.721383f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(78.16619f, 97.31726f, 82.475784f), new Quaternion(0.0f, 0.91136694f, 0.0f, 0.41159478f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(86.48432f, 97.170715f, 42.465282f), new Quaternion(0.0f, -0.34439534f, 0.0f, -0.93882906f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(-53.945488f, 97.52969f, 231.8853f), new Quaternion(0.0f, -0.7780751f, 0.0f, -0.628175f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(-61.854595f, 96.94649f, 238.05736f), new Quaternion(0.0f, -0.8819598f, 0.0f, 0.47133f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(-86.61476f, 96.90808f, 246.41492f), new Quaternion(0.0f, -0.6205824f, 0.0f, -0.78414446f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(-92.74218f, 97.239204f, 213.87512f), new Quaternion(-3.2177793E-6f, -0.07327357f, 3.0149986E-6f, -0.99731433f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(160.4662f, 95.67944f, 52.367104f), new Quaternion(0.0f, -0.9884112f, 0.0f, 0.15180023f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(-68.913086f, 96.90005f, 252.0893f), new Quaternion(0.0f, 0.98507035f, 0.0f, 0.17217581f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(62.595802f, 98.19941f, 70.25858f), new Quaternion(0.0f, 0.38795218f, 0.0f, -0.9216832f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(326.65497f, 96.93109f, 130.67523f), new Quaternion(0.0f, 0.73900586f, 0.0f, 0.6736998f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(327.71066f, 95.96161f, 120.342834f), new Quaternion(0.0f, 0.988136f, 0.0f, -0.15359321f), new Vector(0.7f, 0.7f, 0.7f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(329.68738f, 95.94789f, 110.10412f), new Quaternion(0.0f, 0.988136f, 0.0f, -0.15359321f), new Vector(0.7f, 0.7f, 0.7f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(330.8568f, 95.930984f, 100.39261f), new Quaternion(0.0f, 0.988136f, 0.0f, -0.15359321f), new Vector(0.7f, 0.7f, 0.7f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(350.8653f, 95.97881f, 111.342255f), new Quaternion(0.0f, 0.7402314f, 0.0f, -0.67236257f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(123.548294f, 101.01445f, -286.72476f), new Quaternion(0.0f, 0.9707699f, 0.0f, -0.24002326f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(106.760475f, 100.95137f, -347.2597f), new Quaternion(0.0f, 0.25861996f, 0.0f, -0.96598136f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(116.51121f, 100.05997f, -336.4931f), new Quaternion(0.0f, 0.86321133f, 0.0f, -0.5048469f), new Vector(0.7f, 0.7f, 0.7f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(-235.40268f, 97.1148f, -193.67079f), new Quaternion(0.0f, 0.99909425f, 0.0f, 0.042597845f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(118.80721f, 100.33241f, -261.75403f), new Quaternion(0.0f, 0.8889288f, 0.0f, 0.458047f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(115.681656f, 100.06957f, -362.59695f), new Quaternion(0.0f, -0.081503056f, 0.0f, -0.9966777f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(-248.21228f, 96.112724f, -177.75186f), new Quaternion(-9.313226E-10f, -0.9474002f, 0.0f, 0.32006508f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(-256.1153f, 97.590614f, -170.75073f), new Quaternion(0.0f, -0.8555407f, 0.0f, -0.51773745f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(-291.6729f, 96.23219f, -242.44035f), new Quaternion(0.0f, 0.5944603f, 0.0f, -0.80412745f), new Vector(0.7f, 0.7f, 0.7f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(112.42636f, 100.33241f, -269.3277f), new Quaternion(0.0f, 0.8889288f, 0.0f, 0.458047f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(106.51669f, 100.33241f, -277.8734f), new Quaternion(0.0f, 0.8889288f, 0.0f, 0.458047f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(130.08974f, 101.14649f, -278.54166f), new Quaternion(0.0f, 0.9452684f, 0.0f, -0.32630324f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(110.09275f, 100.951355f, -355.61517f), new Quaternion(0.0f, 0.25861996f, 0.0f, -0.96598136f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(121.79952f, 100.50152f, -346.0753f), new Quaternion(0.0f, 0.86321133f, 0.0f, -0.5048469f), new Vector(0.7f, 0.7f, 0.7f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(74.035934f, 100.06957f, -361.57007f), new Quaternion(0.0f, 0.5084724f, 0.0f, -0.8610839f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(79.44162f, 100.06957f, -352.27124f), new Quaternion(0.0f, 0.5084724f, 0.0f, -0.8610839f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(67.942795f, 100.13351f, -344.39246f), new Quaternion(0.0f, 0.84416354f, 0.0f, 0.5360895f), new Vector(0.7f, 0.7f, 0.7f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(63.96949f, 100.13351f, -353.38663f), new Quaternion(0.0f, 0.84416354f, 0.0f, 0.5360895f), new Vector(0.7f, 0.7f, 0.7f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(67.63751f, 101.17485f, -365.89313f), new Quaternion(0.0f, -0.54669076f, 0.0f, 0.8373346f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(44.139374f, 100.95136f, -328.6428f), new Quaternion(0.0f, 0.66432834f, 0.0f, -0.74744415f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(50.925816f, 100.951355f, -328.6524f), new Quaternion(0.0f, 0.66432834f, 0.0f, -0.74744415f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(57.769135f, 100.95135f, -328.12537f), new Quaternion(0.0f, 0.66432834f, 0.0f, -0.74744415f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(37.655777f, 100.15298f, -316.66983f), new Quaternion(0.0f, 0.8889288f, 0.0f, 0.458047f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(47.208714f, 101.11962f, -314.56528f), new Quaternion(0.0f, -0.67546517f, 0.0f, -0.73739547f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(-241.23492f, 96.11273f, -186.65125f), new Quaternion(-9.186741E-10f, -0.88196206f, -1.5297202E-10f, 0.4713309f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(-261.9796f, 97.590614f, -167.89899f), new Quaternion(0.0f, -0.8555407f, 0.0f, -0.51773745f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(-294.70306f, 96.21984f, -252.12906f), new Quaternion(0.0f, 0.5944603f, 0.0f, -0.80412745f), new Vector(0.7f, 0.7f, 0.7f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(-357.4186f, 96.49775f, -210.37169f), new Quaternion(0.0f, 0.9520927f, 0.0f, 0.30581787f), new Vector(0.7f, 0.7f, 0.7f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(-310.2513f, 97.16935f, -232.86774f), new Quaternion(0.0f, -0.155954f, 0.0f, -0.98776776f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(-314.04636f, 97.590614f, -240.68694f), new Quaternion(0.0f, -0.12625504f, 0.0f, -0.9919999f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(-317.69565f, 97.59062f, -247.95282f), new Quaternion(0.0f, -0.12625504f, 0.0f, -0.9919999f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(-364.74133f, 96.49776f, -221.56511f), new Quaternion(0.0f, 0.6365387f, 0.0f, 0.77124834f), new Vector(0.7f, 0.7f, 0.7f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(-356.24576f, 96.11275f, -228.16621f), new Quaternion(-4.5122994E-10f, -0.17903028f, -8.1471113E-10f, 0.9838485f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(-346.6033f, 96.339905f, -224.18909f), new Quaternion(-4.5122994E-10f, -0.17903028f, -8.1471113E-10f, 0.9838485f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(-336.2033f, 96.112755f, -219.12177f), new Quaternion(-4.5122994E-10f, -0.17903028f, -8.1471113E-10f, 0.9838485f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(-330.44193f, 97.227974f, -167.19185f), new Quaternion(0.0f, -0.036584105f, 0.0f, -0.9993345f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(-331.11667f, 97.06876f, -176.90742f), new Quaternion(0.0f, -0.03658466f, 0.0f, -0.99933356f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(-331.56793f, 97.07747f, -156.47841f), new Quaternion(0.0f, 0.008407291f, 0.0f, -0.9999668f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(-330.9898f, 96.112755f, -146.50629f), new Quaternion(4.086152E-10f, 0.7032834f, -8.368974E-10f, 0.7109173f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(-340.83585f, 97.07747f, -156.5674f), new Quaternion(0.0f, 0.008407291f, 0.0f, -0.9999668f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(-315.2739f, 96.86195f, 16.324844f), new Quaternion(0.0f, 0.65463406f, 0.0f, 0.75594586f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(-303.69046f, 94.940025f, 19.279285f), new Quaternion(0.0f, 0.9995347f, 0.0f, 0.030498976f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(-291.807f, 96.11444f, 19.737808f), new Quaternion(0.0f, 0.9995177f, 0.0f, 0.031054825f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(-327.18765f, 96.25331f, 17.42927f), new Quaternion(4.15916E-7f, 0.65745425f, -5.627828E-7f, 0.75349444f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(-334.87363f, 95.96025f, 0.9493122f), new Quaternion(0.0f, 0.6960731f, 0.0f, 0.7179709f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(-332.2797f, 96.19855f, -17.737953f), new Quaternion(0.0f, 0.0f, 0.0f, 1.0f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(-332.37317f, 96.1994f, -27.538048f), new Quaternion(0.0f, 0.0f, 0.0f, 1.0f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(-331.9042f, 96.82576f, -40.902504f), new Quaternion(0.0f, 0.65357065f, 0.0f, 0.75686544f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house.j3o", new Vector(301.952f, 95.8321f, 70.84405f), new Quaternion(-4.001059E-5f, 0.89567614f, 1.8993427E-5f, -0.44470677f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(-79.707985f, 99.61575f, 281.56363f), new Quaternion(0.0f, -0.7884256f, 0.0f, 0.61513007f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(-287.635f, 96.04944f, -16.623535f), new Quaternion(0.0f, -0.3876631f, 0.0f, 0.92180115f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(-298.18524f, 95.93712f, -26.86688f), new Quaternion(0.0f, -0.45022833f, 0.0f, 0.89291346f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(93.377846f, 97.069496f, 65.78844f), new Quaternion(0.0f, 0.83539045f, 0.0f, 0.549657f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house_1.j3o", new Vector(290.91483f, 95.97122f, 70.57654f), new Quaternion(1.4775533E-6f, 0.46462822f, -4.1854616E-7f, 0.8855058f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(-98.84273f, 100.620346f, 282.51523f), new Quaternion(0.0f, 0.0f, 0.0f, 1.0f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/house2.j3o", new Vector(311.3182f, 96.62903f, 50.18342f), new Quaternion(4.5202418E-7f, -0.889705f, -4.742429E-7f, 0.45653576f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(285.73584f, 95.51353f, 56.277416f), new Quaternion(0.0f, 0.5368402f, 0.0f, 0.84368396f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(351.31018f, 95.73422f, 95.87686f), new Quaternion(0.0f, -0.6404571f, 0.0f, 0.767994f), new Vector(0.8f, 0.8f, 0.8f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(-93.83813f, 97.63855f, 264.37146f), new Quaternion(0.0f, 0.5960692f, 0.0f, 0.8029331f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/cottage.j3o", new Vector(-93.49419f, 100.16747f, 296.4483f), new Quaternion(9.3070497E-7f, 0.97711825f, -1.986277E-6f, 0.21269658f), new Vector(1.0f, 1.0f, 1.0f)));
        houses.add(new HouseMessage("Models/Structures/casaMedieval.j3o", new Vector(-90.86111f, 96.53069f, 231.42224f), new Quaternion(0.0f, 0.9971563f, 0.0f, 0.075361215f), new Vector(1.0f, 1.0f, 1.0f)));

        houses.forEach(h->housesDAO.putHouse(h));

        Set<BunkerMessage> bunkers = new HashSet<>();
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(-100.58569f, 97.62429f, 231.24171f), new Quaternion(0.0f, 0.3360924f, 0.0f, 0.941829f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(336.1358f, 95.88243f, 66.05069f), new Quaternion(0.0f, 0.5893206f, 0.0f, 0.8078993f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(-250.0f, 96.289536f, -225.11862f), new Quaternion(0.0f, -0.7550778f, 0.0f, 0.65563524f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(-342.9169f, 96.32557f, -144.11838f), new Quaternion(0.0f, -0.98937446f, 0.0f, 0.14538974f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(82.83392f, 97.43404f, 100.658516f), new Quaternion(0.0f, 0.7570388f, 0.0f, 0.65336996f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(57.210407f, 100.232506f, -270.88477f), new Quaternion(0.0f, 0.90899706f, 0.0f, 0.41680256f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(137.88399f, 100.28509f, -270.88477f), new Quaternion(0.0f, -0.930234f, 0.0f, 0.3669669f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(200.1758f, 102.116066f, -42.26669f), new Quaternion(0.0f, 0.0f, 0.0f, 1.0f)));

        bunkers.forEach(bunker->housesDAO.putBunker(bunker));

        housesDAO.calculateGameSectors();
        
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
