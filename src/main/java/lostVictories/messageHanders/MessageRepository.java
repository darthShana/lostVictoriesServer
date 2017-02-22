package lostVictories.messageHanders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MessageRepository {

	Map<UUID, List<String>> messages = new HashMap<UUID, List<String>>();
	
	public void addMessage(UUID clientId, String msg) {
		messages.putIfAbsent(clientId, new ArrayList<String>());
		messages.get(clientId).add(msg);
	}

	public List<String> popMessages(UUID clientID) {
		return messages.remove(clientID);
	}

}
