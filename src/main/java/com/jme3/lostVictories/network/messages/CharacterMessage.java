package com.jme3.lostVictories.network.messages;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static com.jme3.lostVictories.network.messages.Vector.latLongToVector;
import static lostVictories.dao.CharacterDAO.MAPPER;
import static com.jme3.lostVictories.objectives.Objective.toObjectiveSafe;
import static com.jme3.lostVictories.objectives.Objective.toJsonNodeSafe;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lostVictories.CharacterRunner;
import lostVictories.VehicleFactory;
import lostVictories.WeaponsFactory;
import lostVictories.dao.CharacterDAO;
import lostVictories.messageHanders.CharacterCatch;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.xcontent.XContentBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.jme3.lostVictories.network.messages.actions.Action;
import com.jme3.lostVictories.objectives.FollowCommander;
import com.jme3.lostVictories.objectives.Objective;
import com.jme3.lostVictories.objectives.PassiveObjective;

public class CharacterMessage implements Serializable{

	private static Logger log = Logger.getLogger(CharacterMessage.class);

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
	CharacterType type;
	Vector orientation = new Vector(0, 0, 1);
	Set<Action> actions = new HashSet<Action>();
	Map<String, String> objectives = new HashMap<String, String>();
	Set<String> completedObjectives;
	boolean dead;
	boolean engineDamaged;
	Long timeOfDeath;
	long version;
	Set<UUID> kills = new HashSet<UUID>();
	SquadType squadType = SquadType.RIFLE_TEAM;
	long creationTime;


	private CharacterMessage(){}

	public CharacterMessage(UUID identity, CharacterType type, Vector location, Country country, Weapon weapon, RankMessage rank, UUID commandingOfficer) {
		this(identity, null, type, location, country, weapon, rank, commandingOfficer);
	}

