package com.fumbbl.ffb.server.step.bb2016.move;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.fumbbl.ffb.FactoryType;
import com.fumbbl.ffb.FieldCoordinate;
import com.fumbbl.ffb.PlayerAction;
import com.fumbbl.ffb.PlayerState;
import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.factory.IFactorySource;
import com.fumbbl.ffb.json.UtilJson;
import com.fumbbl.ffb.model.ActingPlayer;
import com.fumbbl.ffb.model.Game;
import com.fumbbl.ffb.model.property.NamedProperties;
import com.fumbbl.ffb.server.GameState;
import com.fumbbl.ffb.server.IServerJsonOption;
import com.fumbbl.ffb.server.factory.SequenceGeneratorFactory;
import com.fumbbl.ffb.server.step.AbstractStep;
import com.fumbbl.ffb.server.step.StepAction;
import com.fumbbl.ffb.server.step.StepId;
import com.fumbbl.ffb.server.step.StepParameter;
import com.fumbbl.ffb.server.step.UtilServerSteps;
import com.fumbbl.ffb.server.step.generator.BlitzBlock;
import com.fumbbl.ffb.server.step.generator.BlitzMove;
import com.fumbbl.ffb.server.step.generator.Block;
import com.fumbbl.ffb.server.step.generator.EndPlayerAction;
import com.fumbbl.ffb.server.step.generator.Foul;
import com.fumbbl.ffb.server.step.generator.KickTeamMate;
import com.fumbbl.ffb.server.step.generator.Move;
import com.fumbbl.ffb.server.step.generator.Pass;
import com.fumbbl.ffb.server.step.generator.Select;
import com.fumbbl.ffb.server.step.generator.SequenceGenerator;
import com.fumbbl.ffb.server.step.generator.ThrowTeamMate;
import com.fumbbl.ffb.server.util.UtilServerDialog;
import com.fumbbl.ffb.util.UtilPlayer;

/**
 * Last step in select sequence. Consumes all expected stepParameters.
 * <p>
 * Expects stepParameter BLOCK_DEFENDER_ID to be set by a preceding step.
 * Expects stepParameter DISPATCH_PLAYER_ACTION to be set by a preceding step.
 * Expects stepParameter END_PLAYER_ACTION to be set by a preceding step.
 * Expects stepParameter END_TURN to be set by a preceding step. Expects
 * stepParameter FOUL_DEFENDER_ID to be set by a preceding step. Expects
 * stepParameter GAZE_VICTIM_ID to be set by a preceding step. Expects
 * stepParameter HAIL_MARY_PASS to be set by a preceding step. Expects
 * stepParameter MOVE_STACK to be set by a preceding step. Expects stepParameter
 * TARGET_COORDINATE to be set by a preceding step. Expects stepParameter
 * THROWN_PLAYER_ID to be set by a preceding step. Expects stepParameter
 * USING_STAB to be set by a preceding step.
 * <p>
 * Will push a new sequence on the stack.
 *
 * @author Kalimar
 */
@RulesCollection(RulesCollection.Rules.BB2016)
public final class StepEndSelecting extends AbstractStep {

	private boolean fEndTurn;
	private boolean fEndPlayerAction;
	private PlayerAction fDispatchPlayerAction;
	// moveSequence
	private FieldCoordinate[] fMoveStack;
	private String fGazeVictimId;
	// blockSequence
	private String fBlockDefenderId;
	private Boolean fUsingStab;
	// foulSequence
	private String fFoulDefenderId;
	// passSequence + throwTeamMateSequence
	private FieldCoordinate fTargetCoordinate;
	private boolean fHailMaryPass;
	private String fThrownPlayerId;
	private String fKickedPlayerId;
	private int fNumDice;

	public StepEndSelecting(GameState pGameState) {
		super(pGameState);
	}

	public StepId getId() {
		return StepId.END_SELECTING;
	}

	@Override
	public void start() {
		super.start();
		executeStep();
	}

