package lostVictories;

import java.util.EnumMap;
import java.util.Set;

import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.Weapon;

public class WeaponsFactory {

	private static final int MAX_ALLOWED_MG42 = 2;
	private static final int MAX_ALLOWED_MORTAR = 2;
	private long mg42_last_produced = System.currentTimeMillis();
	private long mortar_last_produced = System.currentTimeMillis();
	private EnumMap<Weapon, Long> senses = new EnumMap<Weapon, Long>(Weapon.class);
	private static Logger log = Logger.getLogger(WeaponsFactory.class); 
	
	public void updateSenses(Set<CharacterMessage> allCharacters){
		senses.put(Weapon.MG42, allCharacters.stream().filter(c->c.getWeapon()==Weapon.MG42).count());
		senses.put(Weapon.MORTAR, allCharacters.stream().filter(c->c.getWeapon()==Weapon.MORTAR).count());		
	}

	public Weapon getWeapon() {
		if(senses .get(Weapon.MG42)<MAX_ALLOWED_MG42 && System.currentTimeMillis()-mg42_last_produced >(3*60000)){
			mg42_last_produced = System.currentTimeMillis();
			log.info("producing mg");
			return Weapon.MG42;
		}
		if(senses .get(Weapon.MORTAR)<MAX_ALLOWED_MORTAR && System.currentTimeMillis()-mortar_last_produced >(3*60000)){
			mortar_last_produced = System.currentTimeMillis();
			log.info("producing mortar");
			return Weapon.MORTAR;
		}
		
		return Weapon.RIFLE;
	}

}
