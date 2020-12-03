package com.balancedbytes.games.ffb.server.skillbehaviour;

import java.util.Set;

import com.balancedbytes.games.ffb.DodgeModifier;
import com.balancedbytes.games.ffb.DodgeModifierFactory;
import com.balancedbytes.games.ffb.DodgeModifiers;
import com.balancedbytes.games.ffb.PlayerChoiceMode;
import com.balancedbytes.games.ffb.SkillUse;
import com.balancedbytes.games.ffb.TurnMode;
import com.balancedbytes.games.ffb.dialog.DialogPlayerChoiceParameter;
import com.balancedbytes.games.ffb.model.ActingPlayer;
import com.balancedbytes.games.ffb.model.Game;
import com.balancedbytes.games.ffb.model.Player;
import com.balancedbytes.games.ffb.model.SkillConstants;
import com.balancedbytes.games.ffb.model.modifier.NamedProperties;
import com.balancedbytes.games.ffb.net.commands.ClientCommandUseSkill;
import com.balancedbytes.games.ffb.report.ReportSkillUse;
import com.balancedbytes.games.ffb.server.DiceInterpreter;
import com.balancedbytes.games.ffb.server.model.SkillBehaviour;
import com.balancedbytes.games.ffb.server.model.StepModifier;
import com.balancedbytes.games.ffb.server.step.StepAction;
import com.balancedbytes.games.ffb.server.step.StepCommandStatus;
import com.balancedbytes.games.ffb.server.step.StepParameter;
import com.balancedbytes.games.ffb.server.step.StepParameterKey;
import com.balancedbytes.games.ffb.server.step.action.move.StepDivingTackle;
import com.balancedbytes.games.ffb.server.step.action.move.StepDivingTackle.StepState;
import com.balancedbytes.games.ffb.server.util.UtilServerDialog;
import com.balancedbytes.games.ffb.skill.DivingTackle;
import com.balancedbytes.games.ffb.util.ArrayTool;
import com.balancedbytes.games.ffb.util.UtilPlayer;

