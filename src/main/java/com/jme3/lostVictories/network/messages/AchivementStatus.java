package com.jme3.lostVictories.network.messages;

import java.io.Serializable;

public class AchivementStatus implements Serializable{

	private String achivementStatusText;
    private int achivementTotal;
    private int achivementCurrent;
    private long sentTime;
    
    public AchivementStatus(String achivementStatusText, int achivementCurrent, int achivementTotal, long sentTime) {
		this.achivementStatusText = achivementStatusText;
		this.achivementCurrent = achivementCurrent;
		this.achivementTotal = achivementTotal;
		this.sentTime = sentTime;
	}
    
    public long getSentTime() {
		return sentTime;
	}
}
