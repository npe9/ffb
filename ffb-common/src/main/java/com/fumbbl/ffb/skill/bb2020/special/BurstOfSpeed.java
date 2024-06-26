package com.fumbbl.ffb.skill.bb2020.special;

import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.RulesCollection.Rules;
import com.fumbbl.ffb.SkillCategory;
import com.fumbbl.ffb.model.property.NamedProperties;
import com.fumbbl.ffb.model.skill.Skill;
import com.fumbbl.ffb.model.skill.SkillUsageType;

/**
 * Once per game, Roxanna may attempt to Rush three times, rather than the usual two.
 * You may declare you are using this special rule after Roxanna has rushed twice.
 */

@RulesCollection(Rules.BB2020)
public class BurstOfSpeed extends Skill {
	public BurstOfSpeed() {
		super("Burst of Speed", SkillCategory.TRAIT, SkillUsageType.ONCE_PER_GAME);
	}

	@Override
	public void postConstruct() {
		registerProperty(NamedProperties.canMakeAnExtraGfiOnce);
	}
}
