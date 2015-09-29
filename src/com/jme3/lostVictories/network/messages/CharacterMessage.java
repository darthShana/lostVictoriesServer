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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lostVictories.CharacterDAO;
import lostVictories.LostVictoryScene;

import org.apache.log4j.Logger;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.collect.ImmutableSet;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.type.TypeReference;

import com.jme3.lostVictories.network.messages.actions.Action;

public class CharacterMessage implements Serializable{
	
	private static final long serialVersionUID = 2491659254334134796L;
	private static Logger log = Logger.getLogger(CharacterMessage.class);
	public static final long CHECKOUT_TIMEOUT = 10*1000;
	
	UUID id;
	Vector location;
	Country country;
	Weapon weapon;
	RankMessage rank;
	UUID commandingOfficer;
	Set<UUID> unitsUnderCommand = new HashSet<UUID>();
	UUID checkoutClient;
	Long checkoutTime;
	boolean gunnerDead;
	CharacterType type;
	Vector orientation = new Vector(0, 0, 1);
	Set<Action> actions = new HashSet<Action>();
	Map<String, String> objectives = new HashMap<String, String>();
	boolean isDead;
	Long timeOfDeath;
	long version;
	int kills;
	
	public CharacterMessage(UUID identity, CharacterType type, Vector location, Country country, Weapon weapon, RankMessage rank, UUID commandingOfficer, boolean gunnerDead) {
		this.id = identity;
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
			if(!"{}".equals(a)){
				this.actions = CharacterDAO.MAPPER.readValue(a, new TypeReference<Set<Action>>() {});
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		this.orientation = new Vector(ori.get("x").floatValue(), ori.get("y").floatValue(), ori.get("z").floatValue());
		String co = (String) source.get("commandingOfficer");
		if(co!=null && !co.isEmpty()){
			this.commandingOfficer = UUID.fromString(co);
		}
		String cc = (String) source.get("checkoutClient");
		if(cc!=null && !cc.isEmpty()){
			this.checkoutClient = UUID.fromString(cc);
			this.checkoutTime = (Long) source.get("checkoutTime");
		}
		this.timeOfDeath = (Long) source.get("timeOfDeath");
		
		unitsUnderCommand = ((Collection<String>)source.get("unitsUnderCommand")).stream().map(s -> UUID.fromString(s)).collect(Collectors.toSet());
		gunnerDead = (boolean) source.get("gunnerDead");
		isDead = (boolean) source.get("isDead");
		if(isDead){
			this.checkoutTime = (Long) source.get("checkoutTime");
		}
		this.version = version;
		this.kills = (int) source.get("kills");
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

	public void addCharactersUnderCommand(Set<CharacterMessage> cc) {
		unitsUnderCommand.addAll(cc.stream().map(c -> c.id).collect(Collectors.toList()));
	}

	public void addCharactersUnderCommand(CharacterMessage... atg) {
		unitsUnderCommand.addAll(Arrays.asList(atg).stream().map(c -> c.id).collect(Collectors.toList()));
	}

	public UUID getId() {
		return id;
	}

	public XContentBuilder getJSONRepresentation() throws IOException {
		return jsonBuilder()
		            .startObject()
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
		                .field("type", type)
		                .field("checkoutClient", checkoutClient)
		                .field("checkoutTime", checkoutTime)
		                .field("gunnerDead", gunnerDead)
		                .field("isDead", isDead)
		                .field("timeOfDeath", timeOfDeath)
		            .endObject();
	}
	
	
	public XContentBuilder getCommandStructureUpdate() throws IOException {	
		return jsonBuilder()
				.startObject()
				.field("unitsUnderCommand", unitsUnderCommand)
				.field("commandingOfficer", commandingOfficer)
				.field("isDead", isDead)
				.field("timeOfDeath", timeOfDeath)
				.field("kills", kills)				
				.endObject();
	}
	
	public XContentBuilder getStateUpdate() throws IOException{
		return jsonBuilder()
				.startObject()
				.field("location", new GeoPoint(toLatitute(getLocation()), toLongitude(getLocation())))
				.field("orientation", orientation.toMap())
				.field("actions", CharacterDAO.MAPPER.writeValueAsString(actions))
				.field("objectives", CharacterDAO.MAPPER.writeValueAsString(objectives))				
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
		
		return !location.equals(other.location) || !orientation.equals(other.orientation) || !actions.equals(other.actions);
	}

	public boolean isAvailableForUpdate(UUID clientID) {
		return this.id.equals(clientID) || this.checkoutClient==null || clientID.equals(this.checkoutClient) || checkoutTime==null ||System.currentTimeMillis()-checkoutTime>CHECKOUT_TIMEOUT;
	}

	public void updateState(CharacterMessage other, UUID clientID, long checkoutTime) {
		location = other.location;
		orientation = other.orientation;
		actions = other.actions;
		objectives = objectives.entrySet().stream().filter(e->other.objectives.containsKey(e.getKey())).collect(Collectors.toMap(e->e.getKey(), e->e.getValue()));
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

	public Collection<CharacterMessage> reenforceCharacter(Vector spawnPoint) {
		RankMessage rankToReenforce;
        if(rank == RankMessage.COLONEL){
        	rankToReenforce = RankMessage.LIEUTENANT;
        }else if(rank == RankMessage.LIEUTENANT){
            rankToReenforce = RankMessage.CADET_CORPORAL;
        }else{
            rankToReenforce = RankMessage.PRIVATE;
        }
		final CharacterMessage loadCharacter = new CharacterMessage(UUID.randomUUID(), CharacterType.SOLDIER, spawnPoint, country, Weapon.RIFLE, rankToReenforce, id, false);
		loadCharacter.commandingOfficer = id;
		unitsUnderCommand.add(loadCharacter.getId());
        return ImmutableSet.of(loadCharacter);
	}

	public CharacterMessage replaceWithAvatar(UUID uuid) {
		CharacterMessage characterMessage = new CharacterMessage(uuid, CharacterType.AVATAR, location, country, Weapon.RIFLE, RankMessage.CADET_CORPORAL, commandingOfficer, false);
		characterMessage.unitsUnderCommand = unitsUnderCommand;
		return characterMessage;
	}

	public long getVersion() {
		return version;
	}
	
	public long getTimeOfDeath(){
		return timeOfDeath;
	}

	public Map<UUID, CharacterMessage> replaceMe(CharacterDAO characterDAO) {
		Map<UUID, CharacterMessage> ret = new HashMap<UUID, CharacterMessage>();
		Map<UUID, CharacterMessage> allCharacters = characterDAO.getAllCharacters(unitsUnderCommand);
		
		if(commandingOfficer!=null){
			CharacterMessage co = characterDAO.getCharacter(commandingOfficer);
			try{
				co.unitsUnderCommand.remove(id);
				ret.put(co.getId(), co);
			}catch(NullPointerException e){
				log.error(commandingOfficer+" not found in repo");
			}
		}
		
		if(!allCharacters.isEmpty()){
			final CharacterMessage toPromote = allCharacters.values().iterator().next();
			toPromote.rank = rank;
			toPromote.kills = 0;
			if(commandingOfficer!=null){
				toPromote.commandingOfficer = commandingOfficer;
			}
			log.info("promoting:"+toPromote.getId()+" to "+rank);
			Set<CharacterMessage> newSquad = allCharacters.values().stream().filter(c->!c.equals(toPromote)).collect(Collectors.toSet());
			newSquad.forEach(c->c.commandingOfficer = toPromote.getId());
			toPromote.addCharactersUnderCommand(newSquad);
			ret.putAll(allCharacters);
		}

		return ret;
	}

	public void incrementKillCount() {
		kills++;		
	}

    public boolean hasAchivedRankObjectives() {
        return kills>=rank.getKillCountForPromotion();
    }

	public Set<CharacterMessage> promoteCharacter(CharacterMessage co, CharacterDAO characterDAO) {
		Set<CharacterMessage> ret = new HashSet<CharacterMessage>();
		RankMessage oldRank = rank;
		rank = co.getRank();
		co.rank = oldRank;
		
		co.unitsUnderCommand = unitsUnderCommand;
		unitsUnderCommand = characterDAO.getAllCharacters(co.unitsUnderCommand).entrySet().stream().filter(c->!c.getKey().equals(id)).map(c->c.getValue().getId()).collect(Collectors.toSet());
		
		ret.add(this);
		ret.add(co);
		kills = 0;
		return ret;
	}

	public void addObjective(UUID id, String objective) {
		objectives.put(id.toString(), objective);
	}

	public Set<UUID> getUnitsUnderCommand() {
		return unitsUnderCommand;
	}

	


}
