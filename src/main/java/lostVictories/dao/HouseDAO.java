package lostVictories.dao;


import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.jme3.lostVictories.network.messages.*;
import com.jme3.lostVictories.network.messages.Vector;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.Jedis;

import static com.jme3.lostVictories.network.messages.CharacterMessage.toLatitute;
import static com.jme3.lostVictories.network.messages.CharacterMessage.toLongitude;

public class HouseDAO {
	private static Logger log = LoggerFactory.getLogger(HouseDAO.class);
    private final String houseStatus;
    private final String houseLocation;

    Jedis jedis;

	private List<BunkerMessage> bunkers = new ArrayList<>();


	public HouseDAO(Jedis jedis, String nameSpace) {
		this.jedis = jedis;
        this.houseStatus = nameSpace+".houseStatus";
        this.houseLocation = nameSpace+".houseLocation";

        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(-100.58569f, 97.62429f, 231.24171f), new Quaternion(0.0f, 0.3360924f, 0.0f, 0.941829f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(336.1358f, 95.88243f, 66.05069f), new Quaternion(0.0f, 0.5893206f, 0.0f, 0.8078993f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(-250.0f, 96.289536f, -225.11862f), new Quaternion(0.0f, -0.7550778f, 0.0f, 0.65563524f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(-342.9169f, 96.32557f, -144.11838f), new Quaternion(0.0f, -0.98937446f, 0.0f, 0.14538974f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(82.83392f, 97.43404f, 100.658516f), new Quaternion(0.0f, 0.7570388f, 0.0f, 0.65336996f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(57.210407f, 100.232506f, -270.88477f), new Quaternion(0.0f, 0.90899706f, 0.0f, 0.41680256f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(137.88399f, 100.28509f, -270.88477f), new Quaternion(0.0f, -0.930234f, 0.0f, 0.3669669f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(200.1758f, 102.116066f, -42.26669f), new Quaternion(0.0f, 0.0f, 0.0f, 1.0f)));

    }
	
	public void putHouse(HouseMessage house) {
		try {
            jedis.del(houseStatus+"."+house.getId().toString());
            jedis.zrem(houseLocation, house.getId().toString());
            house.getMapRepresentation().entrySet().forEach(e->{
                jedis.hset(houseStatus+"."+house.getId().toString(), e.getKey(), e.getValue());
            });
            jedis.geoadd(houseLocation, toLongitude(house.getLocation()), toLatitute(house.getLocation()), house.getId().toString());


        } catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Set<HouseMessage> getAllHouses() {
        List<GeoRadiusResponse> mapResponse = jedis.georadius(this.houseLocation, 0, 0, 1000000, GeoUnit.KM);
        return mapResponse.stream().map(r->getHouse(UUID.fromString(r.getMemberByString()))).filter(c->c!=null).collect(Collectors.toSet());

	}


	public void save(Set<HouseMessage> values) {
        values.forEach(character -> {
            try {
                jedis.del(houseStatus + "." + character.getId().toString());
                jedis.zrem(this.houseLocation, character.getId().toString());
                character.getMapRepresentation().entrySet().forEach(e -> {
                    jedis.hset(houseStatus + "." + character.getId().toString(), e.getKey(), e.getValue());
                });
                jedis.geoadd(this.houseLocation, toLongitude(character.getLocation()), toLatitute(character.getLocation()), character.getId().toString());
            }catch (IOException e){
                throw new RuntimeException(e);
            }
        });
	}

	public HouseMessage getHouse(UUID id) {
        Map<String, String> mapResponse = jedis.hgetAll(houseStatus + "." + id.toString());
        List<GeoCoordinate> geoLocation = jedis.geopos(houseLocation, id.toString());

        if(mapResponse !=null && !mapResponse.isEmpty() && !geoLocation.isEmpty() && geoLocation.get(0)!=null){
            try {
                return new HouseMessage(mapResponse, geoLocation.get(0));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e){
                log.info("partial house:"+id+" present"+mapResponse);
                throw e;
            }
        }else{
            return null;
        }
	}

    public List<BunkerMessage> getAllBunkers() {
        return bunkers;
    }

    public List<BunkerMessage> getBunkers(Set<UUID> ids) {
	    return bunkers.stream().filter(b->ids.contains(b.getId())).collect(Collectors.toList());
    }

    public BunkerMessage getBunker(UUID i) {
        Optional<BunkerMessage> first = bunkers.stream().filter(b -> i.equals(b.getId())).findFirst();
        return first.orElse(null);
    }
}
