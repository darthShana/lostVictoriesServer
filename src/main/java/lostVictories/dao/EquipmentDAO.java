package lostVictories.dao;

import static com.jme3.lostVictories.network.messages.CharacterMessage.toLatitute;
import static com.jme3.lostVictories.network.messages.CharacterMessage.toLongitude;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import com.jme3.lostVictories.network.messages.UnClaimedEquipmentMessage;
import com.jme3.lostVictories.network.messages.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

public class EquipmentDAO {

	private static Logger log = LoggerFactory.getLogger(EquipmentDAO.class);
	private Jedis jedis;
	private String nameSpace;
	private String equipmentLocation;
	private String equipmentStatus;

	public EquipmentDAO(Jedis jedis, String nameSpace) {
		this.jedis = jedis;
		this.nameSpace = nameSpace;
        this.equipmentStatus = nameSpace+".equipmentStatus";
        this.equipmentLocation = nameSpace+".equipmentLocation";
	}
	
	public void addUnclaimedEquipment(UnClaimedEquipmentMessage equipment) {
		try {
            jedis.del(equipmentStatus + "." + equipment.getId());
            jedis.zrem(equipmentLocation, equipment.getId().toString());
            equipment.getMapRepresentation().entrySet().forEach(e -> {
                jedis.hset(equipmentStatus + "." + equipment.getId(), e.getKey(), e.getValue());
            });
            jedis.geoadd(equipmentLocation, toLongitude(equipment.getLocation()), toLatitute(equipment.getLocation()), equipment.getId().toString());
        } catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

	public Set<UnClaimedEquipmentMessage> getUnClaimedEquipment(float x, float y, float z, float range) {
        Vector v1 = new Vector(range, 0, range);
        double lat = toLatitute(v1);
        double lon = toLongitude(v1);
        double inKM = CharacterDAO.haversineInKM(CharacterDAO.ZERO_LAT, CharacterDAO.ZERO_LONG, lat, lon);

        Vector v2 = new Vector(x, y, z);
        double lat1 = toLatitute(v2);
        double lon1 = toLongitude(v2);

        Rectangle.Float boundingBox = new Rectangle2D.Float(x-range, z-range, range*2, range*2);

        List<GeoRadiusResponse> geoLocation = jedis.georadius(equipmentLocation, lon1, lat1, inKM, GeoUnit.KM);

        return getUnClaimedEquipmentInRange(boundingBox, geoLocation);
	}

    private Set<UnClaimedEquipmentMessage> getUnClaimedEquipmentInRange(Rectangle2D.Float boundingBox, List<GeoRadiusResponse> geoLocation) {
        Map<String, Response<Map<String, String>>> propertyMap = new HashMap<>();
        Map<String, Response<List<GeoCoordinate>>> locationMap = new HashMap<>();
        Pipeline pipelined = jedis.pipelined();
        geoLocation.stream().forEach(geo->{
            propertyMap.put(geo.getMemberByString(), pipelined.hgetAll(equipmentStatus + "." + geo.getMemberByString()));
            locationMap.put(geo.getMemberByString(), pipelined.geopos(equipmentLocation, geo.getMemberByString()));

        });
        pipelined.sync();


        return geoLocation.stream()
                .map(r ->  new UnClaimedEquipmentMessage(propertyMap.get(r.getMemberByString()).get(), locationMap.get(r.getMemberByString()).get().get(0)))
                .filter(c->c!=null && boundingBox.contains(c.getLocation().x, c.getLocation().z))
                .collect(Collectors.toSet());
    }

    public Set<UnClaimedEquipmentMessage> getAllUnclaimedEquipment() {
        Rectangle.Float boundingBox = new Rectangle2D.Float(-1024, -1024, 2048, 2048);

        List<GeoRadiusResponse> geoLocation = jedis.georadius(this.equipmentLocation, 0, 0, 1000000, GeoUnit.KM);
        return getUnClaimedEquipmentInRange(boundingBox, geoLocation);
    }

	public UnClaimedEquipmentMessage get(UUID id) {
        Map<String, String> mapResponse = jedis.hgetAll(equipmentStatus + "." + id.toString());
        List<GeoCoordinate> geoLocation = jedis.geopos(equipmentLocation, id.toString());
        if(mapResponse !=null && !mapResponse.isEmpty() && !geoLocation.isEmpty() && geoLocation.get(0)!=null){
            return new UnClaimedEquipmentMessage(mapResponse, geoLocation.get(0));
        }
        return null;
	}

	public void delete(UnClaimedEquipmentMessage equipment) {
        jedis.zrem(this.equipmentLocation, equipment.getId().toString());
        jedis.del(equipmentStatus + "." + equipment.getId().toString());

	}	

}
