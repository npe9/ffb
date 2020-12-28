package com.balancedbytes.games.ffb.skill;

import com.balancedbytes.games.ffb.ReRollSources;
import com.balancedbytes.games.ffb.ReRolledActions;
import com.balancedbytes.games.ffb.SkillCategory;
import com.balancedbytes.games.ffb.model.Skill;
import com.balancedbytes.games.ffb.model.SkillConstants;
import com.balancedbytes.games.ffb.model.modifier.CancelSkillProperty;

/**
 * A player with the Sure Hands skill is allowed to re-roll the D6 if he fails
 * to pick up the ball. In addition, the Strip Ball skill will not work against
 * a player with this skill.
 */
public class SureHands extends Skill {

	public SureHands() {
		super("Sure Hands", SkillCategory.GENERAL);
	}

	@Override
	public void postConstruct() {
		registerProperty(new CancelSkillProperty(SkillConstants.STRIP_BALL));

		registerRerollSource(ReRolledActions.PICK_UP, ReRollSources.SURE_HANDS);
	}

}
