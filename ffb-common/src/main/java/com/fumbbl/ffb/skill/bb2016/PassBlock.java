package com.fumbbl.ffb.skill.bb2016;

import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.SkillCategory;
import com.fumbbl.ffb.RulesCollection.Rules;
import com.fumbbl.ffb.model.property.NamedProperties;
import com.fumbbl.ffb.model.skill.Skill;

/**
 * A player with this skill is allowed to move up to three squares when the
 * opposing coach announces that one of his players is going to pass the ball or
 * a bomb. This move is made out of sequence, after the range has been measured,
 * but before any interception attempts have been made. The coach declares the
 * full route that the player will take and this route must finish with the
 * player in a legal Pass Block square. A legal Pass Block square puts the
 * player in a position to attempt an interception, in the empty square that is
 * the target of the pass, or with the thrower or catcher in one of his tackle
 * zones. The player may not stop from moving along this exact route this turn
 * unless he has reached the final square, has been held fast by Tentacles, has
 * been Knocked Down, or has reached another legal Pass Block square on the
 * route. The opposing coach is not allowed to change his mind about passing
 * after the player with this skill has made his move. The special move is free,
 * and in no way affects the player's ability to move in the following turn.
 * Apart from this, however, the move is made using all of the normal rules and
 * skills and the player does have to dodge in order to leave opposing players'
 * tackle zones.
 */
@RulesCollection(Rules.BB2016)
public class PassBlock extends Skill {

	public PassBlock() {
		super("Pass Block", SkillCategory.GENERAL);
	}

	@Override
	public void postConstruct() {
		registerProperty(NamedProperties.canMoveWhenOpponentPasses);
	}

}
