package lostVictories.service;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.LostVictoryScene;
import lostVictories.WorldRunner;
import lostVictories.dao.*;
import lostVictories.messageHanders.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by dharshanar on 6/05/17.
 */
public class LostVictoryService {
    private static Logger log = LoggerFactory.getLogger(LostVictoryService.class);

    private JedisPool jedisPool;
    private String nameSpace;
    private final TreeDAO treeDAO;
    private final EquipmentDAO equipmentDAO;
    private final GameRequestDAO gameRequestDAO;
    private final PlayerUsageDAO playerUsageDAO;
    private MessageRepository messageRepository;
    private WorldRunner worldRunner;

    public LostVictoryService(JedisPool jedisPool, String nameSpace, TreeDAO treeDAO, EquipmentDAO equipmentDAO, GameRequestDAO gameRequestDAO, PlayerUsageDAO playerUsageDAO, MessageRepository messageRepository, WorldRunner worldRunner) {
        this.jedisPool = jedisPool;
        this.nameSpace = nameSpace;
        this.treeDAO = treeDAO;
        this.equipmentDAO = equipmentDAO;
        this.gameRequestDAO = gameRequestDAO;
        this.playerUsageDAO = playerUsageDAO;
        this.messageRepository = messageRepository;
        this.worldRunner = worldRunner;
    }

    public void loadScene() {

        try (Jedis jedis = jedisPool.getResource()){
            CharacterDAO characterDAO = new CharacterDAO(jedis, nameSpace);
            HouseDAO houseDAO = new HouseDAO(jedis, nameSpace);
            characterDAO.deleteAllCharacters();
            new LostVictoryScene().loadScene(characterDAO, houseDAO, treeDAO);
        } catch(Throwable e){
            e.printStackTrace();
        }

    }



    public void doRunCharacters(CharacterMessage characterMessage) {
        try (Jedis jedis = jedisPool.getResource()){
            CharacterDAO characterDAO = new CharacterDAO(jedis, nameSpace);
            HouseDAO houseDAO = new HouseDAO(jedis, nameSpace);
            new CharacterRunnerInstance().doRunCharacter(characterMessage, characterDAO, houseDAO, playerUsageDAO);
        }catch(Throwable e){
            e.printStackTrace();
        }
    }
}
