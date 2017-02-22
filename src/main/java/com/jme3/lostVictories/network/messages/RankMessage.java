package com.jme3.lostVictories.network.messages;

public enum RankMessage {
	COLONEL{
		@Override
        int getFullStrengthPopulation() {
            return 4;
        }
        @Override
        int getKillCountForPromotion() {
            return 100;
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
            return 30;
        }

		@Override
		public String getAchivementMessage() {
			return "Congradulations on your promotion Lieutenant!: You have several squads in your command.";
		}
	}, 
	CADET_CORPORAL{
        @Override
        int getKillCountForPromotion() {
            return 5;
        }
        
		@Override
        int getFullStrengthPopulation() {
            return 4;
        }

		@Override
		public String getAchivementMessage() {
			return "Greetings Cadet Corporal: Our country needs squad leaders like you. Prove your self and you will be promoted. But try and keep you and and your squad alive.";
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
