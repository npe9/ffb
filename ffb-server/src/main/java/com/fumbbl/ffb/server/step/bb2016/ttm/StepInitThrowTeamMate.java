package com.fumbbl.ffb.server.step.bb2016.ttm;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.fumbbl.ffb.FieldCoordinate;
import com.fumbbl.ffb.PlayerAction;
import com.fumbbl.ffb.PlayerState;
import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.factory.IFactorySource;
import com.fumbbl.ffb.json.UtilJson;
import com.fumbbl.ffb.model.ActingPlayer;
import com.fumbbl.ffb.model.Game;
import com.fumbbl.ffb.net.commands.ClientCommandActingPlayer;
import com.fumbbl.ffb.net.commands.ClientCommandThrowTeamMate;
import com.fumbbl.ffb.server.GameState;
import com.fumbbl.ffb.server.IServerJsonOption;
import com.fumbbl.ffb.server.net.ReceivedCommand;
import com.fumbbl.ffb.server.step.AbstractStep;
import com.fumbbl.ffb.server.step.StepAction;
import com.fumbbl.ffb.server.step.StepCommandStatus;
import com.fumbbl.ffb.server.step.StepException;
import com.fumbbl.ffb.server.step.StepId;
import com.fumbbl.ffb.server.step.StepParameter;
import com.fumbbl.ffb.server.step.StepParameterKey;
import com.fumbbl.ffb.server.step.StepParameterSet;
import com.fumbbl.ffb.server.step.UtilServerSteps;
import com.fumbbl.ffb.util.StringTool;
import com.fumbbl.ffb.util.UtilRangeRuler;

/**
 * Step to init the throw team mate sequence.
 * 
 * Needs to be initialized with stepParameter GOTO_LABEL_ON_END. May be
 * initialized with stepParameter TARGET_COORDINATE. May be initialized with
 * stepParameter THROWN_PLAYER_ID.
 * 
 * Sets stepParameter END_PLAYER_ACTION for all steps on the stack. Sets
 * stepParameter END_TURN for all steps on the stack. Sets stepParameter
 * THROWN_PLAYER_ID for all steps on the stack. Sets stepParameter
 * THROWN_PLAYER_STATE for all steps on the stack.
 *
 * @author Kalimar
 */
@RulesCollection(RulesCollection.Rules.BB2016)
public final class StepInitThrowTeamMate extends AbstractStep {

	private String fGotoLabelOnEnd;
	private String fThrownPlayerId;
	private FieldCoordinate fTargetCoordinate;
	private boolean fEndTurn;
	private boolean fEndPlayerAction;

	public StepInitThrowTeamMate(GameState pGameState) {
		super(pGameState);
	}

	public StepId getId() {
		return StepId.INIT_THROW_TEAM_MATE;
	}

	@Override
	public void init(StepParameterSet pParameterSet) {
		if (pParameterSet != null) {
			for (StepParameter parameter : pParameterSet.values()) {
				switch (parameter.getKey()) {
				// mandatory
				case GOTO_LABEL_ON_END:
					fGotoLabelOnEnd = (String) parameter.getValue();
					break;
				// optional
				case TARGET_COORDINATE:
					fTargetCoordinate = (FieldCoordinate) parameter.getValue();
					break;
				// optional
				case THROWN_PLAYER_ID:
					fThrownPlayerId = (String) parameter.getValue();
					break;
				default:
					break;
				}
			}
		}
		if (!StringTool.isProvided(fGotoLabelOnEnd)) {
			throw new StepException("StepParameter " + StepParameterKey.GOTO_LABEL_ON_END + " is not initialized.");
		}
	}

	@Override
	public void start() {
		super.start();
		executeStep();
	}

