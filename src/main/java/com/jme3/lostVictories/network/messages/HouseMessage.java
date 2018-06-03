package com.jme3.lostVictories.network.messages;

import static lostVictories.dao.CharacterDAO.MAPPER;
import static com.jme3.lostVictories.network.messages.Vector.latLongToVector;
import static com.jme3.lostVictories.network.messages.LostVictoryScene.SCENE_SCALE;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lostVictories.dao.CharacterDAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.GeoCoordinate;


public class HouseMessage implements Serializable, Structure{
	private static Logger log = LoggerFactory.getLogger(HouseMessage.class);
	public static final float CAPTURE_RANGE = 45;
	
	private final UUID id;
	private String type;
	private Vector location;
	private Vector scale;
	private Quaternion rotation;
	Country owner;
	Country contestingOwner;
	CaptureStatus captureStatus;
	Long statusChangeTime;

	public HouseMessage(String type, Vector location, Quaternion rotation, Vector scale) {
		this.id = UUID.randomUUID();
		this.type = type;
		this.location = location;
		this.rotation = rotation;
		this.scale = scale;
		this.captureStatus = CaptureStatus.NONE;
	}

	public HouseMessage(Map<String, String> source, GeoCoordinate geoCoordinate) throws IOException {
        this.id = UUID.fromString(source.get("id"));
		this.type = source.get("type");
		
        float altitude = Float.parseFloat(source.get("altitude"));

        this.location = latLongToVector(altitude, (float) geoCoordinate.getLongitude(), (float) geoCoordinate.getLatitude());

        this.rotation = MAPPER.readValue(source.get("rotation"), Quaternion.class);
        this.scale = MAPPER.readValue(source.get("scale"), Vector.class);


        if(source.get("owner")!=null){
			this.owner = Country.valueOf(source.get("owner"));
		}
		if(source.get("contestingOwner")!=null){
			this.contestingOwner = Country.valueOf(source.get("contestingOwner"));
		}
		if(source.get("captureStatus")!=null){
			this.captureStatus = CaptureStatus.valueOf(source.get("captureStatus"));
		}
		if(source.get("statusChangeTime")!=null){
			this.statusChangeTime = Long.parseLong(source.get("statusChangeTime"));
		}
	}

    public HouseMessage(String type2, Vector vector, Quaternion quaternion) {
	    this(type2, vector, quaternion, new Vector(1, 1, 1));
    }

    public Map<String, String> getMapRepresentation() throws IOException {
        Map<String, String> ret = new HashMap<>();
        ret.put("id", id.toString());
		ret.put("type", getType());
        ret.put("altitude", getLocation().y+"");
        ret.put("rotation", CharacterDAO.MAPPER.writeValueAsString(rotation));
        ret.put("scale", CharacterDAO.MAPPER.writeValueAsString(scale));
        if(owner!=null) {
            ret.put("owner", owner + "");
        }
        if(contestingOwner!=null) {
            ret.put("contestingOwner", contestingOwner + "");
        }
        if(captureStatus!=null) {
            ret.put("captureStatus", getStatus() + "");
        }
        if(statusChangeTime!=null) {
            ret.put("statusChangeTime", getStatusChangeTime() + "");
        }
        return ret;
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
	public Vector getScale() {
		return scale;
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

	public boolean captureTimeExceeded() {
		return (statusChangeTime!=null)?System.currentTimeMillis()-statusChangeTime>10000:false;
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
