package com.fumbbl.ffb.skill.bb2020;

import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.RulesCollection.Rules;
import com.fumbbl.ffb.SkillCategory;
import com.fumbbl.ffb.model.property.NamedProperties;
import com.fumbbl.ffb.model.skill.Skill;

/**
 * A player with the Right Stuff skill can be thrown by another player from his
 * team who has the Throw Team-Mate skill. See the Throw Team-Mate skill entry
 * below for details of how the player is thrown. When a player with this skill
 * is thrown or fumbled and ends up in an unoccupied square, he must make a
 * landing roll unless he landed on another player during the throw. A landing
 * roll is an Agility roll with a -1 modifier for each opposing player's tackle
 * zone on the square he lands in. If he passes the roll he lands on his feet.
 * If the landing roll is failed or he landed on another player during the throw
 * he is Placed Prone and must pass an Armour roll to avoid injury. If the
 * player is not injured during his landing he may take an Action later this
 * turn if he has not already done so. A failed landing roll or landing in the
 * crowd does not cause a turnover, unless he was holding the ball.
 */
@RulesCollection(Rules.BB2020)
public class RightStuff extends Skill {

	public RightStuff() {
		super("Right Stuff", SkillCategory.TRAIT);
	}

	@Override
	public void postConstruct() {
		registerProperty(NamedProperties.canBeThrownIfStrengthIs3orLess);
		registerProperty(NamedProperties.ignoreTackleWhenBlocked);
	}

}
