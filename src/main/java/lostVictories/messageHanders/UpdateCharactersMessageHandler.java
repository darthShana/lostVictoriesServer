package lostVictories.messageHanders;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.jme3.lostVictories.network.messages.wrapper.*;
import io.netty.channel.group.ChannelMatchers;
import org.apache.log4j.Logger;

import com.jme3.lostVictories.network.messages.AchievementStatus;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.GameStatistics;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.UnClaimedEquipmentMessage;

import com.jme3.lostVictories.network.messages.Vector;

import lostVictories.WorldRunner;
import lostVictories.dao.CharacterDAO;
import lostVictories.dao.EquipmentDAO;
import lostVictories.dao.HouseDAO;
import org.elasticsearch.common.collect.Lists;
import org.lwjgl.Sys;

public class UpdateCharactersMessageHandler {

    public static final long CHECKOUT_TIMEOUT = 2*1000;

    private CharacterDAO characterDAO;
	private static Logger log = Logger.getLogger(UpdateCharactersMessageHandler.class);
	private HouseDAO houseDAO;
	private EquipmentDAO equipmentDAO;
	private WorldRunner worldRunner;
	private MessageRepository messageRepository;
	private long lastFlushTime;
	private long lastREc;

	public UpdateCharactersMessageHandler(CharacterDAO characterDAO, HouseDAO houseDAO, EquipmentDAO equipmentDAO, WorldRunner worldRunner, MessageRepository messageRepository) {
		this.characterDAO = characterDAO;
		this.houseDAO = houseDAO;
		this.equipmentDAO = equipmentDAO;
		this.worldRunner = worldRunner;
		this.messageRepository = messageRepository;
	}

	public Set<LostVictoryMessage> handle(UpdateCharactersRequest msg) throws IOException {

		Set<CharacterMessage> allCharacter = new HashSet<>();
		allCharacter.add(msg.getCharacter());
		
		Map<UUID, CharacterMessage> sentFromClient = allCharacter.stream().collect(Collectors.toMap(CharacterMessage::getId, Function.identity()));
        Map<UUID, CharacterMessage> serverVersion = characterDAO.getAllCharacters(allCharacter.stream().filter(c->!c.isDead()).map(c->c.getId()).collect(Collectors.toSet()));

		Map<UUID, CharacterMessage> toSave = serverVersion.values().stream()
				.filter(c->c.isAvailableForUpdate(msg.getClientID(), sentFromClient.get(c.getId()), CHECKOUT_TIMEOUT))
				.collect(Collectors.toMap(c->c.getId(), Function.identity()));

		toSave.values().stream().forEach(c->c.updateState(sentFromClient.get(c.getId()), msg.getClientID(), System.currentTimeMillis()));


		CharacterMessage storedAvatar = characterDAO.getCharacter(msg.getAvatar());
		Vector v = storedAvatar.getLocation();
		Map<UUID, CharacterMessage> inRange = characterDAO.getAllCharacters(v.x, v.y, v.z, CheckoutScreenMessageHandler.CLIENT_RANGE).stream().collect(Collectors.toMap(c->c.getId(), Function.identity()));

		Map<UUID, CharacterMessage> toReturn = toSave.values().stream()
                .map(s->characterDAO.updateCharacterState(s))
                .filter(u->u!=null)
				.filter(b->inRange.containsKey(b.getId()))
                .collect(Collectors.toMap(c->c.getId(), Function.identity()));

        serverVersion.values().stream()
				.filter(b->inRange.containsKey(b.getId()) && !toReturn.containsKey(b.getId())).forEach(c->{
                	toReturn.put(c.getId(), c);
        });


		Set<CharacterMessage> relatedCharacters = new HashSet<>();
		if(msg.getClientStartTime()>5000) {
			if(sentFromClient.containsKey(msg.getAvatar())) {
				inRange.values().stream().filter(c -> !toReturn.containsKey(c.getId())).filter(cc -> !cc.isCheckedOutBy(msg.getClientID(), CHECKOUT_TIMEOUT)).forEach(c -> toReturn.put(c.getId(), c));
			}
			relatedCharacters = toReturn.values().stream()
					.filter(u->!u.isDead()).map(c->c.getUnitsUnderCommand()).filter(u->!toReturn.containsKey(u))
					.map(u->characterDAO.getAllCharacters(u).values()).flatMap(l->l.stream())
					.filter(u->u!=null && !inRange.containsKey(u.getId())).collect(Collectors.toSet());
			Set<CharacterMessage> relatedCharacters2 = toReturn.values().stream()
					.filter(c->!c.isDead()).map(c->c.getCommandingOfficer()).filter(u->u!=null && !toReturn.containsKey(u))
					.map(u->characterDAO.getCharacter(u)).filter(u->u!=null && !inRange.containsKey(u.getId())).collect(Collectors.toSet());
			relatedCharacters.addAll(relatedCharacters2);

		}

        Set<LostVictoryMessage> ret = toReturn.values().stream().map(c->new CharacterStatusResponse(c)).collect(Collectors.toSet());
        relatedCharacters.stream().map(c->new RelatedCharacterStatusResponse(c)).forEach(m->ret.add(m));

        if(sentFromClient.containsKey(msg.getAvatar())){
			GameStatistics statistics = worldRunner.getStatistics(storedAvatar.getCountry());
			AchievementStatus achievementStatus = worldRunner.getAchivementStatus(storedAvatar, characterDAO);
			Set<UnClaimedEquipmentMessage> unClaimedEquipment = equipmentDAO.getUnClaimedEquipment(v.x, v.y, v.z, CheckoutScreenMessageHandler.CLIENT_RANGE);
			ret.add(new EquipmentStatusResponse(unClaimedEquipment));
			Set<HouseMessage> allHouses = houseDAO.getAllHouses();
            Lists.partition(new ArrayList<>(allHouses), 5).forEach(subList -> {
                ret.add(new HouseStatusResponse(subList));
            });

			ret.add(new GameStatsResponse(messageRepository.popMessages(msg.getClientID()), statistics, achievementStatus));

			if(storedAvatar.getBoardedVehicle()!=null){
				CharacterMessage vehicle = characterDAO.getCharacter(storedAvatar.getBoardedVehicle());
				if(vehicle!=null && vehicle.getCheckoutClient()!=null && !vehicle.getCheckoutClient().equals(msg.getClientID())){
					log.debug("force checkout of vehicle:"+vehicle.getId());
					vehicle.setCheckoutClient(msg.getClientID());
					vehicle.setCheckoutTime(System.currentTimeMillis());
					List<CharacterMessage> values = new ArrayList<>();
					values.add(vehicle);
					characterDAO.save(values);
					ret.add(new CharacterStatusResponse(vehicle));
				}

			}
		}

		return ret;
	}

}
