package com.balancedbytes.games.ffb.server.inducements.bb2016;

import com.balancedbytes.games.ffb.CardEffect;
import com.balancedbytes.games.ffb.RulesCollection;
import com.balancedbytes.games.ffb.inducement.Card;
import com.balancedbytes.games.ffb.inducement.CardHandlerKey;
import com.balancedbytes.games.ffb.model.Game;
import com.balancedbytes.games.ffb.model.Player;
import com.balancedbytes.games.ffb.server.inducements.CardHandler;
import com.balancedbytes.games.ffb.server.step.IStep;

import static com.balancedbytes.games.ffb.inducement.bb2016.CardHandlerKey.ILLEGAL_SUBSTITUTION;

@RulesCollection(RulesCollection.Rules.BB2016)
public class IllegalSubstitutionHandler extends CardHandler {
	@Override
	protected CardHandlerKey handlerKey() {
		return ILLEGAL_SUBSTITUTION;
	}

	@Override
	public void deactivate(Card card, IStep step, Player<?> unused) {
		Game game = step.getGameState().getGame();
		Player<?>[] players = game.getFieldModel().findPlayers(CardEffect.ILLEGALLY_SUBSTITUTED);
		for (Player<?> player : players) {
			game.getFieldModel().removeCardEffect(player, CardEffect.ILLEGALLY_SUBSTITUTED);
		}
	}
}
