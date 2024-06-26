package com.fumbbl.ffb.server.step.bb2016.start;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.fumbbl.ffb.FactoryType;
import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.dialog.DialogBuyCardsParameter;
import com.fumbbl.ffb.factory.CardTypeFactory;
import com.fumbbl.ffb.factory.IFactorySource;
import com.fumbbl.ffb.inducement.Card;
import com.fumbbl.ffb.inducement.CardType;
import com.fumbbl.ffb.json.UtilJson;
import com.fumbbl.ffb.model.Game;
import com.fumbbl.ffb.model.GameResult;
import com.fumbbl.ffb.net.NetCommandId;
import com.fumbbl.ffb.net.commands.ClientCommandBuyCard;
import com.fumbbl.ffb.option.GameOptionId;
import com.fumbbl.ffb.option.GameOptionInt;
import com.fumbbl.ffb.option.UtilGameOption;
import com.fumbbl.ffb.report.bb2016.ReportCardsBought;
import com.fumbbl.ffb.server.CardDeck;
import com.fumbbl.ffb.server.GameState;
import com.fumbbl.ffb.server.IServerJsonOption;
import com.fumbbl.ffb.server.net.ReceivedCommand;
import com.fumbbl.ffb.server.step.AbstractStep;
import com.fumbbl.ffb.server.step.StepAction;
import com.fumbbl.ffb.server.step.StepCommandStatus;
import com.fumbbl.ffb.server.step.StepId;
import com.fumbbl.ffb.server.step.StepParameter;
import com.fumbbl.ffb.server.step.StepParameterKey;
import com.fumbbl.ffb.server.step.UtilServerSteps;
import com.fumbbl.ffb.server.step.game.start.UtilInducementSequence;
import com.fumbbl.ffb.server.util.UtilServerDialog;
import com.fumbbl.ffb.util.ArrayTool;

import java.util.HashMap;
import java.util.Map;

/**
 * Step in start game sequence to buy cards.
 *
 * Sets stepParameter INDUCEMENT_GOLD_AWAY for all steps on the stack. Sets
 * stepParameter INDUCEMENT_GOLD_HOME for all steps on the stack.
 *
 * @author Kalimar
 */
@RulesCollection(RulesCollection.Rules.BB2016)
public final class StepBuyCards extends AbstractStep {

	private int fInducementGoldHome;
	private int fInducementGoldAway;

	private boolean fCardsSelectedHome;
	private boolean fCardsSelectedAway;

	private boolean fReportedHome;
	private boolean fReportedAway;

	private final transient Map<CardType, CardDeck> fDeckByType;
	private transient CardType fBuyCardHome;
	private transient CardType fBuyCardAway;
	private transient Map<CardType, Integer> cardPrices;
	private transient int minimumCardPrice = Integer.MAX_VALUE;

	public StepBuyCards(GameState pGameState) {
		super(pGameState);
		fDeckByType = new HashMap<>();
		fBuyCardHome = null;
		fBuyCardAway = null;
	}