	@Override
	public boolean setParameter(StepParameter parameter) {
		if ((parameter != null) && !super.setParameter(parameter)) {
			switch (parameter.getKey()) {
				case BLOCK_DEFENDER_ID:
					fBlockDefenderId = (String) parameter.getValue();
					consume(parameter);
					return true;
				case DISPATCH_PLAYER_ACTION:
					fDispatchPlayerAction = (PlayerAction) parameter.getValue();
					consume(parameter);
					return true;
				case END_PLAYER_ACTION:
					fEndPlayerAction = (parameter.getValue() != null) ? (Boolean) parameter.getValue() : false;
					consume(parameter);
					return true;
				case END_TURN:
					fEndTurn = (parameter.getValue() != null) ? (Boolean) parameter.getValue() : false;
					consume(parameter);
					return true;
				case FOUL_DEFENDER_ID:
					fFoulDefenderId = (String) parameter.getValue();
					consume(parameter);
					return true;
				case GAZE_VICTIM_ID:
					fGazeVictimId = (String) parameter.getValue();
					consume(parameter);
					return true;
				case HAIL_MARY_PASS:
					fHailMaryPass = (parameter.getValue() != null) ? (Boolean) parameter.getValue() : false;
					consume(parameter);
					return true;
				case MOVE_STACK:
					fMoveStack = (FieldCoordinate[]) parameter.getValue();
					consume(parameter);
					return true;
				case TARGET_COORDINATE:
					fTargetCoordinate = (FieldCoordinate) parameter.getValue();
					consume(parameter);
					return true;
				case THROWN_PLAYER_ID:
					fThrownPlayerId = (String) parameter.getValue();
					consume(parameter);
					return true;
				case KICKED_PLAYER_ID:
					fKickedPlayerId = (String) parameter.getValue();
					consume(parameter);
					return true;
				case NR_OF_DICE:
					fNumDice = (parameter.getValue() != null) ? (Integer) parameter.getValue() : 0;
					consume(parameter);
					return true;
				case USING_STAB:
					fUsingStab = (parameter.getValue() != null) ? (Boolean) parameter.getValue() : false;
					consume(parameter);
					return true;
				default:
					break;
			}
		}
		return false;
	}

	private void executeStep() {
		UtilServerDialog.hideDialog(getGameState());
		Game game = getGameState().getGame();
		ActingPlayer actingPlayer = game.getActingPlayer();
		if (fEndTurn || fEndPlayerAction) {
			SequenceGeneratorFactory factory = game.getFactory(FactoryType.Factory.SEQUENCE_GENERATOR);
			((EndPlayerAction) factory.forName(SequenceGenerator.Type.EndPlayerAction.name()))
					.pushSequence(new EndPlayerAction.SequenceParams(getGameState(), true, true, fEndTurn));
		} else if (actingPlayer.isSufferingBloodLust()) {
			if (fDispatchPlayerAction != null) {
				if (!fDispatchPlayerAction.isMoving()) {
					fDispatchPlayerAction = PlayerAction.MOVE;
				}
				dispatchPlayerAction(fDispatchPlayerAction, false);
			} else {
				if ((actingPlayer.getPlayerAction() != null) && !actingPlayer.getPlayerAction().isMoving()) {
					UtilServerSteps.changePlayerAction(this, actingPlayer.getPlayerId(), PlayerAction.MOVE,
							actingPlayer.isJumping());
				}
				dispatchPlayerAction(actingPlayer.getPlayerAction(), false);
			}
		} else if (fDispatchPlayerAction != null) {
			dispatchPlayerAction(fDispatchPlayerAction, true);
		} else {
			dispatchPlayerAction(actingPlayer.getPlayerAction(), false);
		}
		getResult().setNextAction(StepAction.NEXT_STEP);
	}

