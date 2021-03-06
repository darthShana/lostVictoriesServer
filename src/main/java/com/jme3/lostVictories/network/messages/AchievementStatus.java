package com.jme3.lostVictories.network.messages;

import java.io.Serializable;

public class AchievementStatus implements Serializable{

	private String achivementStatusText;
    private int achivementTotal;
    private int achivementCurrent;
    private long sentTime;
    
    public AchievementStatus(String achivementStatusText, int achivementCurrent, int achivementTotal, long sentTime) {
		this.achivementStatusText = achivementStatusText;
		this.achivementCurrent = achivementCurrent;
		this.achivementTotal = achivementTotal;
		this.sentTime = sentTime;
	}

	public String getAchivementStatusText() {
		return achivementStatusText;
	}

	public int getAchivementTotal() {
		return achivementTotal;
	}

	public int getAchivementCurrent() {
		return achivementCurrent;
	}

	public long getSentTime() {
		return sentTime;
	}
}
