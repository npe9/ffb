package com.balancedbytes.games.ffb.server.step.action.ktm;

import com.balancedbytes.games.ffb.FieldCoordinate;
import com.balancedbytes.games.ffb.PlayerAction;
import com.balancedbytes.games.ffb.PlayerState;
import com.balancedbytes.games.ffb.json.UtilJson;
import com.balancedbytes.games.ffb.model.Game;
import com.balancedbytes.games.ffb.model.Player;
import com.balancedbytes.games.ffb.server.GameState;
import com.balancedbytes.games.ffb.server.IServerJsonOption;
import com.balancedbytes.games.ffb.server.net.ReceivedCommand;
import com.balancedbytes.games.ffb.server.step.AbstractStep;
import com.balancedbytes.games.ffb.server.step.IStepLabel;
import com.balancedbytes.games.ffb.server.step.SequenceGenerator;
import com.balancedbytes.games.ffb.server.step.StepAction;
import com.balancedbytes.games.ffb.server.step.StepCommandStatus;
import com.balancedbytes.games.ffb.server.step.StepId;
import com.balancedbytes.games.ffb.server.step.StepParameter;
import com.balancedbytes.games.ffb.server.util.UtilServerDialog;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * Final step of the throw team mate sequence.
 * Consumes all expected stepParameters.
 * 
 * Expects stepParameter END_PLAYER_ACTION to be set by a preceding step.
 * Expects stepParameter END_TURN to be set by a preceding step.
 * Expects stepParameter THROWN_PLAYER_COORDINATE to be set by a preceding step.
 * Expects stepParameter THROWN_PLAYER_HAS_BALL to be set by a preceding step.
 * Expects stepParameter THROWN_PLAYER_ID to be set by a preceding step.
 * Expects stepParameter THROWN_PLAYER_STATE to be set by a preceding step.
 * 
 * @author Kalimar
 */
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
  public boolean setParameter(StepParameter pParameter) {
		if ((pParameter != null) && !super.setParameter(pParameter)) {
	  	switch (pParameter.getKey()) {
				case END_TURN:
					fEndTurn = (pParameter.getValue() != null) ? (Boolean) pParameter.getValue() : false;
					consume(pParameter);
					return true;
        case THROWN_PLAYER_COORDINATE:
				case KICKED_PLAYER_COORDINATE:
					fKickedPlayerCoordinate = (FieldCoordinate) pParameter.getValue();
					consume(pParameter);
					return true;
        case THROWN_PLAYER_HAS_BALL:
				case KICKED_PLAYER_HAS_BALL:
					fKickedPlayerHasBall = (pParameter.getValue() != null) ? (Boolean) pParameter.getValue() : false;
					consume(pParameter);
					return true;
        case THROWN_PLAYER_ID:
				case KICKED_PLAYER_ID:
					fKickedPlayerId = (String) pParameter.getValue();
					consume(pParameter);
					return true;
        case THROWN_PLAYER_STATE:
				case KICKED_PLAYER_STATE:
					fKickedPlayerState = (PlayerState) pParameter.getValue();
					consume(pParameter);
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
			switch (pReceivedCommand.getId()) {
				case CLIENT_ACTING_PLAYER:
					SequenceGenerator.getInstance().pushSelectSequence(getGameState(), false);
					getResult().setNextAction(StepAction.NEXT_STEP_AND_REPEAT);
					commandStatus = StepCommandStatus.SKIP_STEP;
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
		UtilServerDialog.hideDialog(getGameState());
    Game game = getGameState().getGame();
    game.setPassCoordinate(null);
    game.getFieldModel().setRangeRuler(null);
    // reset thrown player (e.g. failed confusion roll, successful escape roll)
    Player thrownPlayer = game.getPlayerById(fKickedPlayerId);
    if ((thrownPlayer != null) && (fKickedPlayerCoordinate != null) && (fKickedPlayerState != null) && (fKickedPlayerState.getId() > 0)) {
    	game.getFieldModel().setPlayerCoordinate(thrownPlayer, fKickedPlayerCoordinate);
    	game.getFieldModel().setPlayerState(thrownPlayer, fKickedPlayerState);
    	if (fKickedPlayerHasBall) {
    		game.getFieldModel().setBallCoordinate(fKickedPlayerCoordinate);
    	}
    }
    getGameState().cleanupStepStack(IStepLabel.END_MOVING);
    SequenceGenerator.getInstance().pushEndPlayerActionSequence(getGameState(), true, true, fEndTurn);
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
  public StepEndKickTeamMate initFrom(JsonValue pJsonValue) {
    super.initFrom(pJsonValue);
    JsonObject jsonObject = UtilJson.toJsonObject(pJsonValue);
    fEndTurn = IServerJsonOption.END_TURN.getFrom(jsonObject);
    fEndPlayerAction = IServerJsonOption.END_PLAYER_ACTION.getFrom(jsonObject);
    fKickedPlayerId = IServerJsonOption.KICKED_PLAYER_ID.getFrom(jsonObject);
    fKickedPlayerState = IServerJsonOption.KICKED_PLAYER_STATE.getFrom(jsonObject);
    fKickedPlayerHasBall = IServerJsonOption.KICKED_PLAYER_HAS_BALL.getFrom(jsonObject);
    fKickedPlayerCoordinate = IServerJsonOption.KICKED_PLAYER_COORDINATE.getFrom(jsonObject);
    return this;
  }

}