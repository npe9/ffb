package com.fumbbl.ffb.client.util;

import com.fumbbl.ffb.FactoryType;
import com.fumbbl.ffb.FieldCoordinate;
import com.fumbbl.ffb.IIconProperty;
import com.fumbbl.ffb.PlayerState;
import com.fumbbl.ffb.client.ActionKey;
import com.fumbbl.ffb.client.DimensionProvider;
import com.fumbbl.ffb.client.IconCache;
import com.fumbbl.ffb.client.net.ClientCommunication;
import com.fumbbl.ffb.client.state.ClientState;
import com.fumbbl.ffb.client.state.IPlayerPopupMenuKeys;
import com.fumbbl.ffb.client.ui.swing.JMenuItem;
import com.fumbbl.ffb.mechanics.GameMechanic;
import com.fumbbl.ffb.mechanics.Mechanic;
import com.fumbbl.ffb.model.ActingPlayer;
import com.fumbbl.ffb.model.Game;
import com.fumbbl.ffb.model.Player;
import com.fumbbl.ffb.model.property.NamedProperties;
import com.fumbbl.ffb.model.skill.Skill;
import com.fumbbl.ffb.util.UtilCards;
import com.fumbbl.ffb.util.UtilPlayer;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Kalimar
 */
public class UtilClientStateBlocking {

	public static boolean actionKeyPressed(ClientState pClientState, ActionKey pActionKey, boolean pDoBlitz) {
		boolean actionHandled;
		Game game = pClientState.getClient().getGame();
		ActingPlayer actingPlayer = game.getActingPlayer();
		Player<?> player = actingPlayer.getPlayer();
		switch (pActionKey) {
			case PLAYER_ACTION_BLOCK:
				menuItemSelected(pClientState, player, IPlayerPopupMenuKeys.KEY_BLOCK);
				actionHandled = true;
				break;
			case PLAYER_ACTION_STAB:
				menuItemSelected(pClientState, player, IPlayerPopupMenuKeys.KEY_STAB);
				actionHandled = true;
				break;
			case PLAYER_ACTION_CHAINSAW:
				menuItemSelected(pClientState, player, IPlayerPopupMenuKeys.KEY_CHAINSAW);
				actionHandled = true;
				break;
			case PLAYER_ACTION_PROJECTILE_VOMIT:
				menuItemSelected(pClientState, player, IPlayerPopupMenuKeys.KEY_PROJECTILE_VOMIT);
				actionHandled = true;
				break;
			case PLAYER_ACTION_TREACHEROUS:
				menuItemSelected(pClientState, player, IPlayerPopupMenuKeys.KEY_TREACHEROUS);
				actionHandled = true;
				break;
			case PLAYER_ACTION_WISDOM:
				menuItemSelected(pClientState, player, IPlayerPopupMenuKeys.KEY_WISDOM);
				actionHandled = true;
				break;
			case PLAYER_ACTION_RAIDING_PARTY:
				menuItemSelected(pClientState, player, IPlayerPopupMenuKeys.KEY_RAIDING_PARTY);
				actionHandled = true;
				break;
			case PLAYER_ACTION_BALEFUL_HEX:
				menuItemSelected(pClientState, player, IPlayerPopupMenuKeys.KEY_BALEFUL_HEX);
				actionHandled = true;
				break;
			default:
				FieldCoordinate playerPosition = game.getFieldModel().getPlayerCoordinate(player);
				FieldCoordinate moveCoordinate = UtilClientActionKeys.findMoveCoordinate(playerPosition,
					pActionKey);
				Player<?> defender = game.getFieldModel().getPlayer(moveCoordinate);
				actionHandled = showPopupOrBlockPlayer(pClientState, defender, pDoBlitz);
				break;
		}
		return actionHandled;
	}

	public static boolean menuItemSelected(ClientState pClientState, Player<?> pPlayer, int pMenuKey) {
		boolean handled = false;
		if (pPlayer != null) {
			Game game = pClientState.getClient().getGame();
			ActingPlayer actingPlayer = game.getActingPlayer();
			ClientCommunication communication = pClientState.getClient().getCommunication();
			switch (pMenuKey) {
				case IPlayerPopupMenuKeys.KEY_BLOCK:
					handled = true;
					block(pClientState, actingPlayer.getPlayerId(), pPlayer, false, false, false);
					break;
				case IPlayerPopupMenuKeys.KEY_STAB:
					handled = true;
					block(pClientState, actingPlayer.getPlayerId(), pPlayer, true, false, false);
					break;
				case IPlayerPopupMenuKeys.KEY_CHAINSAW:
					handled = true;
					block(pClientState, actingPlayer.getPlayerId(), pPlayer, false, true, false);
					break;
				case IPlayerPopupMenuKeys.KEY_PROJECTILE_VOMIT:
					handled = true;
					block(pClientState, actingPlayer.getPlayerId(), pPlayer, false, false, true);
					break;
				case IPlayerPopupMenuKeys.KEY_TREACHEROUS:
					Skill skill = pPlayer.getSkillWithProperty(NamedProperties.canStabTeamMateForBall);
					communication.sendUseSkill(skill, true, pPlayer.getId());
					break;
				case IPlayerPopupMenuKeys.KEY_WISDOM:
					communication.sendUseWisdom();
					break;
				case IPlayerPopupMenuKeys.KEY_RAIDING_PARTY:
					Skill raidingSkill = pPlayer.getSkillWithProperty(NamedProperties.canMoveOpenTeamMate);
					communication.sendUseSkill(raidingSkill, true, pPlayer.getId());
					break;
				case IPlayerPopupMenuKeys.KEY_LOOK_INTO_MY_EYES:
					UtilCards.getUnusedSkillWithProperty(pPlayer, NamedProperties.canStealBallFromOpponent)
						.ifPresent(lookSkill -> communication.sendUseSkill(lookSkill, true, pPlayer.getId()));
					break;
				case IPlayerPopupMenuKeys.KEY_BALEFUL_HEX:
					Skill balefulSkill = pPlayer.getSkillWithProperty(NamedProperties.canMakeOpponentMissTurn);
					communication.sendUseSkill(balefulSkill, true, pPlayer.getId());
					break;
				case IPlayerPopupMenuKeys.KEY_BLACK_INK:
					Skill blackInk = pPlayer.getSkillWithProperty(NamedProperties.canGazeAutomatically);
					communication.sendUseSkill(blackInk, true, pPlayer.getId());
					break;
				default:
					break;
			}
		}
		return handled;
	}

