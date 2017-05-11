package lostVictories.service;

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

    public void doHandleMessage(LostVictoryMessage msg, Set<LostVictoryMessage> lostVictoryMessages) throws IOException {

        try (Jedis jedis = jedisPool.getResource()){
            CharacterDAO characterDAO = new CharacterDAO(jedis, nameSpace);
            if(msg instanceof CheckoutScreenRequest){
                CheckoutScreenMessageHandler checkoutScreenMessageHandler = new CheckoutScreenMessageHandler(characterDAO, houseDAO, equipmentDAO, treeDAO, playerUsageDAO);
                lostVictoryMessages.addAll(checkoutScreenMessageHandler.handle((CheckoutScreenRequest) msg));
                log.info("returning scene");
            } else if(msg instanceof UpdateCharactersRequest){
                UpdateCharactersMessageHandler updateCharactersMessageHandler = new UpdateCharactersMessageHandler(characterDAO, houseDAO, equipmentDAO, worldRunner, messageRepository);
                lostVictoryMessages.addAll(updateCharactersMessageHandler.handle((UpdateCharactersRequest)msg));
            } else if(msg instanceof DeathNotificationRequest) {
                DeathNotificationMessageHandler deathNotificationMessageHandler = new DeathNotificationMessageHandler(characterDAO, equipmentDAO);
                lostVictoryMessages.addAll(deathNotificationMessageHandler.handle((DeathNotificationRequest)msg));
            } else if(msg instanceof PassengerDeathNotificationRequest) {
                PassengerDeathNotificationMessageHandler gunnerDeathNotificationMessageHandler = new PassengerDeathNotificationMessageHandler(characterDAO);
                lostVictoryMessages.addAll(gunnerDeathNotificationMessageHandler.handle((PassengerDeathNotificationRequest)msg));
            }else if(msg instanceof EquipmentCollectionRequest) {
                CollectEquipmentMessageHandler collectEquipmentMessageHandler = new CollectEquipmentMessageHandler(characterDAO, equipmentDAO, messageRepository);
                lostVictoryMessages.addAll(collectEquipmentMessageHandler.handle((EquipmentCollectionRequest)msg));
            } else if(msg instanceof BoardVehicleRequest){
                BoardingVehicleMessageHandler boardingVehicleMessageHandler = new BoardingVehicleMessageHandler(characterDAO, messageRepository);
                lostVictoryMessages.addAll(boardingVehicleMessageHandler.handle((BoardVehicleRequest)msg));
            } else if(msg instanceof DisembarkPassengersRequest){
                DisembarkPassengersMessageHandler disembarkPassengersMessageHandler = new DisembarkPassengersMessageHandler(characterDAO);

                lostVictoryMessages.addAll(disembarkPassengersMessageHandler.handle((DisembarkPassengersRequest)msg));
            } else if(msg instanceof AddObjectiveRequest){
                AddObjectiveMessageHandler addObjectiveMessageHandler = new AddObjectiveMessageHandler(characterDAO);

                lostVictoryMessages.addAll(addObjectiveMessageHandler.handle((AddObjectiveRequest)msg));
            } else{
                throw new RuntimeException("unknown request:"+msg);
            }
        } catch(Throwable e){
            e.printStackTrace();
        }
    }

    public void runWorld(Map<Country, Integer> victoryPoints, Map<Country, Long> manPower, Map<Country, WeaponsFactory> weaponsFactory, Map<Country, VehicleFactory> vehicleFactory, Map<Country, Long> nextRespawnTime, String gameName) {

        try (Jedis jedis = jedisPool.getResource()){
            CharacterDAO characterDAO = new CharacterDAO(jedis, nameSpace);
            new WorldRunnerInstance().runWorld(characterDAO, houseDAO, victoryPoints, manPower, weaponsFactory, vehicleFactory, nextRespawnTime, messageRepository, gameStatusDAO, playerUsageDAO, gameRequestDAO, gameName);
        }catch(Throwable e){
            e.printStackTrace();
        }
    }

    public void doRunCharacters() {;
        try (Jedis jedis = jedisPool.getResource()){
            CharacterDAO characterDAO = new CharacterDAO(jedis, nameSpace);
            new CharacterRunnerInstance().doRunCharacters(characterDAO, houseDAO, playerUsageDAO);
        }catch(Throwable e){
            e.printStackTrace();
        }
    }
}
