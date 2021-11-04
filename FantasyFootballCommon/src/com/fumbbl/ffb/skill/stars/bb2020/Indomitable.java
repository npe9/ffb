package com.fumbbl.ffb.skill.stars.bb2020;

import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.SkillCategory;
import com.fumbbl.ffb.RulesCollection.Rules;
import com.fumbbl.ffb.model.skill.Skill;
import com.fumbbl.ffb.model.skill.SkillUsageType;
/**
*Once per game, when Karla succesfully rolls to use her Dauntless skill,
* she may increase her Strength characteristic to double that of the nominated target of her Block action

*/

@RulesCollection(Rules.BB2020)
public class Indomitable extends Skill {
	public Indomitable() {
		super("Indomitable", SkillCategory.TRAIT, SkillUsageType.ONCE_PER_GAME);
	}
}