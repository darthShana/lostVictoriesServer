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

import com.google.gson.Gson;

public class Character implements Serializable{
	
	UUID id;
	Vector location;
	Country country;
	Weapon weapon;
	Rank rank;
	UUID commandingOfficer;
	Set<UUID> unitsUnderCommand = new HashSet<UUID>();
	UUID checkout;
	boolean gunnerDead;
	CharacterType type;
	Action action = new IdleAction();
	
	public Character(UUID identity, CharacterType type, Vector location, Country country, Weapon weapon, Rank rank, Character commandingOfficer, boolean gunnerDead) {
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
	
	public Character(UUID id, Map<String, Object> source) {
		this.id = id;
		HashMap<String, Double> location =  (HashMap<String, Double>) source.get("location");
		long altitude = ((Double)source.get("altitude")).longValue();
		this.type = CharacterType.valueOf((String) source.get("type"));
		this.location = new Vector(location.get("lon")/180*LostVictoryScene.SCENE_WIDTH, altitude, location.get("lat")/80*LostVictoryScene.SCENE_HEIGHT);
		this.country = Country.valueOf((String)source.get("country"));
		this.weapon = Weapon.valueOf((String) source.get("weapon"));
		this.rank = Rank.valueOf((String) source.get("rank"));
		this.action = new Gson().fromJson((String)source.get("action"), Action.class);
		String co = (String) source.get("commandingOfficer");
		if(co!=null && !co.isEmpty()){
			this.commandingOfficer = UUID.fromString(co);
		}
		unitsUnderCommand = ((Collection<String>)source.get("unitsUnderCommand")).stream().map(s -> UUID.fromString(s)).collect(Collectors.toSet());
		gunnerDead = (boolean) source.get("gunnerDead");
	}

	void addUnit(Character u){
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
	
	public Rank getRank() {
		return rank;
	}
	
	public UUID getCommandingOfficer() {
		return commandingOfficer;
	}

	public void addCharactersUnderCommand(Set<Character> cc) {
		unitsUnderCommand.addAll(cc.stream().map(c -> c.id).collect(Collectors.toList()));
	}

	public void addCharactersUnderCommand(Character... atg) {
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
		                .field("country", getCountry())
		                .field("weapon", getWeapon())
		                .field("rank", getRank())
		                .field("action", new Gson().toJson(action))
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
