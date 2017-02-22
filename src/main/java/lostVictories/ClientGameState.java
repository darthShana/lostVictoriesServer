package lostVictories;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.jme3.lostVictories.network.messages.CharacterMessage;

public class ClientGameState {

	private long timeStamp;
	private UUID checkedOutBy;
	private Map<UUID, CharacterMessage> unitPositions = new HashMap<UUID, CharacterMessage>();
	
	public ClientGameState(long timeStamp, UUID checkedOutBy, Map<UUID, CharacterMessage> unitPossitions) {
		this.timeStamp = timeStamp;
		this.unitPositions = unitPossitions;
		this.checkedOutBy = checkedOutBy;
	}
	
	@Override
	public String toString() {
		
		return timeStamp+":"+unitPositions;
	}

	public long getTimeStamp() {
		return timeStamp;
	}
	
	public Map<UUID, CharacterMessage> getUnitPositions(){
		return unitPositions;
	}

	public UUID getCheckedOutBy() {
		return checkedOutBy;
	}
}
