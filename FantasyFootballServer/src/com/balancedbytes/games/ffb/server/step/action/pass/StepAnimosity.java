package com.balancedbytes.games.ffb.server.step.action.pass;

import com.balancedbytes.games.ffb.FieldCoordinate;
import com.balancedbytes.games.ffb.PlayerAction;
import com.balancedbytes.games.ffb.PlayerState;
import com.balancedbytes.games.ffb.ReRolledAction;
import com.balancedbytes.games.ffb.Skill;
import com.balancedbytes.games.ffb.bytearray.ByteArray;
import com.balancedbytes.games.ffb.bytearray.ByteList;
import com.balancedbytes.games.ffb.json.UtilJson;
import com.balancedbytes.games.ffb.model.ActingPlayer;
import com.balancedbytes.games.ffb.model.Game;
import com.balancedbytes.games.ffb.model.Player;
import com.balancedbytes.games.ffb.model.Team;
import com.balancedbytes.games.ffb.net.NetCommand;
import com.balancedbytes.games.ffb.report.ReportId;
import com.balancedbytes.games.ffb.report.ReportSkillRoll;
import com.balancedbytes.games.ffb.server.DiceInterpreter;
import com.balancedbytes.games.ffb.server.GameState;
import com.balancedbytes.games.ffb.server.IServerJsonOption;
import com.balancedbytes.games.ffb.server.step.AbstractStepWithReRoll;
import com.balancedbytes.games.ffb.server.step.StepAction;
import com.balancedbytes.games.ffb.server.step.StepCommandStatus;
import com.balancedbytes.games.ffb.server.step.StepException;
import com.balancedbytes.games.ffb.server.step.StepId;
import com.balancedbytes.games.ffb.server.step.StepParameter;
import com.balancedbytes.games.ffb.server.step.StepParameterKey;
import com.balancedbytes.games.ffb.server.step.StepParameterSet;
import com.balancedbytes.games.ffb.server.util.UtilReRoll;
import com.balancedbytes.games.ffb.util.StringTool;
import com.balancedbytes.games.ffb.util.UtilCards;
import com.balancedbytes.games.ffb.util.UtilPassing;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * Step in the pass sequence to handle skill ANIMOSITY.
 * 
 * Needs to be initialized with stepParameter GOTO_LABEL_ON_FAILURE.
 * 
 * Expects stepParameter CATCHER_ID to be set by a preceding step.
 *  
 * @author Kalimar
 */
public final class StepAnimosity extends AbstractStepWithReRoll {
	
  private String fGotoLabelOnFailure;
  private String fCatcherId;

	public StepAnimosity(GameState pGameState) {
		super(pGameState);
	}
	
	public StepId getId() {
		return StepId.ANIMOSITY;
	}
	
  @Override
  public void init(StepParameterSet pParameterSet) {
  	if (pParameterSet != null) {
  		for (StepParameter parameter : pParameterSet.values()) {
  			switch (parameter.getKey()) {
  			  // mandatory
  				case GOTO_LABEL_ON_FAILURE:
  					fGotoLabelOnFailure = (String) parameter.getValue();
  					break;
					default:
						break;
  			}
  		}
  	}
  	if (fGotoLabelOnFailure == null) {
			throw new StepException("StepParameter " + StepParameterKey.GOTO_LABEL_ON_FAILURE + " is not initialized.");
  	}
  }
  
