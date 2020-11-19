package com.balancedbytes.games.ffb.server.InjuryType;

import com.balancedbytes.games.ffb.ApothecaryMode;
import com.balancedbytes.games.ffb.ArmorModifier;
import com.balancedbytes.games.ffb.ArmorModifierFactory;
import com.balancedbytes.games.ffb.Card;
import com.balancedbytes.games.ffb.FieldCoordinate;
import com.balancedbytes.games.ffb.InjuryContext;
import com.balancedbytes.games.ffb.InjuryModifier;
import com.balancedbytes.games.ffb.InjuryModifierFactory;
import com.balancedbytes.games.ffb.PlayerState;
import com.balancedbytes.games.ffb.SendToBoxReason;
import com.balancedbytes.games.ffb.model.Game;
import com.balancedbytes.games.ffb.model.Player;
import com.balancedbytes.games.ffb.model.modifier.NamedProperties;
import com.balancedbytes.games.ffb.option.GameOptionId;
import com.balancedbytes.games.ffb.option.UtilGameOption;
import com.balancedbytes.games.ffb.server.model.ServerSkill;
import com.balancedbytes.games.ffb.server.step.IStep;
import com.balancedbytes.games.ffb.util.UtilCards;
import com.balancedbytes.games.ffb.util.UtilPlayer;

public class InjuryTypeFoul extends InjuryTypeServer {
		public InjuryTypeFoul(IStep step) {
			super(step, "foul", false, SendToBoxReason.FOULED);
		}

		public boolean isCausedByOpponent() {
			return true;
		}


		@Override
		public InjuryContext handleInjury(Game game, Player<?> pAttacker, Player<?> pDefender,
				FieldCoordinate pDefenderCoordinate, InjuryContext pOldInjuryContext, ApothecaryMode pApothecaryMode) {
			// Blatant Foul breaks armor without roll
			if (UtilCards.isCardActive(game, Card.BLATANT_FOUL)) {
				injuryContext.setArmorBroken(true);
			}

			if (!injuryContext.isArmorBroken()) {

				boolean attackerHasChainsaw = UtilCards.hasSkillWithProperty(pAttacker,
						NamedProperties.blocksLikeChainsaw);

				injuryContext.setArmorRoll(diceRoller.rollArmour());
				if (attackerHasChainsaw) {
					injuryContext.addArmorModifier(ArmorModifier.CHAINSAW);
				}
				if (UtilGameOption.isOptionEnabled(game, GameOptionId.FOUL_BONUS)
						|| (UtilGameOption.isOptionEnabled(game, GameOptionId.FOUL_BONUS_OUTSIDE_TACKLEZONE)
								&& (UtilPlayer.findTacklezones(game, pAttacker) < 1))) {
					injuryContext.addArmorModifier(ArmorModifier.FOUL);
				}
				int foulAssists = UtilPlayer.findFoulAssists(game, pAttacker, pDefender);
				if (foulAssists != 0) {
					ArmorModifier assistModifier = new ArmorModifierFactory().getFoulAssist(foulAssists);
					injuryContext.addArmorModifier(assistModifier);
				}
				injuryContext.setArmorBroken(diceInterpreter.isArmourBroken(gameState, injuryContext));
				if (!injuryContext.isArmorBroken() && UtilCards.hasSkill(game, pAttacker, ServerSkill.DIRTY_PLAYER)) {
					injuryContext.addArmorModifier(ArmorModifier.DIRTY_PLAYER);
					injuryContext.setArmorBroken(diceInterpreter.isArmourBroken(gameState, injuryContext));
				}
			}

			if (injuryContext.isArmorBroken()) {
				injuryContext.setInjuryRoll(diceRoller.rollInjury());
				injuryContext.addInjuryModifier(new InjuryModifierFactory().getNigglingInjuryModifier(pDefender));

				if (UtilCards.hasSkill(game, pAttacker, ServerSkill.DIRTY_PLAYER)
						&& !injuryContext.hasArmorModifier(ArmorModifier.DIRTY_PLAYER)) {
					injuryContext.addInjuryModifier(InjuryModifier.DIRTY_PLAYER);
				}

				setInjury(pDefender);

			} else {
				injuryContext.setInjury(new PlayerState(PlayerState.PRONE));
			}

			return injuryContext;
		}
	}