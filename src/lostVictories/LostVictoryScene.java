package lostVictories;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.Character;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.Country;
import com.jme3.lostVictories.network.messages.Rank;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.Weapon;

public class LostVictoryScene {
	
	public static int SCENE_WIDTH = 1024;
	public static int SCENE_HEIGHT = 1024;
	
	private static Logger log = Logger.getLogger(LostVictoryScene.class); 
	
	public void loadScene(CharacterDAO characterDAO) {
		log.debug("Loading Scene");
		
		Set<Character> characters = new HashSet<Character>();
		
		Character a = new Character(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(-125, 7, 370), Country.GERMAN, Weapon.RIFLE, Rank.COLONEL, null, false);
		Character b = new Character(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(195, 7, -375), Country.AMERICAN, Weapon.RIFLE, Rank.COLONEL, null, false);
		
		Character gl1 = new Character(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(-120, 7, 375), Country.GERMAN, Weapon.RIFLE, Rank.LIEUTENANT, a, false);
		Character gl2 = new Character(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(-115, 7, 375), Country.GERMAN, Weapon.RIFLE, Rank.LIEUTENANT, a, false);
		Character d = new Character(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(190, 7, -380), Country.AMERICAN, Weapon.RIFLE, Rank.LIEUTENANT, b, false);
		Character e = new Character(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(190, 7, -385), Country.AMERICAN, Weapon.RIFLE, Rank.LIEUTENANT, b, false );
		
		characters.add(a);
		characters.add(b);
		characters.add(gl1);
		characters.add(gl2);
		characters.add(d);
		characters.add(e);
		
		Character a1 = new Character(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(-210, 7, 380), Country.GERMAN, Weapon.RIFLE, Rank.CADET_CORPORAL, gl1, false);
		loadSquad(characters, a1, new Vector(-215, 7, 385), Country.GERMAN, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
		
		Character a2 = new Character(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(-230, 7, 380), Country.GERMAN, Weapon.RIFLE, Rank.CADET_CORPORAL, gl1, false);
        loadSquad(characters, a2, new Vector(-35, 7, 385), Country.GERMAN, Weapon.RIFLE, Weapon.RIFLE, Weapon.MG42);
        
        Character a3 = new Character(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(-250, 7, 380), Country.GERMAN, Weapon.RIFLE, Rank.CADET_CORPORAL, gl1, false);
        loadSquad(characters, a3, new Vector(-55, 10, 385), Country.GERMAN, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        
        Character a4 = new Character(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(-270, 7, 380), Country.GERMAN, Weapon.RIFLE, Rank.CADET_CORPORAL, gl1, false);
        loadSquad(characters, a4, new Vector(-275, 7, 385), Country.GERMAN, Weapon.RIFLE, Weapon.RIFLE, Weapon.MORTAR );
        
        Character gv1 = new Character(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(-200, 7, 380), Country.GERMAN, Weapon.RIFLE, Rank.CADET_CORPORAL, gl2, false);
        loadSquad(characters, gv1, new Vector(-195, 7, 380), Country.GERMAN);
        characters.add(loadAntiTankGun(new Vector(-295, 7, 400), Country.GERMAN, gv1));
        
        Character gv2 = new Character(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(-295, 7, 380), Country.GERMAN, Weapon.RIFLE, Rank.CADET_CORPORAL, gl2, false);
        loadSquad(characters, gv2, new Vector(-195, 7, 380), Country.GERMAN);
        characters.add(loadHalfTrack(new Vector(-195, 7, 400), Country.GERMAN, gv2));
        
        Character b1 = new Character(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(180, 7, -385), Country.AMERICAN, Weapon.RIFLE, Rank.CADET_CORPORAL, d, false);
        loadSquad(characters, b1, new Vector(175, 7, -390), Country.AMERICAN, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        
        Character b2 = new Character(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(200, 7, -385), Country.AMERICAN, Weapon.RIFLE, Rank.CADET_CORPORAL, d, false);
        loadSquad(characters,  b2, new Vector(195, 7, -390), Country.AMERICAN, Weapon.RIFLE, Weapon.RIFLE, Weapon.MG42);
        
        Character b3 = new Character(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(220, 7, -385), Country.AMERICAN, Weapon.RIFLE, Rank.CADET_CORPORAL, d, false);
        loadSquad(characters, b3, new Vector(215, 7, -390), Country.AMERICAN, Weapon.RIFLE, Weapon.RIFLE, Weapon.RIFLE);
        
        Character b4 = new Character(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(240, 7, -385), Country.AMERICAN, Weapon.RIFLE, Rank.CADET_CORPORAL, d, false);
        loadSquad(characters, b4, new Vector(235, 7, -390), Country.AMERICAN, Weapon.RIFLE, Weapon.RIFLE, Weapon.MORTAR);
        
        Character c1 = new Character(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(195, 7, -395), Country.AMERICAN, Weapon.RIFLE, Rank.CADET_CORPORAL, e, false);
        loadSquad(characters, c1, new Vector(195, 7, -395), Country.AMERICAN);
        characters.add(loadAntiTankGun(new Vector(190, 7, -390), Country.AMERICAN, c1));
        
        Character c2 = new Character(UUID.randomUUID(), CharacterType.SOLDIER, new Vector(200, 7, -395), Country.AMERICAN, Weapon.RIFLE, Rank.CADET_CORPORAL, e, false);
        loadSquad(characters, c2, new Vector(195, 7, -395), Country.AMERICAN);
        characters.add(loadAmoredCar(new Vector(200, 7, -390), Country.AMERICAN, c2));
        
        gl1.addCharactersUnderCommand(a1, a2, a3, a4);
        d.addCharactersUnderCommand(b1, b2, b3, b4);
        e.addCharactersUnderCommand(c1, c2);
        gl2.addCharactersUnderCommand(gv1, gv2);
        
        a.addCharactersUnderCommand(gl1, gl2);
        b.addCharactersUnderCommand(d, e);
        
        characters.forEach(c -> characterDAO.putCharacter(c.getId(), null, c));
	}
	
	private Character loadAmoredCar(Vector vector, Country country, Character c2) {
		Character armoredCar = new Character(UUID.randomUUID(), CharacterType.ARMORED_CAR, vector, country, Weapon.MG42, Rank.PRIVATE, c2, false);
		c2.addCharactersUnderCommand(armoredCar);
		return armoredCar;
	}

	private Character loadHalfTrack(Vector vector, Country country, Character gv2) {
		Character halfTrack = new Character(UUID.randomUUID(), CharacterType.HALF_TRACK, vector, country, Weapon.MG42, Rank.PRIVATE, gv2, false);
		gv2.addCharactersUnderCommand(halfTrack);
		return halfTrack;
	}

	private Character loadAntiTankGun(Vector vector, Country country, Character gv1) {
		Character atg = new Character(UUID.randomUUID(), CharacterType.ANTI_TANK_GUN, vector, country, Weapon.CANNON, Rank.PRIVATE, gv1, false);
		gv1.addCharactersUnderCommand(atg);
		return atg;
	}

	public void loadSquad(Set<Character> characters, Character a1, Vector vector3f, Country country, Weapon...weapons) {
        characters.add(a1);
		Set<Character> cc = new HashSet<Character>();
        int i = 0;
        for(Weapon w:weapons){
            cc.add(new Character(UUID.randomUUID(), CharacterType.SOLDIER, vector3f.add(i*3, 5, 0), country, w, Rank.PRIVATE, a1, false));
            i++;
        }
        a1.addCharactersUnderCommand(cc);
        characters.addAll(cc);
    }

}
