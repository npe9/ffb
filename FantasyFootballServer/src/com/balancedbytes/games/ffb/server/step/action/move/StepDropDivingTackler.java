package com.balancedbytes.games.ffb.server.step.action.move;

import com.balancedbytes.games.ffb.FieldCoordinate;
import com.balancedbytes.games.ffb.bytearray.ByteArray;
import com.balancedbytes.games.ffb.bytearray.ByteList;
import com.balancedbytes.games.ffb.json.UtilJson;
import com.balancedbytes.games.ffb.model.Game;
import com.balancedbytes.games.ffb.net.NetCommand;
import com.balancedbytes.games.ffb.server.GameState;
import com.balancedbytes.games.ffb.server.IServerJsonOption;
import com.balancedbytes.games.ffb.server.step.AbstractStep;
import com.balancedbytes.games.ffb.server.step.StepAction;
import com.balancedbytes.games.ffb.server.step.StepCommandStatus;
import com.balancedbytes.games.ffb.server.step.StepId;
import com.balancedbytes.games.ffb.server.step.StepParameter;
import com.balancedbytes.games.ffb.server.util.UtilInjury;
import com.balancedbytes.games.ffb.server.util.UtilPlayerMove;
import com.balancedbytes.games.ffb.util.StringTool;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * Step in move sequence to drop a player using the DIVING_TACKLE skill.
 * 
 * Expects stepParameter COORDINATE_FROM to be set by a preceding step.
 * Expects stepParameter USING_DIVING_TACKLE to be set by a preceding step.
 * 
 * Sets stepParameter CATCH_SCATTER_THROWIN_MODE for all steps on the stack.
 * 
 * @author Kalimar
 */
public class StepDropDivingTackler extends AbstractStep {

	private boolean fUsingDivingTackle;
	private FieldCoordinate fCoordinateFrom;

	public StepDropDivingTackler(GameState pGameState) {
		super(pGameState);
	}

	public StepId getId() {
		return StepId.DROP_DIVING_TACKLER;
	}

	@Override
	public boolean setParameter(StepParameter pParameter) {
		if ((pParameter != null) && !super.setParameter(pParameter)) {
			switch (pParameter.getKey()) {
				case COORDINATE_FROM:
					fCoordinateFrom = (FieldCoordinate) pParameter.getValue();
					return true;
				case USING_DIVING_TACKLE:
					fUsingDivingTackle = (pParameter.getValue() != null) ? (Boolean) pParameter.getValue() : false;
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
	public StepCommandStatus handleNetCommand(NetCommand pNetCommand) {
		StepCommandStatus commandStatus = super.handleNetCommand(pNetCommand);
		if (commandStatus == StepCommandStatus.EXECUTE_STEP) {
			executeStep();
		}
		return commandStatus;
	}

	private void executeStep() {
		Game game = getGameState().getGame();
		if (fUsingDivingTackle && StringTool.isProvided(game.getDefenderId())) {
			game.getFieldModel().updatePlayerAndBallPosition(game.getDefender(), fCoordinateFrom);
			publishParameters(UtilInjury.dropPlayer(this, game.getDefender()));
			UtilPlayerMove.updateMoveSquares(getGameState(), game.getActingPlayer().isLeaping());
		}
		// reset DivingTackle & Shadowing attributes
		game.setDefenderId(null);
		getResult().setNextAction(StepAction.NEXT_STEP);
	}

	public int getByteArraySerializationVersion() {
		return 1;
	}

	@Override
	public void addTo(ByteList pByteList) {
		super.addTo(pByteList);
		pByteList.addBoolean(fUsingDivingTackle);
		pByteList.addFieldCoordinate(fCoordinateFrom);
	}

	@Override
	public int initFrom(ByteArray pByteArray) {
		int byteArraySerializationVersion = super.initFrom(pByteArray);
		fUsingDivingTackle = pByteArray.getBoolean();
		fCoordinateFrom = pByteArray.getFieldCoordinate();
		return byteArraySerializationVersion;
	}
	
  // JSON serialization
  
  public JsonObject toJsonValue() {
    JsonObject jsonObject = toJsonValueTemp();
    IServerJsonOption.USING_DIVING_TACKLE.addTo(jsonObject, fUsingDivingTackle);
    IServerJsonOption.COORDINATE_FROM.addTo(jsonObject, fCoordinateFrom);
    return jsonObject;
  }
  
  public StepDropDivingTackler initFrom(JsonValue pJsonValue) {
    initFromTemp(pJsonValue);
    JsonObject jsonObject = UtilJson.toJsonObject(pJsonValue);
    fUsingDivingTackle = IServerJsonOption.USING_DIVING_TACKLE.getFrom(jsonObject);
    fCoordinateFrom = IServerJsonOption.COORDINATE_FROM.getFrom(jsonObject);
    return this;
  }

}
