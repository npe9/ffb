package com.fumbbl.ffb.server.step.action.foul;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.fumbbl.ffb.ApothecaryMode;
import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.factory.IFactorySource;
import com.fumbbl.ffb.json.UtilJson;
import com.fumbbl.ffb.server.GameState;
import com.fumbbl.ffb.server.IServerJsonOption;
import com.fumbbl.ffb.server.InjuryResult;
import com.fumbbl.ffb.server.net.ReceivedCommand;
import com.fumbbl.ffb.server.step.AbstractStep;
import com.fumbbl.ffb.server.step.StepCommandStatus;
import com.fumbbl.ffb.server.step.StepException;
import com.fumbbl.ffb.server.step.StepId;
import com.fumbbl.ffb.server.step.StepParameter;
import com.fumbbl.ffb.server.step.StepParameterKey;
import com.fumbbl.ffb.server.step.StepParameterSet;
import com.fumbbl.ffb.util.StringTool;

/**
 * Step in foul sequence to handle the referee and SNEAKY_GIT skill.
 * 
 * Needs to be initialized with stepParameter GOTO_LABEL_ON_END.
 * 
 * Expects stepParameter INJURY_RESULT to be set by a preceding step.
 * 
 * @author Kalimar
 */
@RulesCollection(RulesCollection.Rules.COMMON)
public class StepReferee extends AbstractStep {

	@Override
	public void init(StepParameterSet pParameterSet) {
		if (pParameterSet != null) {
			for (StepParameter parameter : pParameterSet.values()) {
				if (parameter.getKey() == StepParameterKey.GOTO_LABEL_ON_END) {
					state.gotoLabelOnEnd = (String) parameter.getValue();
				}
			}
		}
		if (!StringTool.isProvided(state.gotoLabelOnEnd)) {
			throw new StepException("StepParameter " + StepParameterKey.GOTO_LABEL_ON_END + " is not initialized.");
		}
	}

	private final StepState state;

	public StepReferee(GameState pGameState) {
		super(pGameState);

		state = new StepState();
	}

	public StepId getId() {
		return StepId.REFEREE;
	}

	@Override
	public boolean setParameter(StepParameter parameter) {
		if ((parameter != null) && !super.setParameter(parameter)) {
			if (parameter.getKey() == StepParameterKey.INJURY_RESULT) {
				InjuryResult injuryResult = (InjuryResult) parameter.getValue();
				if ((injuryResult != null) && (injuryResult.injuryContext().getApothecaryMode() == ApothecaryMode.DEFENDER)) {
					state.injuryResultDefender = injuryResult;
					return true;
				}
				return false;
			}
		}
		return false;
	}

	public static class StepState {
		public String gotoLabelOnEnd;
		public InjuryResult injuryResultDefender;
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

		if (state.injuryResultDefender != null) {
			getGameState().executeStepHooks(this, state);
		}
	}

	// JSON serialization

	@Override
	public JsonObject toJsonValue() {
		JsonObject jsonObject = super.toJsonValue();
		IServerJsonOption.GOTO_LABEL_ON_END.addTo(jsonObject, state.gotoLabelOnEnd);
		if (state.injuryResultDefender != null) {
			IServerJsonOption.INJURY_RESULT_DEFENDER.addTo(jsonObject, state.injuryResultDefender.toJsonValue());
		}
		return jsonObject;
	}

	@Override
	public StepReferee initFrom(IFactorySource source, JsonValue jsonValue) {
		super.initFrom(source, jsonValue);
		JsonObject jsonObject = UtilJson.toJsonObject(jsonValue);
		state.gotoLabelOnEnd = IServerJsonOption.GOTO_LABEL_ON_END.getFrom(source, jsonObject);
		state.injuryResultDefender = null;
		JsonObject injuryResultDefenderObject = IServerJsonOption.INJURY_RESULT_DEFENDER.getFrom(source, jsonObject);
		if (injuryResultDefenderObject != null) {
			state.injuryResultDefender = new InjuryResult().initFrom(source, injuryResultDefenderObject);
		}
		return this;
	}

}
