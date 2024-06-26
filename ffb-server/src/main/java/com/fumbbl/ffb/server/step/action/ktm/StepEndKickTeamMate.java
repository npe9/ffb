package com.fumbbl.ffb.server.step.action.ktm;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.fumbbl.ffb.FactoryType;
import com.fumbbl.ffb.FieldCoordinate;
import com.fumbbl.ffb.PlayerState;
import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.factory.IFactorySource;
import com.fumbbl.ffb.json.UtilJson;
import com.fumbbl.ffb.model.Game;
import com.fumbbl.ffb.model.Player;
import com.fumbbl.ffb.net.NetCommandId;
import com.fumbbl.ffb.server.GameState;
import com.fumbbl.ffb.server.IServerJsonOption;
import com.fumbbl.ffb.server.factory.SequenceGeneratorFactory;
import com.fumbbl.ffb.server.net.ReceivedCommand;
import com.fumbbl.ffb.server.step.AbstractStep;
import com.fumbbl.ffb.server.step.IStepLabel;
import com.fumbbl.ffb.server.step.StepAction;
import com.fumbbl.ffb.server.step.StepCommandStatus;
import com.fumbbl.ffb.server.step.StepId;
import com.fumbbl.ffb.server.step.StepParameter;
import com.fumbbl.ffb.server.step.generator.EndPlayerAction;
import com.fumbbl.ffb.server.step.generator.Select;
import com.fumbbl.ffb.server.step.generator.SequenceGenerator;
import com.fumbbl.ffb.server.util.UtilServerDialog;

/**
 * Final step of the throw team mate sequence. Consumes all expected
 * stepParameters.
 *
 * Expects stepParameter END_PLAYER_ACTION to be set by a preceding step.
 * Expects stepParameter END_TURN to be set by a preceding step. Expects
 * stepParameter THROWN_PLAYER_COORDINATE to be set by a preceding step. Expects
 * stepParameter THROWN_PLAYER_HAS_BALL to be set by a preceding step. Expects
 * stepParameter THROWN_PLAYER_ID to be set by a preceding step. Expects
 * stepParameter THROWN_PLAYER_STATE to be set by a preceding step.
 *
 * @author Kalimar
 */
@RulesCollection(RulesCollection.Rules.COMMON)
public final class StepEndKickTeamMate extends AbstractStep {

	private boolean fEndTurn;
	private boolean fEndPlayerAction;
	private FieldCoordinate fKickedPlayerCoordinate;
	private boolean fKickedPlayerHasBall;
	private String fKickedPlayerId;
	private PlayerState fKickedPlayerState;

	public StepEndKickTeamMate(GameState pGameState) {
		super(pGameState);
	}

	public StepId getId() {
		return StepId.END_KICK_TEAM_MATE;
	}

	@Override
	public boolean setParameter(StepParameter parameter) {
		if ((parameter != null) && !super.setParameter(parameter)) {
			switch (parameter.getKey()) {
			case END_TURN:
				fEndTurn = (parameter.getValue() != null) ? (Boolean) parameter.getValue() : false;
				consume(parameter);
				return true;
			case THROWN_PLAYER_COORDINATE:
			case KICKED_PLAYER_COORDINATE:
				fKickedPlayerCoordinate = (FieldCoordinate) parameter.getValue();
				consume(parameter);
				return true;
			case THROWN_PLAYER_HAS_BALL:
			case KICKED_PLAYER_HAS_BALL:
				fKickedPlayerHasBall = (parameter.getValue() != null) ? (Boolean) parameter.getValue() : false;
				consume(parameter);
				return true;
			case THROWN_PLAYER_ID:
			case KICKED_PLAYER_ID:
				fKickedPlayerId = (String) parameter.getValue();
				consume(parameter);
				return true;
			case THROWN_PLAYER_STATE:
			case KICKED_PLAYER_STATE:
				fKickedPlayerState = (PlayerState) parameter.getValue();
				consume(parameter);
				return true;
			default:
				break;
			}
		}
		return false;
	}

	@Override
	public void start() {
		super.start();
		executeStep();
	}

