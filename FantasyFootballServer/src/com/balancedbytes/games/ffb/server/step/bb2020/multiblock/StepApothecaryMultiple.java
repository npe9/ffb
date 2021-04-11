package com.balancedbytes.games.ffb.server.step.bb2020.multiblock;

import com.balancedbytes.games.ffb.ApothecaryMode;
import com.balancedbytes.games.ffb.ApothecaryStatus;
import com.balancedbytes.games.ffb.CardEffect;
import com.balancedbytes.games.ffb.FactoryType;
import com.balancedbytes.games.ffb.PlayerState;
import com.balancedbytes.games.ffb.PlayerType;
import com.balancedbytes.games.ffb.RulesCollection;
import com.balancedbytes.games.ffb.SeriousInjury;
import com.balancedbytes.games.ffb.dialog.DialogApothecaryChoiceParameter;
import com.balancedbytes.games.ffb.dialog.DialogUseApothecaryParameter;
import com.balancedbytes.games.ffb.dialog.DialogUseIgorParameter;
import com.balancedbytes.games.ffb.factory.IFactorySource;
import com.balancedbytes.games.ffb.inducement.Usage;
import com.balancedbytes.games.ffb.json.UtilJson;
import com.balancedbytes.games.ffb.mechanics.Mechanic;
import com.balancedbytes.games.ffb.model.Game;
import com.balancedbytes.games.ffb.model.InducementSet;
import com.balancedbytes.games.ffb.model.Player;
import com.balancedbytes.games.ffb.model.Team;
import com.balancedbytes.games.ffb.model.property.NamedProperties;
import com.balancedbytes.games.ffb.net.commands.ClientCommandApothecaryChoice;
import com.balancedbytes.games.ffb.net.commands.ClientCommandUseApothecary;
import com.balancedbytes.games.ffb.net.commands.ClientCommandUseInducement;
import com.balancedbytes.games.ffb.report.ReportApothecaryChoice;
import com.balancedbytes.games.ffb.report.ReportApothecaryRoll;
import com.balancedbytes.games.ffb.report.ReportInducement;
import com.balancedbytes.games.ffb.server.GameState;
import com.balancedbytes.games.ffb.server.IServerJsonOption;
import com.balancedbytes.games.ffb.server.InjuryResult;
import com.balancedbytes.games.ffb.server.mechanic.RollMechanic;
import com.balancedbytes.games.ffb.server.net.ReceivedCommand;
import com.balancedbytes.games.ffb.server.step.AbstractStep;
import com.balancedbytes.games.ffb.server.step.StepAction;
import com.balancedbytes.games.ffb.server.step.StepCommandStatus;
import com.balancedbytes.games.ffb.server.step.StepException;
import com.balancedbytes.games.ffb.server.step.StepId;
import com.balancedbytes.games.ffb.server.step.StepParameter;
import com.balancedbytes.games.ffb.server.step.StepParameterKey;
import com.balancedbytes.games.ffb.server.step.StepParameterSet;
import com.balancedbytes.games.ffb.server.util.UtilServerDialog;
import com.balancedbytes.games.ffb.server.util.UtilServerInducementUse;
import com.balancedbytes.games.ffb.server.util.UtilServerInjury;
import com.balancedbytes.games.ffb.util.StringTool;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

@RulesCollection(RulesCollection.Rules.COMMON)
public class StepApothecaryMultiple extends AbstractStep {

	private ApothecaryMode fApothecaryMode;
	private InjuryResult fInjuryResult;
	private boolean fShowReport;
	private boolean fDefenderPoisoned;
	private boolean fAttackerPoisoned;

	public StepApothecaryMultiple(GameState pGameState) {
		super(pGameState);
		fShowReport = true;
	}

	public StepId getId() {
		return StepId.APOTHECARY;
	}

