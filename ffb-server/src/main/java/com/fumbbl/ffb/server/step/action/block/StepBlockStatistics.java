package com.fumbbl.ffb.server.step.action.block;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.factory.IFactorySource;
import com.fumbbl.ffb.json.IJsonOption;
import com.fumbbl.ffb.json.UtilJson;
import com.fumbbl.ffb.model.ActingPlayer;
import com.fumbbl.ffb.model.Game;
import com.fumbbl.ffb.model.PlayerResult;
import com.fumbbl.ffb.server.GameState;
import com.fumbbl.ffb.server.net.ReceivedCommand;
import com.fumbbl.ffb.server.step.AbstractStep;
import com.fumbbl.ffb.server.step.StepAction;
import com.fumbbl.ffb.server.step.StepCommandStatus;
import com.fumbbl.ffb.server.step.StepId;
import com.fumbbl.ffb.server.step.StepParameter;
import com.fumbbl.ffb.server.step.StepParameterKey;
import com.fumbbl.ffb.server.step.StepParameterSet;

/**
 * Step in block sequence to track if acting player has blocked, turn is started
 * etc.
 * 
 * @author Kalimar
 */
@RulesCollection(RulesCollection.Rules.COMMON)
public class StepBlockStatistics extends AbstractStep {

	private int increment = 1;

	public StepBlockStatistics(GameState pGameState) {
		super(pGameState);
	}

	@Override
	public boolean setParameter(StepParameter parameter) {
		if (parameter != null && parameter.getKey() == StepParameterKey.PLAYER_ID_TO_REMOVE) {
			increment--;
			return true;
		}

		return super.setParameter(parameter);
	}

	@Override
	public void init(StepParameterSet parameterSet) {
		if (parameterSet != null) {
			for (StepParameter parameter: parameterSet.values()) {
				if (parameter.getKey() == StepParameterKey.INCREMENT) {
					increment = (int) parameter.getValue();
				}
			}
		}
	}

	public StepId getId() {
		return StepId.BLOCK_STATISTICS;
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
		Game game = getGameState().getGame();
		ActingPlayer actingPlayer = game.getActingPlayer();
		if (!actingPlayer.hasBlocked()) {
			actingPlayer.setHasBlocked(true);
			game.getTurnData().setTurnStarted(true);
			game.setConcessionPossible(false);
			PlayerResult playerResult = game.getGameResult().getPlayerResult(actingPlayer.getPlayer());
			playerResult.setBlocks(playerResult.getBlocks() + increment);
		}
		getResult().setNextAction(StepAction.NEXT_STEP);
	}

	// ByteArray serialization

	public int getByteArraySerializationVersion() {
		return 1;
	}

	// JSON serialization

	@Override
	public JsonObject toJsonValue() {
		JsonObject jsonObject = super.toJsonValue();
		IJsonOption.NUMBER.addTo(jsonObject, increment);
		return jsonObject;
	}

	@Override
	public StepBlockStatistics initFrom(IFactorySource source, JsonValue jsonValue) {
		super.initFrom(source, jsonValue);
		increment = IJsonOption.NUMBER.getFrom(source, UtilJson.toJsonObject(toJsonValue()));
		return this;
	}

}
