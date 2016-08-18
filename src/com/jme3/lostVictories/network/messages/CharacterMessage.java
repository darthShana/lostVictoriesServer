package com.jme3.lostVictories.network.messages;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static com.jme3.lostVictories.network.messages.Vector.latLongToVector;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import lostVictories.LostVictoryScene;
import lostVictories.VehicleFactory;
import lostVictories.WeaponsFactory;
import lostVictories.dao.CharacterDAO;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.common.collect.ImmutableSet;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.codehaus.jackson.type.TypeReference;

import com.jme3.lostVictories.network.messages.actions.Action;
import com.jme3.lostVictories.network.messages.actions.Shoot;

public class CharacterMessage implements Serializable{
	
	private static final long serialVersionUID = 2491659254334134796L;
	private static Logger log = Logger.getLogger(CharacterMessage.class);
	public static final long CHECKOUT_TIMEOUT = 2*1000;
	
	UUID id;
	UUID userID;
	Vector location;
	Country country;
	Weapon weapon;
	RankMessage rank;
	UUID commandingOfficer;
	UUID boardedVehicle;
	Set<UUID> unitsUnderCommand = new HashSet<UUID>();
	Set<UUID> passengers = new HashSet<UUID>();
	UUID checkoutClient;
	Long checkoutTime;
	boolean gunnerDead;
	CharacterType type;
	Vector orientation = new Vector(0, 0, 1);
	Set<Action> actions = new HashSet<Action>();
	Map<String, String> objectives = new HashMap<String, String>();
    Set<String> completedObjectives;
	boolean isDead;
	boolean engineDamaged;
	Long timeOfDeath;
	long version;
	Set<UUID> kills = new HashSet<UUID>();
	SquadType squadType = SquadType.RIFLE_TEAM;
	
	
	public CharacterMessage(UUID identity, CharacterType type, Vector location, Country country, Weapon weapon, RankMessage rank, UUID commandingOfficer, boolean gunnerDead) {
		this(identity, null, type, location, country, weapon, rank, commandingOfficer, gunnerDead);
	}
	
	public CharacterMessage(UUID identity, UUID userID, CharacterType type, Vector location, Country country, Weapon weapon, RankMessage rank, UUID commandingOfficer, boolean gunnerDead) {
		this.id = identity;
		this.userID = userID;
		this.type = type;
		this.location = location;
		this.country = country;
		this.weapon = weapon;
		this.rank = rank;
		if(commandingOfficer!=null){
			this.commandingOfficer = commandingOfficer;
		}
	}
	
