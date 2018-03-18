package com.lostVictories.service;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.lostVictories.api.LostVictoryMessage;
import com.lostVictories.api.UpdateCharactersRequest;
import lostVictories.WorldRunner;
import lostVictories.dao.CharacterDAO;
import lostVictories.dao.EquipmentDAO;
import lostVictories.dao.HouseDAO;
import lostVictories.messageHanders.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.lostVictories.service.LostVictoriesService.uuid;

/**
 * Created by dharshanar on 27/05/17.
 */
public class UpdateCharactersMessageHandler {

    public static final long CHECKOUT_TIMEOUT = 2*1000;

    private CharacterDAO characterDAO;
    private static Logger log = LoggerFactory.getLogger(UpdateCharactersMessageHandler.class);
    private HouseDAO houseDAO;
    private EquipmentDAO equipmentDAO;
    private WorldRunner worldRunner;
    private MessageRepository messageRepository;
    MessageMapper mp = new MessageMapper();

    public UpdateCharactersMessageHandler(CharacterDAO characterDAO, HouseDAO houseDAO, EquipmentDAO equipmentDAO, WorldRunner worldRunner, MessageRepository messageRepository) {
        this.characterDAO = characterDAO;
        this.houseDAO = houseDAO;
        this.equipmentDAO = equipmentDAO;
        this.worldRunner = worldRunner;
        this.messageRepository = messageRepository;
    }

    public void handle(UpdateCharactersRequest msg, SafeStreamObserver responseObserver, Map<UUID, SafeStreamObserver<LostVictoryMessage>> clientObserverMap) throws IOException {

        UUID characterId = uuid(msg.getCharacter().getId());
        UUID clientID = uuid(msg.getClientID());

        com.lostVictories.api.CharacterMessage sentFromClient = msg.getCharacter();

        CharacterMessage serverVersion = characterDAO.getCharacter(characterId, true);
        if(serverVersion==null){
            log.info("unknown character id:"+ characterId);
            return;
        }

        if("2fbe421f-f701-49c9-a0d4-abb0fa904204".equals(characterId.toString()) && (System.currentTimeMillis()-sentFromClient.getCreationTime())>2000){
            System.out.println("older request from client:"+(System.currentTimeMillis()-sentFromClient.getCreationTime()));
        }

        if(characterId.equals(uuid(msg.getAvatar()))){
            if((System.currentTimeMillis()-sentFromClient.getCreationTime())>2000){
                System.out.println("back off initiated");
                responseObserver.backOff = 1000;
            }else if((System.currentTimeMillis()-sentFromClient.getCreationTime())<1000){
                responseObserver.backOff = 0;
            }
        }

        if(serverVersion.isAvailableForUpdate(clientID, sentFromClient, CHECKOUT_TIMEOUT)){
            serverVersion.updateState(sentFromClient, clientID, System.currentTimeMillis());
            CharacterMessage characterMessage = characterDAO.updateCharacterState(serverVersion);
            if(characterMessage!=null) {
                serverVersion = characterMessage;
            }
        }

        CharacterMessage storedAvatar = characterDAO.getCharacter(uuid(msg.getAvatar()));
        Vector v = storedAvatar.getLocation();
        Map<UUID, CharacterMessage> inRange = characterDAO.getAllCharacters(v.x, v.y, v.z, CheckoutScreenMessageHandler.CLIENT_RANGE).stream()
                .collect(Collectors.toMap(c->c.getId(), Function.identity()));

        if(inRange.containsKey(serverVersion.getId())) {
            responseObserver.onNext(mp.toMessage(serverVersion, responseObserver.backOff));
        }

        final CharacterMessage toSend = serverVersion;

        clientObserverMap.values().stream()
                .filter(entry->!entry.getClientID().equals(clientID))
                .filter(ee->characterDAO.isInRangeOf(toSend.getLocation(), ee.getClientID(), CheckoutScreenMessageHandler.CLIENT_RANGE))
                .forEach(e->{
                    e.onNext(mp.toMessage(toSend, responseObserver.backOff));
                });

        if(msg.getClientStartTime()>5000 && characterId.equals(uuid(msg.getAvatar()))) {
            inRange.values().stream()
                    .filter(cc -> cc.isAvailableForCheckout(5000))
                    .forEach(c -> responseObserver.onNext(mp.toMessage(c, responseObserver.backOff)));
        }

        if(!serverVersion.isDead()) {
            characterDAO.getAllCharacters(serverVersion.getUnitsUnderCommand()).values().stream()
                    .filter(u -> u != null && !inRange.containsKey(u.getId()))
                    .forEach(r -> responseObserver.onNext(mp.toMessage(r, true)));
            if(serverVersion.getCommandingOfficer()!=null){
                CharacterMessage commandingOfficer = characterDAO.getCharacter(serverVersion.getCommandingOfficer());
                if(commandingOfficer!=null && !inRange.containsKey(commandingOfficer.getId())){
                    responseObserver.onNext(mp.toMessage(commandingOfficer, true));
                }
            }

        }


        if(characterId.equals(uuid(msg.getAvatar()))){
            if(storedAvatar.getBoardedVehicle()!=null){
                CharacterMessage vehicle = characterDAO.getCharacter(storedAvatar.getBoardedVehicle());
                if(vehicle!=null && vehicle.getCheckoutClient()!=null && !vehicle.getCheckoutClient().equals(clientID)){
                    log.debug("force checkout of vehicle:"+vehicle.getId());
                    vehicle.setCheckoutClient(clientID);
                    vehicle.setCheckoutTime(System.currentTimeMillis());
                    List<CharacterMessage> values = new ArrayList<>();
                    values.add(vehicle);
                    characterDAO.save(values);
                    responseObserver.onNext(mp.toMessage(vehicle, responseObserver.backOff));
                }

            }
        }
    }




}