  @Override
  public boolean setParameter(StepParameter pParameter) {
		if ((pParameter != null) && !super.setParameter(pParameter)) {
	  	switch (pParameter.getKey()) {
				case CATCHER_ID:
					fCatcherId = (String) pParameter.getValue();
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
    boolean doRoll = false;
    Game game = getGameState().getGame();
    ActingPlayer actingPlayer = game.getActingPlayer();
    if (game.getTurnMode().isBombTurn()) {
    	getResult().setNextAction(StepAction.NEXT_STEP);
    	return;
    }
    Player thrower = game.getThrower();
  	Player catcher = game.getPlayerById(fCatcherId);
    if (actingPlayer.isSufferingAnimosity()) {
  	  if ((catcher != null) && !(thrower.getRace().equalsIgnoreCase(catcher.getRace()))) {
        // step END_PASSING will push a new pass sequence onto the stack
        game.setPassCoordinate(null);
        game.getFieldModel().setRangeRuler(null);
        getResult().setNextAction(StepAction.GOTO_LABEL, fGotoLabelOnFailure);
  	  } else {
    		getResult().setNextAction(StepAction.NEXT_STEP);
  	  }
    } else {
      if (ReRolledAction.ANIMOSITY == getReRolledAction()) {
    	  if ((getReRollSource() == null) || !UtilReRoll.useReRoll(this, getReRollSource(), thrower)) {
          actingPlayer.setSufferingAnimosity(true);
        } else {
        	doRoll = true;
        }
      } else {
    	  if (catcher != null) {
    	  	doRoll = (UtilCards.hasSkill(game, thrower, Skill.ANIMOSITY) && !(thrower.getRace().equalsIgnoreCase(catcher.getRace())));
    	  }
      }
      if (doRoll) {
        int roll = getGameState().getDiceRoller().rollSkill();
        int minimumRoll = DiceInterpreter.getInstance().minimumRollAnimosity();
        boolean successful = DiceInterpreter.getInstance().isSkillRollSuccessful(roll, minimumRoll);
        actingPlayer.markSkillUsed(Skill.ANIMOSITY);
        if (successful) {
          actingPlayer.setSufferingAnimosity(false);
      		getResult().setNextAction(StepAction.NEXT_STEP);
        } else {
          if ((ReRolledAction.ANIMOSITY == getReRolledAction()) || !UtilReRoll.askForReRollIfAvailable(getGameState(), actingPlayer.getPlayer(), ReRolledAction.ANIMOSITY, minimumRoll, false)) {
            actingPlayer.setSufferingAnimosity(true);
          }
        }
        boolean reRolled = ((ReRolledAction.ANIMOSITY == getReRolledAction()) && (getReRollSource() != null));
        getResult().addReport(new ReportSkillRoll(ReportId.ANIMOSITY_ROLL, actingPlayer.getPlayerId(), successful, roll, minimumRoll, reRolled));
      }
      if (actingPlayer.isSufferingAnimosity()) {
        boolean animosityPassPossible = false;
        FieldCoordinate throwerCoordinate = game.getFieldModel().getPlayerCoordinate(actingPlayer.getPlayer());
        Team team = game.getTeamHome().hasPlayer(actingPlayer.getPlayer()) ? game.getTeamHome() : game.getTeamAway();
        for (Player player : team.getPlayers()) {
          PlayerState playerState = game.getFieldModel().getPlayerState(player); 
          FieldCoordinate playerCoordinate = game.getFieldModel().getPlayerCoordinate(player);
          if ((playerState != null) && playerState.hasTacklezones() && StringTool.isEqual(actingPlayer.getRace(), player.getRace())) {
            if (((actingPlayer.getPlayerAction() == PlayerAction.HAND_OVER) && playerCoordinate.isAdjacent(throwerCoordinate))
              || ((actingPlayer.getPlayerAction() == PlayerAction.PASS) && UtilPassing.findPassingDistance(game, throwerCoordinate, playerCoordinate, false) != null)) {
              animosityPassPossible = true;
              break;
            }
          }
        }
        if (animosityPassPossible) {
          // step END_PASSING will push a new pass sequence onto the stack
          game.setPassCoordinate(null);
          game.getFieldModel().setRangeRuler(null);
        }
        getResult().setNextAction(StepAction.GOTO_LABEL, fGotoLabelOnFailure);
      } else {
        if (!doRoll) {
      		getResult().setNextAction(StepAction.NEXT_STEP);
        }
      }
    }
  }
  
  public int getByteArraySerializationVersion() {
  	return 1;
  }

  @Override
  public void addTo(ByteList pByteList) {
  	super.addTo(pByteList);
  	pByteList.addString(fGotoLabelOnFailure);
  	pByteList.addString(fCatcherId);
  }
  
  @Override
  public int initFrom(ByteArray pByteArray) {
  	int byteArraySerializationVersion = super.initFrom(pByteArray);
  	fGotoLabelOnFailure = pByteArray.getString();
  	fCatcherId = pByteArray.getString();
  	return byteArraySerializationVersion;
  }
  
  // JSON serialization
  
  public JsonObject toJsonValue() {
    JsonObject jsonObject = toJsonValueTemp();
    IServerJsonOption.GOTO_LABEL_ON_FAILURE.addTo(jsonObject, fGotoLabelOnFailure);
    IServerJsonOption.CATCHER_ID.addTo(jsonObject, fCatcherId);
    return jsonObject;
  }
  
  public StepAnimosity initFrom(JsonValue pJsonValue) {
    initFromTemp(pJsonValue);
    JsonObject jsonObject = UtilJson.toJsonObject(pJsonValue);
    fGotoLabelOnFailure = IServerJsonOption.GOTO_LABEL_ON_FAILURE.getFrom(jsonObject);
    fCatcherId = IServerJsonOption.CATCHER_ID.getFrom(jsonObject);
    return this;
  }
  
}
