package com.balancedbytes.games.ffb.server.step.action.block;

import java.util.Stack;

import com.balancedbytes.games.ffb.CatchScatterThrowInMode;
import com.balancedbytes.games.ffb.FieldCoordinate;
import com.balancedbytes.games.ffb.FieldCoordinateBounds;
import com.balancedbytes.games.ffb.InjuryType;
import com.balancedbytes.games.ffb.PlayerAction;
import com.balancedbytes.games.ffb.PlayerState;
import com.balancedbytes.games.ffb.Pushback;
import com.balancedbytes.games.ffb.PushbackMode;
import com.balancedbytes.games.ffb.PushbackSquare;
import com.balancedbytes.games.ffb.Skill;
import com.balancedbytes.games.ffb.SkillUse;
import com.balancedbytes.games.ffb.bytearray.ByteArray;
import com.balancedbytes.games.ffb.bytearray.ByteList;
import com.balancedbytes.games.ffb.dialog.DialogSkillUseParameter;
import com.balancedbytes.games.ffb.model.ActingPlayer;
import com.balancedbytes.games.ffb.model.FieldModel;
import com.balancedbytes.games.ffb.model.Game;
import com.balancedbytes.games.ffb.model.Player;
import com.balancedbytes.games.ffb.net.NetCommand;
import com.balancedbytes.games.ffb.net.commands.ClientCommandPushback;
import com.balancedbytes.games.ffb.net.commands.ClientCommandUseSkill;
import com.balancedbytes.games.ffb.report.ReportPushback;
import com.balancedbytes.games.ffb.report.ReportSkillUse;
import com.balancedbytes.games.ffb.server.GameState;
import com.balancedbytes.games.ffb.server.step.AbstractStep;
import com.balancedbytes.games.ffb.server.step.StepAction;
import com.balancedbytes.games.ffb.server.step.StepCommandStatus;
import com.balancedbytes.games.ffb.server.step.StepId;
import com.balancedbytes.games.ffb.server.step.StepParameter;
import com.balancedbytes.games.ffb.server.step.StepParameterKey;
import com.balancedbytes.games.ffb.server.step.action.common.ApothecaryMode;
import com.balancedbytes.games.ffb.server.util.UtilCommand;
import com.balancedbytes.games.ffb.server.util.UtilDialog;
import com.balancedbytes.games.ffb.server.util.UtilInjury;
import com.balancedbytes.games.ffb.server.util.UtilPlayerMove;
import com.balancedbytes.games.ffb.server.util.UtilPushback;
import com.balancedbytes.games.ffb.server.util.UtilTimer;
import com.balancedbytes.games.ffb.util.ArrayTool;
import com.balancedbytes.games.ffb.util.UtilCards;

/**
 * Step in block sequence to handle pushbacks.
 * 
 * Expects stepParameter STARTING_PUSHBACK_SQUARE to be set by a preceding step.
 * Expects stepParameter OLD_DEFENDER_STATE to be set by a preceding step.
 * 
 * Sets stepParameter CATCH_SCATTER_THROWIN_MODE for all steps on the stack.
 * Sets stepParameter DEFENDER_PUSHED for all steps on the stack. Sets
 * stepParameter FOLLOWUP_CHOICE for all steps on the stack. Sets stepParameter
 * STARTING_PUSHBACK_SQUARE for all steps on the stack. Sets stepParameter
 * INJURY_RESULT for all steps on the stack.
 * 
 * @author Kalimar
 */
public class StepPushback extends AbstractStep {

	private PlayerState fOldDefenderState;
	private PushbackSquare fStartingPushbackSquare;
	private Boolean fUsingGrab;
	private Boolean fUsingSideStep;
	private Boolean fUsingStandFirm;

	private Stack<Pushback> fPushbackStack;

	public StepPushback(GameState pGameState) {
		super(pGameState);
		fPushbackStack = new Stack<Pushback>();
	}

	public StepId getId() {
		return StepId.PUSHBACK;
	}

	@Override
	public void start() {
		super.start();
		executeStep();
	}