	@Override
	public void init(StepParameterSet pParameterSet) {
		if (pParameterSet != null) {
			for (StepParameter parameter : pParameterSet.values()) {
				// mandatory
				if (parameter.getKey() == StepParameterKey.APOTHECARY_MODE) {
					fApothecaryMode = (ApothecaryMode) parameter.getValue();
				}
			}
		}
		if (fApothecaryMode == null) {
			throw new StepException("StepParameter " + StepParameterKey.APOTHECARY_MODE + " is not initialized.");
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
		if (commandStatus == StepCommandStatus.UNHANDLED_COMMAND) {
			switch (pReceivedCommand.getId()) {
				case CLIENT_APOTHECARY_CHOICE:
					ClientCommandApothecaryChoice apothecaryChoiceCommand = (ClientCommandApothecaryChoice) pReceivedCommand
						.getCommand();
					if ((fInjuryResult != null) && StringTool.isEqual(apothecaryChoiceCommand.getPlayerId(),
						fInjuryResult.injuryContext().getDefenderId())) {
						handleApothecaryChoice(apothecaryChoiceCommand.getPlayerState(), apothecaryChoiceCommand.getSeriousInjury());
						commandStatus = StepCommandStatus.EXECUTE_STEP;
					}
					break;
				case CLIENT_USE_APOTHECARY:
					ClientCommandUseApothecary useApothecaryCommand = (ClientCommandUseApothecary) pReceivedCommand.getCommand();
					if ((fInjuryResult != null)
						&& StringTool.isEqual(useApothecaryCommand.getPlayerId(), fInjuryResult.injuryContext().getDefenderId())) {
						fInjuryResult.injuryContext()
							.setApothecaryStatus(useApothecaryCommand.isApothecaryUsed() ? ApothecaryStatus.USE_APOTHECARY
								: ApothecaryStatus.DO_NOT_USE_APOTHECARY);
						commandStatus = StepCommandStatus.EXECUTE_STEP;
					}
					break;
				case CLIENT_USE_INDUCEMENT:
					ClientCommandUseInducement inducementCommand = (ClientCommandUseInducement) pReceivedCommand.getCommand();
					if (inducementCommand.getInducementType().getUsage() == Usage.REGENERATION) {
						if ((fInjuryResult != null)
							&& (fInjuryResult.injuryContext().getApothecaryStatus() == ApothecaryStatus.WAIT_FOR_IGOR_USE)) {
							if (inducementCommand.hasPlayerId(fInjuryResult.injuryContext().getDefenderId())) {
								fInjuryResult.injuryContext().setApothecaryStatus(ApothecaryStatus.USE_IGOR);
							} else {
								fInjuryResult.injuryContext().setApothecaryStatus(ApothecaryStatus.DO_NOT_USE_IGOR);
							}
							commandStatus = StepCommandStatus.EXECUTE_STEP;
						}
					}
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

	@Override
	public boolean setParameter(StepParameter pParameter) {
		if ((pParameter != null) && !super.setParameter(pParameter)) {
			switch (pParameter.getKey()) {
				case INJURY_RESULT:
					InjuryResult injuryResult = (InjuryResult) pParameter.getValue();
					if ((injuryResult != null) && (fApothecaryMode == injuryResult.injuryContext().getApothecaryMode())) {
						fInjuryResult = injuryResult;
						return true;
					}
					return false;
				case USING_PILING_ON:
					Boolean usingPilingOn = (Boolean) pParameter.getValue();
					if ((ApothecaryMode.DEFENDER == fApothecaryMode) && (usingPilingOn != null) && !usingPilingOn) {
						fShowReport = false;
						return true;
					}
					return false;
				case DEFENDER_POISONED:
					fDefenderPoisoned = (Boolean) pParameter.getValue();
					return fApothecaryMode == ApothecaryMode.DEFENDER;
				case ATTACKER_POISONED:
					fAttackerPoisoned = (Boolean) pParameter.getValue();
					return fApothecaryMode == ApothecaryMode.ATTACKER;
				default:
					break;
			}
		}
		return false;
	}

	private void executeStep() {
		if (fInjuryResult == null) {
			getResult().setNextAction(StepAction.NEXT_STEP);
		} else {
			UtilServerDialog.hideDialog(getGameState());
			boolean doNextStep = true;
			Game game = getGameState().getGame();
			if (fInjuryResult.injuryContext().getApothecaryStatus() != null) {
				switch (fInjuryResult.injuryContext().getApothecaryStatus()) {
					case DO_REQUEST:
						if (fShowReport) {
							fInjuryResult.report(this);
						}
						UtilServerDialog.showDialog(getGameState(),
							new DialogUseApothecaryParameter(fInjuryResult.injuryContext().getDefenderId(),
								fInjuryResult.injuryContext().getPlayerState(), fInjuryResult.injuryContext().getSeriousInjury()),
							true);
						fInjuryResult.injuryContext().setApothecaryStatus(ApothecaryStatus.WAIT_FOR_APOTHECARY_USE);
						doNextStep = false;
						break;
					case USE_APOTHECARY:
						if (rollApothecary()) {
							fInjuryResult.injuryContext().setApothecaryStatus(ApothecaryStatus.WAIT_FOR_APOTHECARY_USE);
							doNextStep = false;
						} else {
							fInjuryResult.injuryContext().setApothecaryStatus(ApothecaryStatus.RESULT_CHOICE);
						}
						break;
					case DO_NOT_USE_APOTHECARY:
						getResult()
							.addReport(new ReportApothecaryRoll(fInjuryResult.injuryContext().getDefenderId(), null, null, null));
						break;
					case NO_APOTHECARY:
						if (fShowReport) {
							fInjuryResult.report(this);
						}
						break;
					default:
						break;
				}
			}
			if (doNextStep) {
				Player<?> player = game.getPlayerById(fInjuryResult.injuryContext().getDefenderId());
				switch (fInjuryResult.injuryContext().getApothecaryStatus()) {
					case DO_NOT_USE_IGOR:
						break;
					case USE_IGOR:
						Team team = game.getTeamHome().hasPlayer(player) ? game.getTeamHome() : game.getTeamAway();
						InducementSet inducementSetIgor = game.isHomePlaying() ? game.getTurnDataHome().getInducementSet() : game.getTurnDataAway().getInducementSet();
						inducementSetIgor.getInducementMapping().keySet().stream().filter(type -> type.getUsage() == Usage.REGENERATION)
							.findFirst().ifPresent(type -> {
							UtilServerInducementUse.useInducement(getGameState(), team, type, 1);
							getResult().addReport(new ReportInducement(team.getId(), type, 0));
							boolean success = UtilServerInjury.handleRegeneration(this, player);
							if (success) {
								curePoison();
							}
						});
						break;
					default:
						fInjuryResult.applyTo(this);
						PlayerState playerState = game.getFieldModel().getPlayerState(player);
						if ((playerState != null) && playerState.isCasualty()
							&& player.hasSkillProperty(NamedProperties.canRollToSaveFromInjury)
							&& (fInjuryResult.injuryContext().getInjuryType().canUseApo())) {
							if (!UtilServerInjury.handleRegeneration(this, player)) {
								InducementSet inducementSet = game.getTeamHome().hasPlayer(player)
									? game.getTurnDataHome().getInducementSet()
									: game.getTurnDataAway().getInducementSet();
								boolean hasInducement = inducementSet.getInducementMapping().keySet().stream().anyMatch(type -> type.getUsage() == Usage.REGENERATION
									&& inducementSet.hasUsesLeft(type));
									if (hasInducement && player.getPlayerType() != PlayerType.STAR) {
										game.setDialogParameter(new DialogUseIgorParameter(player.getId()));
										fInjuryResult.injuryContext().setApothecaryStatus(ApothecaryStatus.WAIT_FOR_IGOR_USE);
										doNextStep = false;
									}
							} else {
								curePoison();
							}
						}
						break;
				}
			}
			if (doNextStep) {
				UtilServerInjury.handleRaiseDead(this, fInjuryResult);
				getResult().setNextAction(StepAction.NEXT_STEP);
			}
		}
	}

	private boolean rollApothecary() {
		Game game = getGameState().getGame();
		Player<?> defender = game.getPlayerById(fInjuryResult.injuryContext().getDefenderId());
		if (game.getTeamHome().hasPlayer(defender)) {
			game.getTurnDataHome().useApothecary();
		} else {
			game.getTurnDataAway().useApothecary();
		}
		boolean apothecaryChoice = ((fInjuryResult.injuryContext().getPlayerState().getBase() != PlayerState.BADLY_HURT)
			&& (fInjuryResult.injuryContext().getPlayerState().getBase() != PlayerState.KNOCKED_OUT));
		if (apothecaryChoice) {
			RollMechanic rollMechanic = ((RollMechanic) game.getFactory(FactoryType.Factory.MECHANIC).forName(Mechanic.Type.ROLL.name()));
			InjuryResult newInjuryResult = new InjuryResult();
			newInjuryResult.injuryContext().setDefenderId(fInjuryResult.injuryContext().getDefenderId());
			newInjuryResult.injuryContext().setCasualtyRoll(rollMechanic.rollCasualty(getGameState().getDiceRoller()));
			newInjuryResult.injuryContext().setInjury(
				rollMechanic.interpretCasualtyRollAndAddModifiers(game, newInjuryResult.injuryContext(), game.getPlayerById(fInjuryResult.injuryContext().getDefenderId())));
			newInjuryResult.injuryContext().setSeriousInjury(
				rollMechanic.interpretSeriousInjuryRoll(newInjuryResult.injuryContext()));
			apothecaryChoice = (newInjuryResult.injuryContext().getPlayerState().getBase() != PlayerState.BADLY_HURT);
			getResult()
				.addReport(new ReportApothecaryRoll(defender.getId(), newInjuryResult.injuryContext().getCasualtyRoll(),
					newInjuryResult.injuryContext().getPlayerState(), newInjuryResult.injuryContext().getSeriousInjury()));
			if (apothecaryChoice) {
				UtilServerDialog.showDialog(getGameState(),
					new DialogApothecaryChoiceParameter(defender.getId(), fInjuryResult.injuryContext().getPlayerState(),
						fInjuryResult.injuryContext().getSeriousInjury(), newInjuryResult.injuryContext().getPlayerState(),
						newInjuryResult.injuryContext().getSeriousInjury()),
					true);
			}
		}
		if (!apothecaryChoice) {
			fInjuryResult.injuryContext().setSeriousInjury(null);
			if ((fInjuryResult.injuryContext().getPlayerState().getBase() == PlayerState.KNOCKED_OUT)
				&& (fInjuryResult.injuryContext().getInjuryType().canApoKoIntoStun())) {
				fInjuryResult.injuryContext().setInjury(new PlayerState(PlayerState.STUNNED));
			} else {
				curePoison();
				fInjuryResult.injuryContext().setInjury(new PlayerState(PlayerState.RESERVE));
			}
			getResult().addReport(
				new ReportApothecaryChoice(defender.getId(), fInjuryResult.injuryContext().getPlayerState(), null));
		}
		return apothecaryChoice;
	}

	private void curePoison() {
		Game game = getGameState().getGame();
		Player<?> player = game.getPlayerById(fInjuryResult.injuryContext().getDefenderId());
		if (fDefenderPoisoned && fApothecaryMode == ApothecaryMode.DEFENDER) {
			game.getFieldModel().removeCardEffect(player, CardEffect.POISONED);
		} else if (fAttackerPoisoned && fApothecaryMode == ApothecaryMode.ATTACKER) {
			game.getFieldModel().removeCardEffect(player, CardEffect.POISONED);
		}
	}

	private void handleApothecaryChoice(PlayerState pPlayerState, SeriousInjury pSeriousInjury) {
		if (fInjuryResult != null) {
			if (pPlayerState.getBase() == PlayerState.BADLY_HURT) {
				fInjuryResult.injuryContext().setInjury(new PlayerState(PlayerState.RESERVE));
				fInjuryResult.injuryContext().setSeriousInjury(null);
			} else {
				fInjuryResult.injuryContext().setInjury(pPlayerState);
				fInjuryResult.injuryContext().setSeriousInjury(pSeriousInjury);
			}
			fInjuryResult.injuryContext().setApothecaryStatus(ApothecaryStatus.RESULT_CHOICE);
		}
	}

	// JSON serialization

	@Override
	public JsonObject toJsonValue() {
		JsonObject jsonObject = super.toJsonValue();
		IServerJsonOption.APOTHECARY_MODE.addTo(jsonObject, fApothecaryMode);
		if (fInjuryResult != null) {
			IServerJsonOption.INJURY_RESULT.addTo(jsonObject, fInjuryResult.toJsonValue());
		}
		IServerJsonOption.SHOW_REPORT.addTo(jsonObject, fShowReport);
		return jsonObject;
	}

	@Override
	public StepApothecaryMultiple initFrom(IFactorySource source, JsonValue pJsonValue) {
		super.initFrom(source, pJsonValue);
		JsonObject jsonObject = UtilJson.toJsonObject(pJsonValue);
		fApothecaryMode = (ApothecaryMode) IServerJsonOption.APOTHECARY_MODE.getFrom(source, jsonObject);
		JsonObject injuryResultObject = IServerJsonOption.INJURY_RESULT.getFrom(source, jsonObject);
		if (injuryResultObject != null) {
			fInjuryResult = new InjuryResult().initFrom(source, injuryResultObject);
		}
		fShowReport = IServerJsonOption.SHOW_REPORT.getFrom(source, jsonObject);
		return this;
	}

}