	public StepId getId() {
		return StepId.BUY_CARDS;
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
			if (pReceivedCommand.getId() == NetCommandId.CLIENT_BUY_CARD) {
				ClientCommandBuyCard buyCardCommand = (ClientCommandBuyCard) pReceivedCommand.getCommand();
				if (UtilServerSteps.checkCommandIsFromHomePlayer(getGameState(), pReceivedCommand)) {
					fBuyCardHome = buyCardCommand.getCardType();
					if (fBuyCardHome == null) {
						fCardsSelectedHome = true;
					}
				} else {
					fBuyCardAway = buyCardCommand.getCardType();
					if (fBuyCardAway == null) {
						fCardsSelectedAway = true;
					}
				}
				commandStatus = StepCommandStatus.EXECUTE_STEP;
			}
		}
		if (commandStatus == StepCommandStatus.EXECUTE_STEP) {
			executeStep();
		}
		return commandStatus;
	}

	private void executeStep() {
		Game game = getGameState().getGame();
		GameResult gameResult = game.getGameResult();
		if ((UtilGameOption.getIntOption(game, GameOptionId.MAX_NR_OF_CARDS) == 0)
				|| UtilGameOption.isOptionEnabled(game, GameOptionId.USE_PREDEFINED_INDUCEMENTS)) {
			int freeCash = UtilGameOption.getIntOption(game, GameOptionId.FREE_INDUCEMENT_CASH);
			fInducementGoldHome = UtilInducementSequence.calculateInducementGold(game, true) + freeCash;
			publishParameter(new StepParameter(StepParameterKey.INDUCEMENT_GOLD_HOME, fInducementGoldHome));
			fInducementGoldAway = UtilInducementSequence.calculateInducementGold(game, false) + freeCash;
			publishParameter(new StepParameter(StepParameterKey.INDUCEMENT_GOLD_AWAY, fInducementGoldAway));
			getResult().setNextAction(StepAction.NEXT_STEP);
			return;
		}
		buildDecks();

		cardPrices = new HashMap<>();
		((CardTypeFactory)game.getFactory(FactoryType.Factory.CARD_TYPE)).getCardTypes().forEach(cardType -> {
			int price = ((GameOptionInt) game.getOptions().getOptionWithDefault(cardType.getCostId())).getValue();
			cardPrices.put(cardType, price);
			CardDeck deck = fDeckByType.get(cardType);
			if (deck != null && deck.size() > 0) {
				minimumCardPrice = Math.min(minimumCardPrice, price);
			}
		});

		if (fBuyCardHome != null) {
			fInducementGoldHome -= cardPrices.getOrDefault(fBuyCardHome, 0);
			CardDeck deck = fDeckByType.get(fBuyCardHome);
			Card card = getGameState().getDiceRoller().drawCard(deck);
			game.getTurnDataHome().getInducementSet().addAvailableCard(card);
			fBuyCardHome = null;
		} else if (fBuyCardAway != null) {
			fInducementGoldAway -= cardPrices.getOrDefault(fBuyCardAway, 0);
			CardDeck deck = fDeckByType.get(fBuyCardAway);
			Card card = getGameState().getDiceRoller().drawCard(deck);
			game.getTurnDataAway().getInducementSet().addAvailableCard(card);
			fBuyCardAway = null;
		} else {
			if (!fCardsSelectedHome && !fCardsSelectedAway) {
				int freeCash = UtilGameOption.getIntOption(game, GameOptionId.FREE_INDUCEMENT_CASH)
						+ UtilGameOption.getIntOption(game, GameOptionId.FREE_CARD_CASH);
				fInducementGoldHome = UtilInducementSequence.calculateInducementGold(game, true) + freeCash;
				fInducementGoldAway = UtilInducementSequence.calculateInducementGold(game, false) + freeCash;
			}
			if (fInducementGoldHome < minimumCardPrice) {
				fCardsSelectedHome = true;
			}
			if (fInducementGoldAway < minimumCardPrice) {
				fCardsSelectedAway = true;
			}
			if (fCardsSelectedHome && !fReportedHome) {
				fReportedHome = true;
				Card[] cardsHome = game.getTurnDataHome().getInducementSet().getAllCards();
				int totalCostHome = calculateTotalCost(cardsHome);
				getResult().addReport(new ReportCardsBought(game.getTeamHome().getId(), cardsHome.length, totalCostHome));
			}
			if (fCardsSelectedAway && !fReportedAway) {
				fReportedAway = true;
				Card[] cardsAway = game.getTurnDataAway().getInducementSet().getAllCards();
				int totalCostAway = calculateTotalCost(cardsAway);
				getResult().addReport(new ReportCardsBought(game.getTeamAway().getId(), cardsAway.length, totalCostAway));
			}
			if (!fCardsSelectedHome && !fCardsSelectedAway) {
				int homeTV = gameResult.getTeamResultHome().getTeamValue();
				int awayTV = gameResult.getTeamResultAway().getTeamValue();
				if (homeTV > awayTV) {
					UtilServerDialog.showDialog(getGameState(),
							createDialogParameter(game.getTeamHome().getId(), fInducementGoldHome), false);
				} else {
					UtilServerDialog.showDialog(getGameState(),
							createDialogParameter(game.getTeamAway().getId(), fInducementGoldAway), false);
				}
			} else if (!fCardsSelectedHome) {
				UtilServerDialog.showDialog(getGameState(),
						createDialogParameter(game.getTeamHome().getId(), fInducementGoldHome), false);
			} else if (!fCardsSelectedAway) {
				UtilServerDialog.showDialog(getGameState(),
						createDialogParameter(game.getTeamAway().getId(), fInducementGoldAway), false);
			} else {
				int freeCash = UtilGameOption.getIntOption(game, GameOptionId.FREE_INDUCEMENT_CASH);
				int maxInducementGoldHome = UtilInducementSequence.calculateInducementGold(game, true) + freeCash;
				publishParameter(new StepParameter(StepParameterKey.INDUCEMENT_GOLD_HOME,
						Math.min(fInducementGoldHome, maxInducementGoldHome)));
				int maxInducementGoldAway = UtilInducementSequence.calculateInducementGold(game, false) + freeCash;
				publishParameter(new StepParameter(StepParameterKey.INDUCEMENT_GOLD_AWAY,
						Math.min(fInducementGoldAway, maxInducementGoldAway)));
				getResult().setNextAction(StepAction.NEXT_STEP);
			}
		}
	}

	private int calculateTotalCost(Card[] pCards) {
		int totalCost = 0;
		if (ArrayTool.isProvided(pCards)) {
			for (Card card : pCards) {
				totalCost += cardPrices.getOrDefault(card.getType(), 0);
			}
		}
		return totalCost;
	}

	private void buildDecks() {
		Game game = getGameState().getGame();
		fDeckByType.clear();
		((CardTypeFactory)game.getFactory(FactoryType.Factory.CARD_TYPE)).getCardTypes().forEach(type -> {
			CardDeck deck = new CardDeck(type);
			deck.build(game);
			fDeckByType.put(type, deck);
		});
	}

	private DialogBuyCardsParameter createDialogParameter(String pTeamId, int pAvailableGold) {
		int availableCards = UtilGameOption.getIntOption(getGameState().getGame(), GameOptionId.MAX_NR_OF_CARDS);
		DialogBuyCardsParameter dialogParameter = new DialogBuyCardsParameter(pTeamId, availableCards, pAvailableGold);
		for (CardType type : fDeckByType.keySet()) {
			CardDeck deck = fDeckByType.get(type);
			dialogParameter.put(type, deck.size());
		}
		return dialogParameter;
	}

	// JSON serialization

	@Override
	public JsonObject toJsonValue() {
		JsonObject jsonObject = super.toJsonValue();
		IServerJsonOption.INDUCEMENT_GOLD_AWAY.addTo(jsonObject, fInducementGoldAway);
		IServerJsonOption.INDUCEMENT_GOLD_HOME.addTo(jsonObject, fInducementGoldHome);
		IServerJsonOption.CARDS_SELECTED_AWAY.addTo(jsonObject, fCardsSelectedAway);
		IServerJsonOption.CARDS_SELECTED_HOME.addTo(jsonObject, fCardsSelectedHome);
		IServerJsonOption.REPORTED_AWAY.addTo(jsonObject, fReportedAway);
		IServerJsonOption.REPORTED_HOME.addTo(jsonObject, fReportedHome);
		return jsonObject;
	}

	@Override
	public StepBuyCards initFrom(IFactorySource source, JsonValue jsonValue) {
		super.initFrom(source, jsonValue);
		JsonObject jsonObject = UtilJson.toJsonObject(jsonValue);
		fInducementGoldAway = IServerJsonOption.INDUCEMENT_GOLD_AWAY.getFrom(source, jsonObject);
		fInducementGoldHome = IServerJsonOption.INDUCEMENT_GOLD_HOME.getFrom(source, jsonObject);
		fCardsSelectedAway = IServerJsonOption.CARDS_SELECTED_AWAY.getFrom(source, jsonObject);
		fCardsSelectedHome = IServerJsonOption.CARDS_SELECTED_HOME.getFrom(source, jsonObject);
		fReportedAway = IServerJsonOption.REPORTED_AWAY.getFrom(source, jsonObject);
		fReportedHome = IServerJsonOption.REPORTED_HOME.getFrom(source, jsonObject);
		return this;
	}

}
