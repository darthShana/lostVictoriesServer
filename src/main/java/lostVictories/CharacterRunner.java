package lostVictories;

import static lostVictories.dao.CharacterDAO.MAPPER;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import lostVictories.service.LostVictoryService;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.objectives.Objective;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.HouseDAO;
import lostVictories.dao.PlayerUsageDAO;
import lostVictories.messageHanders.CharacterCatch;
import redis.clients.jedis.JedisPool;

public class CharacterRunner implements Runnable{

	private static Logger log = Logger.getLogger(CharacterRunner.class);

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
        characterDAO.getAllCharacters().stream().forEach(c->lostVictoryService.doRunCharacters(c));
	}


}
