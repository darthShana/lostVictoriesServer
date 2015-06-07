package com.jme3.lostVictories.network.messages;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

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

import lostVictories.LostVictoryScene;

import org.apache.lucene.queryparser.flexible.core.util.StringUtils;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.netty.util.internal.StringUtil;
import org.elasticsearch.common.xcontent.XContentBuilder;

public class CharacterMessage implements Serializable{
	
	UUID id;
	Vector location;
	Country country;
	Weapon weapon;
	RankMessage rank;
	UUID commandingOfficer;
	Set<UUID> unitsUnderCommand = new HashSet<UUID>();
	UUID checkout;
	boolean gunnerDead;
	CharacterType type;
	double orientation = 0.0;
	Action action = Action.IDLE;
	
	public CharacterMessage(UUID identity, CharacterType type, Vector location, Country country, Weapon weapon, RankMessage rank, CharacterMessage commandingOfficer, boolean gunnerDead) {
		this.id = identity;
		this.type = type;
		this.location = location;
		this.country = country;
		this.weapon = weapon;
		this.rank = rank;
		if(commandingOfficer!=null){
			this.commandingOfficer = commandingOfficer.id;
		}
	}
	
	public CharacterMessage(UUID id, Map<String, Object> source) {
		this.id = id;
		HashMap<String, Double> location =  (HashMap<String, Double>) source.get("location");
		long altitude = ((Double)source.get("altitude")).longValue();
		this.type = CharacterType.valueOf((String) source.get("type"));
		this.location = new Vector(location.get("lon")/180*LostVictoryScene.SCENE_WIDTH, altitude, location.get("lat")/80*LostVictoryScene.SCENE_HEIGHT);
		this.country = Country.valueOf((String)source.get("country"));
		this.weapon = Weapon.valueOf((String) source.get("weapon"));
		this.rank = RankMessage.valueOf((String) source.get("rank"));
		this.action = Action.valueOf((String)source.get("action"));
		this.orientation = (Double)source.get("orientation");
		String co = (String) source.get("commandingOfficer");
		if(co!=null && !co.isEmpty()){
			this.commandingOfficer = UUID.fromString(co);
		}
		unitsUnderCommand = ((Collection<String>)source.get("unitsUnderCommand")).stream().map(s -> UUID.fromString(s)).collect(Collectors.toSet());
		gunnerDead = (boolean) source.get("gunnerDead");
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
		                .field("orientation", orientation)
		                .field("country", getCountry())
		                .field("weapon", getWeapon())
		                .field("rank", getRank())
		                .field("action", action)
		                .field("commandingOfficer", commandingOfficer)
		                .field("unitsUnderCommand", unitsUnderCommand)
		                .field("type", type)
		                .field("gunnerDead", gunnerDead)
		            .endObject();
	}

	public static double toLongitude(Vector location) {
		return location.x/LostVictoryScene.SCENE_WIDTH*180;
	}

	public static double toLatitute(Vector location) {
		return location.z/LostVictoryScene.SCENE_HEIGHT*80;
	}
	
}
