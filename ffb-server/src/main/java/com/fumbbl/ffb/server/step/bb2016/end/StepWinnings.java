package com.fumbbl.ffb.server.step.bb2016.end;

import com.fumbbl.ffb.ReRolledActions;
import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.dialog.DialogWinningsReRollParameter;
import com.fumbbl.ffb.model.Game;
import com.fumbbl.ffb.model.GameResult;
import com.fumbbl.ffb.report.bb2016.ReportWinningsRoll;
import com.fumbbl.ffb.server.GameState;
import com.fumbbl.ffb.server.net.ReceivedCommand;
import com.fumbbl.ffb.server.step.AbstractStepWithReRoll;
import com.fumbbl.ffb.server.step.StepAction;
import com.fumbbl.ffb.server.step.StepCommandStatus;
import com.fumbbl.ffb.server.step.StepId;
import com.fumbbl.ffb.server.util.UtilServerDialog;

/**
 * Step in end game sequence to roll winnings.
 * 
 * Needs to be initialized with stepParameter ADMIN_MODE.
 * 
 * @author Kalimar
 */
@RulesCollection(RulesCollection.Rules.BB2016)
public final class StepWinnings extends AbstractStepWithReRoll {

	public StepWinnings(GameState pGameState) {
		super(pGameState);
	}

	public StepId getId() {
		return StepId.WINNINGS;
	}

	@Override
	public void start() {
		super.start();
		executeStep();
	}

	@Override
	public StepCommandStatus handleCommand(ReceivedCommand pReceivedCommand) {
		StepCommandStatus commandStatus = super.handleCommand(pReceivedCommand);
		if (commandStatus == StepCommandStatus.EXECUTE_STEP) {
			executeStep();
		}
		return commandStatus;
	}

	private void executeStep() {
		UtilServerDialog.hideDialog(getGameState());
		Game game = getGameState().getGame();
		if ((getReRolledAction() == null)
				|| ((getReRolledAction() == ReRolledActions.WINNINGS) && (getReRollSource() != null))) {
			ReportWinningsRoll reportWinnings = rollWinnings();
			if (game.isAdminMode()) {
				// roll winnings, reroll on a 1 or 2 -->
				GameResult gameResult = game.getGameResult();
				int scoreDiffHome = gameResult.getTeamResultHome().getScore() - gameResult.getTeamResultAway().getScore();
				if (((scoreDiffHome > 0) && (reportWinnings.getWinningsRollHome() < 3))
						|| ((scoreDiffHome < 0) && (reportWinnings.getWinningsRollAway() < 3))) {
					reportWinnings = rollWinnings();
				}
				// <--
				UtilServerDialog.hideDialog(getGameState());
			}
			getResult().addReport(reportWinnings);
		}
		if (game.getDialogParameter() == null) {
			getResult().addReport(concedeWinnings());
			getResult().setNextAction(StepAction.NEXT_STEP);
		}
	}

	private ReportWinningsRoll rollWinnings() {
		Game game = getGameState().getGame();
		GameResult gameResult = game.getGameResult();
		int scoreDiffHome = gameResult.getTeamResultHome().getScore() - gameResult.getTeamResultAway().getScore();
		int winningsHome = 0;
		int rollHome = 0;
		if ((getReRolledAction() == null) || (scoreDiffHome > 0)) {
			rollHome = getGameState().getDiceRoller().rollWinnings();
			winningsHome = rollHome + gameResult.getTeamResultHome().getFame();
			if (scoreDiffHome >= 0) {
				winningsHome++;
			}
			gameResult.getTeamResultHome().setWinnings(winningsHome * 10000);
		}
		int winningsAway = 0;
		int rollAway = 0;
		if ((getReRolledAction() == null) || (scoreDiffHome < 0)) {
			rollAway = getGameState().getDiceRoller().rollWinnings();
			winningsAway = rollAway + gameResult.getTeamResultAway().getFame();
			if (scoreDiffHome <= 0) {
				winningsAway++;
			}
			gameResult.getTeamResultAway().setWinnings(winningsAway * 10000);
		}
		if (getReRolledAction() == null) {
			if (scoreDiffHome > 0) {
				UtilServerDialog.showDialog(getGameState(),
						new DialogWinningsReRollParameter(game.getTeamHome().getId(), rollHome), false);
			}
			if (scoreDiffHome < 0) {
				UtilServerDialog.showDialog(getGameState(),
						new DialogWinningsReRollParameter(game.getTeamAway().getId(), rollAway), false);
			}
		}
		return new ReportWinningsRoll(rollHome, gameResult.getTeamResultHome().getWinnings(), rollAway,
				gameResult.getTeamResultAway().getWinnings());
	}

	private ReportWinningsRoll concedeWinnings() {
		ReportWinningsRoll report = null;
		Game game = getGameState().getGame();
		GameResult gameResult = game.getGameResult();
		if (gameResult.getTeamResultHome().hasConceded()
			&& !game.isConcededLegally()) {
			gameResult.getTeamResultAway()
				.setWinnings(gameResult.getTeamResultAway().getWinnings() + gameResult.getTeamResultHome().getWinnings());
			gameResult.getTeamResultHome().setWinnings(0);
			report = new ReportWinningsRoll(0, gameResult.getTeamResultHome().getWinnings(), 0,
				gameResult.getTeamResultAway().getWinnings());
		}
		if (gameResult.getTeamResultAway().hasConceded()
			&& !game.isConcededLegally()) {
			gameResult.getTeamResultHome()
				.setWinnings(gameResult.getTeamResultHome().getWinnings() + gameResult.getTeamResultAway().getWinnings());
			gameResult.getTeamResultAway().setWinnings(0);
			report = new ReportWinningsRoll(0, gameResult.getTeamResultHome().getWinnings(), 0,
				gameResult.getTeamResultAway().getWinnings());
		}
		return report;
	}

}