	private void dispatchPlayerAction(PlayerAction pPlayerAction, boolean pWithParameter) {
		Game game = getGameState().getGame();
		SequenceGeneratorFactory factory = game.getFactory(FactoryType.Factory.SEQUENCE_GENERATOR);
		PlayerState playerState = game.getFieldModel().getPlayerState(game.getActingPlayer().getPlayer());

		if (pPlayerAction == null || (pPlayerAction == PlayerAction.MOVE && playerState.isRooted() && UtilPlayer.canGaze(game, game.getActingPlayer().getPlayer()))) {
			((Select) factory.forName(SequenceGenerator.Type.Select.name()))
				.pushSequence(new Select.SequenceParams(getGameState(), false));
			return;
		}
		Pass passGenerator = (Pass) factory.forName(SequenceGenerator.Type.Pass.name());
		ThrowTeamMate ttmGenerator = (ThrowTeamMate) factory.forName(SequenceGenerator.Type.ThrowTeamMate.name());
		KickTeamMate ktmGenerator = (KickTeamMate) factory.forName(SequenceGenerator.Type.KickTeamMate.name());
		Block blockGenerator = (Block) factory.forName(SequenceGenerator.Type.Block.name());
		Foul foulGenerator = (Foul) factory.forName(SequenceGenerator.Type.Foul.name());
		Move moveGenerator = (Move) factory.forName(SequenceGenerator.Type.Move.name());
		EndPlayerAction endGenerator = (EndPlayerAction) factory.forName(SequenceGenerator.Type.EndPlayerAction.name());
		EndPlayerAction.SequenceParams endParams = new EndPlayerAction.SequenceParams(getGameState(), true, true, false);
		BlitzMove blitzMoveGenerator = (BlitzMove) factory.forName(SequenceGenerator.Type.BlitzMove.name());
		BlitzBlock blitzBlockGenerator = (BlitzBlock) factory.forName(SequenceGenerator.Type.BlitzBlock.name());

		ActingPlayer actingPlayer = game.getActingPlayer();
		switch (pPlayerAction) {
			case PASS:
			case HAIL_MARY_PASS:
			case THROW_BOMB:
			case HAIL_MARY_BOMB:
			case HAND_OVER:
				if (pWithParameter) {
					passGenerator.pushSequence(new Pass.SequenceParams(getGameState(), fTargetCoordinate));
				} else {
					passGenerator.pushSequence(new Pass.SequenceParams(getGameState()));
				}
				break;
			case THROW_TEAM_MATE:
				if (pWithParameter) {
					ttmGenerator.pushSequence(new ThrowTeamMate.SequenceParams(getGameState(), fThrownPlayerId, fTargetCoordinate, false));
				} else {
					ttmGenerator.pushSequence(new ThrowTeamMate.SequenceParams(getGameState()));
				}
				break;
			case KICK_TEAM_MATE:
				if (pWithParameter) {
					ktmGenerator.pushSequence(new KickTeamMate.SequenceParams(getGameState(), fNumDice, fKickedPlayerId));
				} else {
					ktmGenerator.pushSequence(new KickTeamMate.SequenceParams(getGameState()));
				}
				break;
			case BLITZ:
				if (pWithParameter) {
					blitzBlockGenerator.pushSequence(new BlitzBlock.SequenceParams(getGameState(), fBlockDefenderId, fUsingStab, null));
				} else {
					blitzBlockGenerator.pushSequence(new BlitzBlock.SequenceParams(getGameState()));
				}
				break;
			case BLOCK:
			case MULTIPLE_BLOCK:
				if (pWithParameter) {
					blockGenerator.pushSequence(new Block.Builder(getGameState()).withDefenderId(fBlockDefenderId).useStab(fUsingStab).build());
				} else {
					blockGenerator.pushSequence(new Block.Builder(getGameState()).build());
				}
				break;
			case FOUL:
				if (pWithParameter) {
					foulGenerator.pushSequence(new Foul.SequenceParams(getGameState(), fFoulDefenderId, false));
				} else {
					foulGenerator.pushSequence(new Foul.SequenceParams(getGameState()));
				}
				break;
			case MOVE:
				if (game.getFieldModel().getPlayerState(game.getActingPlayer().getPlayer()).isRooted()) {
					endGenerator.pushSequence(endParams);
					break;
				}
				// fall through
			case FOUL_MOVE:
			case PASS_MOVE:
			case THROW_TEAM_MATE_MOVE:
			case KICK_TEAM_MATE_MOVE:
			case HAND_OVER_MOVE:
			case GAZE:
				if (pWithParameter) {
					moveGenerator.pushSequence(new Move.SequenceParams(getGameState(), fMoveStack, fGazeVictimId, null));
				} else {
					moveGenerator.pushSequence(new Move.SequenceParams(getGameState()));
				}
				break;
			case BLITZ_MOVE:
				if (pWithParameter) {
					blitzMoveGenerator.pushSequence(new BlitzMove.SequenceParams(getGameState(), fMoveStack, fGazeVictimId, null));
				} else {
					blitzMoveGenerator.pushSequence(new BlitzMove.SequenceParams(getGameState()));
				}
				break;
			case REMOVE_CONFUSION:
				actingPlayer.setHasMoved(true);
				endGenerator.pushSequence(endParams);
				break;
			case STAND_UP:
				if (actingPlayer.getPlayer().hasSkillProperty(NamedProperties.inflictsConfusion)) {
					moveGenerator.pushSequence(new Move.SequenceParams(getGameState()));
				} else {
					endGenerator.pushSequence(endParams);
				}
				break;
			case STAND_UP_BLITZ:
				game.getTurnData().setBlitzUsed(true);
				endGenerator.pushSequence(endParams);
				break;
			default:
				throw new IllegalStateException("Unhandled player action " + pPlayerAction.getName() + ".");
		}
	}

