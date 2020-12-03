package com.balancedbytes.games.ffb.injury;

import com.balancedbytes.games.ffb.InjuryType;
import com.balancedbytes.games.ffb.SendToBoxReason;

public class Foul extends InjuryType{

	public Foul() {
		super("foul", false, SendToBoxReason.FOULED);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean shouldPlayFallSound() { return false; }
	
	@Override
	public boolean isFoul() { return true; }

}