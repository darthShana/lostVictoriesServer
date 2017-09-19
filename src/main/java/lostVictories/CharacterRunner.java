package lostVictories;



import lostVictories.service.LostVictoryService;
import lostVictories.dao.CharacterDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

public class CharacterRunner implements Runnable{

	private static Logger log = LoggerFactory.getLogger(CharacterRunner.class);

	private static CharacterRunner instance;	
	private CharacterDAO characterDAO;
	private LostVictoryService lostVictoryService;

	private CharacterRunner(LostVictoryService lostVictoryService, JedisPool jedisPool, String nameSpace) {
	    this.lostVictoryService = lostVictoryService;
	    characterDAO = new CharacterDAO(jedisPool.getResource(), nameSpace);
	}

	public static CharacterRunner instance(LostVictoryService lostVictoryService, JedisPool jedisPool, String nameSpace) {
		if(instance==null){
			instance = new CharacterRunner(lostVictoryService, jedisPool, nameSpace);
		}
		return instance;
	}

	@Override
	public void run() {
        try {
			characterDAO.getAllCharacters().stream().forEach(c -> lostVictoryService.doRunCharacters(c));
		}catch (Exception e){
        	e.printStackTrace();
		}
	}


}
