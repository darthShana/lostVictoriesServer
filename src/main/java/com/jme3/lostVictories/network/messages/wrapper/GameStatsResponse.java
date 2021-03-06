package com.jme3.lostVictories.network.messages.wrapper;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.jme3.lostVictories.network.messages.AchievementStatus;
import com.jme3.lostVictories.network.messages.GameStatistics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dharshanar on 1/04/17.
 */

public class GameStatsResponse extends LostVictoryMessage{


    private List<String> messages = new ArrayList<>();
    private GameStatistics gameStatistics;
    private AchievementStatus achivementStatus;

    public GameStatsResponse(List<String> messages, GameStatistics gameStatistics, AchievementStatus achivementStatus){
        if(messages!=null){
            this.messages.addAll(messages);
        }
        this.gameStatistics = gameStatistics;
        this.achivementStatus = achivementStatus;
    }

    public List<String> getMessages() {
        return messages;
    }

    public AchievementStatus getAchivementStatus() {
        return achivementStatus;
    }

    public GameStatistics getGameStatistics() {
        return gameStatistics;
    }
}
