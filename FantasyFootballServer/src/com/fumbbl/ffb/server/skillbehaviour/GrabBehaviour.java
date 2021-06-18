package com.fumbbl.ffb.server.skillbehaviour;

import com.fumbbl.ffb.FieldCoordinate;
import com.fumbbl.ffb.PlayerAction;
import com.fumbbl.ffb.PushbackMode;
import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.RulesCollection.Rules;
import com.fumbbl.ffb.dialog.DialogSkillUseParameter;
import com.fumbbl.ffb.model.ActingPlayer;
import com.fumbbl.ffb.model.FieldModel;
import com.fumbbl.ffb.model.Game;
import com.fumbbl.ffb.model.property.NamedProperties;
import com.fumbbl.ffb.model.skill.Skill;
import com.fumbbl.ffb.net.commands.ClientCommandUseSkill;
import com.fumbbl.ffb.server.model.SkillBehaviour;
import com.fumbbl.ffb.server.model.StepModifier;
import com.fumbbl.ffb.server.step.StepCommandStatus;
import com.fumbbl.ffb.server.step.StepParameter;
import com.fumbbl.ffb.server.step.StepParameterKey;
import com.fumbbl.ffb.server.step.bb2016.StepPushback;
import com.fumbbl.ffb.server.step.bb2016.StepPushback.StepState;
import com.fumbbl.ffb.server.util.UtilServerDialog;
import com.fumbbl.ffb.server.util.UtilServerPushback;
import com.fumbbl.ffb.skill.Grab;
import com.fumbbl.ffb.util.ArrayTool;
import com.fumbbl.ffb.util.UtilCards;

@RulesCollection(Rules.COMMON)
public class GrabBehaviour extends SkillBehaviour<Grab> {
	public GrabBehaviour() {
		super();

		registerModifier(new StepModifier<StepPushback, StepPushback.StepState>(3) {

			@Override
			public StepCommandStatus handleCommandHook(StepPushback step, StepPushback.StepState state,
					ClientCommandUseSkill useSkillCommand) {
				state.grabbing = useSkillCommand.isSkillUsed();
				return StepCommandStatus.EXECUTE_STEP;
			}

			@Override
			public boolean handleExecuteStepHook(StepPushback step, StepState state) {
				Game game = step.getGameState().getGame();
				ActingPlayer actingPlayer = game.getActingPlayer();
				FieldModel fieldModel = game.getFieldModel();
				FieldCoordinate attackerCoordinate = fieldModel.getPlayerCoordinate(actingPlayer.getPlayer());
				FieldCoordinate defenderCoordinate = state.startingPushbackSquare.getCoordinate();
				Skill cancellingSkill = UtilCards.getSkillCancelling(state.defender, skill);
				boolean allowGrabOutsideBlock = actingPlayer.getPlayer().hasSkillProperty(NamedProperties.grabOutsideBlock);

				if (((state.grabbing == null) || state.grabbing) && state.freeSquareAroundDefender
						&& UtilCards.hasSkill(actingPlayer, skill) && attackerCoordinate.isAdjacent(defenderCoordinate)
						&& cancellingSkill == null && ((actingPlayer.getPlayerAction() == PlayerAction.BLOCK)
								|| (actingPlayer.getPlayerAction() == PlayerAction.MULTIPLE_BLOCK) || allowGrabOutsideBlock)) {
					if ((state.grabbing == null) && ArrayTool.isProvided(state.pushbackSquares)) {
						state.grabbing = true;
						for (int i = 0; i < state.pushbackSquares.length; i++) {
							if (fieldModel.getPlayer(state.pushbackSquares[i].getCoordinate()) != null) {
								state.grabbing = null;
								break;
							}
						}
					}
					if (state.grabbing == null) {
						UtilServerDialog.showDialog(step.getGameState(),
								new DialogSkillUseParameter(actingPlayer.getPlayerId(), skill, 0), false);
						state.grabbing = null;
					} else {
						if (state.grabbing) {
							state.pushbackMode = PushbackMode.GRAB;
							for (int i = 0; i < state.pushbackSquares.length; i++) {
								if (!state.pushbackSquares[i].isSelected()) {
									fieldModel.remove(state.pushbackSquares[i]);
								}
							}
							fieldModel
									.add(UtilServerPushback.findPushbackSquares(game, state.startingPushbackSquare, state.pushbackMode));
							state.grabbing = null;
						} else {
							state.grabbing = false;
						}
						step.publishParameter(new StepParameter(StepParameterKey.STARTING_PUSHBACK_SQUARE, null));
					}
					return true;
				}
				return false;
			}

		});
	}
}
