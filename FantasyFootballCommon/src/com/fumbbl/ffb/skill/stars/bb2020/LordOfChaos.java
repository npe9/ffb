package com.fumbbl.ffb.skill.stars.bb2020;

import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.SkillCategory;
import com.fumbbl.ffb.RulesCollection.Rules;
import com.fumbbl.ffb.model.skill.Skill;
import com.fumbbl.ffb.model.skill.SkillUsageType;
/**
*A team that includes Lord Borak gain an extra Team re-roll for the first half of the game.
* If this team re-roll is not used during the first half, it may be carried over into the second half.
*  However, if Lord Borak is removed from play before this re-roll is used, it is lost.

*/

@RulesCollection(Rules.BB2020)
public class LordOfChaos extends Skill {
	public LordOfChaos() {
		super("LordOfChaos", SkillCategory.TRAIT, SkillUsageType.ONCE_PER_GAME);
	}
}