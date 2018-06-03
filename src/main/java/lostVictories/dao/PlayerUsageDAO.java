package lostVictories.dao;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import com.lostVictories.rest.api.model.PlayerUsage;
import lostVictories.AppConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;


public class PlayerUsageDAO {
	private static Logger log = LoggerFactory.getLogger(PlayerUsageDAO.class);
    private final Client client;


    public PlayerUsageDAO() throws IOException {

        client = ClientBuilder.newClient();
	}

	public void userConnected(UUID userID) {
        try {
            PlayerUsage playerUsage = new PlayerUsage()
                    .userId(userID.toString())
                    .gameId(AppConfig.GAME_ID.get())
                    .started(true)
                    .stopped(false);

            doPost(playerUsage);
        }catch (Throwable e){
            log.error("unable to register user connected:"+userID);
        }
	}

    public void userDisconnected(UUID userID) {
        try {
            PlayerUsage playerUsage = new PlayerUsage()
                    .userId(userID.toString())
                    .gameId(AppConfig.GAME_ID.get())
                    .started(false)
                    .stopped(true);

            doPost(playerUsage);
        }catch (Throwable e){
            log.error("unable to register user disconnected:"+userID);
        }
    }

    private void doPost(PlayerUsage playerUsage) {
        WebTarget webTarget = client.target(AppConfig.GAME_MANAGER_URL.get()).path("usage");
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        invocationBuilder.post(Entity.entity(playerUsage, MediaType.APPLICATION_JSON_TYPE));
    }



	public void endAllGameSessions(Set<UUID> users) {
		users.forEach(userId->userDisconnected(userId));
		
	}

}
