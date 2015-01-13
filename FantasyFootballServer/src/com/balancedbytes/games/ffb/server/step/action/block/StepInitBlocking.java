package com.balancedbytes.games.ffb.server.step.action.block;

import com.balancedbytes.games.ffb.PlayerAction;
import com.balancedbytes.games.ffb.PlayerState;
import com.balancedbytes.games.ffb.json.UtilJson;
import com.balancedbytes.games.ffb.model.ActingPlayer;
import com.balancedbytes.games.ffb.model.Game;
import com.balancedbytes.games.ffb.model.Player;
import com.balancedbytes.games.ffb.net.commands.ClientCommandActingPlayer;
import com.balancedbytes.games.ffb.net.commands.ClientCommandBlock;
import com.balancedbytes.games.ffb.server.GameState;
import com.balancedbytes.games.ffb.server.IServerJsonOption;
import com.balancedbytes.games.ffb.server.net.ReceivedCommand;
import com.balancedbytes.games.ffb.server.step.AbstractStep;
import com.balancedbytes.games.ffb.server.step.StepAction;
import com.balancedbytes.games.ffb.server.step.StepCommandStatus;
import com.balancedbytes.games.ffb.server.step.StepException;
import com.balancedbytes.games.ffb.server.step.StepId;
import com.balancedbytes.games.ffb.server.step.StepParameter;
import com.balancedbytes.games.ffb.server.step.StepParameterKey;
import com.balancedbytes.games.ffb.server.step.StepParameterSet;
import com.balancedbytes.games.ffb.server.step.UtilServerSteps;
import com.balancedbytes.games.ffb.util.StringTool;
import com.balancedbytes.games.ffb.util.UtilCards;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * Step to init the block sequence.
 * 
 * Needs to be initialized with stepParameter GOTO_LABEL_ON_END.
 * May be initialized with stepParameter BLOCK_DEFENDER_ID.
 * May be initialized with stepParameter USING_STAB.
 * 
 * Sets stepParameter DEFENDER_POSITION for all steps on the stack.
 * Sets stepParameter END_PLAYER_ACTION for all steps on the stack.
 * Sets stepParameter END_TURN for all steps on the stack.
 * Sets stepParameter OLD_DEFENDER_STATE for all steps on the stack.
 * Sets stepParameter USING_STAB for all steps on the stack.
 * 
 * @author Kalimar
 */
public class StepInitBlocking extends AbstractStep {
	
	private String fGotoLabelOnEnd;
  private String fBlockDefenderId;
  private boolean fUsingStab;
  private String fMultiBlockDefenderId;
  private boolean fEndTurn;
  private boolean fEndPlayerAction;
		
	public StepInitBlocking(GameState pGameState) {
		super(pGameState);
	}
	
