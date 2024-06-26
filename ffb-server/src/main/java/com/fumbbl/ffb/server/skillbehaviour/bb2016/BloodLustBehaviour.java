package com.fumbbl.ffb.server.skillbehaviour.bb2016;

import com.fumbbl.ffb.ReRolledActions;
import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.RulesCollection.Rules;
import com.fumbbl.ffb.model.ActingPlayer;
import com.fumbbl.ffb.model.Game;
import com.fumbbl.ffb.net.commands.ClientCommandUseSkill;
import com.fumbbl.ffb.report.ReportBloodLustRoll;
import com.fumbbl.ffb.server.ActionStatus;
import com.fumbbl.ffb.server.DiceInterpreter;
import com.fumbbl.ffb.server.model.SkillBehaviour;
import com.fumbbl.ffb.server.model.StepModifier;
import com.fumbbl.ffb.server.step.StepAction;
import com.fumbbl.ffb.server.step.StepCommandStatus;
import com.fumbbl.ffb.server.step.StepParameter;
import com.fumbbl.ffb.server.step.StepParameterKey;
import com.fumbbl.ffb.server.step.bb2016.StepBloodLust;
import com.fumbbl.ffb.server.step.bb2016.StepBloodLust.StepState;
import com.fumbbl.ffb.server.util.UtilServerReRoll;
import com.fumbbl.ffb.skill.bb2016.BloodLust;
import com.fumbbl.ffb.util.StringTool;
import com.fumbbl.ffb.util.UtilCards;

@RulesCollection(Rules.BB2016)
public class BloodLustBehaviour extends SkillBehaviour<BloodLust> {
	public BloodLustBehaviour() {
		super();

		registerModifier(new StepModifier<StepBloodLust, StepBloodLust.StepState>() {

			@Override
			public StepCommandStatus handleCommandHook(StepBloodLust step, StepState state,
					ClientCommandUseSkill useSkillCommand) {
				return StepCommandStatus.EXECUTE_STEP;
			}

			@Override
			public boolean handleExecuteStepHook(StepBloodLust step, StepState state) {
				ActionStatus status = ActionStatus.SUCCESS;
				Game game = step.getGameState().getGame();
				if (!game.getTurnMode().checkNegatraits()) {
					step.getResult().setNextAction(StepAction.NEXT_STEP);
					return false;
				}
				ActingPlayer actingPlayer = game.getActingPlayer();
				boolean doRoll = true;
				if (ReRolledActions.BLOOD_LUST == step.getReRolledAction()) {
					if ((step.getReRollSource() == null)
							|| !UtilServerReRoll.useReRoll(step, step.getReRollSource(), actingPlayer.getPlayer())) {
						doRoll = false;
						status = ActionStatus.FAILURE;
						actingPlayer.setSufferingBloodLust(true);
					}
				} else {
					doRoll = UtilCards.hasUnusedSkill(actingPlayer, skill);
				}
				if (doRoll) {
					int roll = step.getGameState().getDiceRoller().rollSkill();
					int minimumRoll = DiceInterpreter.getInstance().minimumRollBloodLust();
					boolean successful = DiceInterpreter.getInstance().isSkillRollSuccessful(roll, minimumRoll);
					actingPlayer.markSkillUsed(skill);
					if (!successful) {
						status = ActionStatus.FAILURE;
						if ((ReRolledActions.BLOOD_LUST != step.getReRolledAction()) && UtilServerReRoll.askForReRollIfAvailable(
								step.getGameState(), actingPlayer.getPlayer(), ReRolledActions.BLOOD_LUST, minimumRoll, false)) {
							status = ActionStatus.WAITING_FOR_RE_ROLL;
						} else {
							actingPlayer.setSufferingBloodLust(true);
						}
					}
					boolean reRolled = ((ReRolledActions.BLOOD_LUST == step.getReRolledAction())
							&& (step.getReRollSource() != null));
					step.getResult().addReport(new ReportBloodLustRoll(actingPlayer.getPlayerId(),
							successful, roll, minimumRoll, reRolled, null));
				}
				if (status == ActionStatus.SUCCESS) {
					step.getResult().setNextAction(StepAction.NEXT_STEP);
				}
				if (status == ActionStatus.FAILURE) {
					step.publishParameter(new StepParameter(StepParameterKey.MOVE_STACK, null));
					if (StringTool.isProvided(state.goToLabelOnFailure)) {
						step.getResult().setNextAction(StepAction.GOTO_LABEL, state.goToLabelOnFailure);
					} else {
						step.getResult().setNextAction(StepAction.NEXT_STEP);
					}
				}
				return false;
			}
		});
	}
}