package lostVictories.dao;

import static com.jme3.lostVictories.network.messages.CharacterMessage.toLatitute;
import static com.jme3.lostVictories.network.messages.CharacterMessage.toLongitude;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


import com.jme3.lostVictories.network.messages.TreeGroupMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.Jedis;

public class TreeDAO {

    private static Logger log = LoggerFactory.getLogger(TreeDAO.class);

    private final String treeStatus;
    private final String treeLocation;
    private Jedis jedis;

	public TreeDAO(Jedis jedis, String nameSpace) {
		this.jedis = jedis;
        this.treeStatus = nameSpace+".treeStatus";
        this.treeLocation = nameSpace+".treeLocation";
	}
	
	public Set<TreeGroupMessage> getAllTrees() {
        List<GeoRadiusResponse> mapResponse = jedis.georadius(this.treeLocation, 0, 0, 1000000, GeoUnit.KM);
        return mapResponse.stream().map(r->getTree(UUID.fromString(r.getMemberByString()))).filter(c->c!=null).collect(Collectors.toSet());

    }

	public void putTree(UUID uuid, TreeGroupMessage t) {
        jedis.del(treeStatus+"."+t.getId().toString());
        jedis.zrem(treeLocation, t.getId().toString());
        try {
            t.getMapRepresentation().entrySet().forEach(e->{
                jedis.hset(treeStatus+"."+t.getId().toString(), e.getKey(), e.getValue());
            });
            jedis.geoadd(treeLocation, toLongitude(t.getLocation()), toLatitute(t.getLocation()), t.getId().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }


	}

    public TreeGroupMessage getTree(UUID id) {
        Map<String, String> mapResponse = jedis.hgetAll(treeStatus + "." + id.toString());
        List<GeoCoordinate> geoLocation = jedis.geopos(treeLocation, id.toString());

        if(mapResponse !=null && !mapResponse.isEmpty() && !geoLocation.isEmpty() && geoLocation.get(0)!=null){
            try {
                return new TreeGroupMessage(mapResponse, geoLocation.get(0));
            } catch (Exception e){
                log.info("partial house:"+id+" present"+mapResponse);
                throw e;
            }
        }else{
            return null;
        }
    }
}
