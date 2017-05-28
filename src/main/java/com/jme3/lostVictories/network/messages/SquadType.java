package com.jme3.lostVictories.network.messages;

public enum SquadType {
	ANTI_TANK_GUN, ARMORED_VEHICLE, MORTAR_TEAM, MG42_TEAM, RIFLE_TEAM;

    public com.lostVictories.api.SquadType toMessage() {
        switch (this) {
            case ANTI_TANK_GUN:
                return com.lostVictories.api.SquadType.ANTI_TANK_GUN_SQUAD;
            case ARMORED_VEHICLE:
                return com.lostVictories.api.SquadType.ARMORED_VEHICLE_SQUAD;
            case MORTAR_TEAM:
                return com.lostVictories.api.SquadType.MORTAR_TEAM_SQUAD;
            case MG42_TEAM:
                return com.lostVictories.api.SquadType.MG42_TEAM_SQUAD;
            case RIFLE_TEAM:
                return com.lostVictories.api.SquadType.RIFLE_TEAM_SQUAD;
        }
        throw new RuntimeException("unknown squad type:"+this);
    }
}
