package com.fumbbl.ffb.client.state;

import com.fumbbl.ffb.ClientStateId;
import com.fumbbl.ffb.Direction;
import com.fumbbl.ffb.FieldCoordinate;
import com.fumbbl.ffb.Pushback;
import com.fumbbl.ffb.PushbackSquare;
import com.fumbbl.ffb.client.ActionKey;
import com.fumbbl.ffb.client.FantasyFootballClient;
import com.fumbbl.ffb.client.FieldComponent;
import com.fumbbl.ffb.client.util.UtilClientActionKeys;
import com.fumbbl.ffb.model.Game;
import com.fumbbl.ffb.model.Player;

/**
 *
 * @author Kalimar
 */
public class ClientStatePushback extends ClientState {

	protected ClientStatePushback(FantasyFootballClient pClient) {
		super(pClient);
	}

	public ClientStateId getId() {
		return ClientStateId.PUSHBACK;
	}

	protected boolean mouseOverPlayer(Player<?> pPlayer) {
		super.mouseOverPlayer(pPlayer);
		FieldCoordinate playerCoordinate = getClient().getGame().getFieldModel().getPlayerCoordinate(pPlayer);
		return !mouseOverPushback(playerCoordinate);
	}

	protected boolean mouseOverField(FieldCoordinate pCoordinate) {
		super.mouseOverField(pCoordinate);
		return !mouseOverPushback(pCoordinate);
	}

	protected void clickOnField(FieldCoordinate pCoordinate) {
		Pushback pushback = findPushback(findUnlockedPushbackSquare(pCoordinate));
		if (pushback != null) {
			getClient().getCommunication().sendPushback(pushback);
		}
	}

	protected void clickOnPlayer(Player<?> pPlayer) {
		FieldCoordinate playerCoordinate = getClient().getGame().getFieldModel().getPlayerCoordinate(pPlayer);
		Pushback pushback = findPushback(findUnlockedPushbackSquare(playerCoordinate));
		if (pushback != null) {
			getClient().getCommunication().sendPushback(pushback);
		}
	}

	private PushbackSquare findUnlockedPushbackSquare(FieldCoordinate pCoordinate) {
		PushbackSquare unlockedPushbackSquare = null;
		PushbackSquare[] pushbackSquares = getClient().getGame().getFieldModel().getPushbackSquares();
		for (int i = 0; i < pushbackSquares.length; i++) {
			if (!pushbackSquares[i].isLocked() && pushbackSquares[i].getCoordinate().equals(pCoordinate)
					&& pushbackSquares[i].isHomeChoice()) {
				unlockedPushbackSquare = pushbackSquares[i];
				break;
			}
		}
		return unlockedPushbackSquare;
	}

	private boolean mouseOverPushback(FieldCoordinate pCoordinate) {
		boolean overPushback = false;
		FieldComponent fieldComponent = getClient().getUserInterface().getFieldComponent();
		PushbackSquare[] pushbackSquares = getClient().getGame().getFieldModel().getPushbackSquares();
		for (int i = 0; i < pushbackSquares.length; i++) {
			if (pCoordinate.equals(pushbackSquares[i].getCoordinate())) {
				overPushback = true;
				if (pushbackSquares[i].isHomeChoice() && !pushbackSquares[i].isSelected() && !pushbackSquares[i].isLocked()) {
					pushbackSquares[i].setSelected(true);
					fieldComponent.getLayerOverPlayers().drawPushbackSquare(pushbackSquares[i]);
				}
			} else {
				if (pushbackSquares[i].isSelected() && !pushbackSquares[i].isLocked()) {
					pushbackSquares[i].setSelected(false);
					fieldComponent.getLayerOverPlayers().drawPushbackSquare(pushbackSquares[i]);
				}
			}
		}
		fieldComponent.refresh();
		return overPushback;
	}

	public boolean actionKeyPressed(ActionKey pActionKey) {
		boolean actionHandled = false;
		Game game = getClient().getGame();
		Direction moveDirection = UtilClientActionKeys.findMoveDirection(pActionKey);
		if (moveDirection != null) {
			PushbackSquare pushbackSquare = null;
			PushbackSquare[] pushbackSquares = game.getFieldModel().getPushbackSquares();
			for (int i = 0; i < pushbackSquares.length; i++) {
				if (!pushbackSquares[i].isLocked() && (pushbackSquares[i].getDirection() == moveDirection)) {
					pushbackSquare = pushbackSquares[i];
					break;
				}
			}
			Pushback pushback = findPushback(pushbackSquare);
			if (pushback != null) {
				actionHandled = true;
				getClient().getCommunication().sendPushback(pushback);
			}
		}
		return actionHandled;
	}

	private Pushback findPushback(PushbackSquare pPushbackSquare) {
		Pushback pushback = null;
		if (pPushbackSquare != null) {
			FieldCoordinate fromSquare = null;
			FieldCoordinate toSquare = pPushbackSquare.getCoordinate();
			if (toSquare != null) {
				switch (pPushbackSquare.getDirection()) {
				case NORTH:
					fromSquare = toSquare.add(0, 1);
					break;
				case NORTHEAST:
					fromSquare = toSquare.add(-1, 1);
					break;
				case EAST:
					fromSquare = toSquare.add(-1, 0);
					break;
				case SOUTHEAST:
					fromSquare = toSquare.add(-1, -1);
					break;
				case SOUTH:
					fromSquare = toSquare.add(0, -1);
					break;
				case SOUTHWEST:
					fromSquare = toSquare.add(1, -1);
					break;
				case WEST:
					fromSquare = toSquare.add(1, 0);
					break;
				case NORTHWEST:
					fromSquare = toSquare.add(1, 1);
					break;
				}
			}
			Player<?> pushedPlayer = getClient().getGame().getFieldModel().getPlayer(fromSquare);
			if ((fromSquare != null) && (pushedPlayer != null)) {
				pushback = new Pushback(pushedPlayer.getId(), toSquare);
			}
		}
		return pushback;
	}

}
