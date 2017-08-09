package lostVictories;

import java.util.EnumMap;
import java.util.Set;

import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.Country;
import com.jme3.lostVictories.network.messages.Weapon;

public class WeaponsFactory {

	private static final int MAX_ALLOWED_MG42 = 4;
	private static final int MAX_ALLOWED_MORTAR = 4;
	private static final int MAX_ALLOWED_BAZOOKA = 4;
	private long mg42_last_produced = System.currentTimeMillis();
	private long mortar_last_produced = System.currentTimeMillis();
	private long bazooka_last_produced = System.currentTimeMillis();
	private EnumMap<Weapon, Long> senses = new EnumMap<Weapon, Long>(Weapon.class);
	private static Logger log = Logger.getLogger(WeaponsFactory.class);
	private Country country; 
	
	public WeaponsFactory(Country country) {
		this.country = country;	
	}
	
	public void updateSenses(Set<CharacterMessage> allCharacters){
		senses.put(Weapon.MG42, allCharacters.stream()
				.filter(c->c.getCountry()==country)
				.filter(c->c.getCharacterType()==CharacterType.SOLDIER || c.getCharacterType()==CharacterType.AVATAR)
				.filter(c->c.getWeapon()==Weapon.MG42).count());
		senses.put(Weapon.MORTAR, allCharacters.stream()
				.filter(c->c.getCountry()==country)
				.filter(c->c.getCharacterType()==CharacterType.SOLDIER || c.getCharacterType()==CharacterType.AVATAR)
				.filter(c->c.getWeapon()==Weapon.MORTAR).count());
		senses.put(Weapon.BAZOOKA, allCharacters.stream()
				.filter(c->c.getCountry()==country)
				.filter(c->c.getCharacterType()==CharacterType.SOLDIER || c.getCharacterType()==CharacterType.AVATAR)
				.filter(c->c.getWeapon()==Weapon.BAZOOKA).count());
	}

	public Weapon getWeapon() {
		if(senses .get(Weapon.MG42)<MAX_ALLOWED_MG42 && System.currentTimeMillis()-mg42_last_produced >(2*60000)){
			mg42_last_produced = System.currentTimeMillis();
			log.info("producing mg");
			return Weapon.MG42;
		}
		if(senses .get(Weapon.MORTAR)<MAX_ALLOWED_MORTAR && System.currentTimeMillis()-mortar_last_produced >(2*60000)){
			mortar_last_produced = System.currentTimeMillis();
			log.info("producing mortar");
			return Weapon.MORTAR;
		}
		if(senses .get(Weapon.BAZOOKA)<MAX_ALLOWED_BAZOOKA && System.currentTimeMillis()-bazooka_last_produced >(2*60000)){
			bazooka_last_produced = System.currentTimeMillis();
			log.info("producing bazooka");
			return Weapon.BAZOOKA;
		}
		
		return Weapon.RIFLE;
	}

}
