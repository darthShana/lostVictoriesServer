package com.jme3.lostVictories.network.messages;

public enum CharacterType {
    ARMORED_CAR {
		@Override
		public Weapon getDefaultWeapon() {
			return Weapon.MG42;
		}

		@Override
		public boolean requiresOperator() { return true;}
	}, ANTI_TANK_GUN {
		@Override
		public Weapon getDefaultWeapon() {
			return Weapon.CANNON;
		}

		@Override
		public boolean requiresOperator() { return true;}
	}, SOLDIER {
		@Override
		public Weapon getDefaultWeapon() {
			return Weapon.RIFLE;
		}

		@Override
		public boolean requiresOperator() { return false;}
	}, HALF_TRACK {
		@Override
		public Weapon getDefaultWeapon() {
			return Weapon.MG42;
		}

		@Override
		public boolean requiresOperator() { return true;}
	}, PANZER4 {
    	@Override
		public Weapon getDefaultWeapon(){ return Weapon.CANNON; }

		@Override
		public boolean requiresOperator() { return true;}
	}, AVATAR {
		@Override
		public Weapon getDefaultWeapon() {
			return Weapon.RIFLE;
		}

		@Override
		public boolean requiresOperator() { return false;}
	}, M4SHERMAN{
		@Override
		public Weapon getDefaultWeapon() { return Weapon.CANNON; }

		@Override
		public boolean requiresOperator() { return true;}
	};

	public abstract Weapon getDefaultWeapon();

    public abstract boolean requiresOperator();
}
