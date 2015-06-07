package lostVictories;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.jme3.lostVictories.network.messages.Character;

public class ClientGameState {

	private long timeStamp;
	private UUID checkedOutBy;
	private Map<UUID, Character> unitPositions = new HashMap<UUID, Character>();
	
	public ClientGameState(long timeStamp, UUID checkedOutBy, Map<UUID, Character> unitPossitions) {
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
	
	public Map<UUID, Character> getUnitPositions(){
		return unitPositions;
	}

	public UUID getCheckedOutBy() {
		return checkedOutBy;
	}
}
