package com.jme3.lostVictories.network.messages;

public enum CharacterType {
    ARMORED_CAR {
		@Override
		public Weapon getDefaultWeapon() {
			return Weapon.MG42;
		}
	}, ANTI_TANK_GUN {
		@Override
		public Weapon getDefaultWeapon() {
			return Weapon.CANNON;
		}
	}, SOLDIER {
		@Override
		public Weapon getDefaultWeapon() {
			return Weapon.RIFLE;
		}
	}, HALF_TRACK {
		@Override
		public Weapon getDefaultWeapon() {
			return Weapon.MG42;
		}
	}, PANZER4 {
    	@Override
		public Weapon getDefaultWeapon(){ return Weapon.CANNON; }
	}, AVATAR {
		@Override
		public Weapon getDefaultWeapon() {
			return Weapon.RIFLE;
		}
	}, M4SHERMAN{
		@Override
		public Weapon getDefaultWeapon() { return Weapon.CANNON; }
	};

	public abstract Weapon getDefaultWeapon();
}
