package com.balancedbytes.games.ffb.skill.bb2020;

import com.balancedbytes.games.ffb.RulesCollection;
import com.balancedbytes.games.ffb.RulesCollection.Rules;
import com.balancedbytes.games.ffb.SkillCategory;
import com.balancedbytes.games.ffb.model.skill.Skill;
import com.balancedbytes.games.ffb.modifiers.ModifierType;
import com.balancedbytes.games.ffb.modifiers.PassContext;
import com.balancedbytes.games.ffb.modifiers.PassModifier;

/**
 * The player may add 1 to the D6 when attempting a TTM action.
 */
@RulesCollection(Rules.BB2020)
public class StrongArm extends Skill {

	public StrongArm() {
		super("Strong Arm", SkillCategory.STRENGTH);
	}

	@Override
	public void postConstruct() {
		registerModifier(new PassModifier("Strong Arm", -1, ModifierType.REGULAR) {
			@Override
			public boolean appliesToContext(Skill skill, PassContext context) {
				return context.isDuringThrowTeamMate();
			}
		});
	}

}
