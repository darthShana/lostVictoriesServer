package com.jme3.lostVictories.network.messages;

import static com.jme3.lostVictories.network.messages.Vector.latLongToVector;
import static com.lostVictories.service.LostVictoriesService.uuid;
import static lostVictories.dao.CharacterDAO.MAPPER;
import static com.jme3.lostVictories.objectives.Objective.toObjectiveSafe;
import static com.jme3.lostVictories.objectives.Objective.toJsonNodeSafe;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
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
import com.fasterxml.jackson.databind.JavaType;
import com.jme3.lostVictories.objectives.*;
import lostVictories.VehicleFactory;
import lostVictories.WeaponsFactory;
import lostVictories.dao.CharacterDAO;
import lostVictories.messageHanders.CharacterCatch;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.jme3.lostVictories.network.messages.actions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.GeoCoordinate;

public class CharacterMessage implements Serializable{

	private static Logger log = LoggerFactory.getLogger(CharacterMessage.class);

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
	Set<String> completedObjectives = new HashSet<>();
	boolean dead;
	boolean engineDamaged;
	Long timeOfDeath;
	long version;
	@JsonIgnore
	Set<UUID> kills = new HashSet<UUID>();
	SquadType squadType;
	long creationTime;


	public CharacterMessage(){}

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
		this.squadType = getSquadType(SquadType.RIFLE_TEAM);
		if(commandingOfficer!=null){
			this.commandingOfficer = commandingOfficer;
		}


    }

	public CharacterMessage(Map<String, String> source, GeoCoordinate geoCoordinate) {
        try {
            this.id = UUID.fromString(source.get("id"));
            this.type = CharacterType.valueOf((String) source.get("type"));
            if(source.containsKey("userID")){
                this.userID = UUID.fromString(source.get("userID"));
            }

            this.country = Country.valueOf(source.get("country"));
            this.weapon = Weapon.valueOf(source.get("weapon"));
            this.rank = RankMessage.valueOf(source.get("rank"));
            if(source.containsKey("commandingOfficer")){
                this.commandingOfficer = UUID.fromString(source.get("commandingOfficer"));
            }
            if(source.containsKey("boardedVehicle")){
                this.boardedVehicle = UUID.fromString(source.get("boardedVehicle"));
            }
            if(source.containsKey("unitsUnderCommand")){
                JavaType type = MAPPER.getTypeFactory().constructCollectionType(Set.class, UUID.class);
                unitsUnderCommand = MAPPER.readValue(source.get("unitsUnderCommand"), type);
            }
            if(source.containsKey("passengers")){
                JavaType type = MAPPER.getTypeFactory().constructCollectionType(Set.class, UUID.class);
                passengers = MAPPER.readValue(source.get("passengers"), type);
            }
            if(source.containsKey("boardedVehicle")){
                this.boardedVehicle = UUID.fromString(source.get("boardedVehicle"));
            }
            if(source.containsKey("checkoutClient")){
                this.checkoutClient = UUID.fromString(source.get("checkoutClient"));
            }
            if(source.containsKey("checkoutTime")){
                this.checkoutTime = Long.parseLong(source.get("checkoutTime"));
            }
            this.orientation = MAPPER.readValue(source.get("orientation"), Vector.class);

            if(source.containsKey("actions")){
                JavaType type = MAPPER.getTypeFactory().constructCollectionType(Set.class, Action.class);
                actions = MAPPER.readValue(source.get("actions"), type);
            }
            String o = source.get("objectives");
            if(!"[{}]".equals(o)){
                this.objectives = CharacterDAO.MAPPER.readValue(o, new TypeReference<Map<String, String>>() {});
            }
            dead = Boolean.parseBoolean(source.get("isDead"));
            engineDamaged = Boolean.parseBoolean(source.get("engineDamaged"));
            if(source.containsKey("timeOfDeath")){
                this.timeOfDeath = Long.parseLong(source.get("timeOfDeath"));
            }
            this.version = Long.parseLong(source.get("version"));
            if(source.containsKey("kills")){
                JavaType type = MAPPER.getTypeFactory().constructCollectionType(Set.class, UUID.class);
                kills = MAPPER.readValue(source.get("kills"), type);
            }
            squadType = SquadType.valueOf(source.get("squadType"));
            float altitude = Float.parseFloat(source.get("altitude"));

            this.location = latLongToVector(altitude, (float) geoCoordinate.getLongitude(), (float) geoCoordinate.getLatitude());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

	public int getKillCount(){ return kills.size();}

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

	public Map<String, String> getMapRepresentation() throws IOException {
		Map<String, String> ret = new HashMap<>();
		ret.put("id", id.toString());
		if(userID!=null) {
			ret.put("userID",userID.toString());
		}
		if(boardedVehicle!=null){
			ret.put("boardedVehicle", boardedVehicle.toString());
		}

		ret.put("altitude", getLocation().y+"");
		ret.put("orientation", CharacterDAO.MAPPER.writeValueAsString(orientation));
		ret.put("country", getCountry().toString());
		ret.put("weapon", getWeapon().toString());
		ret.put("rank", getRank().toString());
		ret.put("kills", CharacterDAO.MAPPER.writeValueAsString(kills));
		ret.put("actions", MAPPER.writerFor(new TypeReference<Set<Action>>() {}).writeValueAsString(actions));
		ret.put("objectives", CharacterDAO.MAPPER.writeValueAsString(objectives));
		if(commandingOfficer!=null){
			ret.put("commandingOfficer", commandingOfficer.toString());
		}

		ret.put("unitsUnderCommand", CharacterDAO.MAPPER.writeValueAsString(unitsUnderCommand));
		ret.put("passengers", CharacterDAO.MAPPER.writeValueAsString(passengers));
		ret.put("type", type.toString());
		ret.put("squadType", squadType.toString());
		if(checkoutClient!=null){
			ret.put("checkoutClient", checkoutClient.toString());
		}

		if(checkoutTime!=null){
			ret.put("checkoutTime", checkoutTime.toString());
		}

		ret.put("isDead", dead+"");
		ret.put("engineDamaged", engineDamaged+"");
		if(timeOfDeath!=null){
			ret.put("timeOfDeath", timeOfDeath.toString());
		}
		ret.put("version", version+"");
		return ret;
	}

	public Map<String, String> getCommandStructureUpdate() {
		Map<String, String> ret = new HashMap<>();

		try{
			ret.put("unitsUnderCommand", CharacterDAO.MAPPER.writeValueAsString(unitsUnderCommand));
			ret.put("commandingOfficer", (commandingOfficer!=null)?commandingOfficer.toString():null);
			ret.put("isDead", dead+"");
			ret.put("passengers", CharacterDAO.MAPPER.writeValueAsString(passengers));
			ret.put("rank", getRank().toString());
			ret.put("objectives", CharacterDAO.MAPPER.writeValueAsString(objectives));
			ret.put("timeOfDeath", (timeOfDeath!=null)?timeOfDeath.toString():null);
			ret.put("kills", CharacterDAO.MAPPER.writeValueAsString(kills));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		return ret;
	}

	public Map<String, String> getStateUpdate() {
		Map<String, String> ret = new HashMap<>();

		try{
			ret.put("altitude", getLocation().y+"");
			ret.put("orientation", CharacterDAO.MAPPER.writeValueAsString(orientation));
			ret.put("actions", MAPPER.writerFor(new TypeReference<Set<Action>>() {}).writeValueAsString(actions));
			ret.put("objectives", CharacterDAO.MAPPER.writeValueAsString(objectives));
			ret.put("checkoutClient", (checkoutClient!=null)?checkoutClient.toString():null);
			ret.put("checkoutTime", (checkoutTime!=null)?checkoutTime.toString():null);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		return ret;
	}

	public Map<String, String> getStateUpdateNoCheckout() {
		Map<String, String> ret = new HashMap<>();

		try {
			ret.put("altitude", getLocation().y+"");
			ret.put("orientation", CharacterDAO.MAPPER.writeValueAsString(orientation));
			ret.put("objectives", CharacterDAO.MAPPER.writeValueAsString(objectives));
			ret.put("engineDamaged", engineDamaged+"");
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}


		return ret;
	}


	public static double toLongitude(Vector location) {
		return location.x/LostVictoryScene.SCENE_WIDTH*180;
	}

	public static double toLatitute(Vector location) {
		return location.z/LostVictoryScene.SCENE_HEIGHT*80;
	}

	public boolean isAvailableForUpdate(UUID clientID, com.lostVictories.api.CharacterMessage msg, long duration) {
		if(msg.getVersion()<version){
			return false;
		}
		if(dead || msg.getDead()){
		    return false;
        }
		return this.id.equals(clientID) || this.checkoutClient==null || clientID.equals(this.checkoutClient) || checkoutTime==null ||System.currentTimeMillis()-checkoutTime>duration;
	}

	@JsonIgnore
	public boolean isAvailableForCheckout(long duration) {
		return this.checkoutClient==null || checkoutTime==null || (System.currentTimeMillis()-checkoutTime)>duration;
	}

	public boolean isCheckedOutBy(UUID clientID, long duration) {
		return this.checkoutClient!=null && (checkoutClient.equals(clientID)) && checkoutTime!=null && (System.currentTimeMillis()-checkoutTime)<duration;
	}

	public boolean isBusy() {
		return isDead() || objectives.values().stream()
				.map(s->toJsonNodeSafe(s))
				.map(json->toObjectiveSafe(json))
				.filter(o->o!=null)
				.anyMatch(o->!(o instanceof PassiveObjective));
	}

	public boolean isAttacking() {
		for(String s:objectives.values()){
			if(isAttackingObjective(toJsonNodeSafe(s))){
				return true;
			}
		}
		return false;
	}

	private boolean isAttackingObjective(JsonNode n) {
		String s = n.get("class").asText();
		return  "com.jme3.lostVictories.objectives.AttackBoggies".equals(s) ||
				"com.jme3.lostVictories.objectives.AttackAndTakeCoverObjective".equals(s) ||
				"com.jme3.lostVictories.objectives.AttackObjective".equals(s);
	}

	public void updateState(com.lostVictories.api.CharacterMessage other, UUID clientID, long checkoutTime) {
		location = new Vector(other.getLocation());
		orientation = new Vector(other.getOrientation());
		actions = other.getActionsList().stream().map(action -> Action.fromMessage(action)).collect(Collectors.toSet());

		other.getObjectivesMap().entrySet().stream().forEach(e->objectives.put(e.getKey(), e.getValue()));
		Set<String> completed = other.getCompletedObjectivesList().stream().map(co->uuid(co).toString()).collect(Collectors.toSet());
		objectives = objectives.entrySet().stream().filter(e->!completed.contains(e.getKey())).collect(Collectors.toMap(e->e.getKey(), e->e.getValue()));

		this.checkoutClient = clientID;
		this.checkoutTime = checkoutTime;
		this.version = other.getVersion();
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

	public Long getCheckoutTime(){
		return checkoutTime;
	}

	public void setCheckoutClient(UUID checkoutClient) {
		this.checkoutClient = checkoutClient;
	}

	public void setCheckoutTime(long checkoutTime) {
		this.checkoutTime = checkoutTime;
	}

	public Map<String, String> readObjectives(){
		return new HashMap<>(objectives);
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

		if(loadCharacter.type.requiresOperator()){
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



		CharacterMessage co = null;
		if(commandingOfficer!=null){
			co = characterDAO.getCharacter(commandingOfficer);
		}

		if(co!=null){
			co.unitsUnderCommand.remove(id);
			co.calculateSquadType(characterDAO.getAllCharacters(co.unitsUnderCommand).values(), co.squadType);
			toSave.put(co.getId(), co);
		}
		if(boardedVehicle!=null && characterDAO.getCharacter(boardedVehicle)!=null){
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
            log.info("field replacement for"+country+":"+id+" ->["+unitsUnderCommand+"] with =>"+findReplacement.get().id);
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
		log.info("replacing co:"+co.getId()+" with avatar:"+id);
		Set<CharacterMessage> ret = new HashSet<CharacterMessage>();
		CharacterMessage replacement = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, co.location, co.country, co.weapon, co.rank, id);

		replacement.rank = rank;
		replacement.unitsUnderCommand = new HashSet<>(unitsUnderCommand);

		rank = co.getRank();
		objectives = new HashMap<>();
		unitsUnderCommand = co.unitsUnderCommand.stream().filter(c->!c.equals(id)).collect(Collectors.toSet());
		commandingOfficer = co.commandingOfficer;
		kills = new HashSet<>();

		Map<UUID, CharacterMessage> myNewUnits = characterDAO.getAllCharacters(unitsUnderCommand);
		unitsUnderCommand.add(replacement.id);

		myNewUnits.entrySet().stream().forEach(u->u.getValue().commandingOfficer=id);
		myNewUnits.entrySet().stream().forEach(u->u.getValue().kills = new HashSet<UUID>());
		ret.addAll(myNewUnits.values());
		Map<UUID, CharacterMessage> coNewUnits = characterDAO.getAllCharacters(replacement.unitsUnderCommand);
		coNewUnits.entrySet().stream().forEach(u->u.getValue().commandingOfficer=replacement.id);
		ret.addAll(coNewUnits.values());
		ret.add(this);

		if(commandingOfficer!=null){
			CharacterMessage myNewCO = characterDAO.getCharacter(commandingOfficer);
			myNewCO.unitsUnderCommand.remove(co.getId());
			myNewCO.unitsUnderCommand.add(id);
			ret.add(myNewCO);
		}

        log.info("creating new character:"+replacement.getId()+" to replace deleted character:"+co.getId());
		characterDAO.putCharacter(replacement.id, replacement);
		characterDAO.delete(co);

		log.info("marked for command structure update:"+ret.stream().map(ss->ss.getId()).collect(Collectors.toSet()));


		return ret;
	}

	public void addObjective(UUID id, String objective) {
		try{
			String asText = toJsonNodeSafe(objective).get("class").asText();

			Map<String, JsonNode> objectives = this.objectives.entrySet().stream().collect(Collectors.toMap(e->e.getKey(), e->toJsonNodeSafe(e.getValue())));
			for(Entry<String, JsonNode> entry:objectives.entrySet()){
				String asText2 = entry.getValue().get("class").asText();

				if(asText.equals(asText2)){
					this.objectives.remove(entry.getKey());
				}
			}


			Class newObjective = Class.forName(asText);

			for(Entry<String, JsonNode> entry:objectives.entrySet()){
				try{
					Class objectiveClass = Class.forName(entry.getValue().get("class").asText());
					if(objectiveClass!=null){
						Objective obj = (Objective) MAPPER.treeToValue(entry.getValue(), objectiveClass);
						if(obj.clashesWith(newObjective)){
							this.objectives.remove(entry.getKey());
						}
					}
				}catch(ClassNotFoundException | IOException e) {
					log.trace("unknown objective on server. will save but no run: "+entry.getValue().get("class").asText());
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

	public SquadType getSquadType(SquadType squadType) {
		if(type==CharacterType.ANTI_TANK_GUN){
			squadType = SquadType.ANTI_TANK_GUN;
		}else if(type==CharacterType.PANZER4 || type==CharacterType.M4SHERMAN){
		    squadType = SquadType.TANK_SQUAD;
        }else if(type==CharacterType.ARMORED_CAR || type==CharacterType.HALF_TRACK){
			squadType = SquadType.ARMORED_VEHICLE;
		}else if(weapon==Weapon.MORTAR){
			squadType = SquadType.MORTAR_TEAM;
		}else if(weapon==Weapon.MG42){
			squadType = SquadType.MG42_TEAM;
		}else if(weapon==Weapon.BAZOOKA){
		    squadType = SquadType.BAZOOKA_TEAM;
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
		log.debug("now boarding pasenger:"+id+", "+vehicle.isAbandoned());

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
			if(vehicle.passengers.stream().map(p->characterDAO.getCharacter(p)).anyMatch(p->p.getCharacterType()==CharacterType.AVATAR)){
				return;
			}
			vehicle.disembarkPassengers(characterDAO, false).forEach(c->toSave.put(c.id, c));
			CharacterMessage oldCo = characterDAO.getCharacter(vehicle.commandingOfficer);
			vehicle.commandingOfficer = co.id;
			vehicle.objectives.clear();
			vehicle.country = country;
			co.unitsUnderCommand.add(vehicle.id);
			if(oldCo!=null) {
                oldCo.unitsUnderCommand.remove(vehicle.id);
                toSave.put(oldCo.id, oldCo);
            }
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


	public void updateObjective(String key, String s) {
		if(objectives.containsKey(key)) {
			objectives.put(key, s);
		} else{
			throw new RuntimeException("unknown objective key:"+key+" body:"+s);
		}
	}

	public void removeObjective(String key) {
		objectives.remove(key);
	}

	public void clearObjectives() {
		objectives.clear();
	}

	@JsonIgnore
	public Set<UUID> getKills() {
		return kills;
	}

	@JsonIgnore
	public boolean hasEngineDamage() {
		return engineDamaged;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public Set<String> getAllCompletedObjectives() {
		return completedObjectives;
	}

    public void setType(CharacterType characterType) {
        this.type = characterType;
    }
}