	public StepId getId() {
		return StepId.INIT_BLOCKING;
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
  				case BLOCK_DEFENDER_ID:
  					fBlockDefenderId = (String) parameter.getValue();
  					break;
					// optional
  				case USING_STAB:
						fUsingStab = (parameter.getValue() != null) ? (Boolean) parameter.getValue() : false;
  					break;
					// optional
  				case MULTI_BLOCK_DEFENDER_ID:
  					fMultiBlockDefenderId = (String) parameter.getValue();
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
    if ((pReceivedCommand != null) && (commandStatus == StepCommandStatus.UNHANDLED_COMMAND) && UtilServerSteps.checkCommandIsFromCurrentPlayer(getGameState(), pReceivedCommand)) {
      switch (pReceivedCommand.getId()) {
        case CLIENT_BLOCK:
          ClientCommandBlock blockCommand = (ClientCommandBlock) pReceivedCommand.getCommand();
          if (UtilServerSteps.checkCommandWithActingPlayer(getGameState(), blockCommand)) {
            if ((fMultiBlockDefenderId == null) || !fMultiBlockDefenderId.equals(blockCommand.getDefenderId())) { 
              fBlockDefenderId = blockCommand.getDefenderId();
              fUsingStab = blockCommand.isUsingStab();
              commandStatus = StepCommandStatus.EXECUTE_STEP;
            }
          }
          break;
        case CLIENT_END_TURN:
          if (UtilServerSteps.checkCommandIsFromCurrentPlayer(getGameState(), pReceivedCommand)) {
            fEndTurn = true;
            commandStatus = StepCommandStatus.EXECUTE_STEP;
          }
          break;
        case CLIENT_ACTING_PLAYER:
          ClientCommandActingPlayer actingPlayerCommand = (ClientCommandActingPlayer) pReceivedCommand.getCommand();
          if (StringTool.isProvided(actingPlayerCommand.getPlayerId())) {
            UtilServerSteps.changePlayerAction(this, actingPlayerCommand.getPlayerId(), actingPlayerCommand.getPlayerAction(), actingPlayerCommand.isLeaping());
          } else {
            fEndPlayerAction = true;
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
	
  private void executeStep() {
    Game game = getGameState().getGame();
    ActingPlayer actingPlayer = game.getActingPlayer();
    if (fEndTurn) {
    	publishParameter(new StepParameter(StepParameterKey.END_TURN, true));
    	getResult().setNextAction(StepAction.GOTO_LABEL, fGotoLabelOnEnd);
    } else if (fEndPlayerAction) {
    	publishParameter(new StepParameter(StepParameterKey.END_PLAYER_ACTION, true));
    	getResult().setNextAction(StepAction.GOTO_LABEL, fGotoLabelOnEnd);
    } else if (actingPlayer.isSufferingBloodLust() && (actingPlayer.getPlayerAction() == PlayerAction.MOVE)) {
    	getResult().setNextAction(StepAction.GOTO_LABEL, fGotoLabelOnEnd);
    } else {
	    Player defender = game.getPlayerById(fBlockDefenderId);
	    if (defender != null) {
	      game.setDefenderId(defender.getId());
	      actingPlayer.setStrength(UtilCards.getPlayerStrength(game, actingPlayer.getPlayer()));
	      PlayerState oldDefenderState = game.getFieldModel().getPlayerState(defender);
	      publishParameter(new StepParameter(StepParameterKey.OLD_DEFENDER_STATE, oldDefenderState));
	    	publishParameter(new StepParameter(StepParameterKey.DEFENDER_POSITION, game.getFieldModel().getPlayerCoordinate(game.getDefender())));
	      publishParameter(new StepParameter(StepParameterKey.USING_STAB, fUsingStab));
	      game.getFieldModel().setPlayerState(defender, oldDefenderState.changeBase(PlayerState.BLOCKED));
	      if (actingPlayer.getPlayerAction() == PlayerAction.BLITZ_MOVE) {
	      	UtilServerSteps.changePlayerAction(this, actingPlayer.getPlayerId(), PlayerAction.BLITZ, actingPlayer.isLeaping());
	      }
	      getResult().setNextAction(StepAction.NEXT_STEP);
	    }
    }
  }
  
  // JSON serialization
  
  @Override
  public JsonObject toJsonValue() {
    JsonObject jsonObject = super.toJsonValue();
    IServerJsonOption.GOTO_LABEL_ON_END.addTo(jsonObject, fGotoLabelOnEnd);
    IServerJsonOption.BLOCK_DEFENDER_ID.addTo(jsonObject, fBlockDefenderId);
    IServerJsonOption.USING_STAB.addTo(jsonObject, fUsingStab);
    IServerJsonOption.MULTI_BLOCK_DEFENDER_ID.addTo(jsonObject, fMultiBlockDefenderId);
    IServerJsonOption.END_TURN.addTo(jsonObject, fEndTurn);
    IServerJsonOption.END_PLAYER_ACTION.addTo(jsonObject, fEndPlayerAction);
    return jsonObject;
  }
  
  @Override
  public StepInitBlocking initFrom(JsonValue pJsonValue) {
    super.initFrom(pJsonValue);
    JsonObject jsonObject = UtilJson.toJsonObject(pJsonValue);
    fGotoLabelOnEnd = IServerJsonOption.GOTO_LABEL_ON_END.getFrom(jsonObject);
    fBlockDefenderId = IServerJsonOption.BLOCK_DEFENDER_ID.getFrom(jsonObject);
    fUsingStab = IServerJsonOption.USING_STAB.getFrom(jsonObject);
    fMultiBlockDefenderId = IServerJsonOption.MULTI_BLOCK_DEFENDER_ID.getFrom(jsonObject);
    fEndTurn = IServerJsonOption.END_TURN.getFrom(jsonObject);
    fEndPlayerAction = IServerJsonOption.END_PLAYER_ACTION.getFrom(jsonObject);
    return this;
  }
  
}