	@Override
	public StepCommandStatus handleNetCommand(NetCommand pNetCommand) {
		StepCommandStatus commandStatus = super.handleNetCommand(pNetCommand);
		if (commandStatus == StepCommandStatus.UNHANDLED_COMMAND) {
			switch (pNetCommand.getId()) {
  			case CLIENT_USE_SKILL:
  				ClientCommandUseSkill useSkillCommand = (ClientCommandUseSkill) pNetCommand;
  				switch (useSkillCommand.getSkill()) {
    				case STAND_FIRM:
    					fUsingStandFirm = useSkillCommand.isSkillUsed();
    					commandStatus = StepCommandStatus.EXECUTE_STEP;
    					break;
    				case SIDE_STEP:
    					fUsingSideStep = useSkillCommand.isSkillUsed();
    					commandStatus = StepCommandStatus.EXECUTE_STEP;
    					break;
    				case GRAB:
    					fUsingGrab = useSkillCommand.isSkillUsed();
    					commandStatus = StepCommandStatus.EXECUTE_STEP;
    					break;
  					default:
  						break;
  				}
  				break;
  			case CLIENT_PUSHBACK:
  				ClientCommandPushback pushbackCommand = (ClientCommandPushback) pNetCommand;
  				if (UtilCommand.isHomeCommand(getGameState().getServer(), pushbackCommand)) {
  					fPushbackStack.push(pushbackCommand.getPushback());
  				} else {
  					fPushbackStack.push(pushbackCommand.getPushback().transform());
  				}
  				commandStatus = StepCommandStatus.EXECUTE_STEP;
  				break;
  			default:
  				break;
			}
		}
		if (commandStatus == StepCommandStatus.EXECUTE_STEP) {
			executeStep();
		}
		return commandStatus;
	}

	@Override
	public boolean setParameter(StepParameter pParameter) {
		if ((pParameter != null) && !super.setParameter(pParameter)) {
			switch (pParameter.getKey()) {
  			case OLD_DEFENDER_STATE:
  				fOldDefenderState = (PlayerState) pParameter.getValue();
  				return true;
  			case STARTING_PUSHBACK_SQUARE:
  				fStartingPushbackSquare = (PushbackSquare) pParameter.getValue();
  				return true;
				default:
					break;
			}
		}
		return false;
	}

