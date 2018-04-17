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
    private String nameSpace;

	private List<BunkerMessage> bunkers = new ArrayList<>();


	public HouseDAO(Jedis jedis, String nameSpace) {
		this.jedis = jedis;
		this.nameSpace = nameSpace;
        this.houseStatus = nameSpace+".houseStatus";
        this.houseLocation = nameSpace+".houseLocation";

        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(204.1758f, 102.116066f, -24.266691f), new Quaternion(0.06678886f, -0.1626839f, 0.011040457f, 0.98435324f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(99.83392f, 96.43404f, 95.658516f), new Quaternion(0.0f, 0.9294944f, 0.0f, 0.36883622f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(336.1358f, 95.88243f, 66.05069f), new Quaternion(0.0f, 0.6047698f, 0.0f, 0.7964003f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(-96.58569f, 97.62429f, 231.24171f), new Quaternion(0.0f, 0.2922327f, 0.0f, 0.9563472f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(-342.9169f, 96.32557f, -144.11838f), new Quaternion(0.0f, -0.9938203f, 0.0f, 0.111000516f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(-250.0f, 96.289536f, -225.11862f), new Quaternion(0.0f, -0.75833607f, 0.0f, 0.65186375f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(137.88399f, 100.28509f, -270.88477f), new Quaternion(0.0f, -0.9334759f, 0.0f, 0.35864007f)));
        bunkers.add(new BunkerMessage(UUID.randomUUID(), new Vector(57.210407f, 100.232506f, -270.88477f), new Quaternion(-4.4577834E-7f, 0.7684584f, 4.2731781E-7f, 0.6398997f)));
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
                log.info("partial character:"+id+" present"+mapResponse);
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
