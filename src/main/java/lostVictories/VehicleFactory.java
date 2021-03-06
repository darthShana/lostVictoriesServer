package lostVictories;

import java.util.EnumMap;
import java.util.Set;


import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.Country;
import com.jme3.lostVictories.network.messages.RankMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VehicleFactory {

	private static Logger log = LoggerFactory.getLogger(VehicleFactory.class);

	
	EnumMap<CharacterType, Long> senses = new EnumMap<CharacterType, Long>(CharacterType.class);
	EnumMap<CharacterType, Long> maxAllowed = new EnumMap<CharacterType, Long>(CharacterType.class);
	EnumMap<CharacterType, Long> lastProduced = new EnumMap<CharacterType, Long>(CharacterType.class);
	private Country country;
	
	public VehicleFactory(Country country) {
		this.country = country;
		maxAllowed.put(CharacterType.ANTI_TANK_GUN, 4l);
		if(country==Country.GERMAN){
			maxAllowed.put(CharacterType.HALF_TRACK, 4l);
			maxAllowed.put(CharacterType.ARMORED_CAR, 0l);
			maxAllowed.put(CharacterType.PANZER4, 2l);
			maxAllowed.put(CharacterType.M4SHERMAN, 0l);
		}else{
			maxAllowed.put(CharacterType.HALF_TRACK, 0l);
			maxAllowed.put(CharacterType.ARMORED_CAR, 4l);
			maxAllowed.put(CharacterType.PANZER4, 0l);
			maxAllowed.put(CharacterType.M4SHERMAN, 2l);
		}
		
		lastProduced.put(CharacterType.ANTI_TANK_GUN, 0l);
		lastProduced.put(CharacterType.HALF_TRACK, 0l);
		lastProduced.put(CharacterType.ARMORED_CAR, 0l);
		lastProduced.put(CharacterType.PANZER4, 0l);
		lastProduced.put(CharacterType.M4SHERMAN, 0l);
	}
	
	public CharacterType getVehicle(RankMessage rank) {
		if(RankMessage.PRIVATE!=rank){
			return null;
		}
		for(CharacterType c:maxAllowed.keySet()){
			if(senses.get(c)<maxAllowed.get(c) && System.currentTimeMillis()-lastProduced.get(c)>(60000*2)){
				lastProduced.put(c, System.currentTimeMillis());
				log.info("getVehicle :"+c);
				return c;
			}
		}
		return null;
	}

	public void updateSenses(Set<CharacterMessage> allCharacters) {
	    senses.put(CharacterType.SOLDIER, allCharacters.stream().filter(c->c.getCountry()==country).filter(c->c.getCharacterType()==CharacterType.SOLDIER).count());
		senses.put(CharacterType.ANTI_TANK_GUN, allCharacters.stream().filter(c->c.getCountry()==country).filter(c->c.getCharacterType()==CharacterType.ANTI_TANK_GUN).count());
		senses.put(CharacterType.HALF_TRACK, allCharacters.stream().filter(c->c.getCountry()==country).filter(c->c.getCharacterType()==CharacterType.HALF_TRACK).count());
		senses.put(CharacterType.ARMORED_CAR, allCharacters.stream().filter(c->c.getCountry()==country).filter(c->c.getCharacterType()==CharacterType.ARMORED_CAR).count());
		senses.put(CharacterType.PANZER4, allCharacters.stream().filter(c->c.getCountry()==country).filter(c->c.getCharacterType()==CharacterType.PANZER4).count());
		senses.put(CharacterType.M4SHERMAN, allCharacters.stream().filter(c->c.getCountry()==country).filter(c->c.getCharacterType()==CharacterType.M4SHERMAN).count());

	}

}
