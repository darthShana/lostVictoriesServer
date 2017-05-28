package com.jme3.lostVictories.network.messages;

import java.io.Serializable;

public class GameStatistics implements Serializable{

	
	private Integer blueHouses;
	private Integer redHouses;
	private Integer blueVictoryPoints;
	private Integer redVictoryPoints;
	private Integer avatarRespawnEstimate;

	public void setHousesCaptured(Integer blue, Integer red){
		this.blueHouses = blue;
		this.redHouses = red;
	}
	
	public void setVictorypoints(Integer blue, Integer red){
		this.blueVictoryPoints = blue;
		this.redVictoryPoints = red;
	}
	
	public void setAvatarRespawnEstimate(Integer time){
		this.avatarRespawnEstimate = time;
	}

    public Integer getBlueHouses() {
        return blueHouses;
    }

	public Integer getRedHouses() {
		return redHouses;
	}

	public Integer getBlueVictoryPoints() {
		return blueVictoryPoints;
	}

	public Integer getRedVictoryPoints() {
		return redVictoryPoints;
	}

	public Integer getAvatarRespawnEstimate() {
		return avatarRespawnEstimate;
	}
}