	private void executeStep() {
		boolean doPush = false;
		UtilDialog.hideDialog(getGameState());
		Game game = getGameState().getGame();
		ActingPlayer actingPlayer = game.getActingPlayer();
		FieldModel fieldModel = game.getFieldModel();
		// player chose a coordinate
		if (fPushbackStack.size() > 0) {
			Pushback lastPushback = fPushbackStack.pop();
			fPushbackStack.push(lastPushback);
			PushbackSquare[] pushbackSquares = fieldModel.getPushbackSquares();
			for (int i = 0; i < pushbackSquares.length; i++) {
				if (!pushbackSquares[i].isLocked()) {
					fieldModel.remove(pushbackSquares[i]);
					if (pushbackSquares[i].getCoordinate().equals(lastPushback.getCoordinate())) {
						publishParameter(new StepParameter(StepParameterKey.STARTING_PUSHBACK_SQUARE, pushbackSquares[i]));
						pushbackSquares[i].setSelected(true);
						pushbackSquares[i].setLocked(true);
						fieldModel.add(pushbackSquares[i]);
					}
				}
			}
			doPush = (fieldModel.getPlayer(lastPushback.getCoordinate()) == null);
		}
		// calculate new pushback squares
		if (!doPush && (fStartingPushbackSquare != null)) {

			FieldCoordinate attackerCoordinate = game.getFieldModel().getPlayerCoordinate(actingPlayer.getPlayer());
			FieldCoordinate defenderCoordinate = fStartingPushbackSquare.getCoordinate();
			Player defender = fieldModel.getPlayer(defenderCoordinate);
			if (defender == null) {
				throw new IllegalStateException("Defender unknown at this point - cannot continue.");
			}
			PushbackMode pushbackMode = PushbackMode.REGULAR;
			PushbackSquare[] pushbackSquares = UtilPushback.findPushbackSquares(game, fStartingPushbackSquare, pushbackMode);
			fieldModel.add(pushbackSquares);
			boolean freeSquareAroundDefender = false;
			FieldCoordinate[] adjacentSquares = fieldModel.findAdjacentCoordinates(fStartingPushbackSquare.getCoordinate(), FieldCoordinateBounds.FIELD, 1, false);
			for (int i = 0; !freeSquareAroundDefender && (i < adjacentSquares.length); i++) {
				if (fieldModel.getPlayer(adjacentSquares[i]) == null) {
					freeSquareAroundDefender = true;
				}
			}

			// handle auto-stand firm
			PlayerState playerState = game.getFieldModel().getPlayerState(defender);
			if (playerState.isRooted()) {
				fUsingStandFirm = true;
			} else if (playerState.isProne() || ((fOldDefenderState != null) && fOldDefenderState.isProne())) {
				fUsingStandFirm = false;
			} else if ((PlayerAction.BLITZ == actingPlayer.getPlayerAction()) && UtilCards.hasSkill(game, actingPlayer, Skill.JUGGERNAUT)
					&& game.getFieldModel().getPlayerCoordinate(actingPlayer.getPlayer()).isAdjacent(game.getFieldModel().getPlayerCoordinate(defender))) {
				fUsingStandFirm = false;
				getResult().addReport(new ReportSkillUse(actingPlayer.getPlayerId(), Skill.JUGGERNAUT, true, SkillUse.CANCEL_STAND_FIRM));
			}

			// handle stand firm
			if (UtilCards.hasSkill(game, defender, Skill.STAND_FIRM) && ((fUsingStandFirm == null) || fUsingStandFirm)) {
				if (fUsingStandFirm == null) {
					UtilDialog.showDialog(getGameState(), new DialogSkillUseParameter(defender.getId(), Skill.STAND_FIRM, 0));
				}
				if (fUsingStandFirm != null) {
					if (fUsingStandFirm) {
						doPush = true;
						fPushbackStack.clear();
						publishParameter(new StepParameter(StepParameterKey.STARTING_PUSHBACK_SQUARE, null));
						publishParameter(new StepParameter(StepParameterKey.FOLLOWUP_CHOICE, false));
						getResult().addReport(new ReportSkillUse(defender.getId(), Skill.STAND_FIRM, true, SkillUse.AVOID_PUSH));
					} else {
						getResult().addReport(new ReportSkillUse(defender.getId(), Skill.STAND_FIRM, false, null));
					}

				}

				// handle side step
			} else if (((fUsingSideStep == null) || fUsingSideStep)
				&& freeSquareAroundDefender
				&& UtilCards.hasSkill(game, defender, Skill.SIDE_STEP)
				&& !(UtilCards.hasSkill(game, actingPlayer, Skill.GRAB)
				&& game.getFieldModel().getPlayerCoordinate(actingPlayer.getPlayer()).isAdjacent(game.getFieldModel().getPlayerCoordinate(defender)))
				&& !(playerState.getBase() == PlayerState.PRONE || playerState.getBase() == PlayerState.STUNNED)) {
				if (fUsingSideStep == null) {
					UtilDialog.showDialog(getGameState(), new DialogSkillUseParameter(defender.getId(), Skill.SIDE_STEP, 0));
				} else {
					if (fUsingSideStep) {
						pushbackMode = PushbackMode.SIDE_STEP;
						for (int i = 0; i < pushbackSquares.length; i++) {
							if (!pushbackSquares[i].isSelected()) {
								fieldModel.remove(pushbackSquares[i]);
							}
						}
						pushbackSquares = UtilPushback.findPushbackSquares(game, fStartingPushbackSquare, pushbackMode);
						boolean sideStepHomePlayer = game.getTeamHome().hasPlayer(defender);
						for (PushbackSquare pushbackSquare : pushbackSquares) {
							pushbackSquare.setHomeChoice(sideStepHomePlayer);
						}
						fieldModel.add(pushbackSquares);
						if ((sideStepHomePlayer && !game.isHomePlaying()) || (!sideStepHomePlayer && game.isHomePlaying())) {
							UtilTimer.waitForOpponent(getGameState(), true);
						}
					}
					publishParameter(new StepParameter(StepParameterKey.STARTING_PUSHBACK_SQUARE, null));
				}

				// handle grab
			} else if (((fUsingGrab == null) || fUsingGrab)
				&& freeSquareAroundDefender
				&& UtilCards.hasSkill(game, actingPlayer, Skill.GRAB)
				&& attackerCoordinate.isAdjacent(defenderCoordinate)
				&& !UtilCards.hasSkill(game, defender, Skill.SIDE_STEP)
				&& ((actingPlayer.getPlayerAction() == PlayerAction.BLOCK) || (actingPlayer.getPlayerAction() == PlayerAction.MULTIPLE_BLOCK) || UtilCards.hasSkill(game, actingPlayer, Skill.BALL_AND_CHAIN))) {
				if ((fUsingGrab == null) && ArrayTool.isProvided(pushbackSquares)) {
					fUsingGrab = true;
					for (int i = 0; i < pushbackSquares.length; i++) {
						if (fieldModel.getPlayer(pushbackSquares[i].getCoordinate()) != null) {
							fUsingGrab = null;
							break;
						}
					}
				}
				if (fUsingGrab == null) {
					UtilDialog.showDialog(getGameState(), new DialogSkillUseParameter(actingPlayer.getPlayerId(), Skill.GRAB, 0));
					fUsingGrab = null;
				} else {
					if (fUsingGrab) {
						pushbackMode = PushbackMode.GRAB;
						for (int i = 0; i < pushbackSquares.length; i++) {
							if (!pushbackSquares[i].isSelected()) {
								fieldModel.remove(pushbackSquares[i]);
							}
						}
						fieldModel.add(UtilPushback.findPushbackSquares(game, fStartingPushbackSquare, pushbackMode));
						fUsingGrab = null;
					} else {
						fUsingGrab = false;
					}
					publishParameter(new StepParameter(StepParameterKey.STARTING_PUSHBACK_SQUARE, null));
				}

			} else {
				if (!ArrayTool.isProvided(pushbackSquares)) {
					// Crowdpush
					publishParameter(new StepParameter(StepParameterKey.INJURY_RESULT, UtilInjury.handleInjury(this, InjuryType.CROWDPUSH, null, defender,
							fStartingPushbackSquare.getCoordinate(), null, ApothecaryMode.CROWD_PUSH)));
					game.getFieldModel().remove(defender);
					if (defenderCoordinate.equals(game.getFieldModel().getBallCoordinate())) {
						game.getFieldModel().setBallCoordinate(null);
						publishParameter(new StepParameter(StepParameterKey.CATCH_SCATTER_THROW_IN_MODE, CatchScatterThrowInMode.THROW_IN));
						publishParameter(new StepParameter(StepParameterKey.THROW_IN_COORDINATE, defenderCoordinate));
					}
					publishParameter(new StepParameter(StepParameterKey.STARTING_PUSHBACK_SQUARE, null));
					doPush = true;
				}
			}
			if (fStartingPushbackSquare == null) {
				getResult().addReport(new ReportPushback(defender.getId(), pushbackMode));
			}
		}
		if (doPush) {
			publishParameter(new StepParameter(StepParameterKey.DEFENDER_PUSHED, true));
			if (fPushbackStack.size() > 0) {
				while (fPushbackStack.size() > 0) {
					Pushback pushback = fPushbackStack.pop();
					Player player = game.getPlayerById(pushback.getPlayerId());
					pushPlayer(player, pushback.getCoordinate());
				}
			}
			fieldModel.clearPushbackSquares();
			publishParameter(new StepParameter(StepParameterKey.STARTING_PUSHBACK_SQUARE, null));
			game.setWaitingForOpponent(false);
			getResult().setNextAction(StepAction.NEXT_STEP);
		}
	}

