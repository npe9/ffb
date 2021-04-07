package com.balancedbytes.games.ffb.server.skillbehaviour.bb2020;

import com.balancedbytes.games.ffb.ReRolledAction;
import com.balancedbytes.games.ffb.SoundId;
import com.balancedbytes.games.ffb.dialog.DialogReRollForTargetsParameter;
import com.balancedbytes.games.ffb.model.ActingPlayer;
import com.balancedbytes.games.ffb.model.Game;
import com.balancedbytes.games.ffb.model.Player;
import com.balancedbytes.games.ffb.net.commands.ClientCommandUseSkill;
import com.balancedbytes.games.ffb.report.IReport;
import com.balancedbytes.games.ffb.server.DiceInterpreter;
import com.balancedbytes.games.ffb.server.model.StepModifier;
import com.balancedbytes.games.ffb.server.step.IStep;
import com.balancedbytes.games.ffb.server.step.StepAction;
import com.balancedbytes.games.ffb.server.step.StepCommandStatus;
import com.balancedbytes.games.ffb.server.step.StepParameter;
import com.balancedbytes.games.ffb.server.step.StepParameterKey;
import com.balancedbytes.games.ffb.server.util.UtilServerDialog;
import com.balancedbytes.games.ffb.server.util.UtilServerReRoll;
import com.balancedbytes.games.ffb.util.StringTool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractStepModifierMultipleRolls<T extends IStep, V extends StepStateMultipleRolls> extends StepModifier<T, V> {

	@Override
	public StepCommandStatus handleCommandHook(T step, V state, ClientCommandUseSkill useSkillCommand) {
		return StepCommandStatus.EXECUTE_STEP;
	}

	@Override
	public boolean handleExecuteStepHook(T step, V state) {
		Game game = step.getGameState().getGame();
		ActingPlayer actingPlayer = game.getActingPlayer();
		actingPlayer.setHasBlocked(true);

		if (canBeSkipped(actingPlayer.getPlayer())) {
			step.getResult().setNextAction(StepAction.NEXT_STEP);
			return false;
		}

		if (state.firstRun) {
			state.firstRun = false;

			state.blockTargets = state.blockTargets.stream().map(game::getPlayerById)
				.filter(this::requiresRoll)
				.map(Player::getId).collect(Collectors.toList());

			for (String targetId: new ArrayList<>(state.blockTargets)) {
				roll(step, actingPlayer, state.blockTargets, targetId, false, state.minimumRolls);
			}
			state.reRollAvailableAgainst.addAll(state.blockTargets);
			decideNextStep(game, step, state);

		} else {
			if (!StringTool.isProvided(state.reRollTarget) || state.reRollSource == null) {
				step.getResult().setNextAction(StepAction.NEXT_STEP);
			} else {
				if (UtilServerReRoll.useReRoll(step, state.reRollSource, actingPlayer.getPlayer())) {
					roll(step, actingPlayer, state.blockTargets, state.reRollTarget, true, state.minimumRolls);
				}
				state.reRollAvailableAgainst.remove(state.reRollTarget);
				decideNextStep(game, step, state);
			}
		}
		return false;
	}

	protected abstract ReRolledAction reRolledAction();

	protected abstract boolean requiresRoll(Player<?> opponentPlayer);

	protected abstract boolean canBeSkipped(Player<?> actingPlayer);

	protected abstract int skillRoll(T step);

	protected abstract int minimumRoll();

	protected abstract IReport report(String playerId, boolean mayBlock, int actualRoll, int minimumRoll, boolean reRolling, String currentTargetId);

	private void decideNextStep(Game game, T step, V state) {
		if (state.blockTargets.isEmpty()) {
			step.getResult().setNextAction(StepAction.NEXT_STEP);
		} else {
			state.teamReRollAvailable = UtilServerReRoll.isTeamReRollAvailable(step.getGameState(), game.getActingPlayer().getPlayer());
			state.proReRollAvailable = UtilServerReRoll.isProReRollAvailable(game.getActingPlayer().getPlayer(), game);
			if (state.reRollAvailableAgainst.isEmpty() || (!state.teamReRollAvailable && !state.proReRollAvailable)) {
				if (state.blockTargets.size() == 1) {
					step.publishParameter(new StepParameter(StepParameterKey.PLAYER_ID_TO_REMOVE, state.blockTargets.get(0)));
					step.getResult().setNextAction(StepAction.NEXT_STEP);
				} else {
					step.getResult().setNextAction(StepAction.GOTO_LABEL, state.goToLabelOnFailure);
				}
			} else {
				UtilServerDialog.showDialog(step.getGameState(), createDialogParameter(game.getActingPlayer().getPlayer(), state), false);
			}
		}
	}

	private void roll(T step, ActingPlayer actingPlayer, List<String> targets, String currentTargetId, boolean reRolling, Map<String, Integer> minimumRolls) {
		int actualRoll = skillRoll(step);
		int minimumRoll = minimumRoll();
		boolean mayBlock = DiceInterpreter.getInstance().isSkillRollSuccessful(actualRoll, minimumRoll);
		minimumRolls.put(currentTargetId, minimumRoll);
		step.getResult().addReport(report(actingPlayer.getPlayerId(), mayBlock, actualRoll, minimumRoll, reRolling, currentTargetId));
		if (mayBlock) {
			targets.remove(currentTargetId);
		} else if (!reRolling) {
			step.getResult().setSound(SoundId.EW);
		}
	}

	private DialogReRollForTargetsParameter createDialogParameter(Player<?> player, V state) {
		return new DialogReRollForTargetsParameter(player.getId(), state.blockTargets, reRolledAction(),
			state.minimumRolls, state.reRollAvailableAgainst, state.proReRollAvailable, state.teamReRollAvailable);
	}
}
