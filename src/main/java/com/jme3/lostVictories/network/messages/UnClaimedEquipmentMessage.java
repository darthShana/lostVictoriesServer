package com.jme3.lostVictories.network.messages;
import static com.jme3.lostVictories.network.messages.Vector.latLongToVector;
import static lostVictories.dao.CharacterDAO.MAPPER;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lostVictories.dao.CharacterDAO;
import redis.clients.jedis.GeoCoordinate;


public class UnClaimedEquipmentMessage implements Serializable{

	private UUID id;
	private Weapon weapon;
	private Vector location;
	private Vector rotation;
	private Long creationTime;

	public UnClaimedEquipmentMessage(UUID id, Weapon weapon, Vector location, Vector rotation) {
		this.id = id;
		this.weapon = weapon;
		this.location = location;
		this.rotation = rotation;
		this.creationTime = System.currentTimeMillis();
	}

	public UnClaimedEquipmentMessage(Map<String, String> source, GeoCoordinate geoCoordinate) {
	    try {
            this.id = UUID.fromString(source.get("id"));
            this.weapon = Weapon.valueOf(source.get("weapon"));
            float altitude = Float.parseFloat(source.get("altitude"));
            this.location = latLongToVector(altitude, (float) geoCoordinate.getLongitude(), (float) geoCoordinate.getLatitude());
            this.rotation = MAPPER.readValue(source.get("rotation"), Vector.class);
            this.creationTime = Long.parseLong(source.get("creationTime"));
        }catch(IOException e){
	        throw new RuntimeException(e);
        }
	}

	public UUID getId() {
		return id;
	}

	public Map<String, String> getMapRepresentation() throws IOException {
		Map<String, String> ret = new HashMap<>();
		ret.put("id", id.toString());
		ret.put("weapon", weapon.name());
	    ret.put("altitude", location.y+"");
        ret.put("rotation", CharacterDAO.MAPPER.writeValueAsString(rotation));
        ret.put("creationTime", creationTime.toString());
        return ret;

	}

	public Weapon getWeapon() {
		return weapon;
	}

	public Vector getLocation() {
		return location;
		
	}

	public Vector getRotation() {
		return rotation;
	}

    public long getCreationTime() {
        return creationTime;
    }
}
