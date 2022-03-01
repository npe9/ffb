package com.fumbbl.ffb.server.step.bb2020;

import com.fumbbl.ffb.ApothecaryMode;
import com.fumbbl.ffb.FieldCoordinate;
import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.SkillUse;
import com.fumbbl.ffb.SoundId;
import com.fumbbl.ffb.model.ActingPlayer;
import com.fumbbl.ffb.model.Game;
import com.fumbbl.ffb.model.Player;
import com.fumbbl.ffb.model.property.NamedProperties;
import com.fumbbl.ffb.model.skill.Skill;
import com.fumbbl.ffb.report.ReportSkillUse;
import com.fumbbl.ffb.server.GameState;
import com.fumbbl.ffb.server.InjuryResult;
import com.fumbbl.ffb.server.injury.injuryType.InjuryTypeStab;
import com.fumbbl.ffb.server.model.DropPlayerContext;
import com.fumbbl.ffb.server.step.AbstractStep;
import com.fumbbl.ffb.server.step.StepAction;
import com.fumbbl.ffb.server.step.StepId;
import com.fumbbl.ffb.server.step.StepParameter;
import com.fumbbl.ffb.server.step.StepParameterKey;
import com.fumbbl.ffb.server.util.UtilServerInjury;
import com.fumbbl.ffb.util.UtilCards;
import com.fumbbl.ffb.util.UtilPlayer;

import java.util.Arrays;
import java.util.Optional;

@RulesCollection(RulesCollection.Rules.BB2020)
public class StepTreacherous extends AbstractStep {
	public StepTreacherous(GameState pGameState) {
		super(pGameState);
	}

	@Override
	public StepId getId() {
		return StepId.TREACHEROUS;
	}

	@Override
	public void start() {
		super.start();
		executeStep();
	}

	private void executeStep() {
		Game game = getGameState().getGame();
		ActingPlayer actingPlayer = game.getActingPlayer();
		Skill skill = UtilCards.getUnusedSkillWithProperty(actingPlayer, NamedProperties.canStabTeamMateForBall);
		if (skill != null) {
			treacherousTarget(game, actingPlayer).ifPresent(player -> {
				FieldCoordinate playerCoordinate = game.getFieldModel().getPlayerCoordinate(actingPlayer.getPlayer());
				game.getFieldModel().setBallCoordinate(playerCoordinate);
				getResult().addReport(new ReportSkillUse(actingPlayer.getPlayerId(), skill, true, SkillUse.TREACHEROUS));

				getResult().setSound(SoundId.STAB);
				FieldCoordinate defenderCoordinate = game.getFieldModel().getPlayerCoordinate(player);
				InjuryResult injuryResultDefender = UtilServerInjury.handleInjury(this, new InjuryTypeStab(true),
					actingPlayer.getPlayer(), player, defenderCoordinate, null, null, ApothecaryMode.DEFENDER);

				publishParameter(new StepParameter(StepParameterKey.DROP_PLAYER_CONTEXT,
					new DropPlayerContext(injuryResultDefender, false, false, null,
						player.getId(), ApothecaryMode.DEFENDER, false)));
			});

			actingPlayer.markSkillUsed(skill);

			switch (actingPlayer.getPlayerAction()) {
				case BLITZ:
				case BLITZ_MOVE:
					game.getTurnData().setBlitzUsed(true);
					break;
				case KICK_TEAM_MATE:
				case KICK_TEAM_MATE_MOVE:
					game.getTurnData().setKtmUsed(true);
					break;
				case PASS:
				case PASS_MOVE:
				case THROW_TEAM_MATE:
				case THROW_TEAM_MATE_MOVE:
					game.getTurnData().setPassUsed(true);
					break;
				case HAND_OVER:
				case HAND_OVER_MOVE:
					game.getTurnData().setHandOverUsed(true);
					break;
				case FOUL:
				case FOUL_MOVE:
					if (!actingPlayer.getPlayer().hasSkillProperty(NamedProperties.allowsAdditionalFoul)) {
						game.getTurnData().setFoulUsed(true);
					}
					break;
				default:
					break;
			}
		}

		getResult().setNextAction(StepAction.NEXT_STEP);
	}

	private Optional<Player<?>> treacherousTarget(Game game, ActingPlayer actingPlayer) {
		if (!actingPlayer.hasActed()) {
			return Arrays.stream(UtilPlayer.findAdjacentBlockablePlayers(game, game.getActingTeam(), game.getFieldModel().getPlayerCoordinate(actingPlayer.getPlayer())))
				.filter(adjacentPlayer -> UtilPlayer.hasBall(game, adjacentPlayer)).findFirst();
		}
		return Optional.empty();
	}
}
