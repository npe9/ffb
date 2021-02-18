package com.balancedbytes.games.ffb.skill;

import com.balancedbytes.games.ffb.RulesCollection;
import com.balancedbytes.games.ffb.RulesCollection.Rules;
import com.balancedbytes.games.ffb.SkillCategory;
import com.balancedbytes.games.ffb.model.ModifierDictionary;
import com.balancedbytes.games.ffb.model.Skill;
import com.balancedbytes.games.ffb.model.modifier.NamedProperties;

/**
 * A player with this skill assists an offensive or defensive block even if he
 * is in another player's tackle zone. This skill may not be used to assist a
 * foul.
 */
@RulesCollection(Rules.COMMON)
public class Guard extends Skill {

	public Guard() {
		super("Guard", SkillCategory.STRENGTH);
	}

	@Override
	public void postConstruct(ModifierDictionary dictionary) {
		registerProperty(NamedProperties.assistInTacklezones);
	}

}
