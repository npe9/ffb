package com.fumbbl.ffb.client.state.bb2020;

import com.fumbbl.ffb.ClientStateId;
import com.fumbbl.ffb.FieldCoordinate;
import com.fumbbl.ffb.IIconProperty;
import com.fumbbl.ffb.client.ActionKey;
import com.fumbbl.ffb.client.FantasyFootballClient;
import com.fumbbl.ffb.client.FieldComponent;
import com.fumbbl.ffb.client.IconCache;
import com.fumbbl.ffb.client.UserInterface;
import com.fumbbl.ffb.client.net.ClientCommunication;
import com.fumbbl.ffb.client.state.ClientStateMove;
import com.fumbbl.ffb.client.state.IPlayerPopupMenuKeys;
import com.fumbbl.ffb.client.util.UtilClientCursor;
import com.fumbbl.ffb.model.ActingPlayer;
import com.fumbbl.ffb.model.Game;
import com.fumbbl.ffb.model.Player;
import com.fumbbl.ffb.model.property.NamedProperties;
import com.fumbbl.ffb.model.skill.Skill;
import com.fumbbl.ffb.util.UtilPlayer;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import java.util.ArrayList;
import java.util.List;

public class ClientStateSelectBlitzTarget extends ClientStateMove {

	public ClientStateSelectBlitzTarget(FantasyFootballClient pClient) {
		super(pClient);
	}

	public ClientStateId getId() {
		return ClientStateId.SELECT_BLITZ_TARGET;
	}

	public void clickOnPlayer(Player<?> pPlayer) {
		Game game = getClient().getGame();
		ActingPlayer actingPlayer = game.getActingPlayer();
		if (pPlayer.equals(actingPlayer.getPlayer()) && isTreacherousAvailable(actingPlayer)) {
			createAndShowPopupMenuForActingPlayer();
		} else if (pPlayer.equals(actingPlayer.getPlayer()) || (!actingPlayer.hasBlocked() && UtilPlayer.isValidBlitzTarget(game, pPlayer))) {
			getClient().getCommunication().sendTargetSelected(pPlayer.getId());
		}
	}

	protected boolean mouseOverPlayer(Player<?> pPlayer) {
		super.mouseOverPlayer(pPlayer);
		Game game = getClient().getGame();
		FieldComponent fieldComponent = getClient().getUserInterface().getFieldComponent();
		fieldComponent.getLayerUnderPlayers().clearMovePath();
		ActingPlayer actingPlayer = game.getActingPlayer();
		if (!actingPlayer.hasBlocked() && UtilPlayer.isValidBlitzTarget(game, pPlayer)) {
			UtilClientCursor.setCustomCursor(getClient().getUserInterface(), IIconProperty.CURSOR_BLOCK);
		} else {
			UtilClientCursor.setCustomCursor(getClient().getUserInterface(), IIconProperty.CURSOR_INVALID_BLOCK);
		}

		showShortestPath(game.getFieldModel().getPlayerCoordinate(pPlayer), game, fieldComponent, actingPlayer);

		return true;
	}

	protected boolean mouseOverField(FieldCoordinate pCoordinate) {
		super.mouseOverField(pCoordinate);
		Game game = getClient().getGame();
		FieldComponent fieldComponent = getClient().getUserInterface().getFieldComponent();
		fieldComponent.getLayerUnderPlayers().clearMovePath();
		ActingPlayer actingPlayer = game.getActingPlayer();

		UtilClientCursor.setCustomCursor(getClient().getUserInterface(), IIconProperty.CURSOR_INVALID_BLOCK);

		showShortestPath(pCoordinate, game, fieldComponent, actingPlayer);

		return true;
	}

	protected void createAndShowPopupMenuForActingPlayer() {
		Game game = getClient().getGame();
		UserInterface userInterface = getClient().getUserInterface();
		IconCache iconCache = userInterface.getIconCache();
		userInterface.getFieldComponent().getLayerUnderPlayers().clearMovePath();
		List<JMenuItem> menuItemList = new ArrayList<>();
		ActingPlayer actingPlayer = game.getActingPlayer();
		String endMoveActionLabel = "Deselect Player";
		JMenuItem endMoveAction = new JMenuItem(endMoveActionLabel,
			new ImageIcon(iconCache.getIconByProperty(IIconProperty.ACTION_END_MOVE)));
		endMoveAction.setMnemonic(IPlayerPopupMenuKeys.KEY_END_MOVE);
		endMoveAction.setAccelerator(KeyStroke.getKeyStroke(IPlayerPopupMenuKeys.KEY_END_MOVE, 0));
		menuItemList.add(endMoveAction);

		menuItemList.add(createTreacherousItem(iconCache));
		createPopupMenu(menuItemList.toArray(new JMenuItem[0]));
		showPopupMenuForPlayer(actingPlayer.getPlayer());
	}


	public boolean actionKeyPressed(ActionKey pActionKey) {
		boolean actionHandled = true;
		Game game = getClient().getGame();
		ActingPlayer actingPlayer = game.getActingPlayer();
		switch (pActionKey) {
			case PLAYER_SELECT:
				createAndShowPopupMenuForActingPlayer();
				break;
			case PLAYER_ACTION_END_MOVE:
				menuItemSelected(actingPlayer.getPlayer(), IPlayerPopupMenuKeys.KEY_END_MOVE);
				break;
			case PLAYER_ACTION_TREACHEROUS:
				menuItemSelected(actingPlayer.getPlayer(), IPlayerPopupMenuKeys.KEY_TREACHEROUS);
				break;
			default:
				actionHandled = false;
				break;
		}
		return actionHandled;
	}

	protected void menuItemSelected(Player<?> pPlayer, int pMenuKey) {
		if (pPlayer != null) {
			ClientCommunication communication = getClient().getCommunication();
			switch (pMenuKey) {
				case IPlayerPopupMenuKeys.KEY_END_MOVE:
					getClient().getCommunication().sendTargetSelected(pPlayer.getId());
					break;
				case IPlayerPopupMenuKeys.KEY_TREACHEROUS:
					Skill skill = pPlayer.getSkillWithProperty(NamedProperties.canStabTeamMateForBall);
					communication.sendUseSkill(skill, true, pPlayer.getId());
					break;
				default:
					break;
			}
		}
	}

	@Override
	protected void clickOnField(FieldCoordinate pCoordinate) {
		// clicks on fields are ignored
	}
}
