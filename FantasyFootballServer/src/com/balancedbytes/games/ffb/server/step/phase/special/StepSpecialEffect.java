package com.balancedbytes.games.ffb.server.step.phase.special;

import com.balancedbytes.games.ffb.FieldCoordinate;
import com.balancedbytes.games.ffb.InjuryType;
import com.balancedbytes.games.ffb.SpecialEffect;
import com.balancedbytes.games.ffb.TurnMode;
import com.balancedbytes.games.ffb.bytearray.ByteArray;
import com.balancedbytes.games.ffb.bytearray.ByteList;
import com.balancedbytes.games.ffb.model.Game;
import com.balancedbytes.games.ffb.model.Player;
import com.balancedbytes.games.ffb.model.Team;
import com.balancedbytes.games.ffb.report.ReportSpecialEffectRoll;
import com.balancedbytes.games.ffb.server.DiceInterpreter;
import com.balancedbytes.games.ffb.server.GameState;
import com.balancedbytes.games.ffb.server.step.AbstractStep;
import com.balancedbytes.games.ffb.server.step.StepAction;
import com.balancedbytes.games.ffb.server.step.StepException;
import com.balancedbytes.games.ffb.server.step.StepId;
import com.balancedbytes.games.ffb.server.step.StepParameter;
import com.balancedbytes.games.ffb.server.step.StepParameterKey;
import com.balancedbytes.games.ffb.server.step.StepParameterSet;
import com.balancedbytes.games.ffb.server.step.action.common.ApothecaryMode;
import com.balancedbytes.games.ffb.server.util.UtilInjury;

/**
 * Step in inducement sequence to handle spell effect.
 * 
 * Needs to be initialized with stepParameter PLAYER_ID.
 * Needs to be initialized with stepParameter ROLL_FOR_EFFECT.
 * Needs to be initialized with stepParameter SPECIAL_EFFECT.
 * 
 * Sets stepParameter END_TURN for all steps on the stack.
 * Sets stepParameter INJURY_RESULT for all steps on the stack.
 *
 * @author Kalimar
 */
public final class StepSpecialEffect extends AbstractStep {

	protected String fGotoLabelOnFailure;
	protected String fPlayerId;
	protected boolean fRollForEffect;
	protected SpecialEffect fSpecialEffect;
	
	public StepSpecialEffect(GameState pGameState) {
		super(pGameState);
	}
	
	public StepId getId() {
		return StepId.SPECIAL_EFFECT;
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
  			  // mandatory
  				case PLAYER_ID:
  					fPlayerId = (String) parameter.getValue();
  					break;
  			  // mandatory
  				case ROLL_FOR_EFFECT:
  					fRollForEffect = (parameter.getValue() != null) ? (Boolean) parameter.getValue() : false;
  					break;
  				// mandatory
  				case SPECIAL_EFFECT:
  					fSpecialEffect = (SpecialEffect) parameter.getValue();
  					break;
					default:
						break;
  			}
  		}
  	}
  	if (fGotoLabelOnFailure == null) {
			throw new StepException("StepParameter " + StepParameterKey.GOTO_LABEL_ON_FAILURE + " is not initialized.");
  	}
  	if (fPlayerId == null) {
			throw new StepException("StepParameter " + StepParameterKey.PLAYER_ID + " is not initialized.");
  	}
  	if (fSpecialEffect == null) {
			throw new StepException("StepParameter " + StepParameterKey.SPECIAL_EFFECT + " is not initialized.");
  	}
  }

  @Override
	public void start() {
		super.start();
		executeStep();
	}
	
  private void executeStep() {
		
  	Game game = getGameState().getGame();
		
		Player player = game.getPlayerById(fPlayerId);
		if (player != null) {
			
			boolean successful = true;
			
			if (fRollForEffect) {
				int roll = getGameState().getDiceRoller().rollWizardSpell();
				successful = DiceInterpreter.getInstance().isSpecialEffectSuccesful(fSpecialEffect, roll);
				getResult().addReport(new ReportSpecialEffectRoll(fSpecialEffect, player.getId(), roll, successful));
			} else {
				getResult().addReport(new ReportSpecialEffectRoll(fSpecialEffect, player.getId(), 0, true));
			}
			
			if (successful) {
				
				FieldCoordinate playerCoordinate = game.getFieldModel().getPlayerCoordinate(player);
				if (fSpecialEffect == SpecialEffect.LIGHTNING) {
					publishParameter(new StepParameter(StepParameterKey.INJURY_RESULT,
						UtilInjury.handleInjury(this, InjuryType.LIGHTNING, null, player, playerCoordinate, null, ApothecaryMode.SPECIAL_EFFECT)));
				}
				if (fSpecialEffect == SpecialEffect.FIREBALL) {
					publishParameter(new StepParameter(StepParameterKey.INJURY_RESULT,
						UtilInjury.handleInjury(this, InjuryType.FIREBALL, null, player, playerCoordinate, null, ApothecaryMode.SPECIAL_EFFECT)));
				}
				if (fSpecialEffect == SpecialEffect.BOMB) {
					publishParameter(new StepParameter(StepParameterKey.INJURY_RESULT,
						UtilInjury.handleInjury(this, InjuryType.BOMB, null, player, playerCoordinate, null, ApothecaryMode.SPECIAL_EFFECT)));
				}

				publishParameters(UtilInjury.dropPlayer(this, player));

				// check end turn
				Team actingTeam = game.isHomePlaying() ? game.getTeamHome() : game.getTeamAway();
				if ((TurnMode.BOMB_HOME == game.getTurnMode()) || (TurnMode.BOMB_HOME_BLITZ == game.getTurnMode())) {
					actingTeam = game.getTeamHome();
				}
				if ((TurnMode.BOMB_AWAY == game.getTurnMode()) || (TurnMode.BOMB_AWAY_BLITZ == game.getTurnMode())) {
					actingTeam = game.getTeamAway();
				}
				if (actingTeam.hasPlayer(player) && (fSpecialEffect != SpecialEffect.FIREBALL)) {
					publishParameter(new StepParameter(StepParameterKey.END_TURN, true));
				}

				getResult().setNextAction(StepAction.NEXT_STEP);
			
			} else {
				getResult().setNextAction(StepAction.GOTO_LABEL, fGotoLabelOnFailure);
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
  	pByteList.addString(fPlayerId);
  	pByteList.addBoolean(fRollForEffect);
  	pByteList.addByte((byte) ((fSpecialEffect != null) ? fSpecialEffect.getId() : 0));
  }
  
  @Override
  public int initFrom(ByteArray pByteArray) {
  	int byteArraySerializationVersion = super.initFrom(pByteArray);
  	fGotoLabelOnFailure = pByteArray.getString();
  	fPlayerId = pByteArray.getString();
  	fRollForEffect = pByteArray.getBoolean();
  	fSpecialEffect = SpecialEffect.fromId(pByteArray.getByte());
  	return byteArraySerializationVersion;
  }

}
