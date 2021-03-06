package com.balancedbytes.games.ffb.model.property;

import com.balancedbytes.games.ffb.PassingDistance;
import com.balancedbytes.games.ffb.modifiers.PassContext;

import java.util.HashSet;
import java.util.Set;

public class NamedProperties {
	public static final ISkillProperty addBonusForAccuratePass = new NamedProperty("Add Bonus For Accurate Pass");
	public static final ISkillProperty addStrengthOnBlitz = new NamedProperty("Add Strength on Blitz");
	public static final ISkillProperty affectsEitherArmourOrInjuryOnBlock = new NamedProperty("Affects Either Armour Or Injury On Block");
	public static final ISkillProperty affectsEitherArmourOrInjuryOnFoul = new NamedProperty("Affects Either Armour Or Injury On Foul");
	public static final ISkillProperty allowStandupAssists = new NamedProperty("Allow Stand Up Assists");
	public static final ISkillProperty appliesConfusion = new NamedProperty("Applies Confusion");
	public static final ISkillProperty appliesPoisonOnBadlyHurt = new NamedProperty("Applies Poison on Badly Hurt");
	public static final ISkillProperty armourRollWithoutBlockHasIncreasedEffectOnUndead = new NamedProperty("Armour Roll Without Block Has Increased Effect On Undead");
	public static final ISkillProperty assistInTacklezones = new NamedProperty("Assist in Tacklezones");
	public static final ISkillProperty blocksLikeChainsaw = new NamedProperty("Blocks Like Chainsaw");
	public static final ISkillProperty canAlwaysAssistFouls = new NamedProperty("Can Always Assist Fouls");
	public static final ISkillProperty canAttemptCatchInAdjacentSquares = new NamedProperty(
			"Can Attempt Catch In Adjacent Squares");
	public static final ISkillProperty canAttemptToTackleDodgingPlayer = new NamedProperty(
			"Can Attempt To Tackle Dodging Player");
	public static final ISkillProperty canBeThrown = new NamedProperty("Can Be Thrown");
	public static final ISkillProperty canBeKicked = new NamedProperty("Can Be Kicked");
	public static final ISkillProperty canBlockMoreThanOnce = new NamedProperty("Can Block More Than Once");
	public static final ISkillProperty canBlockSameTeamPlayer = new NamedProperty("Can Block Same Team Player");
	public static final ISkillProperty canCancelInterceptions = new NamedProperty("Can Force Interception Reroll");
	public static final ISkillProperty canChooseOwnPushedBackSquare = new NamedProperty(
			"Can Choose Own Pushed Back Square");
	public static final ISkillProperty canFollowPlayerLeavingTacklezones = new NamedProperty("Can Follow Player Leaving Tacklezones");
	public static final PassingProperty canForceInterceptionRerollOfLongPasses = new PassingProperty("Can Force Interception Reroll of Long Passes") {
		private final Set<PassingDistance> longDistances = new HashSet<PassingDistance>() {{
				add(PassingDistance.LONG_PASS);
				add(PassingDistance.LONG_BOMB);
			}};
		@Override
		public boolean appliesToContext(PassContext context) {
			return longDistances.contains(context.getDistance());
		}
	};
	public static final ISkillProperty canHoldPlayersLeavingTacklezones = new NamedProperty("Can Hold Players Leaving Tacklezones");
	public static final ISkillProperty canKickTeamMates = new NamedProperty("Can Kick Team Mates");
	public static final ISkillProperty canLeap = new NamedProperty("Can Leap");
	public static final ISkillProperty canMakeAnExtraGfi = new NamedProperty("Use Special Block Rules");
	public static final ISkillProperty canMoveDuringKickOffScatter = new NamedProperty(
			"Can Move During Kick Off Scatter");
	public static final ISkillProperty canMoveWhenOpponentPasses = new NamedProperty("Can Move When Opponent Passes");
	public static final ISkillProperty canPassToAnySquare = new NamedProperty("Can Pass To Any Square");
	public static final ISkillProperty canPerformArmourRollInsteadOfBlock = new NamedProperty(
			"Can Perform Armour Roll Instead Of Block");
	public static final ISkillProperty canPushBackToAnySquare = new NamedProperty("Can Push Back To Any Square");
	public static final ISkillProperty canPileOnOpponent = new NamedProperty("Can Pile On Opponent");
	public static final ISkillProperty canReduceKickDistance = new NamedProperty("Can Reduce Kick Distance");
	public static final ISkillProperty canRefuseToBePushed = new NamedProperty("Can Refuse To Be Pushed");
	public static final ISkillProperty canRerollOncePerTurn = new NamedProperty("Can Reroll Once Per Turn");
	public static final ISkillProperty canRerollDodge = new NamedProperty("Can Reroll Dodge");
	public static final ISkillProperty canRollToMatchOpponentsStrength = new NamedProperty(
			"Can Roll To Match Opponents Strength");
	public static final ISkillProperty canRollToSaveFromInjury = new NamedProperty("Can Roll To Save From Injury");
	public static final ISkillProperty canSneakExtraPlayersOntoPitch = new NamedProperty(
			"Can Sneak Extra Players Onto Pitch");
	public static final ISkillProperty canStandUpForFree = new NamedProperty("Can Stand Up For Free");
	public static final ISkillProperty canTakeDownPlayersWithHimOnBothDown = new NamedProperty("Can Take Down Players With Him On Both Down");
	public static final ISkillProperty canThrowTeamMates = new NamedProperty("Can Throw Team Mates");
	public static final ISkillProperty convertKOToStunOn8 = new NamedProperty("Convert KO to Stun on a roll of 8");
	public static final ISkillProperty convertStunToKO = new NamedProperty("Convert Stun to KO");
	public static final ISkillProperty dontDropFumbles = new NamedProperty("Don't Drop 2+ Fumbles");
	public static final ISkillProperty enableStandUpAndEndBlitzAction = new NamedProperty(
			"Enable Stand Up and End Blitz Action");
	public static final ISkillProperty enableThrowBombAction = new NamedProperty("Enable Throw Bomb Action");
	public static final ISkillProperty flipSameTeamOpponentToOtherTeam = new NamedProperty(
			"Flip Same Team Opponent to Other Team");
	public static final ISkillProperty forceOpponentToDropBallOnPushback = new NamedProperty(
			"Force Opponent To Drop Ball On Pushback");
	public static final ISkillProperty forceFollowup = new NamedProperty("Force Followup");
	public static final ISkillProperty forceFullMovement = new NamedProperty("Force Full Movement");
	public static final ISkillProperty forceRollBeforeBeingBlocked = new NamedProperty("Force Roll Before Being Blocked");
	public static final ISkillProperty forceSecondBlock = new NamedProperty("Force Second Block");
	public static final ISkillProperty getsSentOffAtEndOfDrive = new NamedProperty("Gets Sent Off At End Of Drive");
	public static final ISkillProperty goForItAfterBlock = new NamedProperty("Go For It After Block");
	public static final ISkillProperty grabOutsideBlock = new NamedProperty("Grab Outside Block");
	public static final ISkillProperty grantsTeamRerollWhenOnPitch = new NamedProperty(
			"Grants Team Reroll When On Pitch");
	public static final ISkillProperty hasNoTacklezone = new NamedProperty("Has No Tacklezone");
	public static final ISkillProperty hasNurglesRot = new NamedProperty("Has Nurgles Rot");
	public static final ISkillProperty hasToRollToUseTeamReroll = new NamedProperty("Has To Roll To Use Team Reroll");
	public static final ISkillProperty ignoreDefenderStumblesResult = new NamedProperty(
			"Ignore Defender Stumbles Result");
	public static final ISkillProperty ignoreTackleWhenBlocked = new NamedProperty("Ignore Tackle When Blocked");
	public static final ISkillProperty ignoreTacklezonesWhenCatching = new NamedProperty(
			"Ignore Tacklezones when Catching");
	public static final ISkillProperty ignoreTacklezonesWhenDodging = new NamedProperty("Ignore Tacklezones When Dodging");
	public static final ISkillProperty ignoreTacklezonesWhenMoving = new NamedProperty("Ignore Tacklezones When Moving");
	public static final ISkillProperty ignoreTacklezonesWhenPassing = new NamedProperty(
			"Ignore Tacklezones when Passing");
	public static final ISkillProperty ignoreTacklezonesWhenPickingUp = new NamedProperty(
			"Ignore Tacklezones When Picking Up");
	public static final ISkillProperty ignoreWeatherWhenPickingUp = new NamedProperty("Ignore Weather when Picking Up");
	public static final ISkillProperty increasesTeamsFame = new NamedProperty("Increases Teams Fame");
	public static final ISkillProperty inflictsConfusion = new NamedProperty("Inflicts Confusion");
	public static final ISkillProperty inflictsDisturbingPresence = new NamedProperty("Inflicts Disturbing Presence");
	public static final ISkillProperty isHurtMoreEasily = new NamedProperty("Is Hurt More Easily");
	public static final ISkillProperty makesDodgingHarder = new NamedProperty("Makes Dodging Harder");
	public static final ISkillProperty mightEatPlayerToThrow = new NamedProperty("Might Eat Player To Throw");
	public static final ISkillProperty movesRandomly = new NamedProperty("Moves Randomly");
	public static final ISkillProperty needsToRollForActionButKeepsTacklezone = new NamedProperty("Needs To Roll For Action But Keeps Tacklezone");
	public static final ISkillProperty needsToRollHighToAvoidConfusion = new NamedProperty("Need To Roll High To Avoid Confusion");
	public static final ISkillProperty placedProneCausesInjuryRoll = new NamedProperty("Placed Prone Causes Injury Roll");
	public static final ISkillProperty preventAutoMove = new NamedProperty("Prevent AutoMove");
	public static final ISkillProperty preventCardRabbitsFoot = new NamedProperty("Prevent Rabbit's Foot Card");
	public static final ISkillProperty preventCatch = new NamedProperty("Prevent Catch");
	public static final ISkillProperty preventDamagingInjuryModifications = new NamedProperty("PreventDamagingInjuryModifications");
	public static final ISkillProperty preventFallOnBothDown = new NamedProperty("Prevent Fall on Both Down");
	public static final ISkillProperty preventHoldBall = new NamedProperty("Prevent Hold Ball");
	public static final ISkillProperty preventKickTeamMateAction = new NamedProperty("Prevent Kick Team Mate Action");
	public static final ISkillProperty preventOpponentFollowingUp = new NamedProperty("Prevent Opponent Following Up");
	public static final ISkillProperty preventRaiseFromDead = new NamedProperty("Prevent Raise From Dead");
	public static final ISkillProperty preventRecoverFromConcusionAction = new NamedProperty(
			"Prevent Recover from Confusion Action");
	public static final ISkillProperty preventRecoverFromGazeAction = new NamedProperty(
			"Prevent Recover from Gaze Aztion");
	public static final ISkillProperty preventRegularBlitzAction = new NamedProperty("Prevent Regular Blitz Action");
	public static final ISkillProperty preventRegularBlockAction = new NamedProperty("Prevent Regular Block Action");
	public static final ISkillProperty preventRegularFoulAction = new NamedProperty("Prevent Regular Foul Action");
	public static final ISkillProperty preventRegularHandOverAction = new NamedProperty(
			"Prevent Regular Hand Over Action");
	public static final ISkillProperty preventRegularPassAction = new NamedProperty("Prevent Regular Pass Action");
	public static final ISkillProperty preventStandUpAction = new NamedProperty("Prevent Regular Stand Up Action");
	public static final ISkillProperty preventStuntyDodgeModifier = new NamedProperty("Prevent Stunty Dodge Modifier");
	public static final ISkillProperty preventThrowTeamMateAction = new NamedProperty("Prevent Throw Team Mate Action");
	public static final ISkillProperty reducesArmourToFixedValue = new NamedProperty("Reduces Armour To Fixed Value");
	public static final ISkillProperty requiresSecondCasualtyRoll = new NamedProperty("Requires Second Casualty Roll");
	public static final ISkillProperty smallIcon = new NamedProperty("Display with a small icon");
	public static final ISkillProperty ttmScattersInSingleDirection = new NamedProperty(
			"Throw Team Mate Scatters In Single Direction");
	public static final ISkillProperty useSpecialBlockRules = new NamedProperty("Use Special Block Rules");
}
