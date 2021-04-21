package com.fumbbl.ffb.modifiers;

import com.fumbbl.ffb.model.KickTeamMateRange;
import com.fumbbl.ffb.model.skill.Skill;

public class RightStuffModifierCollection extends ModifierCollection<RightStuffContext, RightStuffModifier> {
	public RightStuffModifierCollection() {
		add(new RightStuffModifier("Medium Kick", 1, ModifierType.REGULAR) {
			@Override
			public boolean appliesToContext(Skill skill, RightStuffContext context) {
				return context.getKtmRange() == KickTeamMateRange.MEDIUM;
			}
		});
		add(new RightStuffModifier("Long Kick", 2, ModifierType.REGULAR) {
			@Override
			public boolean appliesToContext(Skill skill, RightStuffContext context) {
				return context.getKtmRange() == KickTeamMateRange.LONG;
			}
		});
		add(new RightStuffModifier("1 Tacklezone", 1, ModifierType.TACKLEZONE));
		add(new RightStuffModifier("2 Tacklezones", 2, ModifierType.TACKLEZONE));
		add(new RightStuffModifier("3 Tacklezones", 3, ModifierType.TACKLEZONE));
		add(new RightStuffModifier("4 Tacklezones", 4, ModifierType.TACKLEZONE));
		add(new RightStuffModifier("5 Tacklezones", 5, ModifierType.TACKLEZONE));
		add(new RightStuffModifier("6 Tacklezones", 6, ModifierType.TACKLEZONE));
		add(new RightStuffModifier("7 Tacklezones", 7, ModifierType.TACKLEZONE));
		add(new RightStuffModifier("8 Tacklezones", 8, ModifierType.TACKLEZONE));
	}
}