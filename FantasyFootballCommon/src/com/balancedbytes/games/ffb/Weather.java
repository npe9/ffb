package com.balancedbytes.games.ffb;

import com.balancedbytes.games.ffb.factory.CatchModifierFactory;
import com.balancedbytes.games.ffb.factory.InterceptionModifierFactory;
import com.balancedbytes.games.ffb.model.Game;
import com.balancedbytes.games.ffb.modifiers.CatchModifierKey;
import com.balancedbytes.games.ffb.modifiers.InterceptionModifierKey;
import com.balancedbytes.games.ffb.modifiers.ModifierSource;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum Weather implements INamedObject, ModifierSource {

	SWELTERING_HEAT("Sweltering Heat", "heat",
			"Each player on the pitch may suffer from heat exhaustion on a roll of 1 before the next kick-off."),
	VERY_SUNNY("Very Sunny", "sunny", "A -1 modifier applies to all passing rolls.") {
		@Override
		public Set<IRollModifier> modifier(Game game) {
			return Collections.singleton(PassingModifiers.VERY_SUNNY);
		}
	},
	NICE("Nice Weather", "nice", "Perfect Fantasy Football weather."),
	POURING_RAIN("Pouring Rain", "rain", "A -1 modifier applies to all catch, intercept, or pick-up rolls.") {
		@Override
		public Collection<IRollModifier> modifier(Game game) {

			InterceptionModifierFactory interceptionModifierFactory = game.getFactory(FactoryType.Factory.INTERCEPTION_MODIFIER);
			CatchModifierFactory catchModifierFactory = game.getFactory(FactoryType.Factory.CATCH_MODIFIER);

			return new HashSet<IRollModifier>() {{
					add(catchModifierFactory.forKey(CatchModifierKey.POURING_RAIN));
					add(interceptionModifierFactory.forKey(InterceptionModifierKey.POURING_RAIN));
					add(PickupModifiers.POURING_RAIN);
				}};
		}
	},
	BLIZZARD("Blizzard", "blizzard",
			"Going For It fails on a roll of 1 or 2 and only quick or short passes can be attempted.") {
		@Override
		public Collection<IRollModifier> modifier(Game game) {
			return new HashSet<IRollModifier>() {{
				add(GoForItModifier.BLIZZARD);
				add(PassingModifiers.BLIZZARD);
			}};
		}
	},
	INTRO("Intro", "intro", "No weather at all, but the intro screen shown by the client.");

	private String fName;
	private String fShortName;
	private String fDescription;

	Weather(String pName, String pShortName, String pDescription) {
		fName = pName;
		fShortName = pShortName;
		fDescription = pDescription;
	}

	public String getName() {
		return fName;
	}

	public String getShortName() {
		return fShortName;
	}

	public String getDescription() {
		return fDescription;
	}

	@Override
	public Collection<IRollModifier> modifier(Game game) {
		return Collections.emptySet();
	}
}