	@Override
	public StepCommandStatus handleCommand(ReceivedCommand pReceivedCommand) {
		StepCommandStatus commandStatus = super.handleCommand(pReceivedCommand);
		if (commandStatus == StepCommandStatus.UNHANDLED_COMMAND) {
			if (pReceivedCommand.getId() == NetCommandId.CLIENT_ACTING_PLAYER) {
				SequenceGeneratorFactory factory = getGameState().getGame().getFactory(FactoryType.Factory.SEQUENCE_GENERATOR);
				((Select) factory.forName(SequenceGenerator.Type.Select.name()))
					.pushSequence(new Select.SequenceParams(getGameState(), false));
				getResult().setNextAction(StepAction.NEXT_STEP_AND_REPEAT);
				commandStatus = StepCommandStatus.SKIP_STEP;
			}
		}
		if (commandStatus == StepCommandStatus.EXECUTE_STEP) {
			executeStep();
		}
		return commandStatus;
	}

	private void executeStep() {
		UtilServerDialog.hideDialog(getGameState());
		Game game = getGameState().getGame();
		game.setPassCoordinate(null);
		game.getFieldModel().setRangeRuler(null);
		// reset thrown player (e.g. failed confusion roll, successful escape roll)
		Player<?> thrownPlayer = game.getPlayerById(fKickedPlayerId);
		if ((thrownPlayer != null) && (fKickedPlayerCoordinate != null) && (fKickedPlayerState != null)
				&& (fKickedPlayerState.getId() > 0)) {
			game.getFieldModel().setPlayerCoordinate(thrownPlayer, fKickedPlayerCoordinate);
			game.getFieldModel().setPlayerState(thrownPlayer, fKickedPlayerState);
			if (fKickedPlayerHasBall) {
				game.getFieldModel().setBallCoordinate(fKickedPlayerCoordinate);
			}
		}
		getGameState().cleanupStepStack(IStepLabel.END_MOVING);
		SequenceGeneratorFactory factory = game.getFactory(FactoryType.Factory.SEQUENCE_GENERATOR);
		((EndPlayerAction) factory.forName(SequenceGenerator.Type.EndPlayerAction.name()))
			.pushSequence(new EndPlayerAction.SequenceParams(getGameState(), true, true, fEndTurn));
		getResult().setNextAction(StepAction.NEXT_STEP);
	}

	// JSON serialization

	@Override
	public JsonObject toJsonValue() {
		JsonObject jsonObject = super.toJsonValue();
		IServerJsonOption.END_TURN.addTo(jsonObject, fEndTurn);
		IServerJsonOption.END_PLAYER_ACTION.addTo(jsonObject, fEndPlayerAction);
		IServerJsonOption.KICKED_PLAYER_ID.addTo(jsonObject, fKickedPlayerId);
		IServerJsonOption.KICKED_PLAYER_STATE.addTo(jsonObject, fKickedPlayerState);
		IServerJsonOption.KICKED_PLAYER_HAS_BALL.addTo(jsonObject, fKickedPlayerHasBall);
		IServerJsonOption.KICKED_PLAYER_COORDINATE.addTo(jsonObject, fKickedPlayerCoordinate);
		return jsonObject;
	}

	@Override
	public StepEndKickTeamMate initFrom(IFactorySource source, JsonValue jsonValue) {
		super.initFrom(source, jsonValue);
		JsonObject jsonObject = UtilJson.toJsonObject(jsonValue);
		fEndTurn = IServerJsonOption.END_TURN.getFrom(source, jsonObject);
		fEndPlayerAction = IServerJsonOption.END_PLAYER_ACTION.getFrom(source, jsonObject);
		fKickedPlayerId = IServerJsonOption.KICKED_PLAYER_ID.getFrom(source, jsonObject);
		fKickedPlayerState = IServerJsonOption.KICKED_PLAYER_STATE.getFrom(source, jsonObject);
		fKickedPlayerHasBall = IServerJsonOption.KICKED_PLAYER_HAS_BALL.getFrom(source, jsonObject);
		fKickedPlayerCoordinate = IServerJsonOption.KICKED_PLAYER_COORDINATE.getFrom(source, jsonObject);
		return this;
	}

}
