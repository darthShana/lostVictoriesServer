package lostVictories;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UnitState {

	private long timeStamp;
	private Map<UUID, Vector> unitPositions = new HashMap<UUID, Vector>();
	
	public UnitState(long timeStamp, Map<UUID, Vector> unitPossitions) {
		this.timeStamp = timeStamp;
		this.unitPositions = unitPossitions;
	}
	
	@Override
	public String toString() {
		
		return timeStamp+":"+unitPositions;
	}

	public long getTimeStamp() {
		return timeStamp;
	}
	
	public Map<UUID, Vector> getUnitPositions(){
		return unitPositions;
	}
}