	public CharacterMessage(UUID id, long version, Map<String, Object> source) {
		this.id = id;
		HashMap<String, Double> location =  (HashMap<String, Double>) source.get("location");
		HashMap<String, Double> ori =  (HashMap<String, Double>) source.get("orientation");
		float altitude = ((Double)source.get("altitude")).floatValue();
		this.type = CharacterType.valueOf((String) source.get("type"));
		if(source.get("squadType")!=null){
			this.squadType = SquadType.valueOf((String) source.get("squadType"));
		}
		this.location = latLongToVector(location, altitude);
		this.country = Country.valueOf((String)source.get("country"));
		this.weapon = Weapon.valueOf((String) source.get("weapon"));
		this.rank = RankMessage.valueOf((String) source.get("rank"));
		
		try{
			String o = (String)source.get("objectives");
			if(!"[{}]".equals(o)){
				this.objectives = CharacterDAO.MAPPER.readValue(o, new TypeReference<Map<String, String>>() {});
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		try {
			String a = (String)source.get("actions");
			if(!"[]".equals(a)){
				this.actions = CharacterDAO.MAPPER.readValue(a, new TypeReference<Set<Action>>() {});
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		try{
			this.orientation = new Vector(ori.get("x").floatValue(), ori.get("y").floatValue(), ori.get("z").floatValue());
		}catch(Exception e){
			this.orientation = new Vector(0, 0, 1);
		}
		
		Function<Object, UUID> toUUIDifPresent = new Function<Object, UUID>() {
			@Override
			public UUID apply(Object t) {
				String s = (String)t;
				if(StringUtils.isNotBlank(s)){
					return UUID.fromString(s);
				}
				return null;
			}
		};
		
		this.commandingOfficer = toUUIDifPresent.apply(source.get("commandingOfficer"));
		this.userID = toUUIDifPresent.apply(source.get("userID"));
		this.boardedVehicle = toUUIDifPresent.apply(source.get("boardedVehicle"));
		
		String cc = (String) source.get("checkoutClient");
		if(cc!=null && !cc.isEmpty()){
			this.checkoutClient = UUID.fromString(cc);
			this.checkoutTime = (Long) source.get("checkoutTime");
		}
		this.timeOfDeath = (Long) source.get("timeOfDeath");
		
		unitsUnderCommand = ((Collection<String>)source.get("unitsUnderCommand")).stream().map(s -> UUID.fromString(s)).collect(Collectors.toSet());
		passengers = ((Collection<String>)source.get("passengers")).stream().map(s -> UUID.fromString(s)).collect(Collectors.toSet());
		gunnerDead = (boolean) source.get("gunnerDead");
		isDead = (boolean) source.get("isDead");
		engineDamaged = (boolean) source.get("engineDamaged");
		if(isDead){
			this.checkoutTime = (Long) source.get("checkoutTime");
		}
		this.version = version;
		this.kills = ((Collection<String>)source.get("kills")).stream().map(s -> UUID.fromString(s)).collect(Collectors.toSet());
	}

	void addUnit(CharacterMessage u){
		unitsUnderCommand.add(u.id);
	}

	public Vector getLocation() {
		return location;
	}

	public Country getCountry() {
		return country;
	}
	
	public Weapon getWeapon() {
		return weapon;
	}
	
	public RankMessage getRank() {
		return rank;
	}
	
	public UUID getCommandingOfficer() {
		return commandingOfficer;
	}
	
	public CharacterType getCharacterType(){
		return type;
	}

	public void addCharactersUnderCommand(Set<CharacterMessage> cc) {
		squadType = calculateSquadType(cc, squadType);
		unitsUnderCommand.addAll(cc.stream().map(c -> c.id).collect(Collectors.toList()));
	}

	public void addCharactersUnderCommand(CharacterMessage... atg) {
		List<CharacterMessage> asList = Arrays.asList(atg);
		squadType = calculateSquadType(asList, squadType);
		unitsUnderCommand.addAll(asList.stream().map(c -> c.id).collect(Collectors.toList()));
	}

	public UUID getId() {
		return id;
	}
	
	public UUID getUserID(){
		return userID;
	}

	public XContentBuilder getJSONRepresentation() throws IOException {
		return jsonBuilder()
		            .startObject()
		            	.field("userID", userID)
		            	.field("boardedVehicle", boardedVehicle)
		                .field("date", new Date())
		                .field("location", new GeoPoint(toLatitute(getLocation()), toLongitude(getLocation())))
		                .field("altitude", getLocation().y)
		                .field("orientation", orientation.toMap())
		                .field("country", getCountry())
		                .field("weapon", getWeapon())
		                .field("rank", getRank())
		                .field("kills", kills)
		                .field("actions", CharacterDAO.MAPPER.writeValueAsString(actions))
		                .field("objectives", CharacterDAO.MAPPER.writeValueAsString(objectives))
		                .field("commandingOfficer", commandingOfficer)
		                .field("unitsUnderCommand", unitsUnderCommand)
		                .field("passengers", passengers)
		                .field("type", type)
		                .field("squadType", squadType)
		                .field("checkoutClient", checkoutClient)
		                .field("checkoutTime", checkoutTime)
		                .field("gunnerDead", gunnerDead)
		                .field("isDead", isDead)
		                .field("engineDamaged", engineDamaged)
		                .field("timeOfDeath", timeOfDeath)
		            .endObject();
	}
	
	
	public XContentBuilder getCommandStructureUpdate() throws IOException {	
		return jsonBuilder()
				.startObject()
				.field("unitsUnderCommand", unitsUnderCommand)
				.field("commandingOfficer", commandingOfficer)
				.field("isDead", isDead)
				.field("rank", rank)
				.field("objectives", CharacterDAO.MAPPER.writeValueAsString(objectives))
				.field("timeOfDeath", timeOfDeath)
				.field("kills", kills)				
				.endObject();
	}
	
	public XContentBuilder getStateUpdate() throws IOException{
			
		return jsonBuilder()
				.startObject()
				.field("location", new GeoPoint(toLatitute(getLocation()), toLongitude(getLocation())))
				.field("altitude", getLocation().y)
				.field("orientation", orientation.toMap())
				.field("actions", CharacterDAO.MAPPER.writeValueAsString(actions))
				.field("objectives", CharacterDAO.MAPPER.writeValueAsString(objectives))	
				.field("engineDamaged", engineDamaged)
				.field("checkoutClient", checkoutClient)
				.field("checkoutTime", checkoutTime)
				.field("gunnerDead", gunnerDead)
				.endObject();
	}

	public static double toLongitude(Vector location) {
		return location.x/LostVictoryScene.SCENE_WIDTH*180;
	}

	public static double toLatitute(Vector location) {
		return location.z/LostVictoryScene.SCENE_HEIGHT*80;
	}

	public boolean hasChanged(CharacterMessage other) {
		if(other==null || other.isDead){
			return false;
		}
		if(other.rank!=rank){
			return false;
		}
		
		return !location.equals(other.location) || !orientation.equals(other.orientation) || !actions.equals(other.actions);
	}

	public boolean isAvailableForUpdate(UUID clientID) {
		return this.id.equals(clientID) || this.checkoutClient==null || clientID.equals(this.checkoutClient) || checkoutTime==null ||System.currentTimeMillis()-checkoutTime>CHECKOUT_TIMEOUT;
	}

	public boolean isAvailableForCheckout() {
		return this.checkoutClient==null || checkoutTime==null ||System.currentTimeMillis()-checkoutTime>CHECKOUT_TIMEOUT;
	}
	
	public void updateState(CharacterMessage other, UUID clientID, long checkoutTime) {
		location = other.location;
		orientation = other.orientation;
		actions = other.actions;
		
		other.objectives.entrySet().stream().forEach(e->objectives.putIfAbsent(e.getKey(), e.getValue()));
		objectives = objectives.entrySet().stream().filter(e->!other.completedObjectives.contains(e.getKey())).collect(Collectors.toMap(e->e.getKey(), e->e.getValue()));
		
		this.checkoutClient = clientID;
		this.checkoutTime = checkoutTime;
	}

	public void setActions(Set<Action> actions) {
		this.actions = actions;
	}

	public Vector getOrientation() {
		return orientation;
	}

	public Set<Action> getActions() {
		return actions;
	}

	public void setOrientation(Vector orientation2) {
		this.orientation = orientation2;
	}

	public UUID getCheckoutClient() {
		return checkoutClient;
	}

	public void setCheckoutClient(UUID checkoutClient) {
		this.checkoutClient = checkoutClient;
	}

	public void setCheckoutTime(long checkoutTime) {
		this.checkoutTime = checkoutTime;
	}

	public XContentBuilder getJSONRepresentationUnChecked() {
		try {
			return getJSONRepresentation();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Map<String, String> getObjectives(){
		return objectives;
	}

	public void kill() {
		isDead = true;
		timeOfDeath = System.currentTimeMillis();
	}

	public boolean isDead() {
		return isDead;
	}

	public boolean isFullStrength() {
		return unitsUnderCommand.size()>=rank.getFullStrengthPopulation();
	}

	public Collection<CharacterMessage> reenforceCharacter(Vector spawnPoint, WeaponsFactory weaponsFactory, VehicleFactory vehicleFactory) {
		RankMessage rankToReenforce;
        rankToReenforce = reenformentCharacterRank(rank);
        Weapon weapon = weaponsFactory.getWeapon();
        CharacterType type = vehicleFactory.getVehicle(rankToReenforce);
        if(type!=null){
        	weapon = type.getDefaultWeapon();
        }else{        	
        	type = type==null?CharacterType.SOLDIER:type;
        }
		final CharacterMessage loadCharacter = new CharacterMessage(UUID.randomUUID(), type, spawnPoint, country, weapon, rankToReenforce, id, false);
		log.debug("creating reenforcement:"+loadCharacter.getId());
		loadCharacter.commandingOfficer = id;
		unitsUnderCommand.add(loadCharacter.getId());
        return ImmutableSet.of(loadCharacter);
	}

	private RankMessage reenformentCharacterRank(RankMessage rankMessage) {
		RankMessage rankToReenforce;
		if(rankMessage == RankMessage.COLONEL){
        	rankToReenforce = RankMessage.LIEUTENANT;
        }else if(rankMessage == RankMessage.LIEUTENANT){
            rankToReenforce = RankMessage.CADET_CORPORAL;
        }else{
            rankToReenforce = RankMessage.PRIVATE;
        }
		return rankToReenforce;
	}

	public CharacterMessage replaceWithAvatar(CharacterMessage deadAvatar, Collection<CharacterMessage> toUpdate, Map<UUID, CharacterMessage> allCharacters) {
		if(RankMessage.CADET_CORPORAL==rank){
			CharacterMessage characterMessage = new CharacterMessage(deadAvatar.getId(), deadAvatar.getUserID(), CharacterType.AVATAR, location, country, Weapon.RIFLE, RankMessage.CADET_CORPORAL, commandingOfficer, false);
			characterMessage.unitsUnderCommand = unitsUnderCommand;
			toUpdate.add(characterMessage);
			Set<CharacterMessage> collect = unitsUnderCommand.stream().map(uuid->allCharacters.get(uuid)).filter(c->c!=null).collect(Collectors.toSet());
			collect.forEach(c->c.commandingOfficer=characterMessage.id);
			toUpdate.addAll(collect);
			return characterMessage;
		}
		else{
			CharacterMessage characterMessage = new CharacterMessage(deadAvatar.getId(), deadAvatar.getUserID(), CharacterType.AVATAR, location, country, Weapon.RIFLE, reenformentCharacterRank(rank), id, false);
			toUpdate.add(characterMessage);
			return null;
		}
	}

	public long getVersion() {
		return version;
	}
	
	public long getTimeOfDeath(){
		return timeOfDeath;
	}

	public void replaceMe(CharacterDAO characterDAO, Map<UUID, CharacterMessage> toSave) {
		Map<UUID, CharacterMessage> oldSquad = characterDAO.getAllCharacters(unitsUnderCommand);
		log.debug("finding field replacement for"+country+":"+id+" ->["+unitsUnderCommand+"]");
		
		if(commandingOfficer!=null){
			CharacterMessage co = characterDAO.getCharacter(commandingOfficer);
			try{
				co.unitsUnderCommand.remove(id);
				co.calculateSquadType(characterDAO.getAllCharacters(co.unitsUnderCommand).values(), co.squadType);
				toSave.put(co.getId(), co);
			}catch(NullPointerException e){
				log.error(commandingOfficer+" not found in repo");
			}
		}
		
		Optional<CharacterMessage> findReplacement = findReplacement(oldSquad);
		if(findReplacement.isPresent()){
			final CharacterMessage toPromote = findReplacement.get();
			toPromote.rank = rank;
			toPromote.kills = new HashSet<UUID>();
			toPromote.objectives = new HashMap<String, String>();
			if(commandingOfficer!=null){
				toPromote.commandingOfficer = commandingOfficer;
			}
			log.info("promoting:"+toPromote.getId()+" to "+rank);
			Map<UUID, CharacterMessage> newSquad = oldSquad.entrySet().stream().filter(c->!c.getValue().equals(toPromote)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			newSquad.values().forEach(c->c.commandingOfficer = toPromote.getId());
			toPromote.addCharactersUnderCommand(newSquad.values().stream().collect(Collectors.toSet()));
			toSave.putAll(newSquad);
			characterDAO.putCharacter(toPromote.id, toPromote);
		}

	}

	private Optional<CharacterMessage> findReplacement(Map<UUID, CharacterMessage> allCharacters) {
		return allCharacters.values().stream().filter(c->c.type==CharacterType.SOLDIER || c.type==CharacterType.AVATAR).findAny();
	}

	public void incrementKills(UUID kill) {
		kills.add(kill);		
	}

    public boolean hasAchivedRankObjectives(CharacterDAO characterDAO) {
        return totalKillCount(characterDAO)>=rank.getKillCountForPromotion();
    }

	public int totalKillCount(CharacterDAO characterDAO) {
		int k = kills.size();
		for(CharacterMessage c:characterDAO.getAllCharacters(unitsUnderCommand).values()){
			k+=c.totalKillCount(characterDAO);
		}
		return k;
	}

	public Set<CharacterMessage> promoteCharacter(CharacterMessage co, CharacterDAO characterDAO) {
		Set<CharacterMessage> ret = new HashSet<CharacterMessage>();
		CharacterMessage replacemet = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, co.location, co.country, co.weapon, co.rank, co.id, co.gunnerDead);		
		replacemet.rank = rank;	
		replacemet.unitsUnderCommand = new HashSet<UUID>(unitsUnderCommand);
		replacemet.commandingOfficer = id;
		
		rank = co.getRank();
		objectives = new HashMap<String, String>();
		unitsUnderCommand = co.unitsUnderCommand.stream().filter(c->!c.equals(id)).collect(Collectors.toSet());
		commandingOfficer = co.commandingOfficer;
		kills = new HashSet<UUID>();
		
		Map<UUID, CharacterMessage> myNewUnits = characterDAO.getAllCharacters(unitsUnderCommand);
		myNewUnits.entrySet().stream().forEach(u->u.getValue().commandingOfficer=id);
		myNewUnits.entrySet().stream().forEach(u->u.getValue().kills = new HashSet<UUID>());
		ret.addAll(myNewUnits.values());
		Map<UUID, CharacterMessage> coNewUnits = characterDAO.getAllCharacters(replacemet.unitsUnderCommand);
		coNewUnits.entrySet().stream().forEach(u->u.getValue().commandingOfficer=replacemet.id);
		ret.addAll(coNewUnits.values());
		ret.add(this);
		
		CharacterMessage myNewCO = characterDAO.getCharacter(commandingOfficer);
		myNewCO.unitsUnderCommand.remove(co.getId());
		myNewCO.unitsUnderCommand.add(id);
		ret.add(myNewCO);
		
		characterDAO.putCharacter(replacemet.id, replacemet);
		characterDAO.delete(co);
		
		return ret;
	}

	public void addObjective(UUID id, String objective) {
		objectives.put(id.toString(), objective);
	}

	public Set<UUID> getUnitsUnderCommand() {
		return unitsUnderCommand;
	}

	public void setLocation(Vector vector) {
		this.location = vector;
	}
	


	private SquadType calculateSquadType(Collection<CharacterMessage> characters, SquadType squadType) {
		squadType = getSquadType(squadType);
		
		for(CharacterMessage c:characters){
			squadType = c.getSquadType(squadType);
		}
		
		return squadType;
	}

	private SquadType getSquadType(SquadType squadType) {
		if(type==CharacterType.ANTI_TANK_GUN){
			squadType = SquadType.ANTI_TANK_GUN;
		}else if(type==CharacterType.ARMORED_CAR && squadType!=SquadType.ANTI_TANK_GUN){
			squadType = SquadType.ARMORED_VEHICLE;
		}else if(weapon==Weapon.MORTAR && squadType!=SquadType.ARMORED_VEHICLE && squadType!=SquadType.ANTI_TANK_GUN){
			squadType = SquadType.MORTAR_TEAM;
		}else if(weapon==Weapon.MG42 && squadType!=SquadType.ARMORED_VEHICLE && squadType!=SquadType.ANTI_TANK_GUN){
			squadType = SquadType.MG42_TEAM;
		}
		return squadType;
	}

	public Weapon switchWeapon(UnClaimedEquipmentMessage equipment) {
		Weapon dropped = null;
		if(weapon.isReusable()){
			dropped = weapon;
		}
		weapon = equipment.getWeapon();
		return dropped;
	}

	public void boardVehicle(CharacterMessage vehicle) {
		this.boardedVehicle = vehicle.id;
		vehicle.passengers.add(id);
		
	}

	public Set<CharacterMessage> disembarkPassengers(CharacterDAO characterRepository) {
		Set<CharacterMessage> toChange = passengers.stream().map(id->characterRepository.getCharacter(id)).collect(Collectors.toSet());
		toChange.forEach(c->c.boardedVehicle=null);
		passengers.clear();
		toChange.add(this);
		return toChange;
	}

	public Set<UUID> getPassengers() {
		return passengers;
		
	}
	


}
