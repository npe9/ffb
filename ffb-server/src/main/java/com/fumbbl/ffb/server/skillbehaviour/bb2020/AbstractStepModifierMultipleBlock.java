package com.fumbbl.ffb.server.skillbehaviour.bb2020;

import com.fumbbl.ffb.PlayerState;
import com.fumbbl.ffb.ReRollSource;
import com.fumbbl.ffb.ReRollSources;
import com.fumbbl.ffb.ReRolledAction;
import com.fumbbl.ffb.dialog.DialogReRollForTargetsParameter;
import com.fumbbl.ffb.model.ActingPlayer;
import com.fumbbl.ffb.model.Game;
import com.fumbbl.ffb.model.Player;
import com.fumbbl.ffb.model.property.NamedProperties;
import com.fumbbl.ffb.model.skill.Skill;
import com.fumbbl.ffb.net.commands.ClientCommandUseSkill;
import com.fumbbl.ffb.report.IReport;
import com.fumbbl.ffb.report.ReportReRoll;
import com.fumbbl.ffb.server.DiceInterpreter;
import com.fumbbl.ffb.server.model.StepModifier;
import com.fumbbl.ffb.server.step.IStep;
import com.fumbbl.ffb.server.step.StepAction;
import com.fumbbl.ffb.server.step.StepCommandStatus;
import com.fumbbl.ffb.server.util.UtilServerDialog;
import com.fumbbl.ffb.server.util.UtilServerReRoll;
import com.fumbbl.ffb.util.StringTool;
import com.fumbbl.ffb.util.UtilCards;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractStepModifierMultipleBlock<T extends IStep, V extends StepStateMultipleRolls> extends StepModifier<T, V> {

	@Override
	public StepCommandStatus handleCommandHook(T step, V state, ClientCommandUseSkill useSkillCommand) {
		return StepCommandStatus.EXECUTE_STEP;
	}

	@Override
	public boolean handleExecuteStepHook(T step, V state) {
		Game game = step.getGameState().getGame();
		ActingPlayer actingPlayer = game.getActingPlayer();

		if (canBeSkipped(actingPlayer.getPlayer())) {
			step.getResult().setNextAction(StepAction.NEXT_STEP);
			return false;
		}

		if (state.firstRun) {
			state.firstRun = false;
			state.initialCount = state.blockTargets.size();
			state.blockTargets = state.blockTargets.stream().map(game::getPlayerById)
				.filter(opponentPlayer -> requiresRoll(actingPlayer.getPlayer(), opponentPlayer))
				.map(Player::getId).collect(Collectors.toList());

			if (!state.blockTargets.isEmpty()) {
				actingPlayer.setHasBlocked(true);
			}

			for (String targetId : new ArrayList<>(state.blockTargets)) {
				roll(step, actingPlayer, targetId, false, state.minimumRolls, state);
			}
			state.reRollAvailableAgainst.addAll(state.blockTargets);
			decideNextStep(game, step, state);

		} else {
			if (!StringTool.isProvided(state.reRollTarget) || state.reRollSource == null) {
				nextStep(step, state);
			} else {
				if (UtilServerReRoll.useReRoll(step, state.reRollSource, actingPlayer.getPlayer())) {
					roll(step, actingPlayer, state.reRollTarget,true, state.minimumRolls, state);
				}
				state.reRollAvailableAgainst.remove(state.reRollTarget);
				decideNextStep(game, step, state);
			}
		}
		return false;
	}

	protected abstract ReRolledAction reRolledAction();

	protected abstract boolean requiresRoll(Player<?> actingPlayer, Player<?> opponentPlayer);

	protected abstract boolean canBeSkipped(Player<?> actingPlayer);

	protected abstract int skillRoll(T step);

	protected abstract int minimumRoll(Game game, Player<?> actingPlayer, Player<?> opponentPlayer);

	protected abstract IReport report(Game game, String playerId, boolean mayBlock, int actualRoll, int minimumRoll, boolean reRolling, String currentTargetId);

	protected abstract void unhandledTargetsCallback(T step, V state);

	protected abstract void cleanUp(T step, V state);

	private Optional<Skill> reRollSkill(ActingPlayer actingPlayer, Game game) {
		return Optional.ofNullable(UtilCards.getUnusedRerollSource(actingPlayer, reRolledAction())).map(reRollSource -> reRollSource.getSkill(game));
	}

	private void nextStep(T step, V state) {
		if (StringTool.isProvided(state.goToLabelOnFailure) && state.blockTargets.size() == state.initialCount) {
			step.getResult().setNextAction(StepAction.GOTO_LABEL, state.goToLabelOnFailure);
			state.blockTargets.forEach(target -> {
					Game game = step.getGameState().getGame();
					Player<?> player = game.getPlayerById(target);
					PlayerState playerState = game.getFieldModel().getPlayerState(player);
					game.getFieldModel().setPlayerState(player, playerState.changeSelectedStabTarget(false).changeSelectedBlockTarget(false));
				}
			);
		} else {
			unhandledTargetsCallback(step, state);
			step.getResult().setNextAction(StepAction.NEXT_STEP);
		}
		cleanUp(step, state);
	}

	private void decideNextStep(Game game, T step, V state) {
		if (state.blockTargets.isEmpty()) {
			step.getResult().setNextAction(StepAction.NEXT_STEP);
		} else {
			ActingPlayer actingPlayer = game.getActingPlayer();
			state.teamReRollAvailable = UtilServerReRoll.isTeamReRollAvailable(step.getGameState(), actingPlayer.getPlayer());
			state.proReRollAvailable = UtilServerReRoll.isProReRollAvailable(actingPlayer.getPlayer(), game, null);
			state.consummateAvailable = UtilCards.hasUnusedSkillWithProperty(actingPlayer, NamedProperties.canRerollSingleDieOncePerPeriod);
			if (UtilServerReRoll.isSingleUseReRollAvailable(step.getGameState(), actingPlayer.getPlayer())) {
				state.singleUseReRollSource = ReRollSources.LORD_OF_CHAOS;
			}
			Optional<Skill> reRollSkill = reRollSkill(actingPlayer, game);
			if (state.reRollAvailableAgainst.isEmpty() ||
				(!state.teamReRollAvailable && !state.proReRollAvailable && !reRollSkill.isPresent() && state.singleUseReRollSource == null)) {
				nextStep(step, state);
			} else {
				state.reRollTarget = null;
				state.reRollSource = null;
				UtilServerDialog.showDialog(step.getGameState(), createDialogParameter(actingPlayer.getPlayer(), state, reRollSkill.orElse(null)), false);
			}
		}
	}

	private void roll(T step, ActingPlayer actingPlayer, String currentTargetId, boolean reRolling, Map<String, Integer> minimumRolls, V state) {
		Game game = step.getGameState().getGame();
		Player<?> defender = game.getPlayerById(currentTargetId);
		int actualRoll = skillRoll(step);
		int minimumRoll = minimumRoll(game, actingPlayer.getPlayer(), defender);
		boolean successful = DiceInterpreter.getInstance().isSkillRollSuccessful(actualRoll, minimumRoll);
		minimumRolls.put(currentTargetId, minimumRoll);

		if (!successful) {
			ReRollSource reRollSource = UtilCards.getUnusedRerollSource(actingPlayer, reRolledAction());

			if (reRollSource != null) {
				step.getResult().addReport(report(step.getGameState().getGame(), actingPlayer.getPlayerId(), false, actualRoll, minimumRoll, reRolling, currentTargetId));

				actualRoll = skillRoll(step);
				successful = (actualRoll >= minimumRoll);
				state.blockTargets.remove(currentTargetId);
				step.getResult().addReport(new ReportReRoll(actingPlayer.getPlayerId(), reRollSource, successful, minimumRoll));
			}
		}

		step.getResult().addReport(report(step.getGameState().getGame(), actingPlayer.getPlayerId(), successful, actualRoll, minimumRoll, reRolling, currentTargetId));
		if (successful) {
			state.blockTargets.remove(currentTargetId);
			successFulRollCallback(step, currentTargetId);
		} else if (!reRolling) {
			failedRollEffect(step);
		}
	}

	protected abstract void successFulRollCallback(T step, String successfulId);

	protected abstract void failedRollEffect(T step);

	private DialogReRollForTargetsParameter createDialogParameter(Player<?> player, V state, Skill skill) {
		return new DialogReRollForTargetsParameter(player.getId(), state.blockTargets, reRolledAction(),
			state.minimumRolls, state.reRollAvailableAgainst, state.proReRollAvailable, state.teamReRollAvailable,
			skill, state.singleUseReRollSource, state.consummateAvailable);
	}
}
