package com.jme3.lostVictories.network.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jme3.math.Vector3f;
import lostVictories.dao.CharacterDAO;
import redis.clients.jedis.GeoCoordinate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.jme3.lostVictories.network.messages.Vector.latLongToVector;
import static lostVictories.dao.CharacterDAO.MAPPER;

public class BunkerMessage implements Structure{


    private Quaternion rotation;
    private Vector location;
    private UUID id;

    BunkerMessage(){}

    public BunkerMessage(UUID id, Vector location, Quaternion rotation) {
        this.id = id;
        this.location = location;
        this.rotation = rotation;
    }

    public BunkerMessage(Map<String, String> source, GeoCoordinate geoCoordinate) throws IOException {
        this.id = UUID.fromString(source.get("id"));
        float altitude = Float.parseFloat(source.get("altitude"));
        this.location = latLongToVector(altitude, (float) geoCoordinate.getLongitude(), (float) geoCoordinate.getLatitude());
        this.rotation = MAPPER.readValue(source.get("rotation"), Quaternion.class);
    }

    public Vector getLocation() {
        return location;
    }

    public Quaternion getRotation() {
        return rotation;
    }

    public Vector getEntryPoint() {
        Vector3f v = new com.jme3.math.Quaternion().mult(Vector3f.UNIT_Z.mult(7));
        return new Vector(this.location.toVector().add(v));
    }

    @Override
    public Country getOwner() {
        return null;
    }

    public UUID getId() {
        return id;
    }

    public Map<String, String> getMapRepresentation() throws JsonProcessingException {
        HashMap<String, String> ret = new HashMap<>();
        ret.put("id", id.toString());
        ret.put("rotation", CharacterDAO.MAPPER.writeValueAsString(rotation));
        ret.put("altitude", getLocation().y+"");
        return ret;
    }
}