	public static boolean showPopupOrBlockPlayer(ClientState pClientState, Player<?> pDefender, boolean pDoBlitz) {
		if (pDefender == null) {
			return false;
		}
		boolean handled = false;
		Game game = pClientState.getClient().getGame();
		ActingPlayer actingPlayer = game.getActingPlayer();
		GameMechanic gameMechanic = (GameMechanic) game.getFactory(FactoryType.Factory.MECHANIC).forName(Mechanic.Type.GAME.name());

		PlayerState playerState = game.getFieldModel().getPlayerState(actingPlayer.getPlayer());
		// rooted players can not move but still spend movement for the blitz action
		if (UtilPlayer.isBlockable(game, pDefender) && (!pDoBlitz || playerState.isRooted() || UtilPlayer.isNextMovePossible(game, false))) {
			handled = true;
			FieldCoordinate defenderCoordinate = game.getFieldModel().getPlayerCoordinate(pDefender);
			if (UtilCards.hasUnusedSkillWithProperty(actingPlayer.getPlayer(), NamedProperties.providesBlockAlternative) && gameMechanic.areSpecialBlockActionsAllowed(game.getTurnMode())) {
				createAndShowBlockOptionsPopupMenu(pClientState, actingPlayer.getPlayer(), pDefender, false);
			} else if (game.getFieldModel().getDiceDecoration(defenderCoordinate) != null) {
				block(pClientState, actingPlayer.getPlayerId(), pDefender, false, false, false);
			} else {
				handled = false;
			}
		}
		return handled;
	}

	public static void createAndShowBlockOptionsPopupMenu(ClientState pClientState, Player<?> attacker, Player<?> defender, boolean multiBlock) {
		IconCache iconCache = pClientState.getClient().getUserInterface().getIconCache();
		DimensionProvider dimensionProvider = pClientState.dimensionProvider();
		List<JMenuItem> menuItemList = new ArrayList<>();
		if (attacker.hasSkillProperty(NamedProperties.canPerformArmourRollInsteadOfBlock)) {
			JMenuItem stabAction = new JMenuItem(dimensionProvider, "Stab Opponent",
				new ImageIcon(iconCache.getIconByProperty(IIconProperty.ACTION_STAB)));
			stabAction.setMnemonic(IPlayerPopupMenuKeys.KEY_STAB);
			stabAction.setAccelerator(KeyStroke.getKeyStroke(IPlayerPopupMenuKeys.KEY_STAB, 0));
			menuItemList.add(stabAction);
		}
		if (attacker.hasSkillProperty(NamedProperties.providesChainsawBlockAlternative) && !multiBlock) {
			JMenuItem chainsawAction = new JMenuItem(dimensionProvider, "Chainsaw",
				new ImageIcon(iconCache.getIconByProperty(IIconProperty.ACTION_CHAINSAW)));
			chainsawAction.setMnemonic(IPlayerPopupMenuKeys.KEY_CHAINSAW);
			chainsawAction.setAccelerator(KeyStroke.getKeyStroke(IPlayerPopupMenuKeys.KEY_CHAINSAW, 0));
			menuItemList.add(chainsawAction);
		}
		Optional<Skill> vomitSkill = UtilCards.getUnusedSkillWithProperty(attacker, NamedProperties.canPerformArmourRollInsteadOfBlockThatMightFail);
		if (vomitSkill.isPresent()) {
			JMenuItem projectileVomit = new JMenuItem(dimensionProvider, vomitSkill.get().getName(),
				new ImageIcon(iconCache.getIconByProperty(IIconProperty.ACTION_VOMIT)));
			projectileVomit.setMnemonic(IPlayerPopupMenuKeys.KEY_PROJECTILE_VOMIT);
			projectileVomit.setAccelerator(KeyStroke.getKeyStroke(IPlayerPopupMenuKeys.KEY_PROJECTILE_VOMIT, 0));
			menuItemList.add(projectileVomit);
		}
		JMenuItem blockAction = new JMenuItem(dimensionProvider, "Block Opponent",
			new ImageIcon(iconCache.getIconByProperty(IIconProperty.ACTION_BLOCK)));
		blockAction.setMnemonic(IPlayerPopupMenuKeys.KEY_BLOCK);
		blockAction.setAccelerator(KeyStroke.getKeyStroke(IPlayerPopupMenuKeys.KEY_BLOCK, 0));
		menuItemList.add(blockAction);
		pClientState.createPopupMenu(menuItemList.toArray(new JMenuItem[0]));
		pClientState.showPopupMenuForPlayer(defender);
	}

	public static void block(ClientState pClientState, String pActingPlayerId, Player<?> pDefender, boolean pUsingStab,
													 boolean usingChainsaw, boolean usingVomit) {
		pClientState.getClient().getUserInterface().getFieldComponent().refresh();
		pClientState.getClient().getCommunication().sendBlock(pActingPlayerId, pDefender, pUsingStab, usingChainsaw, usingVomit);
	}

}