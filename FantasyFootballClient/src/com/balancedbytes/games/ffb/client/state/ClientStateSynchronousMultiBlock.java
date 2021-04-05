package com.balancedbytes.games.ffb.client.state;

import com.balancedbytes.games.ffb.ClientStateId;
import com.balancedbytes.games.ffb.FieldCoordinate;
import com.balancedbytes.games.ffb.IIconProperty;
import com.balancedbytes.games.ffb.client.ActionKey;
import com.balancedbytes.games.ffb.client.FantasyFootballClient;
import com.balancedbytes.games.ffb.client.IconCache;
import com.balancedbytes.games.ffb.client.UserInterface;
import com.balancedbytes.games.ffb.client.util.UtilClientActionKeys;
import com.balancedbytes.games.ffb.client.util.UtilClientCursor;
import com.balancedbytes.games.ffb.client.util.UtilClientStateBlocking;
import com.balancedbytes.games.ffb.model.ActingPlayer;
import com.balancedbytes.games.ffb.model.Game;
import com.balancedbytes.games.ffb.model.Player;
import com.balancedbytes.games.ffb.model.property.NamedProperties;
import com.balancedbytes.games.ffb.util.UtilPlayer;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientStateSynchronousMultiBlock extends ClientState {

	private final Map<String, Boolean> selectedPlayers = new HashMap<>();

	protected ClientStateSynchronousMultiBlock(FantasyFootballClient pClient) {
		super(pClient);
	}

	public ClientStateId getId() {
		return ClientStateId.SYNCHRONOUS_MULTI_BLOCK;
	}

	public void enterState() {
		super.enterState();
		selectedPlayers.clear();
	}

	protected void clickOnPlayer(Player<?> player) {
		Game game = getClient().getGame();
		ActingPlayer actingPlayer = game.getActingPlayer();
		if (actingPlayer.getPlayer() == player) {
			createAndShowPopupMenuForBlockingPlayer();
		} else {
			if (selectedPlayers.containsKey(player.getId())) {
				selectedPlayers.remove(player.getId());
				getClient().getCommunication().sendUnsetBlockTarget(player.getId());
			} else {
				showPopupOrBlockPlayer(player);
			}
		}
	}

	private void showPopupOrBlockPlayer(Player<?> player) {
		if (player == null) {
			return;
		}
		Game game = getClient().getGame();
		ActingPlayer actingPlayer = game.getActingPlayer();
		if (UtilPlayer.isBlockable(game, player)) {
			FieldCoordinate defenderCoordinate = game.getFieldModel().getPlayerCoordinate(player);
			if (actingPlayer.getPlayer().hasSkillProperty(NamedProperties.canPerformArmourRollInsteadOfBlock)) {
				UtilClientStateBlocking.createAndShowStabPopupMenu(this, player);
			} else if (game.getFieldModel().getDiceDecoration(defenderCoordinate) != null) {
				selectPlayerForBlock(player);
			}
		}
	}

	private void selectPlayerForBlock(Player<?> player) {
		selectedPlayers.put(player.getId(), false);
		getClient().getCommunication().sendSetBlockTarget(player.getId(), false);
		sendIfSelectionComplete();
	}

	private void selectPlayerForStab(Player<?> player) {
		selectedPlayers.put(player.getId(), true);
		getClient().getCommunication().sendSetBlockTarget(player.getId(), true);
		sendIfSelectionComplete();
	}

	private void sendIfSelectionComplete() {
		//TODO
	}

	protected boolean mouseOverPlayer(Player<?> pPlayer) {
		super.mouseOverPlayer(pPlayer);
		if (UtilPlayer.isBlockable(getClient().getGame(), pPlayer)) {
			UtilClientCursor.setCustomCursor(getClient().getUserInterface(), IIconProperty.CURSOR_BLOCK);
		} else {
			UtilClientCursor.setDefaultCursor(getClient().getUserInterface());
		}
		return true;
	}

	protected boolean mouseOverField(FieldCoordinate pCoordinate) {
		super.mouseOverField(pCoordinate);
		UtilClientCursor.setDefaultCursor(getClient().getUserInterface());
		return true;
	}

	public boolean actionKeyPressed(ActionKey pActionKey) {
		Game game = getClient().getGame();
		ActingPlayer actingPlayer = game.getActingPlayer();
		if (actingPlayer.isSufferingBloodLust()) {
			boolean actionHandled = true;
			switch (pActionKey) {
				case PLAYER_SELECT:
					createAndShowPopupMenuForBlockingPlayer();
					break;
				case PLAYER_ACTION_MOVE:
					menuItemSelected(actingPlayer.getPlayer(), IPlayerPopupMenuKeys.KEY_MOVE);
					break;
				case PLAYER_ACTION_END_MOVE:
					menuItemSelected(actingPlayer.getPlayer(), IPlayerPopupMenuKeys.KEY_END_MOVE);
					break;
				default:
					actionHandled = false;
					break;
			}
			return actionHandled;
		} else {
			switch (pActionKey) {
				case PLAYER_ACTION_BLOCK:
					menuItemSelected(actingPlayer.getPlayer(), IPlayerPopupMenuKeys.KEY_BLOCK);
					break;
				case PLAYER_ACTION_STAB:
					menuItemSelected(actingPlayer.getPlayer(), IPlayerPopupMenuKeys.KEY_STAB);
					break;
				default:
					FieldCoordinate playerPosition = game.getFieldModel().getPlayerCoordinate(actingPlayer.getPlayer());
					FieldCoordinate moveCoordinate = UtilClientActionKeys.findMoveCoordinate(getClient(), playerPosition,
						pActionKey);
					Player<?> defender = game.getFieldModel().getPlayer(moveCoordinate);
					showPopupOrBlockPlayer(defender);
					break;
			}
			return true;
		}
	}


	@Override
	public void endTurn() {
		Game game = getClient().getGame();
		ActingPlayer actingPlayer = game.getActingPlayer();
		menuItemSelected(actingPlayer.getPlayer(), IPlayerPopupMenuKeys.KEY_END_MOVE);
		getClient().getCommunication().sendEndTurn();
	}

	protected void menuItemSelected(Player<?> player, int pMenuKey) {
		if (player != null) {
			switch (pMenuKey) {
				case IPlayerPopupMenuKeys.KEY_END_MOVE:
					getClient().getCommunication().sendActingPlayer(null, null, false);
					break;
				case IPlayerPopupMenuKeys.KEY_BLOCK:
					selectPlayerForBlock(player);
					break;
				case IPlayerPopupMenuKeys.KEY_STAB:
					selectPlayerForStab(player);
					break;
				default:
					UtilClientStateBlocking.menuItemSelected(this, player, pMenuKey);
					break;
			}
		}
	}

	private void createAndShowPopupMenuForBlockingPlayer() {
		Game game = getClient().getGame();
		ActingPlayer actingPlayer = game.getActingPlayer();
		List<JMenuItem> menuItemList = new ArrayList<>();
		UserInterface userInterface = getClient().getUserInterface();
		IconCache iconCache = userInterface.getIconCache();
		userInterface.getFieldComponent().getLayerUnderPlayers().clearMovePath();
		if (actingPlayer.isSufferingBloodLust()) {
			JMenuItem moveAction = new JMenuItem("Move",
				new ImageIcon(iconCache.getIconByProperty(IIconProperty.ACTION_MOVE)));
			moveAction.setMnemonic(IPlayerPopupMenuKeys.KEY_MOVE);
			moveAction.setAccelerator(KeyStroke.getKeyStroke(IPlayerPopupMenuKeys.KEY_MOVE, 0));
			menuItemList.add(moveAction);
		}
		String endMoveActionLabel = actingPlayer.hasActed() ? "End Move" : "Deselect Player";
		JMenuItem endMoveAction = new JMenuItem(endMoveActionLabel,
			new ImageIcon(iconCache.getIconByProperty(IIconProperty.ACTION_END_MOVE)));
		endMoveAction.setMnemonic(IPlayerPopupMenuKeys.KEY_END_MOVE);
		endMoveAction.setAccelerator(KeyStroke.getKeyStroke(IPlayerPopupMenuKeys.KEY_END_MOVE, 0));
		menuItemList.add(endMoveAction);
		createPopupMenu(menuItemList.toArray(new JMenuItem[menuItemList.size()]));
		showPopupMenuForPlayer(actingPlayer.getPlayer());
	}

}
