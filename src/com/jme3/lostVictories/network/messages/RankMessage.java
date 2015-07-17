package com.jme3.lostVictories.network.messages;

public enum RankMessage {
	COLONEL{
		@Override
        int getFullStrengthPopulation() {
            return 2;
        }
        @Override
        int getKillCountForPromotion() {
            return 45;
        }
	}, 
	LIEUTENANT{
		@Override
        int getFullStrengthPopulation() {
            return 4;
        }
		
        @Override
        int getKillCountForPromotion() {
            return 10;
        }
	}, 
	CADET_CORPORAL{
        @Override
        int getKillCountForPromotion() {
            return 5;
        }
        
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
		
        @Override
        int getKillCountForPromotion() {
            return 1;
        }
	};
	
	abstract int getFullStrengthPopulation();
	abstract int getKillCountForPromotion();
}