	// JSON serialization

	@Override
	public JsonObject toJsonValue() {
		JsonObject jsonObject = super.toJsonValue();
		IServerJsonOption.END_TURN.addTo(jsonObject, fEndTurn);
		IServerJsonOption.END_PLAYER_ACTION.addTo(jsonObject, fEndPlayerAction);
		IServerJsonOption.DISPATCH_PLAYER_ACTION.addTo(jsonObject, fDispatchPlayerAction);
		IServerJsonOption.MOVE_STACK.addTo(jsonObject, fMoveStack);
		IServerJsonOption.GAZE_VICTIM_ID.addTo(jsonObject, fGazeVictimId);
		IServerJsonOption.BLOCK_DEFENDER_ID.addTo(jsonObject, fBlockDefenderId);
		IServerJsonOption.USING_STAB.addTo(jsonObject, fUsingStab);
		IServerJsonOption.FOUL_DEFENDER_ID.addTo(jsonObject, fFoulDefenderId);
		IServerJsonOption.TARGET_COORDINATE.addTo(jsonObject, fTargetCoordinate);
		IServerJsonOption.HAIL_MARY_PASS.addTo(jsonObject, fHailMaryPass);
		IServerJsonOption.THROWN_PLAYER_ID.addTo(jsonObject, fThrownPlayerId);
		IServerJsonOption.KICKED_PLAYER_ID.addTo(jsonObject, fKickedPlayerId);
		IServerJsonOption.NR_OF_DICE.addTo(jsonObject, fNumDice);
		return jsonObject;
	}

	@Override
	public StepEndSelecting initFrom(IFactorySource source, JsonValue jsonValue) {
		super.initFrom(source, jsonValue);
		JsonObject jsonObject = UtilJson.toJsonObject(jsonValue);
		fEndTurn = IServerJsonOption.END_TURN.getFrom(source, jsonObject);
		fEndPlayerAction = IServerJsonOption.END_PLAYER_ACTION.getFrom(source, jsonObject);
		fDispatchPlayerAction = (PlayerAction) IServerJsonOption.DISPATCH_PLAYER_ACTION.getFrom(source, jsonObject);
		fMoveStack = IServerJsonOption.MOVE_STACK.getFrom(source, jsonObject);
		fGazeVictimId = IServerJsonOption.GAZE_VICTIM_ID.getFrom(source, jsonObject);
		fBlockDefenderId = IServerJsonOption.BLOCK_DEFENDER_ID.getFrom(source, jsonObject);
		fUsingStab = IServerJsonOption.USING_STAB.getFrom(source, jsonObject);
		fFoulDefenderId = IServerJsonOption.FOUL_DEFENDER_ID.getFrom(source, jsonObject);
		fTargetCoordinate = IServerJsonOption.TARGET_COORDINATE.getFrom(source, jsonObject);
		fHailMaryPass = IServerJsonOption.HAIL_MARY_PASS.getFrom(source, jsonObject);
		fThrownPlayerId = IServerJsonOption.THROWN_PLAYER_ID.getFrom(source, jsonObject);
		fKickedPlayerId = IServerJsonOption.KICKED_PLAYER_ID.getFrom(source, jsonObject);
		fNumDice = IServerJsonOption.NR_OF_DICE.getFrom(source, jsonObject);
		return this;
	}

}
