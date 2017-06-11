package lostVictories.service;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.Country;
import com.jme3.lostVictories.network.messages.LostVictoryScene;
import com.jme3.lostVictories.network.messages.wrapper.*;
import lostVictories.CharacterRunner;
import lostVictories.VehicleFactory;
import lostVictories.WeaponsFactory;
import lostVictories.WorldRunner;
import lostVictories.dao.*;
import lostVictories.messageHanders.*;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by dharshanar on 6/05/17.
 */
public class LostVictoryService {
    private static Logger log = Logger.getLogger(LostVictoryService.class);

    private JedisPool jedisPool;
    private String nameSpace;
    private final HouseDAO houseDAO;
    private final TreeDAO treeDAO;
    private final EquipmentDAO equipmentDAO;
    private final GameStatusDAO gameStatusDAO;
    private final GameRequestDAO gameRequestDAO;
    private final PlayerUsageDAO playerUsageDAO;
    private MessageRepository messageRepository;
    private WorldRunner worldRunner;

    public LostVictoryService(JedisPool jedisPool, String nameSpace, HouseDAO houseDAO, TreeDAO treeDAO, EquipmentDAO equipmentDAO, GameStatusDAO gameStatusDAO, GameRequestDAO gameRequestDAO, PlayerUsageDAO playerUsageDAO, MessageRepository messageRepository, WorldRunner worldRunner) {
        this.jedisPool = jedisPool;
        this.nameSpace = nameSpace;
        this.houseDAO = houseDAO;
        this.treeDAO = treeDAO;
        this.equipmentDAO = equipmentDAO;
        this.gameStatusDAO = gameStatusDAO;
        this.gameRequestDAO = gameRequestDAO;
        this.playerUsageDAO = playerUsageDAO;
        this.messageRepository = messageRepository;
        this.worldRunner = worldRunner;
    }

    public void loadScene() {

        try (Jedis jedis = jedisPool.getResource()){
            CharacterDAO characterDAO = new CharacterDAO(jedis, nameSpace);
            characterDAO.deleteAllCharacters();
            new LostVictoryScene().loadScene(characterDAO, houseDAO, treeDAO);
        } catch(Throwable e){
            e.printStackTrace();
        }

    }



    public void doRunCharacters(CharacterMessage characterMessage) {
        try (Jedis jedis = jedisPool.getResource()){
            CharacterDAO characterDAO = new CharacterDAO(jedis, nameSpace);
            new CharacterRunnerInstance().doRunCharacters(characterMessage, characterDAO, houseDAO, playerUsageDAO);
        }catch(Throwable e){
            e.printStackTrace();
        }
    }
}
