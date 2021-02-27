package com.balancedbytes.games.ffb.skill;

import com.balancedbytes.games.ffb.RulesCollection;
import com.balancedbytes.games.ffb.RulesCollection.Rules;
import com.balancedbytes.games.ffb.SkillCategory;
import com.balancedbytes.games.ffb.model.Skill;
import com.balancedbytes.games.ffb.model.modifier.NamedProperties;
import com.balancedbytes.games.ffb.modifiers.ArmorModifier;
import com.balancedbytes.games.ffb.modifiers.InjuryModifier;
import com.balancedbytes.games.ffb.modifiers.InjuryModifierContext;

import java.util.Arrays;

/**
 * Add 1 to any Armour or Injury roll made by a player with this skill when an
 * opponent is Knocked Down by this player during a block. Note that you only
 * modify one of the dice rolls, so if you decide to use Mighty Blow to modify
 * the Armour roll, you may not modify the Injury roll as well. Mighty Blow
 * cannot be used with the Stab or Chainsaw skills.
 */
@RulesCollection(Rules.COMMON)
public class MightyBlow extends Skill {

	public MightyBlow() {
		super("Mighty Blow", SkillCategory.STRENGTH);
	}

	@Override
	public void postConstruct() {
		registerModifier(new ArmorModifier("Mighty Blow", 1, false));
		registerModifier(new InjuryModifier("Mighty Blow", 1, false) {
			@Override
			public boolean appliesToContext(InjuryModifierContext context) {
				return (!context.isFoul()
					&& Arrays.stream(context.getInjuryContext().getArmorModifiers())
					.noneMatch(modifier -> modifier.isRegisteredToSkillWithProperty(NamedProperties.affectsEitherArmourOrInjuryOnBlock)));
			}
		});
		registerProperty(NamedProperties.affectsEitherArmourOrInjuryOnBlock);
	}

}
