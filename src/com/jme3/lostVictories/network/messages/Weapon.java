package com.jme3.lostVictories.network.messages;

public enum Weapon {
	RIFLE {
		@Override
		public boolean isReusable() {
			return false;
		}
	}, MG42 {
		@Override
		public boolean isReusable() {
			return true;
		}
	}, MORTAR {
		@Override
		public boolean isReusable() {
			return true;
		}
	}, CANNON {
		@Override
		public boolean isReusable() {
			return true;
		}
	};

	public abstract boolean isReusable();

}