	public CharacterMessage(UUID identity, UUID userID, CharacterType type, Vector location, Country country, Weapon weapon, RankMessage rank, UUID commandingOfficer) {
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
		dead = (boolean) source.get("dead");
		engineDamaged = (boolean) source.get("engineDamaged");
		if(dead){
			this.checkoutTime = (Long) source.get("checkoutTime");
		}
		this.version = version;
		this.kills = ((Collection<String>)source.get("kills")).stream().map(s -> UUID.fromString(s)).collect(Collectors.toSet());
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
	
	public UUID getBoardedVehicle(){
		return boardedVehicle;
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
				.field("actions", MAPPER.writerFor(new TypeReference<Set<Action>>() {}).writeValueAsString(actions))
				.field("objectives", CharacterDAO.MAPPER.writeValueAsString(objectives))
				.field("commandingOfficer", commandingOfficer)
				.field("unitsUnderCommand", unitsUnderCommand)
				.field("passengers", passengers)
				.field("type", type)
				.field("squadType", squadType)
				.field("checkoutClient", checkoutClient)
				.field("checkoutTime", checkoutTime)
				.field("dead", dead)
				.field("engineDamaged", engineDamaged)
				.field("timeOfDeath", timeOfDeath)
				.endObject();
	}

	public XContentBuilder getCommandStructureUpdate() throws IOException {
		return jsonBuilder()
				.startObject()
				.field("unitsUnderCommand", unitsUnderCommand)
				.field("commandingOfficer", commandingOfficer)
				.field("dead", dead)
				.field("passengers", passengers)
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
				.field("actions", MAPPER.writerFor(new TypeReference<Set<Action>>() {}).writeValueAsString(actions))
				.field("objectives", CharacterDAO.MAPPER.writeValueAsString(objectives))	
				.field("checkoutClient", checkoutClient)
				.field("checkoutTime", checkoutTime)
				.endObject();
	}

	public XContentBuilder getStateUpdateNoCheckout() throws IOException{

		return jsonBuilder()
				.startObject()
				.field("location", new GeoPoint(toLatitute(getLocation()), toLongitude(getLocation())))
				.field("altitude", getLocation().y)
				.field("orientation", orientation.toMap())
				.field("objectives", CharacterDAO.MAPPER.writeValueAsString(objectives))	
				.field("engineDamaged", engineDamaged)
				.endObject();
	}

	public static double toLongitude(Vector location) {
		return location.x/LostVictoryScene.SCENE_WIDTH*180;
	}

	public static double toLatitute(Vector location) {
		return location.z/LostVictoryScene.SCENE_HEIGHT*80;
	}

	public boolean isAvailableForUpdate(UUID clientID, CharacterMessage msg, long duration) {
		if(msg.version<version){
			return false;
		}
		return this.id.equals(clientID) || this.checkoutClient==null || clientID.equals(this.checkoutClient) || checkoutTime==null ||System.currentTimeMillis()-checkoutTime>duration;
	}

	@JsonIgnore
	public boolean isAvailableForCheckout(long duration) {
		return this.checkoutClient==null || checkoutTime==null || (System.currentTimeMillis()-checkoutTime)>duration;
	}

	@JsonIgnore
	public boolean isBusy() {
		return isDead() || getObjectives().values().stream()
				.map(s->toJsonNodeSafe(s))
				.map(json->toObjectiveSafe(json))
				.filter(o->o!=null)
				.anyMatch(o->!(o instanceof PassiveObjective));
	}

	public void updateState(CharacterMessage other, UUID clientID, long checkoutTime) {
		location = other.location;
		orientation = other.orientation;
		actions = other.actions;
		
		other.objectives.entrySet().stream().forEach(e->objectives.putIfAbsent(e.getKey(), e.getValue()));
		objectives = objectives.entrySet().stream().filter(e->!other.completedObjectives.contains(e.getKey())).collect(Collectors.toMap(e->e.getKey(), e->e.getValue()));

		this.checkoutClient = clientID;
		this.checkoutTime = checkoutTime;
		this.version = other.version;
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
		dead = true;
		timeOfDeath = System.currentTimeMillis();
	}


	public boolean isDead() {
		return dead;
	}

	@JsonIgnore
	public boolean isFullStrength() {
		return unitsUnderCommand.size()>=rank.getFullStrengthPopulation();
	}

	public Collection<CharacterMessage> reenforceCharacter(Vector spawnPoint, WeaponsFactory weaponsFactory, VehicleFactory vehicleFactory, CharacterDAO characterDAO) throws IOException {
		RankMessage rankToReenforce;
		rankToReenforce = reenformentCharacterRank(rank);
		Weapon weapon = weaponsFactory.getWeapon();
		CharacterType type = vehicleFactory.getVehicle(rankToReenforce);
		if(type!=null){
			weapon = type.getDefaultWeapon();
			spawnPoint = (Country.AMERICAN==country)?LostVictoryScene.americanVehicleSpawnPoint:LostVictoryScene.germanVehicleSpawnPoint;
		}else{        	
			type = CharacterType.SOLDIER;
		}

		Set<CharacterMessage> addedCharacters = new HashSet<CharacterMessage>();
		final CharacterMessage loadCharacter = new CharacterMessage(UUID.randomUUID(), type, spawnPoint, country, weapon, rankToReenforce, id);
		loadCharacter.addObjective(UUID.randomUUID(), new FollowCommander(new Vector(2, 0, 2), 10));

		log.debug("creating reenforcement:"+loadCharacter.getId());
		unitsUnderCommand.add(loadCharacter.getId());
		
		if(CharacterType.ANTI_TANK_GUN==loadCharacter.type || CharacterType.ARMORED_CAR==loadCharacter.type || CharacterType.HALF_TRACK==loadCharacter.type){
			CharacterMessage passenger = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, spawnPoint, country, Weapon.RIFLE, RankMessage.PRIVATE, id);
			unitsUnderCommand.add(passenger.getId());
			loadCharacter.passengers.add(passenger.id);
			passenger.boardedVehicle = loadCharacter.id;
			characterDAO.putCharacter(passenger.getId(), passenger);
		}
		
		characterDAO.putCharacter(loadCharacter.getId(), loadCharacter);
		addedCharacters.add(this);
		return addedCharacters;
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

	public CharacterMessage replaceWithAvatar(CharacterMessage deadAvatar, Collection<CharacterMessage> toUpdate, CharacterCatch allCharacters) throws IOException {
		CharacterMessage newAvatar = null;
		CharacterMessage oldCo = allCharacters.getCharacter(commandingOfficer);

		if(RankMessage.CADET_CORPORAL==rank){
			CharacterMessage characterMessage = new CharacterMessage(deadAvatar.getId(), deadAvatar.getUserID(), CharacterType.AVATAR, location, country, Weapon.RIFLE, RankMessage.CADET_CORPORAL, commandingOfficer);
			characterMessage.unitsUnderCommand = unitsUnderCommand;
			toUpdate.add(characterMessage);
			Set<CharacterMessage> collect = unitsUnderCommand.stream().map(uuid->allCharacters.getCharacter(uuid)).filter(c->c!=null).collect(Collectors.toSet());
			for(CharacterMessage c:collect){
				c.commandingOfficer=characterMessage.id;
				c.addObjective(UUID.randomUUID(), new FollowCommander(new Vector(2, 0, 2), 10));
			}
			if(oldCo!=null){
				oldCo.unitsUnderCommand.remove(id);
				oldCo.unitsUnderCommand.add(characterMessage.getId());
				toUpdate.add(oldCo);
			}
			if(boardedVehicle!=null){
				characterMessage.boardedVehicle = boardedVehicle;
				CharacterMessage vehicle = allCharacters.getCharacter(boardedVehicle);
				vehicle.passengers.remove(id);
				vehicle.passengers.add(characterMessage.id);
				toUpdate.add(vehicle);
			}
			toUpdate.addAll(collect);
			newAvatar = characterMessage;

		}
		else{
			CharacterMessage characterMessage = new CharacterMessage(deadAvatar.getId(), deadAvatar.getUserID(), CharacterType.AVATAR, location, country, Weapon.RIFLE, reenformentCharacterRank(rank), id);
			toUpdate.add(characterMessage);
			if(oldCo!=null){
				oldCo.unitsUnderCommand.add(characterMessage.getId());
				toUpdate.add(oldCo);
			}
		}
		
		
		return newAvatar;
	}

	public long getVersion() {
		return version;
	}

	public Long getTimeOfDeath(){
		return timeOfDeath;
	}

	public void replaceMe(CharacterCatch characterDAO, Map<UUID, CharacterMessage> toSave) {

		Map<UUID, CharacterMessage> oldSquad = characterDAO.getAllCharacters(unitsUnderCommand);
		
		log.debug("finding field replacement for"+country+":"+id+" ->["+unitsUnderCommand+"]");

		CharacterMessage co = null;
		if(commandingOfficer!=null){
			co = characterDAO.getCharacter(commandingOfficer);
		}
		
		if(co!=null){
			co.unitsUnderCommand.remove(id);
			co.calculateSquadType(characterDAO.getAllCharacters(co.unitsUnderCommand).values(), co.squadType);
			toSave.put(co.getId(), co);
		}
		if(boardedVehicle!=null){
			CharacterMessage vehicle = characterDAO.getCharacter(boardedVehicle);
			vehicle.passengers.remove(id);
			toSave.put(vehicle.id, vehicle);
		}
		for(UUID id:getPassengers()){
			CharacterMessage passenger = characterDAO.getCharacter(id);
			passenger.kill();
			toSave.put(passenger.id, passenger);
			passenger.replaceMe(characterDAO, toSave);
		}
		
		Optional<CharacterMessage> findReplacement = findReplacement(oldSquad);
		if(findReplacement.isPresent()){
			CharacterMessage cc = findReplacement.get().promoteCharacter(characterDAO, toSave, oldSquad, rank, commandingOfficer);
			if(cc!=null){
				toSave.put(cc.id, cc);
			}
			if(co!=null){
				co.unitsUnderCommand.add(cc.id);
			}
		}else{
			oldSquad.values().stream().forEach(new Consumer<CharacterMessage>() {

				@Override
				public void accept(CharacterMessage t) {
					t.commandingOfficer = null;
					toSave.put(t.id, t);					
				}
			});
		}

	}

	private CharacterMessage promoteCharacter(CharacterCatch characterDAO, Map<UUID, CharacterMessage> toSave, Map<UUID, CharacterMessage> formerSquadMates, RankMessage newRank, UUID newCO) {
		Map<UUID, CharacterMessage> oldUnits = characterDAO.getAllCharacters(unitsUnderCommand);
		
		kills = new HashSet<UUID>();
		objectives = new HashMap<String, String>();
		unitsUnderCommand.clear();
		commandingOfficer = newCO;
		
		log.info("promoting:"+getId()+" to "+newRank);
		Map<UUID, CharacterMessage> newSquad = formerSquadMates.entrySet().stream().filter(c->!c.getValue().equals(this)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		newSquad.values().forEach(c->c.commandingOfficer = getId());
		addCharactersUnderCommand(newSquad.values().stream().collect(Collectors.toSet()));
		toSave.putAll(newSquad);
		toSave.put(id, this);
		Optional<CharacterMessage> findReplacement = findReplacement(oldUnits);
		if(findReplacement.isPresent()){
			CharacterMessage c = findReplacement.get().promoteCharacter(characterDAO, toSave, oldUnits, rank, id);
			unitsUnderCommand.add(c.id);
			toSave.put(c.id, c);
		}else{
			oldUnits.values().stream().forEach(new Consumer<CharacterMessage>() {

				@Override
				public void accept(CharacterMessage t) {
					t.commandingOfficer = null;
					toSave.put(t.id, t);					
				}
			});
		}
		rank = newRank;
		calculateSquadType(characterDAO.getAllCharacters(unitsUnderCommand).values(), squadType);
		return this;
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

	public Set<CharacterMessage> promoteAvatar(CharacterMessage co, CharacterDAO characterDAO) {
		Set<CharacterMessage> ret = new HashSet<CharacterMessage>();
		CharacterMessage replacemet = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, co.location, co.country, co.weapon, co.rank, co.id);		
		replacemet.rank = rank;	
		replacemet.unitsUnderCommand = new HashSet<UUID>(unitsUnderCommand);
		replacemet.commandingOfficer = id;

		rank = co.getRank();
		objectives = new HashMap<String, String>();
		unitsUnderCommand = co.unitsUnderCommand.stream().filter(c->!c.equals(id)).collect(Collectors.toSet());
		unitsUnderCommand.add(replacemet.id);
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

		if(commandingOfficer!=null){
			CharacterMessage myNewCO = characterDAO.getCharacter(commandingOfficer);
			myNewCO.unitsUnderCommand.remove(co.getId());
			myNewCO.unitsUnderCommand.add(id);
			ret.add(myNewCO);
		}

		characterDAO.putCharacter(replacemet.id, replacemet);
		characterDAO.delete(co);

		return ret;
	}

	public void addObjective(UUID id, String objective) {
		try{
			String asText = toJsonNodeSafe(objective).get("class").asText();

			Map<String, JsonNode> objectives = getObjectives().entrySet().stream().collect(Collectors.toMap(e->e.getKey(), e->toJsonNodeSafe(e.getValue())));
			for(Entry<String, JsonNode> entry:objectives.entrySet()){
				String asText2 = entry.getValue().get("class").asText();

				if(asText.equals(asText2)){
					getObjectives().remove(entry.getKey());
				}
			}


			Class newObjective = Class.forName(asText);

			for(Entry<String, JsonNode> entry:objectives.entrySet()){
				try{
					Class objectiveClass = Class.forName(entry.getValue().get("class").asText());
					if(objectiveClass!=null){
						Objective obj = (Objective) MAPPER.treeToValue(entry.getValue(), objectiveClass);
						if(obj.clashesWith(newObjective)){
							getObjectives().remove(entry.getKey());
						}
					}
				}catch(ClassNotFoundException e){
					log.trace(e);
				} catch (JsonParseException e) {
					log.trace(e);
				} catch (JsonMappingException e) {
					log.trace(e);
				} catch (IOException e) {
					log.trace(e);
				}
			}
		}catch(Throwable e){
			log.debug("adding unknow objective type:"+objective);
		} finally{
			objectives.put(id.toString(), objective);
		}
	}

	public void addObjective(UUID id, Objective objective) throws JsonProcessingException {
		addObjective(id, MAPPER.writeValueAsString(objective));

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

	public void boardVehicle(CharacterMessage vehicle, CharacterDAO characterDAO, Map<UUID, CharacterMessage> toSave) {
		log.debug("now boarding pasenger:"+id+", "+isAbandoned());

		CharacterMessage co;
		if(rank==RankMessage.PRIVATE){
			co = characterDAO.getCharacter(commandingOfficer);
		}else{
			co = this;
		}
		if(vehicle.commandingOfficer!=null && !vehicle.commandingOfficer.equals(co.id)){
			if(!vehicle.isAbandoned() && vehicle.getCountry()!=country){
				return;
			}
			vehicle.disembarkPassengers(characterDAO, false).forEach(c->toSave.put(c.id, c));
			CharacterMessage oldCo = characterDAO.getCharacter(vehicle.commandingOfficer);
			vehicle.commandingOfficer = co.id;
			vehicle.objectives.clear();
			vehicle.country = country;
			oldCo.unitsUnderCommand.remove(vehicle.id);
			co.unitsUnderCommand.add(vehicle.id);
			toSave.put(oldCo.id, oldCo);
			toSave.put(co.id, co);
		}

		this.boardedVehicle = vehicle.id;
		vehicle.passengers.add(id);

		toSave.put(vehicle.getId(), vehicle);
		toSave.put(getId(), this);
	}

	@JsonIgnore
	public boolean isAbandoned() {
		return passengers.isEmpty();
	}

	public Set<CharacterMessage> disembarkPassengers(CharacterDAO characterRepository, boolean leaveGunner) {
		Set<CharacterMessage> toChange = passengers.stream().map(id->characterRepository.getCharacter(id)).collect(Collectors.toSet());
		Optional<CharacterMessage> findAny = toChange.stream().filter(c->c!=null).filter(c->c.getCharacterType()==CharacterType.SOLDIER).findAny();

		toChange.forEach(c->c.boardedVehicle=null);
		passengers.clear();
		if(findAny.isPresent() && leaveGunner){
			passengers.add(findAny.get().id);
			findAny.get().boardedVehicle = id;
		}
		toChange.add(this);
		return toChange;
	}

	public Set<UUID> getPassengers() {
		return new HashSet<UUID>(passengers);

	}

	public CharacterMessage killPassenger(CharacterCatch characterDAO) {
		Set<CharacterMessage> toChange = passengers.stream().map(id->characterDAO.getCharacter(id)).collect(Collectors.toSet());
		Optional<CharacterMessage> findAny = toChange.stream().filter(c->c.getCharacterType()==CharacterType.SOLDIER).findAny();
		if(findAny.isPresent()){
			CharacterMessage characterMessage = findAny.get();
			passengers.remove(characterMessage.id);
			return characterMessage;
		}
		if(!passengers.isEmpty()){
			Iterator<UUID> iterator = passengers.iterator();
			UUID next = iterator.next();
			iterator.remove();
			return characterDAO.getCharacter(next);
		}
		return null;
	}

	public void setVersion(long i) {
		this.version = i;

	}

	public void addopt(CharacterMessage c) {
		c.commandingOfficer = id;
		addCharactersUnderCommand(c);
		
	}

	public int getCurrentStrength(CharacterDAO characterDAO) {
		int population = 1;
		for(UUID u:unitsUnderCommand){
			CharacterMessage character = characterDAO.getCharacter(u);
			if(character!=null){
				population+=character.getCurrentStrength(characterDAO);
			}
		}
		return population;
	}

	public void addPassengers(UUID...newPAssengers) {
		for(UUID p:newPAssengers){
			passengers.add(p);
		}
		
	}

    public Objective getObjectiveSafe(UUID value) {
		if(objectives.get(value.toString())!=null) {
			try {
				return MAPPER.readValue(objectives.get(value.toString()), Objective.class);
			} catch (IOException e) {}
		}
		return null;
    }

	public long getCreationTime() {
		return creationTime;
	}
}
