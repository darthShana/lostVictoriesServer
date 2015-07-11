package com.jme3.lostVictories.network.messages;

public enum RankMessage {
	COLONEL{
		@Override
        int getFullStrengthPopulation() {
            return 2;
        }
	}, 
	LIEUTENANT{
		@Override
        int getFullStrengthPopulation() {
            return 4;
        }
	}, 
	CADET_CORPORAL{
		@Override
        int getFullStrengthPopulation() {
            return 3;
        }
	}, 
	PRIVATE{
		@Override
        int getFullStrengthPopulation() {
            return 0;
        }
	};
	
	abstract int getFullStrengthPopulation();
}
