package com.jme3.lostVictories.network.messages;

import static com.jme3.lostVictories.network.messages.CharacterMessage.toLatitute;
import static com.jme3.lostVictories.network.messages.CharacterMessage.toLongitude;
import static com.jme3.lostVictories.network.messages.Vector.latLongToVector;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.xcontent.XContentBuilder;


public class UnClaimedEquipmentMessage implements Serializable{

	private static final long serialVersionUID = 399807775735308779L;
	private UUID id;
	long version;
	private Weapon weapon;
	private Vector location;
	private Vector rotation;

	public UnClaimedEquipmentMessage(UUID id, Weapon weapon, Vector location, Vector rotation) {
		this.id = id;
		this.weapon = weapon;
		this.location = location;
		this.rotation = rotation;
	}

	public UnClaimedEquipmentMessage(UUID id, long version, Map<String, Object> source) {
		this.id = id;
		this.version = version;
		HashMap<String, Double> location =  (HashMap<String, Double>) source.get("location");
		HashMap<String, Double> ori =  (HashMap<String, Double>) source.get("rotation");
		this.weapon = Weapon.valueOf((String) source.get("weapon"));
		float altitude = ((Double)source.get("altitude")).floatValue();
		this.location = latLongToVector(location, altitude);
		this.rotation = new Vector(ori.get("x").floatValue(), ori.get("y").floatValue(), ori.get("z").floatValue());
		
	}

	public Object getId() {
		return id;
	}

	public long getVersion() {
		return version;
	}
	
	public XContentBuilder getJSONRepresentation() throws IOException {
		return jsonBuilder()
	            .startObject()
	                .field("weapon", weapon)
	                .field("location", new GeoPoint(toLatitute(location), toLongitude(location)))
	                .field("altitude", location.y)
	                .field("rotation", rotation.toMap())                	                
	            .endObject();
	}

	public Weapon getWeapon() {
		return weapon;
	}

	public Vector getLocation() {
		return location;
		
	}

}
