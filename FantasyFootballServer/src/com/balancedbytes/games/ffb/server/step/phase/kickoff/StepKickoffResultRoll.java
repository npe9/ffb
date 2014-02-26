package com.balancedbytes.games.ffb.server.step.phase.kickoff;

import com.balancedbytes.games.ffb.KickoffResult;
import com.balancedbytes.games.ffb.KickoffResultFactory;
import com.balancedbytes.games.ffb.bytearray.ByteArray;
import com.balancedbytes.games.ffb.json.UtilJson;
import com.balancedbytes.games.ffb.report.ReportKickoffResult;
import com.balancedbytes.games.ffb.server.DiceInterpreter;
import com.balancedbytes.games.ffb.server.GameState;
import com.balancedbytes.games.ffb.server.IServerJsonOption;
import com.balancedbytes.games.ffb.server.net.ReceivedCommand;
import com.balancedbytes.games.ffb.server.step.AbstractStep;
import com.balancedbytes.games.ffb.server.step.StepAction;
import com.balancedbytes.games.ffb.server.step.StepCommandStatus;
import com.balancedbytes.games.ffb.server.step.StepId;
import com.balancedbytes.games.ffb.server.step.StepParameter;
import com.balancedbytes.games.ffb.server.step.StepParameterKey;
import com.balancedbytes.games.ffb.server.util.UtilDialog;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * Step in kickoff sequence to roll kickoff result.
 * 
 * Sets stepParameter KICKOFF_RESULT for all steps on the stack.
 * 
 * @author Kalimar
 */
public final class StepKickoffResultRoll extends AbstractStep {
	
  private KickoffResult fKickoffResult;
	
	public StepKickoffResultRoll(GameState pGameState) {
		super(pGameState);
	}
	
	public StepId getId() {
		return StepId.KICKOFF_RESULT_ROLL;
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

    UtilDialog.hideDialog(getGameState());

    int[] rollKickoff = getGameState().getDiceRoller().rollKickoff();
    fKickoffResult = DiceInterpreter.getInstance().interpretRollKickoff(rollKickoff);
    getResult().addReport(new ReportKickoffResult(fKickoffResult, rollKickoff));

    publishParameter(new StepParameter(StepParameterKey.KICKOFF_RESULT, fKickoffResult));
    getResult().setNextAction(StepAction.NEXT_STEP);
    
  }
  
  // ByteArray serialization
  
  @Override
  public int initFrom(ByteArray pByteArray) {
  	int byteArraySerializationVersion = super.initFrom(pByteArray);
  	fKickoffResult = new KickoffResultFactory().forId((int) pByteArray.getByte());
  	return byteArraySerializationVersion;
  }
  
  // JSON serialization
  
  @Override
  public JsonObject toJsonValue() {
    JsonObject jsonObject = super.toJsonValue();
    IServerJsonOption.KICKOFF_RESULT.addTo(jsonObject, fKickoffResult);
    return jsonObject;
  }
  
  @Override
  public StepKickoffResultRoll initFrom(JsonValue pJsonValue) {
    super.initFrom(pJsonValue);
    JsonObject jsonObject = UtilJson.toJsonObject(pJsonValue);
    fKickoffResult = (KickoffResult) IServerJsonOption.KICKOFF_RESULT.getFrom(jsonObject);
    return this;
  }

}
