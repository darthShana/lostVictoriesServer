package lostVictories.dao;


import java.io.IOException;
import java.util.UUID;


import com.jme3.lostVictories.network.messages.Country;
import com.lostVictories.rest.api.model.GameCompleted;
import com.lostVictories.rest.api.model.GameStarted;
import com.lostVictories.rest.api.model.GameUpdateRequest;
import lostVictories.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;

public class GameRequestDAO {

	private static Logger log = LoggerFactory.getLogger(GameRequestDAO.class);
    private final Client client;


    public GameRequestDAO() {
        client = ClientBuilder.newClient();
    }


    public void updateGameStatus(UUID gameID) throws IOException {
        try {
            GameUpdateRequest request = new GameUpdateRequest();
            request.gameStarted(new GameStarted().gameId(gameID.toString()));

            postGameUpdate(request);
        }catch(Throwable e){
            log.error("unable to notify game start..");
        }
    }

    private void postGameUpdate(GameUpdateRequest request) {
        WebTarget webTarget = client.target(AppConfig.GAME_MANAGER_URL.get()).path("games");
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        invocationBuilder.post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
    }

    public void recordAmericanVictory(UUID gameID) {
        try {
            GameUpdateRequest request = new GameUpdateRequest();
            request.gameCompleted(new GameCompleted().gameId(gameID.toString()).winner(Country.AMERICAN.name()));

            postGameUpdate(request);
        }catch(Throwable e){
            log.error("unable to notify game complete with american victory game:"+gameID);
        }
    }

    public void recordGermanVictory(UUID gameID)  {
        try {
            GameUpdateRequest request = new GameUpdateRequest();
            request.gameCompleted(new GameCompleted().gameId(gameID.toString()).winner(Country.GERMAN.name()));

            postGameUpdate(request);
        }catch (Throwable e){
            log.error("unable to notify game complete with german victory game:"+gameID);
        }
    }
}
