package com.lostVictories.service;

import com.jme3.lostVictories.network.messages.*;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.actions.Action;

import com.lostVictories.api.*;
import com.lostVictories.api.CaptureStatus;
import com.lostVictories.api.CharacterMessage;
import com.lostVictories.api.CharacterType;
import com.lostVictories.api.CheckoutScreenRequest;
import com.lostVictories.api.Country;
import com.lostVictories.api.HouseMessage;
import com.lostVictories.api.Quaternion;
import com.lostVictories.api.RankMessage;
import com.lostVictories.api.SquadType;
import com.lostVictories.api.TreeGroupMessage;
import com.lostVictories.api.UnClaimedEquipmentMessage;
import com.lostVictories.api.Weapon;
import io.grpc.stub.StreamObserver;
import lostVictories.dao.*;
import org.apache.log4j.Logger;

import static com.lostVictories.service.LostVictoriesService.uuid;

/**
 * Created by dharshanar on 26/05/17.
 */
public class CheckoutScreenMessageHandler {

    private static Logger log = Logger.getLogger(CheckoutScreenMessageHandler.class);
    public static float CLIENT_RANGE = 250l;

    private final CharacterDAO characterDAO;
    private final HouseDAO houseDAO;
    private final EquipmentDAO equipmentDAO;
    private final TreeDAO treeDAO;
    private final PlayerUsageDAO playerUsageDAO;
    private MessageMapper mp = new MessageMapper();

    public CheckoutScreenMessageHandler(CharacterDAO characterDAO, HouseDAO houseDAO, EquipmentDAO equipmentDAO, TreeDAO treeDAO, PlayerUsageDAO playerUsageDAO) {

        this.characterDAO = characterDAO;
        this.houseDAO = houseDAO;
        this.equipmentDAO = equipmentDAO;
        this.treeDAO = treeDAO;
        this.playerUsageDAO = playerUsageDAO;
    }

    public void handle(CheckoutScreenRequest request, StreamObserver<LostVictoryMessage> responseObserver) {
        log.info("checking out scene for avatar:"+uuid(request.getAvatar()));
        com.jme3.lostVictories.network.messages.CharacterMessage avatar = characterDAO.getCharacter(uuid(request.getAvatar()));
        if(avatar!=null){
            Vector l = avatar.getLocation();

            characterDAO.getAllCharacters(l.x, l.y, l.z, CLIENT_RANGE).stream().map(c->mp.toMessage(c)).forEach(cm->responseObserver.onNext(cm));
            equipmentDAO.getUnClaimedEquipment(l.x, l.y, l.z, CLIENT_RANGE).stream().map(e->mp.toMessage(e)).forEach(em->responseObserver.onNext(em));
            houseDAO.getAllHouses().stream().map(h->mp.toMessage(h)).forEach(hm->responseObserver.onNext(hm));
            treeDAO.getAllTrees().stream().map(t->mp.toMessage(t)).forEach(tm->responseObserver.onNext(tm));

            playerUsageDAO.registerStartGame(avatar.getUserID(), System.currentTimeMillis());
        }

        responseObserver.onCompleted();
    }



}
