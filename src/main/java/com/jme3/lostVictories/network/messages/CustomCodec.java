package com.jme3.lostVictories.network.messages;

/**
 * Created by dharshanar on 26/04/17.
 */
public class CustomCodec {

    String[][] codec = new String[][]{
            {"@1", "\"com.jme3.lostVictories.objectives.SurvivalObjective\""},
            {"@2", "\"com.jme3.lostVictories.objectives.TransportSquad\""},
            {"@3", "\"com.jme3.lostVictories.objectives.CollectUnusedEquipment\""},
            {"@4", "\"com.jme3.lostVictories.objectives.RemanAbandonedVehicles\""},
            {"@5", "\"location\""},
            {"@6", "\"country\""},
            {"@7", "\"weapon\""},
            {"@8", "\"rank\""},
            {"@9", "\"commandingOfficer\""},
            {"@a", "\"unitsUnderCommand\""},
            {"@b", "\"passengers\""},
            {"@c", "\"checkoutClient\""},
            {"@d", "\"checkoutTime\""},
            {"@e", "\"type\""},
            {"@f", "\"orientation\""},
            {"@g", "\"actions\""},
            {"@h", "\"engineDamaged\""},
            {"@i", "\"version\""},
            {"@j", "\"squadType\""},
            {"@k", "\"GERMAN\""},
            {"@l", "\"RIFLE\""},
            {"@m", "\"CADET_CORPORAL\""},
            {"@n", "\"SOLDIER\""},
            {"@o", "\"com.jme3.lostVictories.network.messages.actions.Idle\""},
            {"@p", "\"class\""},
            {"@q", "true"},
            {"@r", "false"},
            {"@s", "\"MG42_TEAM\""},
    };



    public String encode(String uncompressed) {

        for(String[] codes:codec){
            uncompressed = uncompressed.replace(codes[1], codes[0]);
        }

        return uncompressed;
    }
}
