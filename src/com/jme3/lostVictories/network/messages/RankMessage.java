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
		@Override
		public String getAchivementMessage() {
			return "Colonel: This is the highest rank for this gamer at the moment";
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

		@Override
		public String getAchivementMessage() {
			return "Lieutenant: You have several squads in your command. When your squads have 10 kills you will be promoted.";
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

		@Override
		public String getAchivementMessage() {
			return "Corporal: You are a squal leader. When your squad has 5 kills you will be promoted. But when a unit is killed its kills are lost.";
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

		@Override
		public String getAchivementMessage() {
			return "There is no carrier path from a private";
		}
	};
	
	abstract int getFullStrengthPopulation();
	abstract int getKillCountForPromotion();
	public abstract String getAchivementMessage();
	
	public int getTotalAchivementCount(){
		return this.getKillCountForPromotion();
	}
}