	private void pushPlayer(Player pPlayer, FieldCoordinate pCoordinate) {
		Game game = getGameState().getGame();
		FieldModel fieldModel = game.getFieldModel();
		fieldModel.updatePlayerAndBallPosition(pPlayer, pCoordinate);
		UtilPlayerMove.updateMoveSquares(getGameState(), false);
		if (fieldModel.isBallMoving() && pCoordinate.equals(fieldModel.getBallCoordinate())) {
			publishParameter(new StepParameter(StepParameterKey.CATCH_SCATTER_THROW_IN_MODE, CatchScatterThrowInMode.SCATTER_BALL));
		}
	}

	public int getByteArraySerializationVersion() {
		return 1;
	}

	@Override
	public void addTo(ByteList pByteList) {
		super.addTo(pByteList);
		pByteList.addSmallInt((fOldDefenderState != null) ? fOldDefenderState.getId() : 0);
		if (fStartingPushbackSquare != null) {
			pByteList.addBoolean(true);
			fStartingPushbackSquare.addTo(pByteList);
		} else {
			pByteList.addBoolean(false);
		}
		pByteList.addBoolean(fUsingGrab);
		pByteList.addBoolean(fUsingSideStep);
		pByteList.addBoolean(fUsingStandFirm);
	}

	@Override
	public int initFrom(ByteArray pByteArray) {
		int byteArraySerializationVersion = super.initFrom(pByteArray);
		int oldDefenderStateId = pByteArray.getSmallInt();
		fOldDefenderState = (oldDefenderStateId > 0) ? new PlayerState(oldDefenderStateId) : null;
		if (pByteArray.getBoolean()) {
			fStartingPushbackSquare = new PushbackSquare();
			fStartingPushbackSquare.initFrom(pByteArray);
		} else {
			fStartingPushbackSquare = null;
		}
		fUsingGrab = pByteArray.getBoolean();
		fUsingSideStep = pByteArray.getBoolean();
		fUsingStandFirm = pByteArray.getBoolean();
		return byteArraySerializationVersion;
	}

}