public class DivingTackleBehaviour extends SkillBehaviour<DivingTackle> {
	public DivingTackleBehaviour() {
		super();

		registerModifier(new StepModifier<StepDivingTackle, StepDivingTackle.StepState>() {

			@Override
			public StepCommandStatus handleCommandHook(StepDivingTackle step, StepState state,
					ClientCommandUseSkill useSkillCommand) {
				return StepCommandStatus.EXECUTE_STEP;
			}

			@Override
			public boolean handleExecuteStepHook(StepDivingTackle step, StepState state) {
				 Game game = step.getGameState().getGame();
				    ActingPlayer actingPlayer = game.getActingPlayer();
				    if (state.usingDivingTackle == null) {
				      game.setDefenderId(null);
				      state.usingDivingTackle = false;
				      if (game.getFieldModel().getPlayer(state.coordinateFrom) == null) {
				        Player[] divingTacklers = UtilPlayer.findAdjacentOpposingPlayersWithProperty(game, state.coordinateFrom, NamedProperties.canAttemptToTackleDodgingPlayer, true);
				        divingTacklers = UtilPlayer.filterThrower(game, divingTacklers);
				        if (game.getTurnMode() == TurnMode.DUMP_OFF) {
				          divingTacklers = UtilPlayer.filterAttackerAndDefender(game, divingTacklers);
				        }
				        if (ArrayTool.isProvided(divingTacklers) && (state.dodgeRoll > 0)) {
				          DodgeModifierFactory modifierFactory = new DodgeModifierFactory();
				          Set<DodgeModifier> dodgeModifiers = modifierFactory.findDodgeModifiers(game, state.coordinateFrom, state.coordinateTo, 0);
				          dodgeModifiers.add(DodgeModifiers.DIVING_TACKLE);
				          if (state.usingBreakTackle) {
				            dodgeModifiers.add(DodgeModifiers.BREAK_TACKLE);
				          }
				          int minimumRoll = DiceInterpreter.getInstance().minimumRollDodge(game, actingPlayer.getPlayer(), dodgeModifiers);
				          int minimumRollWithoutBreakTackle = minimumRoll;
				          if (dodgeModifiers.contains(DodgeModifiers.BREAK_TACKLE)) {
				        	  dodgeModifiers.remove(DodgeModifiers.BREAK_TACKLE);
				        	  minimumRollWithoutBreakTackle = DiceInterpreter.getInstance().minimumRollDodge(game, actingPlayer.getPlayer(), dodgeModifiers);
				        	  dodgeModifiers.add(DodgeModifiers.BREAK_TACKLE);
				          }
				          if (!DiceInterpreter.getInstance().isSkillRollSuccessful(state.dodgeRoll, minimumRoll)) {
				            String teamId = game.isHomePlaying() ? game.getTeamAway().getId() : game.getTeamHome().getId();
				            UtilServerDialog.showDialog(step.getGameState(), new DialogPlayerChoiceParameter(teamId, PlayerChoiceMode.DIVING_TACKLE, divingTacklers, null, 1), true);
				            state.usingDivingTackle = null;
				          } else if (!DiceInterpreter.getInstance().isSkillRollSuccessful(state.dodgeRoll, minimumRollWithoutBreakTackle)) {
				        	  // Ask if Diving tackle is going to be used strictly to trigger Break Tackle. The dodge will still succeed.
				              String teamId = game.isHomePlaying() ? game.getTeamAway().getId() : game.getTeamHome().getId();
				              String[] descriptions = new String[] { "This will NOT trip the dodger, but will force the use of BREAK TACKLE." };
				              UtilServerDialog.showDialog(step.getGameState(), new DialogPlayerChoiceParameter(teamId, PlayerChoiceMode.DIVING_TACKLE, divingTacklers, descriptions, 1), true);
				              state.usingDivingTackle = null;
				          } else {
				        	  step.getResult().addReport(new ReportSkillUse(null, skill, false, SkillUse.WOULD_NOT_HELP));
				          }
				        }
				      }
				    }
				    if (state.usingDivingTackle != null) {
				    	step.publishParameter(new StepParameter(StepParameterKey.USING_DIVING_TACKLE, state.usingDivingTackle));
				      if (state.usingDivingTackle) {
				    	// Implicitly, a DT use is normally only triggered if it makes the dodge fail.
				    	  
				    	// Check if the dodge is successful with BT (ie. DT was used only to trigger BT)
				    	DodgeModifierFactory modifierFactory = new DodgeModifierFactory();
				    	Set<DodgeModifier> dodgeModifiers = modifierFactory.findDodgeModifiers(game, state.coordinateFrom, state.coordinateTo, 0);
				    	int minimumRoll = DiceInterpreter.getInstance().minimumRollDodge(game, actingPlayer.getPlayer(), dodgeModifiers);
				    	if (dodgeModifiers.contains(DodgeModifiers.BREAK_TACKLE) && DiceInterpreter.getInstance().isSkillRollSuccessful(state.dodgeRoll, minimumRoll)) {
				    		// This dodge will be successful with Break Tackle triggered, so mark it as used.
				    		state.usingBreakTackle = true;
				    	      actingPlayer.markSkillUsed(SkillConstants.BREAK_TACKLE);
				    	      step.publishParameter(new StepParameter(StepParameterKey.USING_BREAK_TACKLE, state.usingBreakTackle));
				    	}
				    	
				    	step.getResult().addReport(new ReportSkillUse(game.getDefender().getId(), skill, true, SkillUse.STOP_OPPONENT));
				    	step.getResult().setNextAction(StepAction.GOTO_LABEL, state.goToLabelOnSuccess);
				      } else {
				    	  step.getResult().setNextAction(StepAction.NEXT_STEP);
				      }
				    }
				return false;
			}
			
		});
	}
}