	@Override
	public StepCommandStatus handleCommand(ReceivedCommand pReceivedCommand) {
		StepCommandStatus commandStatus = super.handleCommand(pReceivedCommand);
		if ((pReceivedCommand != null) && (commandStatus == StepCommandStatus.UNHANDLED_COMMAND)
				&& UtilServerSteps.checkCommandIsFromCurrentPlayer(getGameState(), pReceivedCommand)) {
			Game game = getGameState().getGame();
			switch (pReceivedCommand.getId()) {
			case CLIENT_THROW_TEAM_MATE:
				ClientCommandThrowTeamMate throwTeamMateCommand = (ClientCommandThrowTeamMate) pReceivedCommand.getCommand();
				if (UtilServerSteps.checkCommandWithActingPlayer(getGameState(), throwTeamMateCommand)) {
					if ((throwTeamMateCommand.getTargetCoordinate() != null) && StringTool.isProvided(fThrownPlayerId)) {
						fTargetCoordinate = game.isHomePlaying() ? throwTeamMateCommand.getTargetCoordinate()
								: throwTeamMateCommand.getTargetCoordinate().transform();
					} else {
						fThrownPlayerId = throwTeamMateCommand.getThrownPlayerId();
						fTargetCoordinate = null;
					}
					commandStatus = StepCommandStatus.EXECUTE_STEP;
				}
				break;
			case CLIENT_ACTING_PLAYER:
				ClientCommandActingPlayer actingPlayerCommand = (ClientCommandActingPlayer) pReceivedCommand.getCommand();
				if (StringTool.isProvided(actingPlayerCommand.getPlayerId())) {
					UtilServerSteps.changePlayerAction(this, actingPlayerCommand.getPlayerId(),
							actingPlayerCommand.getPlayerAction(), actingPlayerCommand.isJumping());
				} else {
					fEndPlayerAction = true;
				}
				commandStatus = StepCommandStatus.EXECUTE_STEP;
				break;
			case CLIENT_END_TURN:
				if (UtilServerSteps.checkCommandIsFromCurrentPlayer(getGameState(), pReceivedCommand)) {
					fEndTurn = true;
					commandStatus = StepCommandStatus.EXECUTE_STEP;
				}
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

	private void executeStep() {
		Game game = getGameState().getGame();
		ActingPlayer actingPlayer = game.getActingPlayer();
		if (fEndTurn) {
			publishParameter(new StepParameter(StepParameterKey.END_TURN, true));
			getResult().setNextAction(StepAction.GOTO_LABEL, fGotoLabelOnEnd);
		} else if (fEndPlayerAction) {
			publishParameter(new StepParameter(StepParameterKey.END_PLAYER_ACTION, true));
			getResult().setNextAction(StepAction.GOTO_LABEL, fGotoLabelOnEnd);
		} else {
			if (StringTool.isProvided(fThrownPlayerId)) {
				if (fTargetCoordinate != null) {
					game.setPassCoordinate(fTargetCoordinate);
					game.getFieldModel().setRangeRuler(
							UtilRangeRuler.createRangeRuler(game, actingPlayer.getPlayer(), game.getPassCoordinate(), true));
					if (game.getFieldModel().getRangeRuler() != null) {
						getResult().setNextAction(StepAction.NEXT_STEP);
					}
				} else {
					game.setDefenderId(fThrownPlayerId);
					PlayerState thrownPlayerState = game.getFieldModel().getPlayerState(game.getDefender());
					publishParameter(new StepParameter(StepParameterKey.THROWN_PLAYER_ID, fThrownPlayerId));
					publishParameter(new StepParameter(StepParameterKey.THROWN_PLAYER_STATE, thrownPlayerState));
					FieldCoordinate thrownPlayerCoordinate = game.getFieldModel().getPlayerCoordinate(game.getDefender());
					publishParameter(new StepParameter(StepParameterKey.THROWN_PLAYER_COORDINATE, thrownPlayerCoordinate));
					boolean thrownPlayerHasBall = thrownPlayerCoordinate.equals(game.getFieldModel().getBallCoordinate())
							&& !game.getFieldModel().isBallMoving();
					publishParameter(new StepParameter(StepParameterKey.THROWN_PLAYER_HAS_BALL, thrownPlayerHasBall));
					game.getFieldModel().setPlayerState(game.getDefender(), thrownPlayerState.changeBase(PlayerState.PICKED_UP));
					UtilServerSteps.changePlayerAction(this, actingPlayer.getPlayerId(), PlayerAction.THROW_TEAM_MATE, false);
				}
			}
		}
	}

	// JSON serialization

	@Override
	public JsonObject toJsonValue() {
		JsonObject jsonObject = super.toJsonValue();
		IServerJsonOption.END_TURN.addTo(jsonObject, fEndTurn);
		IServerJsonOption.END_PLAYER_ACTION.addTo(jsonObject, fEndPlayerAction);
		IServerJsonOption.THROWN_PLAYER_ID.addTo(jsonObject, fThrownPlayerId);
		IServerJsonOption.TARGET_COORDINATE.addTo(jsonObject, fTargetCoordinate);
		IServerJsonOption.GOTO_LABEL_ON_END.addTo(jsonObject, fGotoLabelOnEnd);
		return jsonObject;
	}

	@Override
	public StepInitThrowTeamMate initFrom(IFactorySource source, JsonValue jsonValue) {
		super.initFrom(source, jsonValue);
		JsonObject jsonObject = UtilJson.toJsonObject(jsonValue);
		fEndTurn = IServerJsonOption.END_TURN.getFrom(source, jsonObject);
		fEndPlayerAction = IServerJsonOption.END_PLAYER_ACTION.getFrom(source, jsonObject);
		fThrownPlayerId = IServerJsonOption.THROWN_PLAYER_ID.getFrom(source, jsonObject);
		fTargetCoordinate = IServerJsonOption.TARGET_COORDINATE.getFrom(source, jsonObject);
		fGotoLabelOnEnd = IServerJsonOption.GOTO_LABEL_ON_END.getFrom(source, jsonObject);
		return this;
	}

}
