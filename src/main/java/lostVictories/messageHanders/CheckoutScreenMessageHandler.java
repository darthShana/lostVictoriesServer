package lostVictories.messageHanders;

import static lostVictories.messageHanders.MessageHandler.objectMapper;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jme3.lostVictories.network.messages.wrapper.*;
import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.Vector;

import lostVictories.dao.CharacterDAO;
import lostVictories.dao.EquipmentDAO;
import lostVictories.dao.HouseDAO;
import lostVictories.dao.PlayerUsageDAO;
import lostVictories.dao.TreeDAO;
import org.elasticsearch.common.collect.Lists;

public class CheckoutScreenMessageHandler{
	
	private static Logger log = Logger.getLogger(CheckoutScreenMessageHandler.class); 
	public static float CLIENT_RANGE = 250l;
	private CharacterDAO characterDAO;
	private HouseDAO houseDAO;
	private EquipmentDAO equipmentDAO;
	private PlayerUsageDAO playerUsageDAO;
	private TreeDAO treeDAO;

	public CheckoutScreenMessageHandler(CharacterDAO characterDAO, HouseDAO houseDAO, EquipmentDAO equipmentDAO, TreeDAO treeDAO, PlayerUsageDAO playerUsageDAO) {
		this.characterDAO = characterDAO;
		this.houseDAO = houseDAO;
		this.equipmentDAO = equipmentDAO;
		this.treeDAO = treeDAO;
		this.playerUsageDAO = playerUsageDAO;
	}

	public Set<LostVictoryMessage> handle(CheckoutScreenRequest m) {
		log.info("checking out scene for avatar:"+m.avatar);
		Set<LostVictoryMessage> ret = new HashSet<>();
		CharacterMessage avatar = characterDAO.getCharacter(m.avatar);
		if(avatar!=null){
			Vector l = avatar.getLocation();
			Set<CharacterMessage> allCharacters = characterDAO.getAllCharacters(l.x, l.y, l.z, CLIENT_RANGE);

			for(Iterator<CharacterMessage> it = allCharacters.iterator(); it.hasNext();){
				ret.add(new CharacterStatusResponse(it.next()));
			}

			ret.add(new EquipmentStatusResponse(equipmentDAO.getUnClaimedEquipment(l.x, l.y, l.z, CLIENT_RANGE)));
			Lists.partition(new ArrayList<>(houseDAO.getAllHouses()), 10).forEach(subList -> {
				ret.add(new HouseStatusResponse(subList));
			});

			Lists.partition(new ArrayList<>(treeDAO.getAllTrees()), 5).forEach(subList -> {
				ret.add(new TreeStatusResponse(subList));
			});
			playerUsageDAO.registerStartGame(avatar.getUserID(), System.currentTimeMillis());
		}
		return ret;
	}

}
