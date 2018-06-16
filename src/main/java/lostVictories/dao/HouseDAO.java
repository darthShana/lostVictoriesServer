package lostVictories.dao;


import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import com.jme3.lostVictories.network.messages.*;


import com.jme3.lostVictories.objectives.GameSector;
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
    public Rectangle mapBounds = new Rectangle(-512, -512, 1024, 1024);


    private final String houseStatus;
    private final String houseLocation;

    private final String bunkerStatus;
    private final String bunkerLocation;

    private final String sectorStatus;
    private final String sectorSet;


    Jedis jedis;



	public HouseDAO(Jedis jedis, String nameSpace) {
		this.jedis = jedis;
        this.houseStatus = nameSpace+".houseStatus";
        this.houseLocation = nameSpace+".houseLocation";

        this.bunkerStatus = nameSpace+".bunkerStatus";
        this.bunkerLocation = nameSpace+".bunkerLocation";

        this.sectorStatus = nameSpace+".sectorStatus";
        this.sectorSet = nameSpace+".sectorSet";

    }
	
	public void putHouse(HouseMessage house) {
		try {
            jedis.del(houseStatus+"."+house.getId().toString());
            jedis.zrem(houseLocation, house.getId().toString());
            house.getMapRepresentation().entrySet().forEach(e->jedis.hset(houseStatus+"."+house.getId().toString(), e.getKey(), e.getValue()));
            jedis.geoadd(houseLocation, toLongitude(house.getLocation()), toLatitute(house.getLocation()), house.getId().toString());

        } catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void putBunker(BunkerMessage bunker){
	    try{
	        jedis.del(bunkerStatus+"."+bunker.getId().toString());
	        jedis.zrem(bunkerLocation, bunker.getId().toString());
	        bunker.getMapRepresentation().entrySet().forEach(e->jedis.hset(bunkerStatus+""+bunker.getId().toString(), e.getKey(), e.getValue()));
            jedis.geoadd(bunkerLocation, toLongitude(bunker.getLocation()), toLatitute(bunker.getLocation()), bunker.getId().toString());

	    } catch (IOException e) {
	        throw new RuntimeException(e);
        }
    }

    public void putSector(GameSector sector){
	    try{
            jedis.del(sectorStatus+"."+sector.getId());
            sector.getMapRepresentation().entrySet().forEach(e->jedis.hset(sectorStatus+"."+sector.getId(), e.getKey(), e.getValue()));
            jedis.sadd(sectorSet, sector.getId().toString());
        } catch(IOException e){
            throw new RuntimeException(e);
        }
    }

	public Set<HouseMessage> getAllHouses() {
        List<GeoRadiusResponse> mapResponse = jedis.georadius(this.houseLocation, 0, 0, 1000000, GeoUnit.KM);
        return mapResponse.stream().map(r->getHouse(UUID.fromString(r.getMemberByString()))).filter(c->c!=null).collect(Collectors.toSet());

	}

    public Set<BunkerMessage> getAllBunkers() {
        List<GeoRadiusResponse> mapResponse = jedis.georadius(this.houseLocation, 0, 0, 1000000, GeoUnit.KM);
        return mapResponse.stream().map(r->getBunker(UUID.fromString(r.getMemberByString()))).filter(c->c!=null).collect(Collectors.toSet());
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

	public BunkerMessage getBunker(UUID id){
	    Map<String, String> mapResponse = jedis.hgetAll(bunkerStatus+"."+id.toString());
	    List<GeoCoordinate> geoLocation = jedis.geopos(bunkerLocation, id.toString());

	    if(mapResponse !=null && !mapResponse.isEmpty() && !geoLocation.isEmpty() && geoLocation.get(0)!=null){
            try {
                return new BunkerMessage(mapResponse, geoLocation.get(0));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else{
	        return null;
        }

    }



    public Set<GameSector> calculateGameSectors() {
        final List<GameSector> sectors = new ArrayList<>();

        for(int y = mapBounds.y;y<=mapBounds.getMaxY();y=y+33){
            for(int x = mapBounds.x;x<=mapBounds.getMaxX();x=x+33){
                sectors.add(new GameSector(new Rectangle(x, y, 33, 33)));
            }
        }

        getAllHouses().forEach(house->{
            sectors.stream().filter(s->s.contains(house)).findFirst().ifPresent(s->s.addHouse(house));
        });
        getAllBunkers().forEach(bunker->{
            sectors.stream().filter(s->s.contains(bunker)).findFirst().ifPresent(s->s.addBunker(bunker));
        });


        List<GameSector> remaining = sectors.stream().filter(s->!s.allStructures().isEmpty()).collect(Collectors.toList());

        //merge joining houses together with a limit on the number of houses
        Set<GameSector> merged = new HashSet<>();
        GameSector next = remaining.iterator().next();
        merged.add(next);
        remaining.remove(next);

        while(!remaining.isEmpty()){
            boolean foundMerge = false;
            for(GameSector sector:merged){
                Optional<GameSector> neighbour = findNeighbouringSector(sector, remaining);
                if(neighbour.isPresent()){
                    sector.merge(neighbour.get());
                    remaining.remove(neighbour.get());
                    foundMerge = true;
                }
            }
            if(!foundMerge){
                next = remaining.iterator().next();
                merged.add(next);
                remaining.remove(next);
            }

        }

        merged.forEach(sector -> putSector(sector));
        return merged;
    }

    Optional<GameSector> findNeighbouringSector(GameSector sector, List<GameSector> ret) {
        return ret.stream().filter(s->sector.isJoinedTo(s)).findFirst();
    }

    public Collection<GameSector> getGameSectors(){
        Set<String> smembers = jedis.smembers(sectorSet);
        return smembers.stream().map(s->getGameSector(UUID.fromString(s))).collect(Collectors.toSet());
    }

    public GameSector getGameSector(UUID id) {
        Map<String, String> mapResponse = jedis.hgetAll(sectorStatus + "." + id.toString());
        try {
            return new GameSector(mapResponse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
