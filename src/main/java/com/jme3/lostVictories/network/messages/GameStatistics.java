package com.jme3.lostVictories.network.messages;

import java.io.Serializable;

public class GameStatistics implements Serializable{

	
	private Long blueHouses;
	private Long redHouses;
	private Integer blueVictoryPoints;
	private Integer redVictoryPoints;
	private Long avatarRespawnEstimate;

	public void setHousesCaptured(Long blue, Long red){
		this.blueHouses = blue;
		this.redHouses = red;
	}
	
	public void setVictorypoints(Integer blue, Integer red){
		this.blueVictoryPoints = blue;
		this.redVictoryPoints = red;
	}
	
	public void setAvatarRespawnEstimate(Long time){
		this.avatarRespawnEstimate = time;
	}
}
