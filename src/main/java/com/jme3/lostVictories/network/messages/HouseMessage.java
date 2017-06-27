package com.jme3.lostVictories.network.messages;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static com.jme3.lostVictories.network.messages.CharacterMessage.toLatitute;
import static com.jme3.lostVictories.network.messages.CharacterMessage.toLongitude;
import static com.jme3.lostVictories.network.messages.Vector.latLongToVector;
import static com.jme3.lostVictories.network.messages.LostVictoryScene.SCENE_SCALE;
import static com.jme3.lostVictories.network.messages.Quaternion.toQuaternion;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lostVictories.dao.CharacterDAO;

import org.apache.log4j.Logger;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.xcontent.XContentBuilder;


public class HouseMessage implements Serializable{
	private static Logger log = Logger.getLogger(HouseMessage.class); 
	public static final float CAPTURE_RANGE = 45;
	
	private final UUID id;
	private String type;
	private Vector location;
	private Quaternion rotation;
	Country owner;
	Country contestingOwner;
	CaptureStatus captureStatus;
	Long statusChangeTime;

	public HouseMessage(String type, Vector location, Quaternion rotation) {
		this.id = UUID.randomUUID();
		this.type = type;
		this.location = location;
		this.rotation = rotation;
		this.captureStatus = CaptureStatus.NONE;
	}

	public HouseMessage(UUID id, Map<String, Object> source) {
		this.id = id;
		this.type = (String) source.get("type");
		
		HashMap<String, Double> loc =  (HashMap<String, Double>) source.get("location");
		HashMap<String, Double> rot =  (HashMap<String, Double>) source.get("rotation");
		float altitude = ((Double)source.get("altitude")).floatValue();
		this.location = latLongToVector(altitude, loc.get("lon").floatValue(), loc.get("lat").floatValue());
		this.rotation = toQuaternion(rot);
		
		if(source.get("owner")!=null){
			this.owner = Country.valueOf((String) source.get("owner"));
		}
		if(source.get("contestingOwner")!=null){
			this.contestingOwner = Country.valueOf((String) source.get("contestingOwner"));
		}
		if(source.get("captureStatus")!=null){
			this.captureStatus = CaptureStatus.valueOf((String) source.get("captureStatus"));
		}
		if(source.get("statusChangeTime")!=null){
			this.statusChangeTime = (Long) source.get("statusChangeTime");
		}
	}

	public XContentBuilder getJSONRepresentation() throws IOException {
		return jsonBuilder()
	            .startObject()
	                .field("type", getType())
	                .field("location", new GeoPoint(toLatitute(getLocation()), toLongitude(getLocation())))
	                .field("altitude", getLocation().y)
	                .field("rotation", rotation.toMap())
	                .field("owner", owner)
	                .field("contestingOwner", contestingOwner)
	                .field("captureStatus", getStatus())
	                .field("statusChangeTime", getStatusChangeTime())	                
	            .endObject();
	}

	public String getType() {
		return type;
	}

	public Long getStatusChangeTime() {
		return statusChangeTime;
	}

	public CaptureStatus getStatus() {
		return captureStatus;
	}

	public Country getOwner() {
		return owner;
	}
	
	public Country getCompetingOwner(){
		return contestingOwner;
	}
	
	public void contestOwnership(Country contestant){
		this.contestingOwner = contestant;
		this.statusChangeTime = System.currentTimeMillis();
	}

	public void withdrawContest() {
		this.contestingOwner = null;
		this.statusChangeTime = null;
		
	}
	
	public void changeOwnership() {
		this.owner = contestingOwner;
		this.contestingOwner = null;
		this.statusChangeTime = null;
	}
	
	public void vacate() {
		this.owner = null;
	}
	
	public Vector getLocation() {
		return location;
	}

	public boolean checkOwnership(CharacterDAO characterDAO) {
		Set<CharacterMessage> allCharacters = characterDAO.getAllCharacters(location.x, location.y, location.z, CAPTURE_RANGE*SCENE_SCALE);
		allCharacters = allCharacters.stream().filter(c->c.type==CharacterType.SOLDIER || c.type==CharacterType.AVATAR).filter(c->!c.dead).collect(Collectors.toSet());

		if(!allCharacters.isEmpty()){
			log.trace("looking ofr characters near:"+location+" found chata:"+allCharacters.size());
		}
		CaptureStatus c = captureStatus.transition(allCharacters, this);
		if(c!=captureStatus){
			log.trace("changing capture status to:"+c+":"+getLocation());
			captureStatus = c;
			return true;
		}else{
			return false;
		}
	}

	public UUID getId() {
		return id;
	}

	public boolean captureTimeExceded() {
		return (statusChangeTime!=null)?System.currentTimeMillis()-statusChangeTime>10000:false;
	}

	public XContentBuilder getJSONRepresentationUnChecked() {
		try {
			return getJSONRepresentation();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@JsonIgnore
	public boolean isOwned() {
		return owner!=null;
	}

	public Quaternion getRotation() {
		return rotation;
	}

	public Country getContestingOwner() {
		return contestingOwner;
	}

	public CaptureStatus getCaptureStatus() {
		return captureStatus;
	}
}
