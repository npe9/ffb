package com.balancedbytes.games.ffb.skill;

import com.balancedbytes.games.ffb.ArmorModifiers;
import com.balancedbytes.games.ffb.RulesCollection;
import com.balancedbytes.games.ffb.SkillCategory;
import com.balancedbytes.games.ffb.RulesCollection.Rules;
import com.balancedbytes.games.ffb.model.Skill;

/**
 * A player with this skill is blessed with a huge crab like claw or razor sharp
 * talons that make armour useless. When an opponent is Knocked Down by this
 * player during a block, any Armour roll of 8 or more after modifications
 * automatically breaks armour.
 */
@RulesCollection(Rules.COMMON)
public class Claw extends Skill {

	public Claw() {
		super("Claw", SkillCategory.MUTATION);
	}

	@Override
	public void postConstruct() {
		registerModifier(ArmorModifiers.CLAWS);
	}

}
