package com.jme3.lostVictories.network.messages;

import java.util.Set;

public enum CaptureStatus {
	CAPTURING {
		@Override
		public CaptureStatus transition(Set<CharacterMessage> allCharacters, HouseMessage houseMessage) {
			if(!allCharacters.isEmpty() && allCharacters.stream().allMatch(c->c.getCountry()==houseMessage.getCompetingOwner())){
				if(houseMessage.captureTimeExceeded()){
					houseMessage.changeOwnership();
					return CaptureStatus.CAPTURED;
				}else{
					return CAPTURING;
				}
			}
			houseMessage.withdrawContest();
			return NONE;
		}
	}, NONE {
		@Override
		public CaptureStatus transition(Set<CharacterMessage> allCharacters, HouseMessage houseMessage) {
			if(allCharacters.isEmpty()){
				return NONE;
			}
			Country sample = allCharacters.iterator().next().getCountry();
			if(allCharacters.stream().allMatch(c->c.getCountry()==sample)){
				houseMessage.contestOwnership(sample);
				return CaptureStatus.CAPTURING;
			}
			return NONE;
		}
	}, CAPTURED {
		@Override
		public CaptureStatus transition(Set<CharacterMessage> allCharacters, HouseMessage houseMessage) {
			if(!allCharacters.isEmpty() && allCharacters.stream().allMatch(c->c.getCountry()!=houseMessage.getOwner())){
				houseMessage.contestOwnership(houseMessage.getOwner());
				return CaptureStatus.DECAPTURING;
			}
			return CAPTURED;
		}
	}, DECAPTURING {
		@Override
		public CaptureStatus transition(Set<CharacterMessage> allCharacters, HouseMessage houseMessage) {
			if(!allCharacters.isEmpty() && allCharacters.stream().allMatch(c->c.getCountry()!=houseMessage.getOwner())){
				if(houseMessage.captureTimeExceeded()){
					houseMessage.vacate();
					return CaptureStatus.NONE;
				}else{
					return CaptureStatus.DECAPTURING;
				}
			}
			houseMessage.withdrawContest();
			return CAPTURED;
		}
	};

	public abstract CaptureStatus transition(Set<CharacterMessage> allCharacters, HouseMessage houseMessage);

}
